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

package io.ballerina.jsonschema.core;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.jsonschema.core.diagnostic.JsonSchemaDiagnostic;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.options.ForceFormattingOptions;
import org.ballerinalang.formatter.core.options.FormattingOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.jsonschema.core.GeneratorUtils.BOOLEAN;
import static io.ballerina.jsonschema.core.GeneratorUtils.DECIMAL;
import static io.ballerina.jsonschema.core.GeneratorUtils.FLOAT;
import static io.ballerina.jsonschema.core.GeneratorUtils.INTEGER;
import static io.ballerina.jsonschema.core.GeneratorUtils.JSON;
import static io.ballerina.jsonschema.core.GeneratorUtils.NEVER;
import static io.ballerina.jsonschema.core.GeneratorUtils.NULL;
import static io.ballerina.jsonschema.core.GeneratorUtils.NUMBER;
import static io.ballerina.jsonschema.core.GeneratorUtils.PIPE;
import static io.ballerina.jsonschema.core.GeneratorUtils.PUBLIC;
import static io.ballerina.jsonschema.core.GeneratorUtils.SEMI_COLON;
import static io.ballerina.jsonschema.core.GeneratorUtils.STRING;
import static io.ballerina.jsonschema.core.GeneratorUtils.TYPE;
import static io.ballerina.jsonschema.core.GeneratorUtils.UNIVERSAL_ARRAY;
import static io.ballerina.jsonschema.core.GeneratorUtils.UNIVERSAL_OBJECT;
import static io.ballerina.jsonschema.core.GeneratorUtils.WHITE_SPACE;
import static io.ballerina.jsonschema.core.GeneratorUtils.IMPORT;
import static io.ballerina.jsonschema.core.GeneratorUtils.createType;
import static io.ballerina.jsonschema.core.SchemaUtils.ID_TO_TYPE_MAP;

/**
 * Ballerina code generation handler.
 *
 * @since 0.1.0
 */
public class Generator {
    static final String DEFAULT_SCHEMA_NAME = "Schema";
    static final String EOF_TOKEN = "";
    static final String INVALID_IMPORTS_ERROR = "Invalid imports have been found.";

    Map<String, ModuleMemberDeclarationNode> nodes = new LinkedHashMap<>();
    ArrayList<String> imports = new ArrayList<>();
    List<JsonSchemaDiagnostic> diagnostics = new ArrayList<>();

    private record BalTypes(
            List<Object> typeList,
            boolean types
    ) {
    };

    public Response convertBaseSchema(Object schemaObject) throws Exception {
        String generatedTypeName = convert(schemaObject, DEFAULT_SCHEMA_NAME);

        if (!generatedTypeName.equals(DEFAULT_SCHEMA_NAME)) {
            String schemaDefinition = PUBLIC + WHITE_SPACE + TYPE + WHITE_SPACE
                    + DEFAULT_SCHEMA_NAME + WHITE_SPACE + generatedTypeName + SEMI_COLON;
            ModuleMemberDeclarationNode schemaNode = NodeParser.parseModuleMemberDeclaration(schemaDefinition);
            this.nodes.put(DEFAULT_SCHEMA_NAME, schemaNode);
        }

        ModulePartNode modulePartNode = generateModulePartNode();
        String generatedTypes = formatModuleParts(modulePartNode);
        return new Response(generatedTypes, this.diagnostics);
    }

    String convert(Object schemaObject, String name) {
        // Handle boolean schemas
        if (schemaObject instanceof Boolean boolValue) {
            return boolValue ? JSON : NEVER;
        }

        Schema schema = (Schema) schemaObject;

        BalTypes balTypes = getCommonType(schema.getEnumKeyword(), schema.getConstKeyword(), schema.getType());
        List<Object> schemaType = balTypes.typeList();

        if (schemaType.isEmpty()) {
            ID_TO_TYPE_MAP.put(schema.getIdKeyword(), NEVER);
            return NEVER;
        }

        if (balTypes.types()) {
            if (schemaType.size() == 1) {
                String typeName = createType(name, schema, schemaType.getFirst(), this);
                ID_TO_TYPE_MAP.put(schema.getIdKeyword(), typeName);
                return typeName;
            }
            Set<String> unionTypes = new HashSet<>();
            for (Object element : schemaType) {
                String subtypeName = name + getBallerinaType(element);
                unionTypes.add(createType(subtypeName, schema, element, this));
            }
            if (unionTypes.containsAll(
                    Set.of(INTEGER, NUMBER, BOOLEAN, STRING, UNIVERSAL_ARRAY, UNIVERSAL_OBJECT, NULL))) {
                ID_TO_TYPE_MAP.put(schema.getIdKeyword(), JSON);
                return JSON;
            }
            if (unionTypes.contains(NUMBER)) {
                unionTypes.remove(NUMBER);
                unionTypes.add(INTEGER);
                unionTypes.add(FLOAT);
                unionTypes.add(DECIMAL);
            }
            String typeName = String.join(PIPE, unionTypes);
            ID_TO_TYPE_MAP.put(schema.getIdKeyword(), typeName);
            return typeName;
        }

        //TODO: Validate constraints on enums
        String typeName = schemaType.stream().map(String::valueOf).collect(Collectors.joining(PIPE));
        ID_TO_TYPE_MAP.put(schema.getIdKeyword(), typeName);
        return typeName;
    }

