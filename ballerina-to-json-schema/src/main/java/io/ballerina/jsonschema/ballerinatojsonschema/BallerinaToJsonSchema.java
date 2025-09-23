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
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
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
 * Generate Draft 2020-12 JSON Schemas from Ballerina source.
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

        public GeneratorOptions rootMeta() {
            return rootMeta;
        }

        public boolean allowOptionalAsTypeArray() {
            return allowOptionalAsTypeArray;
        }

        public Set<String> typeNameFilter() {
            return typeNameFilter;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private GeneratorOptions rootMeta = GeneratorOptions.builder().build();
            private boolean allowOptionalAsTypeArray = true;
            private final Set<String> typeNameFilter = new LinkedHashSet<>();

            public Builder rootMeta(GeneratorOptions opts) {
                this.rootMeta = opts; return this;
            }

            public Builder allowOptionalAsTypeArray(boolean v){
                this.allowOptionalAsTypeArray = v;
                return this;
            }

            public Builder includeTypes(String... names) {
                this.typeNameFilter.addAll(Arrays.asList(names));
                return this;
            }

            public ApiOptions build() {
                return new ApiOptions(this);
            }
        }
    }

    public Map<String, ObjectNode> generate(Path ballerinaPath, ApiOptions apiOptions) {
        Project project = loadProject(ballerinaPath);
        Package currentPackage = project.currentPackage();
        PackageCompilation compilation = currentPackage.getCompilation();

        Map<String, ObjectNode> schemaByTypeName = new LinkedHashMap<>();
        for (ModuleId moduleId : currentPackage.moduleIds()) {
            Module module = currentPackage.module(moduleId);
            SemanticModel semanticModel = compilation.getSemanticModel(moduleId);

            for (DocumentId documentId : module.documentIds()) {
                Document document = module.document(documentId);
                SyntaxTree syntaxTree = document.syntaxTree();

                Map<String, MetadataNode> typedefMetadataByName = collectTypedefMetadata(syntaxTree);

                visitTypeDefs(syntaxTree, typeDefinitionNode -> {
                    Optional<Symbol> optionalSymbol = semanticModel.symbol(typeDefinitionNode);
                    if (optionalSymbol.isEmpty()
                            || !(optionalSymbol.get() instanceof TypeDefinitionSymbol typeDefinitionSymbol)) {
                        return;
                    }

                    String typeName = typeDefinitionSymbol.getName().orElse(null);
                    if (typeName == null) {
                        return;
                    }

                    if (!apiOptions.typeNameFilter().isEmpty()
                            && !apiOptions.typeNameFilter().contains(typeName)) {
                        return;
                    }

                    TypeSymbol underlyingTypeSymbol = typeDefinitionSymbol.typeDescriptor();

                    Map<String, MetadataNode> recordFieldMetadataByName = collectRecordFieldMetadata(typeDefinitionNode);

                    JsonSchemaNode contentSchemaNode =
                            BalTypeAdapter.toSchemaNode(
                                    underlyingTypeSymbol,
                                    apiOptions.allowOptionalAsTypeArray(),
                                    typeDefinitionNode.metadata(), // typedef-level annotations
                                    recordFieldMetadataByName, // field-level annotations
                                    typedefMetadataByName); // alias annotations for TYPE_REFERENCE fields

                    // Assemble root: $schema/$id/title/description, then merge content
                    ObjectNode rootSchemaObject = JsonSchemaNode.mapper().createObjectNode();
                    apiOptions.rootMeta().schemaUri().ifPresent(uri ->
                            rootSchemaObject.put(JsonSchemaConstants.K_SCHEMA, uri));
                    apiOptions.rootMeta().id().ifPresent(u ->
                            rootSchemaObject.put(JsonSchemaConstants.K_ID, u.toString()));

                    String title = apiOptions.rootMeta().title()
                            .filter(t -> !t.isBlank())
                            .orElse(typeName);
                    rootSchemaObject.put(JsonSchemaConstants.K_TITLE, title);

                    // Pull the # doc comment above the typedef into "description"
                    extractDescription(typeDefinitionSymbol, typeDefinitionNode).ifPresent(desc ->
                            rootSchemaObject.put("description", desc));

                    // Merge the content node (keeps "type", "anyOf", properties, etc.)
                    rootSchemaObject.setAll(contentSchemaNode.toObjectNode());

                    schemaByTypeName.put(typeName, rootSchemaObject);
                });
            }
        }
        return schemaByTypeName;
    }

    private static Map<String, MetadataNode> collectTypedefMetadata(SyntaxTree syntaxTree) {
        Map<String, MetadataNode> byName = new LinkedHashMap<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void visit(ModulePartNode node) {
                for (ModuleMemberDeclarationNode member : node.members()) {
                    member.accept(this);
                }
            }

            @Override
            public void visit(TypeDefinitionNode node) {
                node.metadata().ifPresent(meta -> {
                    String typeNameText = tokenText(node.typeName());
                    byName.put(typeNameText, meta);
                });
            }
        };
        syntaxTree.rootNode().accept(visitor);
        return byName;
    }

    /** Get the textual content of a token across Ballerina versions. */
    private static String tokenText(Token token) {
        if (token instanceof IdentifierToken idTok) {
            return idTok.text();
        }
        try {
            Method textGetterMethod = token.getClass().getMethod("text");
            Object methodReturnValue = textGetterMethod.invoke(token);
            if (methodReturnValue instanceof String s) {
                return s;
            }
        } catch (ReflectiveOperationException ignore) {}
        return token.toString();
    }

    private static Map<String, MetadataNode> collectRecordFieldMetadata(TypeDefinitionNode typeDefinitionNode) {
        Map<String, MetadataNode> byFieldName = new LinkedHashMap<>();
        Node typeDescriptorNode = typeDefinitionNode.typeDescriptor();
        if (typeDescriptorNode instanceof RecordTypeDescriptorNode recordTypeDescriptorNode) {
            for (Node fieldNode : recordTypeDescriptorNode.fields()) {
                if (fieldNode instanceof RecordFieldNode n) {
                    n.metadata().ifPresent(m -> byFieldName.put(n.fieldName().text(), m));
                } else if (fieldNode instanceof RecordFieldWithDefaultValueNode n) {
                    n.metadata().ifPresent(m -> byFieldName.put(n.fieldName().text(), m));
                }
            }
        }
        return byFieldName;
    }

    private static Optional<String> extractDescription(
            TypeDefinitionSymbol typeDefinitionSymbol,
            TypeDefinitionNode typeDefinitionNode) {

        Optional<Documentation> documentationOpt = typeDefinitionSymbol.documentation();
        if (documentationOpt.isPresent()) {
            Documentation documentation = documentationOpt.get();
            try {
                Method descriptionMethod = documentation.getClass().getMethod("description");
                Object descriptionResult = descriptionMethod.invoke(documentation);
                String descriptionText = (descriptionResult instanceof Optional)
                        ? ((Optional<?>) descriptionResult).map(Object::toString).orElse(null)
                        : (descriptionResult instanceof String ? (String) descriptionResult : null);
                if (descriptionText != null && !(descriptionText = descriptionText.trim()).isEmpty()) {
                    return Optional.of(descriptionText);
                }
            } catch (ReflectiveOperationException ignore) { }
        }

        Optional<MetadataNode> metadataOpt = typeDefinitionNode.metadata();
        if (metadataOpt.isPresent() && metadataOpt.get().documentationString().isPresent()) {
            MarkdownDocumentationNode markdownDocNode =
                    (MarkdownDocumentationNode) metadataOpt.get().documentationString().get();
            String rawMarkdown = markdownDocNode.toString();
            rawMarkdown = rawMarkdown.replaceAll("(?m)^@.*$", "");   // drop @param/@return lines if present
            String cleanedDescription = rawMarkdown.replaceAll("(?m)^#\\s?", "").trim();
            if (!cleanedDescription.isEmpty()) {
                return Optional.of(cleanedDescription);
            }
        }
        return Optional.empty();
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

    private static Project loadProject(Path path) {
        try {
            return Files.isDirectory(path) ? BuildProject.load(path) : SingleFileProject.load(path);
        } catch (ProjectException e) {
            throw new IllegalArgumentException("Failed to load Ballerina source at " + path, e);
        }
    }

    // convenience: serialize + write one schema
    public void writeOne(ObjectNode schema, Path file) {
        JsonSchemaWriter.writePretty(schema, file);
    }

    public ObjectNode generateForType(TypeSymbol typeSymbol, String title, URI id) {
        JsonSchemaNode content = BalTypeAdapter.toBasicTypeSchemaNode(typeSymbol, true);
        ObjectNode root = JsonSchemaNode.mapper().createObjectNode();
        root.put(JsonSchemaConstants.K_SCHEMA, JsonSchemaConstants.DRAFT_2020_12_SCHEMA);
        if (id != null) {
            root.put(JsonSchemaConstants.K_ID, id.toString());
        }
        if (title != null && !title.isBlank()) {
            root.put(JsonSchemaConstants.K_TITLE, title);
        }
        root.setAll(content.toObjectNode());
        return root;
    }
}
