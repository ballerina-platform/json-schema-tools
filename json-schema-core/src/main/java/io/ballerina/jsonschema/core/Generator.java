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
import io.ballerina.jsonschema.core.SchemaUtils.InvalidDataTypeException;
import io.ballerina.jsonschema.core.diagnostic.JsonSchemaDiagnostic;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.options.ForceFormattingOptions;
import org.ballerinalang.formatter.core.options.FormattingOptions;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.ballerina.jsonschema.core.GeneratorUtils.ADDITIONAL_PROPS;
import static io.ballerina.jsonschema.core.GeneratorUtils.ALL_OF;
import static io.ballerina.jsonschema.core.GeneratorUtils.ANNOTATION_FORMAT;
import static io.ballerina.jsonschema.core.GeneratorUtils.ANNOTATION_MODULE;
import static io.ballerina.jsonschema.core.GeneratorUtils.ANY_OF;
import static io.ballerina.jsonschema.core.GeneratorUtils.ARRAY_CONSTRAINTS;
import static io.ballerina.jsonschema.core.GeneratorUtils.AT;
import static io.ballerina.jsonschema.core.GeneratorUtils.BACK_TICK;
import static io.ballerina.jsonschema.core.GeneratorUtils.BAL_JSON_DATA_MODULE;
import static io.ballerina.jsonschema.core.GeneratorUtils.BOOLEAN;
import static io.ballerina.jsonschema.core.GeneratorUtils.CLOSE_BRACES;
import static io.ballerina.jsonschema.core.GeneratorUtils.CLOSE_BRACKET;
import static io.ballerina.jsonschema.core.GeneratorUtils.CLOSE_SQUARE_BRACKET;
import static io.ballerina.jsonschema.core.GeneratorUtils.COLON;
import static io.ballerina.jsonschema.core.GeneratorUtils.COMMA;
import static io.ballerina.jsonschema.core.GeneratorUtils.COMMENT;
import static io.ballerina.jsonschema.core.GeneratorUtils.CONTAINS;
import static io.ballerina.jsonschema.core.GeneratorUtils.CONTENT_ENCODING;
import static io.ballerina.jsonschema.core.GeneratorUtils.CONTENT_MEDIA_TYPE;
import static io.ballerina.jsonschema.core.GeneratorUtils.CONTENT_SCHEMA;
import static io.ballerina.jsonschema.core.GeneratorUtils.DECIMAL;
import static io.ballerina.jsonschema.core.GeneratorUtils.DEPENDENT_SCHEMA;
import static io.ballerina.jsonschema.core.GeneratorUtils.DEPRECATED;
import static io.ballerina.jsonschema.core.GeneratorUtils.DOUBLE_QUOTATION;
import static io.ballerina.jsonschema.core.GeneratorUtils.EMPTY_ARRAY;
import static io.ballerina.jsonschema.core.GeneratorUtils.EMPTY_RECORD;
import static io.ballerina.jsonschema.core.GeneratorUtils.EXAMPLES;
import static io.ballerina.jsonschema.core.GeneratorUtils.EXCLUSIVE_MAXIMUM;
import static io.ballerina.jsonschema.core.GeneratorUtils.EXCLUSIVE_MINIMUM;
import static io.ballerina.jsonschema.core.GeneratorUtils.FLOAT;
import static io.ballerina.jsonschema.core.GeneratorUtils.FORMAT;
import static io.ballerina.jsonschema.core.GeneratorUtils.IMPORT;
import static io.ballerina.jsonschema.core.GeneratorUtils.INTEGER;
import static io.ballerina.jsonschema.core.GeneratorUtils.ITEM_SUFFIX;
import static io.ballerina.jsonschema.core.GeneratorUtils.JSON;
import static io.ballerina.jsonschema.core.GeneratorUtils.MAXIMUM;
import static io.ballerina.jsonschema.core.GeneratorUtils.MAX_CONTAINS;
import static io.ballerina.jsonschema.core.GeneratorUtils.MAX_ITEMS;
import static io.ballerina.jsonschema.core.GeneratorUtils.MAX_LENGTH;
import static io.ballerina.jsonschema.core.GeneratorUtils.MAX_PROPERTIES;
import static io.ballerina.jsonschema.core.GeneratorUtils.META_DATA;
import static io.ballerina.jsonschema.core.GeneratorUtils.MINIMUM;
import static io.ballerina.jsonschema.core.GeneratorUtils.MIN_CONTAINS;
import static io.ballerina.jsonschema.core.GeneratorUtils.MIN_ITEMS;
import static io.ballerina.jsonschema.core.GeneratorUtils.MIN_LENGTH;
import static io.ballerina.jsonschema.core.GeneratorUtils.MIN_PROPERTIES;
import static io.ballerina.jsonschema.core.GeneratorUtils.MULTIPLE_OF;
import static io.ballerina.jsonschema.core.GeneratorUtils.NAME_REST_ITEM;
import static io.ballerina.jsonschema.core.GeneratorUtils.NEVER;
import static io.ballerina.jsonschema.core.GeneratorUtils.NEW_LINE;
import static io.ballerina.jsonschema.core.GeneratorUtils.NOT;
import static io.ballerina.jsonschema.core.GeneratorUtils.NULL;
import static io.ballerina.jsonschema.core.GeneratorUtils.NUMBER;
import static io.ballerina.jsonschema.core.GeneratorUtils.NUMBER_CONSTRAINTS;
import static io.ballerina.jsonschema.core.GeneratorUtils.OBJECT_CONSTRAINTS;
import static io.ballerina.jsonschema.core.GeneratorUtils.ONE_OF;
import static io.ballerina.jsonschema.core.GeneratorUtils.OPEN_BRACES;
import static io.ballerina.jsonschema.core.GeneratorUtils.OPEN_BRACKET;
import static io.ballerina.jsonschema.core.GeneratorUtils.OPEN_SQUARE_BRACKET;
import static io.ballerina.jsonschema.core.GeneratorUtils.PATTERN;
import static io.ballerina.jsonschema.core.GeneratorUtils.PATTERN_ELEMENT;
import static io.ballerina.jsonschema.core.GeneratorUtils.PATTERN_FORMAT;
import static io.ballerina.jsonschema.core.GeneratorUtils.PATTERN_PROPERTIES;
import static io.ballerina.jsonschema.core.GeneratorUtils.PATTERN_RECORD;
import static io.ballerina.jsonschema.core.GeneratorUtils.PIPE;
import static io.ballerina.jsonschema.core.GeneratorUtils.PROPERTY_NAMES;
import static io.ballerina.jsonschema.core.GeneratorUtils.PROPERTY_NAMES_SUFFIX;
import static io.ballerina.jsonschema.core.GeneratorUtils.PUBLIC;
import static io.ballerina.jsonschema.core.GeneratorUtils.READ_ONLY;
import static io.ballerina.jsonschema.core.GeneratorUtils.RECORD;
import static io.ballerina.jsonschema.core.GeneratorUtils.REGEX_PREFIX;
import static io.ballerina.jsonschema.core.GeneratorUtils.REST;
import static io.ballerina.jsonschema.core.GeneratorUtils.REST_TYPE;
import static io.ballerina.jsonschema.core.GeneratorUtils.SEMI_COLON;
import static io.ballerina.jsonschema.core.GeneratorUtils.STRING;
import static io.ballerina.jsonschema.core.GeneratorUtils.STRING_CONSTRAINTS;
import static io.ballerina.jsonschema.core.GeneratorUtils.STRING_ENCODING;
import static io.ballerina.jsonschema.core.GeneratorUtils.STRING_FORMATS;
import static io.ballerina.jsonschema.core.GeneratorUtils.TAB;
import static io.ballerina.jsonschema.core.GeneratorUtils.TITLE;
import static io.ballerina.jsonschema.core.GeneratorUtils.TYPE;
import static io.ballerina.jsonschema.core.GeneratorUtils.TYPE_FORMAT;
import static io.ballerina.jsonschema.core.GeneratorUtils.UNEVALUATED_ITEMS;
import static io.ballerina.jsonschema.core.GeneratorUtils.UNEVALUATED_ITEMS_SUFFIX;
import static io.ballerina.jsonschema.core.GeneratorUtils.UNEVALUATED_PROPS;
import static io.ballerina.jsonschema.core.GeneratorUtils.UNIQUE_ITEMS;
import static io.ballerina.jsonschema.core.GeneratorUtils.UNIVERSAL_ARRAY;
import static io.ballerina.jsonschema.core.GeneratorUtils.UNIVERSAL_OBJECT;
import static io.ballerina.jsonschema.core.GeneratorUtils.VALUE;
import static io.ballerina.jsonschema.core.GeneratorUtils.WHITE_SPACE;
import static io.ballerina.jsonschema.core.GeneratorUtils.WRITE_ONLY;
import static io.ballerina.jsonschema.core.GeneratorUtils.ZERO;
import static io.ballerina.jsonschema.core.GeneratorUtils.addIfNotNull;
import static io.ballerina.jsonschema.core.GeneratorUtils.addIfNotNullString;
import static io.ballerina.jsonschema.core.GeneratorUtils.convertToCamelCase;
import static io.ballerina.jsonschema.core.GeneratorUtils.convertToPascalCase;
import static io.ballerina.jsonschema.core.GeneratorUtils.getFormattedAnnotation;
import static io.ballerina.jsonschema.core.GeneratorUtils.handleUnion;
import static io.ballerina.jsonschema.core.GeneratorUtils.isInvalidNumberLimit;
import static io.ballerina.jsonschema.core.GeneratorUtils.areAllNull;
import static io.ballerina.jsonschema.core.GeneratorUtils.isPrimitiveBalType;
import static io.ballerina.jsonschema.core.GeneratorUtils.processRecordFields;
import static io.ballerina.jsonschema.core.GeneratorUtils.processRequiredFields;
import static io.ballerina.jsonschema.core.GeneratorUtils.resolveConstMapping;
import static io.ballerina.jsonschema.core.GeneratorUtils.resolveNameConflicts;
import static io.ballerina.jsonschema.core.GeneratorUtils.resolveTypeNameForTypedesc;
import static io.ballerina.jsonschema.core.Schema.deepCopy;
import static io.ballerina.jsonschema.core.SchemaUtils.convertToAbsoluteUri;
import static io.ballerina.jsonschema.core.SchemaUtils.fetchSchemaId;
import static io.ballerina.jsonschema.core.SchemaUtils.getSchemaById;

