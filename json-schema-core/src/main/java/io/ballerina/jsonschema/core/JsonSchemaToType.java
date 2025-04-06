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

import static io.ballerina.jsonschema.core.Generator.BOOLEAN;
import static io.ballerina.jsonschema.core.Generator.DECIMAL;
import static io.ballerina.jsonschema.core.Generator.FLOAT;
import static io.ballerina.jsonschema.core.Generator.INTEGER;
import static io.ballerina.jsonschema.core.Generator.JSON;
import static io.ballerina.jsonschema.core.Generator.NEVER;
import static io.ballerina.jsonschema.core.Generator.NULL;
import static io.ballerina.jsonschema.core.Generator.NUMBER;
import static io.ballerina.jsonschema.core.Generator.PIPE;
import static io.ballerina.jsonschema.core.Generator.PUBLIC;
import static io.ballerina.jsonschema.core.Generator.SEMI_COLON;
import static io.ballerina.jsonschema.core.Generator.STRING;
import static io.ballerina.jsonschema.core.Generator.TYPE;
import static io.ballerina.jsonschema.core.Generator.UNIVERSAL_ARRAY;
import static io.ballerina.jsonschema.core.Generator.UNIVERSAL_OBJECT;
import static io.ballerina.jsonschema.core.Generator.WHITESPACE;
import static io.ballerina.jsonschema.core.Generator.IMPORT;
import static io.ballerina.jsonschema.core.Generator.createType;
import static io.ballerina.jsonschema.core.SchemaUtils.ID_TO_TYPE_MAP;

public class JsonSchemaToType {
    static final String DEFAULT_SCHEMA_NAME = "Schema";
    public static final String EOF_TOKEN = "";
    public static final String INVALID_IMPORTS_ERROR = "Invalid imports have been found.";

    static final Map<String, ModuleMemberDeclarationNode> NODES = new LinkedHashMap<>();
    static final ArrayList<String> IMPORTS = new ArrayList<>();

    private static final List<JsonSchemaDiagnostic> DIAGNOSTICS = new ArrayList<>();

    public static void addDiagnostic(JsonSchemaDiagnostic diagnostic) {
        DIAGNOSTICS.add(diagnostic);
    }

    public static Response convertBaseSchema(Object schemaObject) throws Exception {

        String typeName = convert(schemaObject, DEFAULT_SCHEMA_NAME);

        if (!typeName.equals(DEFAULT_SCHEMA_NAME)) {
            String schemaDefinition =
                    PUBLIC + WHITESPACE + TYPE + WHITESPACE + DEFAULT_SCHEMA_NAME + WHITESPACE + typeName + SEMI_COLON;
            ModuleMemberDeclarationNode schemaNode = NodeParser.parseModuleMemberDeclaration(schemaDefinition);
            NODES.put(DEFAULT_SCHEMA_NAME, schemaNode);
        }

        ModulePartNode modulePartNode = generateModulePartNode();
        String generatedTypes = formatModuleParts(modulePartNode);
        return new Response(generatedTypes, DIAGNOSTICS);
    }

    static ModulePartNode generateModulePartNode() throws Exception {
        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(NODES.values());
        NodeList<ImportDeclarationNode> imports = getImportDeclarations();
        Token eofToken = AbstractNodeFactory.createIdentifierToken(EOF_TOKEN);
        return NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
    }

    static String formatModuleParts(ModulePartNode modulePartNode) throws FormatterException {
        ForceFormattingOptions forceFormattingOptions = ForceFormattingOptions.builder()
                .setForceFormatRecordFields(true).build();
        FormattingOptions formattingOptions = FormattingOptions.builder()
                .setForceFormattingOptions(forceFormattingOptions).build();
        return Formatter.format(modulePartNode.syntaxTree(), formattingOptions).toSourceCode();
    }

