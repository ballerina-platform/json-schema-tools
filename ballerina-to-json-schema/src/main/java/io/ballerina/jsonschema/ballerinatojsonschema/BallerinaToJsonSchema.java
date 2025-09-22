/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.jsonschema.ballerinatojsonschema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.jsonschema.ballerinatojsonschema.adapter.BalTypeAdapter;

import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;

import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Public API: generate Draft 2020-12 JSON Schemas from Ballerina source.
 */
public final class BallerinaToJsonSchema {

    /** Configuration for how generation should behave. */
    public static final class ApiOptions {
        private final GeneratorOptions rootMeta;
        private final boolean allowOptionalAsTypeArray;
        private final Set<String> typeNameFilter;

        private ApiOptions(Builder builder) {
            this.rootMeta = builder.rootMeta;
            this.allowOptionalAsTypeArray = builder.allowOptionalAsTypeArray;
            this.typeNameFilter = Collections.unmodifiableSet(new LinkedHashSet<>(builder.typeNameFilter));
        }

        public GeneratorOptions rootMeta() { return rootMeta; }
        public boolean allowOptionalAsTypeArray() { return allowOptionalAsTypeArray; }
        public Set<String> typeNameFilter() { return typeNameFilter; }

        public static Builder builder() { return new Builder(); }
        public static final class Builder {
            private GeneratorOptions rootMeta = GeneratorOptions.builder().build();
            private boolean allowOptionalAsTypeArray = true;
            private final Set<String> typeNameFilter = new LinkedHashSet<>();
            public Builder rootMeta(GeneratorOptions opts) { this.rootMeta = opts; return this; }
            public Builder allowOptionalAsTypeArray(boolean v){ this.allowOptionalAsTypeArray = v; return this; }
            public Builder includeTypes(String... names) { this.typeNameFilter
                    .addAll(Arrays.asList(names)); return this; }
            public ApiOptions build() { return new ApiOptions(this); }
        }
    }

    /** Generate schemas from a directory (package) or single .bal file. */
    public Map<String, ObjectNode> generate(Path ballerinaPath, ApiOptions apiOptions) {
        Project project = loadProject(ballerinaPath);
        Package current = project.currentPackage();
        PackageCompilation compilation = current.getCompilation();

        Map<String, ObjectNode> schemaByTypeName = new LinkedHashMap<>();
        for (ModuleId id : current.moduleIds()) {
            Module module = current.module(id);
            SemanticModel semanticModel = compilation.getSemanticModel(id);

            for (DocumentId docId : module.documentIds()) {
                Document document = module.document(docId);
                SyntaxTree syntaxTree = document.syntaxTree();

                visitTypeDefs(syntaxTree, def -> {
                    Optional<Symbol> symbolOpt = semanticModel.symbol(def);
                    if (symbolOpt.isEmpty() || !(symbolOpt.get()
                            instanceof TypeDefinitionSymbol typeDefinitionSymbol)) return;

                    String name = typeDefinitionSymbol.getName().orElse(null);
                    if (name == null) return;

                    if (!apiOptions.typeNameFilter().isEmpty()
                            && !apiOptions.typeNameFilter().contains(name)) {
                        return;
                    }

                    TypeSymbol underlying = typeDefinitionSymbol.typeDescriptor();

                    // Build Stage-1 content (handles primitives, and T? -> ["T","null"] if enabled)
                    JsonSchemaNode content =
                            BalTypeAdapter.toSchemaNode(underlying, apiOptions.allowOptionalAsTypeArray());

                    // Assemble root: $schema/$id/title/description, then merge content ("type")
                    ObjectNode root = JsonSchemaNode.mapper().createObjectNode();
                    apiOptions.rootMeta().schemaUri().ifPresent(uri ->
                            root.put(JsonSchemaConstants.K_SCHEMA, uri));
                    apiOptions.rootMeta().id().ifPresent(u ->
                            root.put(JsonSchemaConstants.K_ID, u.toString()));

                    // If a title was not provided or is blank, default to typedef name
                    String title = apiOptions.rootMeta().title()
                            .filter(t -> !t.isBlank())
                            .orElse(name);
                    root.put(JsonSchemaConstants.K_TITLE, title);

                    // Pull the # doc comment above the typedef into "description"
                    extractDescription(typeDefinitionSymbol, def).ifPresent(desc ->
                            root.put("description", desc));

                    // Merge the content node (keeps "type" as string or ["...","null"])
                    root.setAll(content.toObjectNode());

                    schemaByTypeName.put(name, root);
                });
            }
        }
        return schemaByTypeName;
    }