/**
 * Ballerina code generation handler.
 *
 * @since 0.1.0
 */
public class Generator {
    static final String DEFAULT_SCHEMA_NAME = "Schema";
    static final String EOF_TOKEN = "";
    static final String INVALID_IMPORTS_ERROR = "Invalid imports have been found.";

    // If a union contains more than this number of tuple types, it will be represented using annotations
    static final int MAX_UNION_TUPLE_TYPES = 5;
    // If a tuple type has more than this number of elements, it will be represented using annotations
    static final int MAX_TUPLE_MEMBER_COUNT = 10;

    Map<String, ModuleMemberDeclarationNode> nodes = new LinkedHashMap<>();
    final ArrayList<String> imports = new ArrayList<>();
    final List<JsonSchemaDiagnostic> diagnostics = new ArrayList<>();

    //! This is definitely a schema as id's can only be assigned to schema values
    final Map<URI, Schema> idToSchemaMap = new HashMap<>();

    //! Add a comment for this mapping ( If it works )
    final Map<Schema, String> schemaToTypeMap = new HashMap<>();

    private int constCounter = 0;

    int getNextConstIndex() {
        return ++this.constCounter;
    }

    public Response convertBaseSchema(ArrayList<Object> schemaObjectList) throws Exception {
        // If there are multiple schemas (Starting with a non-boolean schema), validate the presence of id's in all
        // References are stored as deepCopies to avoid modifications in the later part of the code
        ArrayList<Object> schemaCopyList = new ArrayList<>();
        if (schemaObjectList.getFirst() instanceof Schema schema) {
            if (schemaObjectList.size() > 1) {
                for (Object schemaObject : schemaObjectList) {
                    if (schemaObject instanceof Boolean) {
                        continue;
                    }
                    if (schema.getIdKeyword() == null) {
                        throw new Exception("All the schemas must have an id if there are multiple schema files.");
                    }
                    fetchSchemaId(schemaObject, URI.create(""), this.idToSchemaMap);
                }
            } else {
                if (schema.getIdKeyword() == null) {
                    schema.setIdKeyword("dummy:/");
                }
                fetchSchemaId(schema, URI.create(""), this.idToSchemaMap);
            }

            for (Object schemaObject : schemaObjectList) {
                convertToAbsoluteUri(schemaObject, URI.create("dummy:/"));
            }
        }

        // Create a copy list to facilitate future schema mutations.
        for (Object schemaObject : schemaObjectList) {
            schemaCopyList.add(deepCopy(schemaObject));
        }

        // Generate the ballerina code based on the first element
        Object schemaObject = schemaCopyList.getFirst();
        String generatedTypeName = convert(schemaObject, DEFAULT_SCHEMA_NAME);

        if (!generatedTypeName.equals(DEFAULT_SCHEMA_NAME)) {
            String schemaDefinition = String.format(TYPE_FORMAT, DEFAULT_SCHEMA_NAME, generatedTypeName);
            ModuleMemberDeclarationNode schemaNode = NodeParser.parseModuleMemberDeclaration(schemaDefinition);
            this.nodes.put(DEFAULT_SCHEMA_NAME, schemaNode);
        }

        ModulePartNode modulePartNode = generateModulePartNode();
        String generatedTypes = formatModuleParts(modulePartNode);
        return new Response(generatedTypes, this.diagnostics);
    }