    private static BalTypes getCommonType(List<Object> enumKeyword, Object constKeyword,
                                          List<String> type) {
        Set<Class<?>> typeList = new HashSet<>();

        if (type == null || type.isEmpty()) {
            typeList.add(Long.class);
            typeList.add(Double.class);
            typeList.add(Boolean.class);
            typeList.add(String.class);
            typeList.add(ArrayList.class);
            typeList.add(LinkedHashMap.class);
            typeList.add(null);
        } else {
            for (String element : type) {
                typeList.add(getIntermediateJsonClassType(element));
            }
        }

        if (enumKeyword == null) {
            if (constKeyword == null) {
                return new BalTypes(new ArrayList<>(typeList), true);
            }
            if (typeList.contains(constKeyword.getClass())) {
                return new BalTypes(new ArrayList<>(List.of(constKeyword)), false);
            }
            return new BalTypes(new ArrayList<>(), false);
        }

        Set<Object> valueList = new HashSet<>();

        for (Object element : enumKeyword) {
            if (typeList.contains(element.getClass())) {
                valueList.add(element);
            }
        }

        if (constKeyword == null) {
            return new BalTypes(new ArrayList<>(valueList), false);
        }

        if (valueList.contains(constKeyword)) {
            return new BalTypes(new ArrayList<>(List.of(constKeyword)), false);
        }
        return new BalTypes(new ArrayList<>(), false);
    }

    private static Class<?> getIntermediateJsonClassType(String type) {
        return switch (type) {
            case "integer" -> Long.class;
            case "number" -> Double.class;
            case "boolean" -> Boolean.class;
            case "string" -> String.class;
            case "array" -> ArrayList.class;
            case "object" -> LinkedHashMap.class;
            case "null" -> null;
            default -> throw new RuntimeException("Unsupported type: " + type);
        };
    }

    private static String getBallerinaType(Object type) {
        if (type == Long.class) {
            return "Integer";
        }
        if (type == Double.class) {
            return "Number";
        }
        if (type == String.class) {
            return "String";
        }
        if (type == Boolean.class) {
            return "Boolean"; // Redundant
        }
        if (type == ArrayList.class) {
            return "Array";
        }
        if (type == LinkedHashMap.class) {
            return "Object";
        }
        return "Null";
    }

    private ModulePartNode generateModulePartNode() throws Exception {
        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(this.nodes.values());
        NodeList<ImportDeclarationNode> imports = getImportDeclarations();
        Token eofToken = AbstractNodeFactory.createIdentifierToken(EOF_TOKEN);
        return NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
    }

    private String formatModuleParts(ModulePartNode modulePartNode) throws FormatterException {
        ForceFormattingOptions forceFormattingOptions = ForceFormattingOptions.builder()
                .setForceFormatRecordFields(true).build();
        FormattingOptions formattingOptions = FormattingOptions.builder()
                .setForceFormattingOptions(forceFormattingOptions).build();
        return Formatter.format(modulePartNode.syntaxTree(), formattingOptions).toSourceCode();
    }

    private NodeList<ImportDeclarationNode> getImportDeclarations() throws Exception {
        Collection<ImportDeclarationNode> imports = new ArrayList<>();
        for (String module : this.getImports()) {
            ImportDeclarationNode node = NodeParser.parseImportDeclaration(module);
            if (node.hasDiagnostics()) {
                throw new Exception(INVALID_IMPORTS_ERROR);
            }
            imports.add(node);
        }
        return AbstractNodeFactory.createNodeList(imports);
    }

    public void addImports(String module) {
        String importDeclaration = IMPORT + WHITE_SPACE + module + SEMI_COLON;
        if (!this.imports.contains(importDeclaration)) {
            this.imports.add(importDeclaration);
        }
    }

    public void addDiagnostic(JsonSchemaDiagnostic diagnostic) {
        this.diagnostics.add(diagnostic);
    }

    public ArrayList<String> getImports() {
        return new ArrayList<>(this.imports);
    }
}