    public static String convert(Object schemaObject, String name) {
        if (schemaObject instanceof Boolean) {
            boolean boolValue = (Boolean) schemaObject;
            return boolValue ? JSON : NEVER;
        }

        Schema schema = (Schema) schemaObject;

        ArrayList<Object> schemaType = getCommonType(schema.getEnumKeyword(), schema.getConstKeyword(),
                schema.getType());
        if (schemaType.isEmpty()) {
            ID_TO_TYPE_MAP.put(schema.getIdKeyword(), NEVER);
            return NEVER;
        } else if (schemaType.contains(Class.class)) {
            //! This is definitely a type
            schemaType.remove(Class.class);
            if (schemaType.size() == 1) {
                // Only a single type.
                String typeName = createType(name, schema, schemaType.getFirst());
                ID_TO_TYPE_MAP.put(schema.getIdKeyword(), typeName);
                return typeName;
            } else {
                Set<String> unionTypes = new HashSet<>();
                for (Object element : schemaType) {
                    String subtypeName = name + getBallerinaType(element);
                    unionTypes.add(createType(subtypeName, schema, element));
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
        } else {
            //TODO: Constraints validate to enums too, need to cross check them and return the value
            String typeName = schemaType.stream().map(String::valueOf).collect(Collectors.joining(PIPE));
            ID_TO_TYPE_MAP.put(schema.getIdKeyword(), typeName);
            return typeName;
        }
    }

    public static ArrayList<Object> getCommonType(ArrayList<Object> enumKeyword, Object constKeyword,
                                                  ArrayList<String> type) {
        Set<Object> typeList = new HashSet<>();
        Set<Object> finalList = new HashSet<>();

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
                switch (element) {
                    case "integer":
                        typeList.add(Long.class);
                        break;
                    case "number":
                        typeList.add(Double.class);
                        break;
                    case "boolean":
                        typeList.add(Boolean.class);
                        break;
                    case "string":
                        typeList.add(String.class);
                        break;
                    case "array":
                        typeList.add(ArrayList.class);
                        break;
                    case "object":
                        typeList.add(LinkedHashMap.class);
                        break;
                    case "null":
                        typeList.add(null);
                        break;
                    default:
                        break;
                }
            }
        }

        if (!typeList.isEmpty()) {
            typeList.add(Class.class);
        }

        // Absence of enum keyword.
        if (enumKeyword == null) {
            if (constKeyword == null) {
                return new ArrayList<>(typeList);
            }
            if (typeList.contains(constKeyword.getClass())) {
                return new ArrayList<>(List.of(constKeyword));
            }
            return new ArrayList<>();
        }

        // Presence of enum keyword.
        for (Object element : enumKeyword) {
            if (typeList.contains(element.getClass())) {
                finalList.add(element);
            }
        }

        if (constKeyword == null) {
            return new ArrayList<>(finalList);
        }

        if (finalList.contains(constKeyword)) { // Checked for reference cross check-ins
            return new ArrayList<>(List.of(constKeyword));
        }
        return new ArrayList<>();
    }

    public static String getBallerinaType(Object type) {
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
            return "Boolean"; // Actually not needed here as there are no keywords for type Boolean
        }
        if (type == ArrayList.class) {
            return "Array";
        }
        if (type == LinkedHashMap.class) {
            return "Object";
        }
        return "Null";
    }

    static NodeList<ImportDeclarationNode> getImportDeclarations() throws Exception {
        Collection<ImportDeclarationNode> imports = new ArrayList<>();
        for (String module : getImports()) {
            ImportDeclarationNode node = NodeParser.parseImportDeclaration(module);
            if (node.hasDiagnostics()) {
                throw new Exception(INVALID_IMPORTS_ERROR);
            }
            imports.add(node);
        }
        return AbstractNodeFactory.createNodeList(imports);
    }


    public static void addImports(String module) {
        String importDeclaration = IMPORT + WHITESPACE + module + SEMI_COLON;
        if (!IMPORTS.contains(importDeclaration)) {
            IMPORTS.add(importDeclaration);
        }
    }

    public static ArrayList<String> getImports() {
        return new ArrayList<>(IMPORTS);
    }
}