    public enum AnnotType {
        TYPE,
        FIELD
    }

    public String convert(Object schemaObject, String name) throws Exception {
        return convert(schemaObject, name, AnnotType.TYPE);
    }

    public String convert(Object schemaObject, String name, AnnotType type) throws Exception {
        // JSON Schema allows a schema to be a boolean: `true` allows any value, `false` allows none.
        // It is handled here before processing object-based schemas.
        if (schemaObject instanceof Boolean boolValue) {
            return boolValue ? JSON : NEVER;
        }

        Schema schema = (Schema) schemaObject;

        if (schema.getRefKeyword() != null) {
            Object obj = getSchemaById(idToSchemaMap, schema.getRefKeyword());
            return convert(obj, name);
        } else if (schema.getDynamicRefKeyword() != null) {
            Object obj = getSchemaById(idToSchemaMap, schema.getDynamicRefKeyword());
            return convert(obj, name);
        }

        if (schemaToTypeMap.containsKey(schema)) {
            return schemaToTypeMap.get(schema);
        }

        extractCombiningSchemas(schema);

        List<Object> allOf = schema.getAllOf();
        List<Object> oneOf = schema.getOneOf();
        List<Object> anyOf = schema.getAnyOf();

        if (allOf != null && !allOf.isEmpty()) {
            return generateCombinedCode(name, schema, allOf, ALL_OF, s -> s.setAllOf(null));
        }
        if (oneOf != null && !oneOf.isEmpty()) {
            return generateCombinedCode(name, schema, oneOf, ONE_OF, s -> s.setOneOf(null));
        }
        if (anyOf != null && !anyOf.isEmpty()) {
            return generateCombinedCode(name, schema, anyOf, ANY_OF, s -> s.setAnyOf(null));
        }

        BalTypes balTypes = getCommonType(schema.getEnumKeyword(), schema.getConstKeyword(), schema.getType());
        List<Object> schemaType = balTypes.typeList();

        if (schemaType.isEmpty()) {
            String finalType = processMetaData(schema, NEVER, name, type);
            schemaToTypeMap.put(schema, finalType);
            return finalType;
        }

        if (balTypes.types()) {
            if (schemaType.contains(Double.class)) {
                schemaType.remove(Long.class);
            }
            if (schemaType.size() == 1) {
                String typeName = createType(name, schema, schemaType.getFirst());
                String finalType = processMetaData(schema, typeName, name, type);
                schemaToTypeMap.put(schema, finalType);
                return finalType;
            }

            Set<String> unionTypes = new LinkedHashSet<>();

            for (Object element : schemaType) {
                String subtypeName = name + getBallerinaType(element);
                unionTypes.add(createType(subtypeName, schema, element));
            }
            if (unionTypes.containsAll(
                    Set.of(NUMBER, BOOLEAN, STRING, UNIVERSAL_ARRAY, UNIVERSAL_OBJECT, NULL))) {
                String finalType = processMetaData(schema, JSON, name, type);
                schemaToTypeMap.put(schema, finalType);
                return finalType;
            }
            if (unionTypes.contains(NUMBER)) {
                unionTypes.remove(NUMBER);
                unionTypes.add(INTEGER);
                unionTypes.add(FLOAT);
                unionTypes.add(DECIMAL);
            }

            String typeName = String.join(PIPE, unionTypes);
            String finalType = processMetaData(schema, typeName, name, type);
            schemaToTypeMap.put(schema, finalType);
            return finalType;
        }

        //TODO: Validate constraints on enums

        String typeName = schemaType.stream()
                .map(element -> {
                    try {
                        return generateStringRepresentation(element);
                    } catch (InvalidDataTypeException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.joining(PIPE));

        Schema duplicateSchema = (Schema) deepCopy(schema);
        removeMetaDataAndTypeInfo(duplicateSchema);

        if (duplicateSchema.equals(new Schema())) {
            String finalType = processMetaData(schema, typeName, name, type);
            schemaToTypeMap.put(schema, finalType);
            return finalType;
        }

        throw new Exception("Constraints on enums and constants are not currently supported");
    }

    private static void removeMetaDataAndTypeInfo(Schema schema) {
        schema.setType(null);
        schema.setConstKeyword(null);
        schema.setEnumKeyword(null);
        schema.setIdKeyword(null);
        schema.setSchemaKeyword(null);
        schema.setAnchorKeyword(null);
        schema.setDynamicRefKeyword(null);
        schema.setVocabularyKeyword(null);
        schema.setCommentKeyword(null);
        schema.setTitle(null);
        schema.setDescription(null);
        schema.setExamples(null);
        schema.setDefaultKeyword(null);
    }

    private void transferType(Schema mainObj, Object subObj) {
        if (subObj instanceof Schema schema && schema.getType() == null) {
            List<String> typeList = mainObj.getType();
            schema.setType(new ArrayList<>(typeList));
        }
    }

    private String generateCombinedCode(String name, Schema schema, List<Object> combiningList, String combType,
                                        Consumer<Schema> schemaMutator) throws Exception {
        schemaMutator.accept(schema);
        name = resolveNameConflicts(name, this);
        String mainType = convert(schema, name + "MainType");

        List<String> allOfElements = new ArrayList<>();
        int count = 0;
        for (Object obj : combiningList) {
            transferType(schema, obj);
            String elementName;
            do {
                elementName = name + combType + (++count);
            } while (this.nodes.containsKey(elementName));
            String allOfElement = convert(obj, elementName);
            allOfElements.add(allOfElement);
        }

        addJsonDataImport();
        String subTypesName = resolveNameConflicts(name + "SubTypes", this);
        String annotSuffix = combType.equals(ANY_OF) ? "" : AT + ANNOTATION_MODULE + COLON + combType + NEW_LINE;
        String subTypeWithAnnot = annotSuffix + String.format(TYPE_FORMAT, subTypesName,
                String.join(PIPE, allOfElements));

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(subTypeWithAnnot);
        nodes.put(subTypesName, moduleNode);

        String fullDeclaration = AT + ANNOTATION_MODULE + COLON + ALL_OF + NEW_LINE +
                String.format(TYPE_FORMAT, name, mainType + PIPE + subTypesName);
        ModuleMemberDeclarationNode moduleNodeMain = NodeParser.parseModuleMemberDeclaration(fullDeclaration);
        nodes.put(name, moduleNodeMain);
        return name;
    }

    private static void extractCombiningSchemas(Schema schema) {
        // Handle if-then-else
        List<Object> combinedSchema = new ArrayList<>(List.of());
        int keywordCount = 0;

        List<Object> ifResolved = new ArrayList<>();
        Object ifCondition = schema.getIfKeyword();
        Object thenCondition = schema.getThen();
        Object elseCondition = schema.getElseKeyword();
        if (ifCondition != null && (thenCondition != null || elseCondition != null)) {
            if (thenCondition != null) {
                List<Object> allOfList1 = new ArrayList<>();
                allOfList1.add(ifCondition);
                allOfList1.add(thenCondition);
                Schema allOf1 = new Schema();
                allOf1.setAllOf(allOfList1);
                ifResolved.add(allOf1);
            }
            if (elseCondition != null) {
                List<Object> allOfList2 = new ArrayList<>();
                Schema notSchema = new Schema();
                notSchema.setNot(ifCondition);
                allOfList2.add(notSchema);
                allOfList2.add(elseCondition);
                Schema allOf2 = new Schema();
                allOf2.setAllOf(allOfList2);
                ifResolved.add(allOf2);
            }
            if (schema.getOneOf() != null) {
                Schema ifOneOf = new Schema();
                ifOneOf.setOneOf(ifResolved);
                combinedSchema.add(ifOneOf);
                keywordCount++;
            } else {
                schema.setOneOf(ifResolved);
            }

            schema.setIfKeyword(null);
            schema.setThen(null);
            schema.setElseKeyword(null);
        }

        // Handle AllOf, OneOf, AnyOf
        if (schema.getAnyOf() != null) {
            keywordCount++;
            Schema anyOfSchema = new Schema();
            anyOfSchema.setAnyOf(schema.getAnyOf());
            combinedSchema.add(anyOfSchema);
        }
        if (schema.getOneOf() != null) {
            keywordCount++;
            Schema oneOfSchema = new Schema();
            oneOfSchema.setOneOf(schema.getOneOf());
            combinedSchema.add(oneOfSchema);
        }
        if (schema.getAllOf() != null) {
            keywordCount++;
            Schema allOfSchema = new Schema();
            allOfSchema.setAllOf(schema.getAllOf());
            combinedSchema.add(allOfSchema);
        }

        if (keywordCount > 1) {
            schema.setAllOf(combinedSchema);
            schema.setAnyOf(null);
            schema.setOneOf(null);
        }
    }

    private String processMetaData(Schema schema, String type, String name, AnnotType typeAnnot) throws Exception {
        //TODO: Extract all the necessary keywords here.

        // Convert all boolean values to "null" or "true" (Default null value represents false)
        if (Boolean.FALSE.equals(schema.getWriteOnly())) {
            schema.setWriteOnly(null);
        }
        if (Boolean.FALSE.equals(schema.getReadOnly())) {
            schema.setReadOnly(null);
        }
        if (Boolean.FALSE.equals(schema.getDeprecated())) {
            schema.setDeprecated(null);
        }

        // Early return if the metadata fields, not keyword and annotations are empty
        if (areAllNull(schema.getTitle(), schema.getCommentKeyword(), schema.getExamples(), schema.getWriteOnly(),
                schema.getNot())) {
            if (typeAnnot == AnnotType.FIELD) {
                return type;
            }
            if (areAllNull(schema.getDescription(), schema.getReadOnly(), schema.getDeprecated())) {
                return type;
            }
        }

        List<String> annotations = new ArrayList<>();

        if (schema.getNot() != null) {
            annotations.add(String.format(ANNOTATION_FORMAT, ANNOTATION_MODULE, NOT,
                    VALUE + COLON + resolveTypeNameForTypedesc(
                            this.convert(schema.getNot(), name + NOT), name + NOT, this)));
        }

        if (typeAnnot != AnnotType.FIELD && schema.getDescription() != null) {
            annotations.add("# " + schema.getDescription());
        }

        List<String> annotationParts = new ArrayList<>();
        addIfNotNullString(annotationParts, TITLE, schema.getTitle());
        addIfNotNullString(annotationParts, COMMENT, schema.getCommentKeyword());
        if (schema.getExamples() != null) {
            List<String> examples = new ArrayList<>();
            for (Object example : schema.getExamples()) {
                examples.add(generateStringRepresentation(example));
            }
            String exampleString = "[" + String.join(COMMA, examples) + "]";
            annotationParts.add(EXAMPLES + COLON + exampleString);
        }
        String annotationFields = String.join(COMMA + NEW_LINE + TAB, annotationParts);
        if (!annotationFields.isEmpty()) {
            addJsonDataImport();
            annotations.add(String.format(ANNOTATION_FORMAT, ANNOTATION_MODULE, META_DATA, annotationFields));
        }

        if (schema.getDeprecated() != null && typeAnnot != AnnotType.FIELD) {
            annotations.add(DEPRECATED);
        }
        if (schema.getWriteOnly() != null) {
            addJsonDataImport();
            annotations.add(WRITE_ONLY);
        }
        if (schema.getReadOnly() != null && typeAnnot != AnnotType.FIELD) {
            addJsonDataImport();
            annotations.add(READ_ONLY);
        }

        String annotString = String.join(NEW_LINE, annotations);

        if (this.nodes.containsKey(type)) {
            this.nodes.put(type, NodeParser.parseModuleMemberDeclaration(annotString +
                    NEW_LINE + this.nodes.get(type)));
            return type;
        } else {
            String resolvedName = resolveNameConflicts(name, this);
            this.nodes.put(resolvedName, NodeParser.parseModuleMemberDeclaration(annotString + NEW_LINE +
                    String.format(TYPE_FORMAT, resolvedName, type)));
            return resolvedName;
        }
    }

    private String createType(String name, Schema schema, Object type) throws Exception {
        if (type == null) {
            return NULL;
        }
        if (type == Boolean.class) {
            return BOOLEAN;
        }
        if (type == Long.class) {
            return createInteger(name, schema);
        }
        if (type == Double.class) {
            return createNumber(name, schema);
        }
        if (type == String.class) {
            return createString(name, schema);
        }
        if (type == ArrayList.class) {
            return createArray(name, schema);
        }
        if (type == Map.class) {
            return createObject(name, schema);
        }
        throw new RuntimeException("Type currently not supported");
    }

    private String createInteger(String name, Schema schema) {
        Double minimum = schema.getMinimum();
        Double exclusiveMinimum = schema.getExclusiveMinimum();
        Double maximum = schema.getMaximum();
        Double exclusiveMaximum = schema.getExclusiveMaximum();
        Double multipleOf = schema.getMultipleOf();

        if (areAllNull(minimum, exclusiveMinimum, maximum, exclusiveMaximum, multipleOf)) {
            return INTEGER;
        }

        if (isInvalidNumberLimit(minimum, exclusiveMinimum, maximum, exclusiveMaximum)) {
            return NEVER;
        }

        this.addJsonDataImport();
        String finalType = resolveNameConflicts(convertToPascalCase(name), this);

        List<String> annotationParts = new ArrayList<>();

        addIfNotNull(annotationParts, MINIMUM, minimum);
        addIfNotNull(annotationParts, EXCLUSIVE_MINIMUM, exclusiveMinimum);
        addIfNotNull(annotationParts, MAXIMUM, maximum);
        addIfNotNull(annotationParts, EXCLUSIVE_MAXIMUM, exclusiveMaximum);
        addIfNotNull(annotationParts, MULTIPLE_OF, multipleOf);

        String formattedAnnotation = getFormattedAnnotation(
                annotationParts, NUMBER_CONSTRAINTS, finalType, INTEGER);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        this.nodes.put(finalType, moduleNode);

        return finalType;
    }

    private String createNumber(String name, Schema schema) {
        Double minimum = schema.getMinimum();
        Double exclusiveMinimum = schema.getExclusiveMinimum();
        Double maximum = schema.getMaximum();
        Double exclusiveMaximum = schema.getExclusiveMaximum();
        Double multipleOf = schema.getMultipleOf();

        if (areAllNull(minimum, exclusiveMinimum, maximum, exclusiveMaximum, multipleOf)) {
            return NUMBER;
        }

        if (isInvalidNumberLimit(minimum, exclusiveMinimum, maximum, exclusiveMaximum)) {
            return NEVER;
        }

        this.addJsonDataImport();
        String finalType = resolveNameConflicts(convertToPascalCase(name), this);

        List<String> annotationParts = new ArrayList<>();

        addIfNotNull(annotationParts, MINIMUM, minimum);
        addIfNotNull(annotationParts, EXCLUSIVE_MINIMUM, exclusiveMinimum);
        addIfNotNull(annotationParts, MAXIMUM, maximum);
        addIfNotNull(annotationParts, EXCLUSIVE_MAXIMUM, exclusiveMaximum);
        addIfNotNull(annotationParts, MULTIPLE_OF, multipleOf);

        String formattedAnnotation = getFormattedAnnotation(
                annotationParts, NUMBER_CONSTRAINTS, finalType, NUMBER);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        this.nodes.put(finalType, moduleNode);

        return finalType;
    }

    private String createString(String name, Schema schema) throws Exception {
        String format = schema.getFormat();
        Long minLength = schema.getMinLength();
        Long maxLength = schema.getMaxLength();
        String pattern = schema.getPattern();
        String contentEncoding = schema.getContentEncoding();
        String contentMediaType = schema.getContentMediaType();
        Object contentSchema = schema.getContentSchema();

        if (areAllNull(format, minLength, maxLength, pattern)) {
            return STRING;
        }

        this.addJsonDataImport();
        String finalType = resolveNameConflicts(convertToPascalCase(name), this);

        List<String> annotations = new ArrayList<>();

        if (!areAllNull(format, minLength, maxLength, pattern)) {
            List<String> annotationParts = new ArrayList<>();

            if (format != null) {
                if (!STRING_FORMATS.contains(format)) {
                    throw new IllegalArgumentException("Invalid format: " + format);
                }
                annotationParts.add(FORMAT + COLON + DOUBLE_QUOTATION +
                        format + DOUBLE_QUOTATION);
            }

            addIfNotNull(annotationParts, MIN_LENGTH, minLength);
            addIfNotNull(annotationParts, MAX_LENGTH, maxLength);

            if (pattern != null) {
                annotationParts.add(PATTERN + COLON + REGEX_PREFIX +
                        BACK_TICK + pattern + BACK_TICK);
            }

            annotations.add(String.format(ANNOTATION_FORMAT, ANNOTATION_MODULE, STRING_CONSTRAINTS,
                    String.join(COMMA, annotationParts)));
        }

        if (!areAllNull(contentEncoding, contentMediaType, contentSchema)) {
            List<String> annotationParts = new ArrayList<>();

            addIfNotNullString(annotationParts, CONTENT_ENCODING, contentEncoding);
            addIfNotNullString(annotationParts, CONTENT_MEDIA_TYPE, contentMediaType);

            if (contentSchema != null) {
                String contentSchemaName = finalType + convertToPascalCase(CONTENT_SCHEMA);
                String contentSchemaType = this.convert(contentSchema, contentSchemaName);
                annotationParts.add(CONTENT_SCHEMA + COLON + DOUBLE_QUOTATION +
                        resolveTypeNameForTypedesc(contentSchemaName, contentSchemaType, this));
            }

            annotations.add(String.format(ANNOTATION_FORMAT, ANNOTATION_MODULE, STRING_ENCODING,
                    String.join(COMMA, annotationParts)));
        }

        String formattedAnnotation = String.join(NEW_LINE, annotations) + NEW_LINE +
                String.format(TYPE_FORMAT, finalType, STRING);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        this.nodes.put(finalType, moduleNode);

        return finalType;
    }

    private String createArray(String name, Schema schema) throws Exception {
        List<Object> prefixItems = schema.getPrefixItems();
        Object items = schema.getItems();
        Object contains = schema.getContains();
        Long minItems = schema.getMinItems();
        Long maxItems = schema.getMaxItems();
        Boolean uniqueItems = schema.getUniqueItems();
        Long maxContains = schema.getMaxContains();
        Long minContains = schema.getMinContains();
        Object unevaluatedItems = schema.getUnevaluatedItems();

        String finalType = resolveNameConflicts(convertToPascalCase(name), this);
        allocateTypeToSchema(finalType, schema);

        ArrayList<String> arrayItems = new ArrayList<>();

        if (prefixItems != null) {
            for (int i = 0; i < prefixItems.size(); i++) {
                Object item = prefixItems.get(i);
                arrayItems.add(this.convert(item, finalType + ITEM_SUFFIX + i));
            }
        }

        long startPosition = minItems == null ? 0L : minItems;
        long endPosition = maxItems == null ? Long.MAX_VALUE : maxItems;
        long annotationLimit = startPosition + MAX_UNION_TUPLE_TYPES;

        String restItem = JSON;
        if (items != null) {
            restItem = this.convert(items, finalType + NAME_REST_ITEM);
            if (restItem.contains(PIPE)) {
                restItem = OPEN_BRACKET + restItem + CLOSE_BRACKET;
            }
        }

        //TODO: Create sub-schemas before all the early return types for schema reference implementation.

        if ((endPosition < startPosition) || (restItem.equals(NEVER) && arrayItems.size() < startPosition)) {
            this.nodes.remove(finalType);
            return NEVER;
        }

        // Determine the rest item type
        if (!restItem.equals(NEVER)) {
            if (arrayItems.size() < startPosition) {
                if (startPosition < MAX_TUPLE_MEMBER_COUNT) {
                    for (int i = arrayItems.size(); i < startPosition; i++) {
                        arrayItems.add(restItem);
                    }
                    // Avoids further annotations on minItems
                    minItems = null;
                } else {
                    // Accommodates the startPosition including the rest item type
                    startPosition = arrayItems.size() + 1;
                }
            }
            if (endPosition < annotationLimit) {
                for (int i = arrayItems.size(); i < endPosition; i++) {
                    arrayItems.add(restItem);
                }
                // Avoids further annotations on maxItems
                maxItems = null;
            } else {
                arrayItems.add(restItem + REST);
            }
        }

        ArrayList<String> tupleList = new ArrayList<>();

        long upperBound = Math.min(Math.min(annotationLimit, endPosition), arrayItems.size());
        for (int i = (int) startPosition; i <= upperBound; i++) {
            tupleList.add(OPEN_SQUARE_BRACKET + String.join(COMMA, arrayItems.subList(0, i)) + CLOSE_SQUARE_BRACKET);
        }

        // Replace [] with json[0] if present.
        if (tupleList.getFirst().equals(OPEN_SQUARE_BRACKET + CLOSE_SQUARE_BRACKET)) {
            tupleList.set(0, EMPTY_ARRAY);
        }

        // If the last item is a rest item, the previous array element is redundant.
        if (tupleList.getLast().contains(REST) && tupleList.size() >= 2) {
            tupleList.remove(tupleList.size() - 2);
        }

        if ((minItems == null) && (maxItems == null) && (uniqueItems == null) && (contains == null)) {
            this.nodes.remove(finalType);
            return String.join(PIPE, tupleList);
        }

        this.addJsonDataImport();
        List<String> annotationParts = new ArrayList<>();

        addIfNotNull(annotationParts, MIN_ITEMS, minItems);
        addIfNotNull(annotationParts, MAX_ITEMS, maxItems);
        addIfNotNull(annotationParts, UNIQUE_ITEMS, uniqueItems);

        if (contains != null) {
            String containsRecordName = resolveNameConflicts(finalType +
                    convertToPascalCase(CONTAINS), this);
            String newType = this.convert(contains, containsRecordName);

            if (newType.contains(PIPE)) {
                // Ballerina typedesc doesn't allow union types. Hence, we need to create a new type definition
                String typeDef = String.format(TYPE_FORMAT, containsRecordName, newType);
                ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(typeDef);
                this.nodes.put(containsRecordName, moduleNode);
                newType = containsRecordName;
            }

            List<String> containsAnnotationParts = new ArrayList<>();

            containsAnnotationParts.add(CONTAINS + COLON + WHITE_SPACE + newType);
            if (minContains == null) {
                containsAnnotationParts.add(MIN_CONTAINS + COLON + WHITE_SPACE +
                        ZERO);
            } else {
                containsAnnotationParts.add(MIN_CONTAINS + COLON + WHITE_SPACE +
                        minContains);
            }

            addIfNotNull(containsAnnotationParts, MAX_CONTAINS, maxContains);

            String combined = String.join(COMMA, containsAnnotationParts);
            annotationParts.add(CONTAINS + COLON + WHITE_SPACE + OPEN_BRACES + combined +
                    CLOSE_BRACES);
        }

        if (unevaluatedItems != null) {
            String customTypeName = finalType + UNEVALUATED_ITEMS_SUFFIX;
            String typeName = this.convert(unevaluatedItems, customTypeName);
            annotationParts.add(UNEVALUATED_ITEMS + COLON + WHITE_SPACE +
                    resolveTypeNameForTypedesc(customTypeName, typeName, this));
        }

        String formattedAnnotation = getFormattedAnnotation(annotationParts,
                ARRAY_CONSTRAINTS, finalType,
                String.join(PIPE, tupleList));

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        this.nodes.put(finalType, moduleNode);

        return finalType;
    }

    private String createObject(String name, Schema schema) throws Exception {
        Object additionalProperties = schema.getAdditionalProperties();
        Map<String, Object> properties = schema.getProperties();
        Map<String, Object> patternProperties = schema.getPatternProperties();
        Map<String, Object> dependentSchema = schema.getDependentSchema();
        Object propertyNames = schema.getPropertyNames();
        Object unevaluatedProperties = schema.getUnevaluatedProperties();
        Long maxProperties = schema.getMaxProperties();
        Long minProperties = schema.getMinProperties();
        Map<String, List<String>> dependentRequired = schema.getDependentRequired();
        List<String> required = schema.getRequired();

        if (Boolean.FALSE.equals(propertyNames)) {
            return EMPTY_RECORD;
        }

        if (areAllNull(additionalProperties, properties, patternProperties,
                dependentSchema, propertyNames,
                unevaluatedProperties, maxProperties, minProperties, dependentRequired, required)) {
            return UNIVERSAL_OBJECT;
        }

        if (maxProperties != null && minProperties != null && maxProperties < minProperties) {
            return NEVER;
        }

        String finalType = resolveNameConflicts(convertToPascalCase(name), this);
        allocateTypeToSchema(finalType, schema);

        List<String> objectAnnotations = new ArrayList<>();

        Map<String, GeneratorUtils.RecordField> recordFields = new HashMap<>();
        if (properties != null) {
            properties.forEach((key, value) -> {
                String fieldName = resolveNameConflicts(key, this);
                try {
                    GeneratorUtils.RecordField recordField =
                            new GeneratorUtils.RecordField(this.convert(value, fieldName, AnnotType.FIELD), false);
                    if (value instanceof Schema fieldSchema && fieldSchema.getDescription() != null) {
                        recordField.setDescription(fieldSchema.getDescription());
                    }
                    recordFields.put(key, recordField);
                    if (value instanceof Schema fieldSchema) {
                        if (fieldSchema.getDefaultKeyword() != null) {
                            recordField.setDefaultValue(
                                    this.generateStringRepresentation(fieldSchema.getDefaultKeyword()));
                        }
                        if (fieldSchema.getReadOnly() != null && fieldSchema.getReadOnly()) {
                            recordField.setReadOnly(true);
                        }
                        if (fieldSchema.getDeprecated() != null && fieldSchema.getDeprecated()) {
                            recordField.setDeprecated(true);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        String restType = getRecordRestType(finalType, additionalProperties,
                unevaluatedProperties, this);

        if (patternProperties != null && !patternProperties.isEmpty()) {
            this.addJsonDataImport();

            List<String> propertyPatternTypes = new ArrayList<>();
            Set<String> patternTypes = new HashSet<>();

            String objectTypePrefix = convertToCamelCase(finalType);

            int count = 0;

            for (Map.Entry<String, Object> entry : patternProperties.entrySet()) {
                String elementName;
                do {
                    elementName = objectTypePrefix + PATTERN_ELEMENT + (++count);
                } while (this.nodes.containsKey(elementName));

                String key = entry.getKey();
                Object value = entry.getValue();

                String typeName = elementName + "Type";
                String generatedType = resolveTypeNameForTypedesc(typeName,
                        this.convert(value, resolveNameConflicts(typeName, this)), this);

                String recordObject = String.format(PATTERN_FORMAT, PATTERN_RECORD,
                        elementName, key, generatedType);

                ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(recordObject);
                this.nodes.put(elementName, moduleNode);

                propertyPatternTypes.add(elementName);
                patternTypes.add(generatedType);
            }

            String resolvedRestType =
                    resolveTypeNameForTypedesc(REST_TYPE, restType, this);

            String restTypeAnnotation = String.format(ANNOTATION_FORMAT,
                    ANNOTATION_MODULE, ADDITIONAL_PROPS,
                    VALUE + COLON + resolvedRestType);
            objectAnnotations.add(restTypeAnnotation);

            String patternElementsArray =
                    OPEN_SQUARE_BRACKET + String.join(COMMA, propertyPatternTypes) + CLOSE_SQUARE_BRACKET;
            String patternAnnotation = String.format(ANNOTATION_FORMAT,
                    ANNOTATION_MODULE, PATTERN_PROPERTIES,
                    VALUE + COLON + patternElementsArray);
            objectAnnotations.add(patternAnnotation);

            // Handle repeating data types.
            for (String type : restType.split("\\|")) {
                patternTypes.add(type.trim());
            }
            if (patternTypes.contains(JSON)) {
                patternTypes.clear();
                patternTypes.add(JSON);
            }
            if (patternTypes.contains(NEVER) && patternTypes.size() > 1) {
                patternTypes.remove(NEVER);
            }

            restType = String.join(PIPE, patternTypes);
        }

        if (maxProperties != null || minProperties != null || propertyNames != null) {
            this.addJsonDataImport();
            List<String> objectProperties = new ArrayList<>();

            addIfNotNull(objectProperties, MIN_PROPERTIES, minProperties);
            addIfNotNull(objectProperties, MAX_PROPERTIES, maxProperties);

            if (propertyNames != null) {
                if (propertyNames instanceof Schema propertyNamesSchema) {
                    propertyNamesSchema.setType(new ArrayList<>(List.of("string")));
                    objectProperties.add(PROPERTY_NAMES + ": " +
                            this.convert(propertyNamesSchema, finalType +
                                    PROPERTY_NAMES_SUFFIX));
                } else {
                    objectProperties.add(PROPERTY_NAMES + ": " + STRING);
                }
            }

            String minMaxAnnotation = String.format(ANNOTATION_FORMAT, ANNOTATION_MODULE,
                    OBJECT_CONSTRAINTS,
                    String.join(", ", objectProperties));
            objectAnnotations.add(minMaxAnnotation);
        }

        if (restType.equals(NEVER) && required != null) {
            try {
                required.forEach((key) -> {
                    if (!recordFields.containsKey(key)) {
                        throw new IllegalStateException("Required field " + key + " is missing.");
                    }
                });
            } catch (IllegalStateException e) {
                return NEVER;
            }
        }

        // Add field names that are present in the required array and are not present in the properties' keyword.
        if (required != null) {
            String finalRestType = restType;
            required.forEach((key) -> {
                if (!recordFields.containsKey(key)) {
                    recordFields.put(key, new GeneratorUtils.RecordField(finalRestType, true));
                } else {
                    recordFields.get(key).setRequired();
                }
            });
        }

        // Add dependent schema fields that are not specified in the properties' keyword.
        if ((dependentSchema != null) && (!restType.equals(NEVER))) {
            String finalRestType = restType;
            dependentSchema.forEach((key, value) -> {
                if (!recordFields.containsKey(key)) {
                    recordFields.put(key, new GeneratorUtils.RecordField(finalRestType, false));
                }
                try {
                    String schemaName =
                            resolveNameConflicts(convertToPascalCase(key) +
                                    DEPENDENT_SCHEMA, this);
                    String dependentSchemaType = this.convert(value, schemaName);

                    if (!dependentSchemaType.equals(schemaName) && !isPrimitiveBalType(dependentSchemaType)) {
                        dependentSchemaType =
                                resolveTypeNameForTypedesc(schemaName, dependentSchemaType, this);
                    }

                    recordFields.get(key).setDependentSchema(dependentSchemaType);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        if (dependentRequired != null) {
            String finalRestType = restType;
            dependentRequired.forEach((key, value) -> {
                if (!recordFields.containsKey(key)) {
                    recordFields.put(key, new GeneratorUtils.RecordField(finalRestType, false));
                }

                value.forEach((dependentKey) -> {
                    recordFields.get(key).addDependentRequired(dependentKey);

                    if (!recordFields.containsKey(dependentKey)) {
                        recordFields.put(dependentKey, new GeneratorUtils.RecordField(finalRestType, false));
                    }
                });
            });
            processRequiredFields(recordFields);
        }

        ArrayList<String> fields = processRecordFields(recordFields, this);

        if (!restType.equals(NEVER)) {
            fields.add(handleUnion(restType) + REST + SEMI_COLON);
        }

        // Additional min/maxProperties check
        if (minProperties != null && (restType.equals(NEVER) && fields.size() < minProperties)) {
            return NEVER;
        }
        if (maxProperties != null && required != null && required.size() > maxProperties) {
            return NEVER;
        }

        String record = PUBLIC + WHITE_SPACE + TYPE + WHITE_SPACE + finalType + WHITE_SPACE +
                RECORD + OPEN_BRACES + PIPE + String.join(NEW_LINE, fields) +
                PIPE + CLOSE_BRACES + SEMI_COLON;

        if (!objectAnnotations.isEmpty()) {
            record = String.join(NEW_LINE, objectAnnotations) + NEW_LINE + record;
        }

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(record);
        this.nodes.put(finalType, moduleNode);
        return finalType;
    }

    private record BalTypes(List<Object> typeList, boolean types) {
    }

    String generateStringRepresentation(Object obj) throws InvalidDataTypeException {
        if (obj == null) {
            return NULL;
        }
        if (obj instanceof String) {
            return "\"" + obj + "\"";
        }
        if (obj instanceof Double || obj instanceof Boolean || obj instanceof Long) {
            return String.valueOf(obj);
        }
        if (obj instanceof ArrayList) {
            List<String> result = new ArrayList<>();
            for (Object element : (ArrayList<?>) obj) {
                result.add(generateStringRepresentation(element));
            }
            if (result.isEmpty()) {
                return UNIVERSAL_ARRAY;
            }
            return OPEN_SQUARE_BRACKET + String.join(COMMA, result) + CLOSE_SQUARE_BRACKET;
        }
        if (obj instanceof Map) {
            String objName = resolveConstMapping(this);
            this.nodes.put(objName, NodeParser.parseModuleMemberDeclaration(""));

            List<String> result = new ArrayList<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                result.add("\"" + key + "\"" + ":" + generateStringRepresentation(value));
            }
            String enumConst = OPEN_BRACES + String.join(COMMA, result) + CLOSE_BRACES;
            String constDefinition = String.format("public const %s = %s;", objName, enumConst);

            this.nodes.put(objName, NodeParser.parseModuleMemberDeclaration(constDefinition));
            return objName;
        }
        throw new InvalidDataTypeException("Type not supported");
    }

    static String getRecordRestType(String name, Object additionalProperties, Object unevaluatedProperties,
                                    Generator generator) throws Exception {
        if (additionalProperties != null) {
            return generator.convert(additionalProperties,
                    resolveNameConflicts(name + ADDITIONAL_PROPS, generator));
        }
        if (unevaluatedProperties != null) {
            return generator.convert(unevaluatedProperties,
                    resolveNameConflicts((name + UNEVALUATED_PROPS), generator));
        }
        return JSON;
    }

    private void allocateTypeToSchema(String name, Object schemaObject) {
        if (schemaObject instanceof Boolean schemaBoolean) {
            String booleanString = schemaBoolean.toString();
            this.nodes.put(name, NodeParser.parseModuleMemberDeclaration(booleanString));
        }
        if (schemaObject instanceof Schema schema) {
            this.nodes.put(name, NodeParser.parseModuleMemberDeclaration(""));
            this.schemaToTypeMap.put(schema, name);
        }
    }

    private static BalTypes getCommonType(List<Object> enumKeyword, Object constKeyword,
                                          List<String> type) {
        Set<Class<?>> typeList = new LinkedHashSet<>();

        if (type == null || type.isEmpty()) {
            typeList.add(Long.class);
            typeList.add(Double.class);
            typeList.add(Boolean.class);
            typeList.add(String.class);
            typeList.add(ArrayList.class);
            typeList.add(Map.class);
            typeList.add(null);
        } else {
            for (String element : type) {
                typeList.add(getIntermediateJsonClassType(element));
            }
        }

        // "integer" represented by "Long" is a subtype of "number" represented by "Double"
        if (typeList.contains(Double.class)) {
            typeList.add(Long.class);
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

        Set<Object> valueList = new LinkedHashSet<>();

        for (Object element : enumKeyword) {
            Class<?> elementClass = (element == null) ? null : element.getClass();

            // Change LinkedTreeMap class to Map
            if (elementClass != null && Map.class.isAssignableFrom(elementClass)) {
                elementClass = Map.class;
            }

            if (typeList.contains(elementClass)) {
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
            case "object" -> Map.class;
            case "null" -> null;
            default -> throw new RuntimeException("Unsupported type: " + type);
        };
    }

//    private static String getJsonType(Class<?> clazz) {
//        if (clazz == null) {
//            return "null";
//        } else if (Long.class.isAssignableFrom(clazz)) {
//            return "integer";
//        } else if (Double.class.isAssignableFrom(clazz)) {
//            return "number";
//        } else if (Boolean.class.isAssignableFrom(clazz)) {
//            return "boolean";
//        } else if (String.class.isAssignableFrom(clazz)) {
//            return "string";
//        } else if (ArrayList.class.isAssignableFrom(clazz)) {
//            return "array";
//        } else if (Map.class.isAssignableFrom(clazz)) {
//            return "object";
//        }
//        throw new RuntimeException("Unsupported class: " + clazz);
//    }

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
        if (type == Map.class) {
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

    private List<String> getImports() {
        return Collections.unmodifiableList(this.imports);
    }

    void addJsonDataImport() {
        String importDeclaration = IMPORT + WHITE_SPACE + BAL_JSON_DATA_MODULE + SEMI_COLON;
        if (!this.imports.contains(importDeclaration)) {
            this.imports.add(importDeclaration);
        }
    }
}