    private static Optional<String> extractDescription(TypeDefinitionSymbol typeDefinitionSymbol,
                                                       TypeDefinitionNode typeDefinitionNode) {
        // 1) Prefer semantic model docs
        Optional<Documentation> documentationOpt = typeDefinitionSymbol.documentation();
        if (documentationOpt.isPresent()) {
            Documentation documentation = documentationOpt.get();
            try {
                // desc can be Optional<String> or String depending on version
                Method descriptionMethod = documentation.getClass().getMethod("description");
                Object descriptionResult = descriptionMethod.invoke(documentation);
                String descriptionText = (descriptionResult instanceof Optional)
                        ? ((Optional<?>) descriptionResult).map(Object::toString).orElse(null)
                        : (descriptionResult instanceof String ? (String) descriptionResult : null);
                if (descriptionText != null && !(descriptionText = descriptionText.trim()).isEmpty())
                    return Optional.of(descriptionText);
            } catch (ReflectiveOperationException ignore) { /* fall back */ }
        }

        // 2) Fallback: syntax metadata
        Optional<MetadataNode> metaOpt = typeDefinitionNode.metadata();
        if (metaOpt.isPresent() && metaOpt.get().documentationString().isPresent()) {
            Node markdownDocNode = metaOpt.get().documentationString().get(); // MarkdownDocumentationNode

            // No children()/toSourceCode(): use toString() and clean it
            String rawMarkdown = markdownDocNode.toString();
            // drop @param/@return lines if present
            rawMarkdown = rawMarkdown.replaceAll("(?m)^@.*$", "");
            // strip leading '# ' from Markdown doc lines
            String cleanedDescription = rawMarkdown.replaceAll("(?m)^#\\s?", "").trim();

            if (!cleanedDescription.isEmpty()) return Optional.of(cleanedDescription);
        }
        return Optional.empty();
    }

    // convenience: serialize + write one schema
    public void writeOne(ObjectNode schema, Path file) {
        JsonSchemaWriter.writePretty(schema, file);
    }

    // convenience: generate for a single known type symbol (if you already have one)
    public ObjectNode generateForType(TypeSymbol ts, String title, URI id) {
        JsonSchemaNode content = BalTypeAdapter.toBasicTypeSchemaNode(ts, true);
        ObjectNode root = JsonSchemaNode.mapper().createObjectNode();
        root.put(JsonSchemaConstants.K_SCHEMA, JsonSchemaConstants.DRAFT_2020_12_SCHEMA);
        if (id != null) root.put(JsonSchemaConstants.K_ID, id.toString());
        if (title != null && !title.isBlank()) root.put(JsonSchemaConstants.K_TITLE, title);
        root.setAll(content.toObjectNode());
        return root;
    }

    private static void visitTypeDefs(SyntaxTree syntaxTree, Consumer<TypeDefinitionNode> onType) {
        NodeVisitor v = new NodeVisitor() {
            @Override
            public void visit(ModulePartNode node) {
                for (ModuleMemberDeclarationNode m : node.members()) {
                    m.accept(this);
                }
            }
            @Override
            public void visit(TypeDefinitionNode node) {
                onType.accept(node);
            }
        };
        syntaxTree.rootNode().accept(v);
    }

    private static Project loadProject(Path p) {
        try {
            return Files.isDirectory(p) ? BuildProject.load(p) : SingleFileProject.load(p);
        } catch (ProjectException e) {
            throw new IllegalArgumentException("Failed to load Ballerina source at " + p, e);
        }
    }
}
