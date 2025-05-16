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
import java.util.stream.Collectors;

import static io.ballerina.jsonschema.core.GeneratorUtils.ADDITIONAL_PROPS;
import static io.ballerina.jsonschema.core.GeneratorUtils.ANNOTATION_FORMAT;
import static io.ballerina.jsonschema.core.GeneratorUtils.ANNOTATION_MODULE;
import static io.ballerina.jsonschema.core.GeneratorUtils.ARRAY_CONSTRAINTS;
import static io.ballerina.jsonschema.core.GeneratorUtils.BACK_TICK;
import static io.ballerina.jsonschema.core.GeneratorUtils.BAL_JSON_DATA_MODULE;
import static io.ballerina.jsonschema.core.GeneratorUtils.BOOLEAN;
import static io.ballerina.jsonschema.core.GeneratorUtils.CLOSE_BRACES;
import static io.ballerina.jsonschema.core.GeneratorUtils.CLOSE_BRACKET;
import static io.ballerina.jsonschema.core.GeneratorUtils.CLOSE_SQUARE_BRACKET;
import static io.ballerina.jsonschema.core.GeneratorUtils.COLON;
import static io.ballerina.jsonschema.core.GeneratorUtils.COMMA;
import static io.ballerina.jsonschema.core.GeneratorUtils.CONTAINS;
import static io.ballerina.jsonschema.core.GeneratorUtils.DECIMAL;
import static io.ballerina.jsonschema.core.GeneratorUtils.DEPENDENT_SCHEMA;
import static io.ballerina.jsonschema.core.GeneratorUtils.DOUBLE_QUOTATION;
import static io.ballerina.jsonschema.core.GeneratorUtils.EMPTY_ARRAY;
import static io.ballerina.jsonschema.core.GeneratorUtils.EMPTY_RECORD;
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
import static io.ballerina.jsonschema.core.GeneratorUtils.MINIMUM;
import static io.ballerina.jsonschema.core.GeneratorUtils.MIN_CONTAINS;
import static io.ballerina.jsonschema.core.GeneratorUtils.MIN_ITEMS;
import static io.ballerina.jsonschema.core.GeneratorUtils.MIN_LENGTH;
import static io.ballerina.jsonschema.core.GeneratorUtils.MIN_PROPERTIES;
import static io.ballerina.jsonschema.core.GeneratorUtils.MULTIPLE_OF;
import static io.ballerina.jsonschema.core.GeneratorUtils.NAME_REST_ITEM;
import static io.ballerina.jsonschema.core.GeneratorUtils.NEVER;
import static io.ballerina.jsonschema.core.GeneratorUtils.NEW_LINE;
import static io.ballerina.jsonschema.core.GeneratorUtils.NULL;
import static io.ballerina.jsonschema.core.GeneratorUtils.NUMBER;
import static io.ballerina.jsonschema.core.GeneratorUtils.NUMBER_CONSTRAINTS;
import static io.ballerina.jsonschema.core.GeneratorUtils.OBJECT_CONSTRAINTS;
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
import static io.ballerina.jsonschema.core.GeneratorUtils.RECORD;
import static io.ballerina.jsonschema.core.GeneratorUtils.REGEX_PREFIX;
import static io.ballerina.jsonschema.core.GeneratorUtils.REST;
import static io.ballerina.jsonschema.core.GeneratorUtils.REST_TYPE;
import static io.ballerina.jsonschema.core.GeneratorUtils.SEMI_COLON;
import static io.ballerina.jsonschema.core.GeneratorUtils.STRING;
import static io.ballerina.jsonschema.core.GeneratorUtils.STRING_CONSTRAINTS;
import static io.ballerina.jsonschema.core.GeneratorUtils.STRING_FORMATS;
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
import static io.ballerina.jsonschema.core.GeneratorUtils.ZERO;
import static io.ballerina.jsonschema.core.GeneratorUtils.addIfNotNull;
import static io.ballerina.jsonschema.core.GeneratorUtils.convertToCamelCase;
import static io.ballerina.jsonschema.core.GeneratorUtils.convertToPascalCase;
import static io.ballerina.jsonschema.core.GeneratorUtils.getFormattedAnnotation;
import static io.ballerina.jsonschema.core.GeneratorUtils.handleUnion;
import static io.ballerina.jsonschema.core.GeneratorUtils.isCustomTypeNotRequired;
import static io.ballerina.jsonschema.core.GeneratorUtils.isInvalidNumberLimit;
import static io.ballerina.jsonschema.core.GeneratorUtils.isPrimitiveBalType;
import static io.ballerina.jsonschema.core.GeneratorUtils.processRecordFields;
import static io.ballerina.jsonschema.core.GeneratorUtils.processRequiredFields;
import static io.ballerina.jsonschema.core.GeneratorUtils.resolveConstMapping;
import static io.ballerina.jsonschema.core.GeneratorUtils.resolveNameConflicts;
import static io.ballerina.jsonschema.core.GeneratorUtils.resolveTypeNameForTypedesc;

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

    private int constCounter = 0;

    int getNextConstIndex() {
        return ++this.constCounter;
    }

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

    String convert(Object schemaObject, String name) throws Exception {
        // JSON Schema allows a schema to be a boolean: `true` allows any value, `false` allows none.
        // It is handled here before processing object-based schemas.
        if (schemaObject instanceof Boolean boolValue) {
            return boolValue ? JSON : NEVER;
        }

        Schema schema = (Schema) schemaObject;

        BalTypes balTypes = getCommonType(schema.getEnumKeyword(), schema.getConstKeyword(), schema.getType());
        List<Object> schemaType = balTypes.typeList();

        if (schemaType.isEmpty()) {
            return NEVER;
        }

        if (balTypes.types()) {
            if (schemaType.contains(Double.class)) {
                schemaType.remove(Long.class);
            }
            if (schemaType.size() == 1) {
                return createType(name, schema, schemaType.getFirst());
            }

            Set<String> unionTypes = new LinkedHashSet<>();

            for (Object element : schemaType) {
                String subtypeName = name + getBallerinaType(element);
                unionTypes.add(createType(subtypeName, schema, element));
            }
            if (unionTypes.containsAll(
                    Set.of(NUMBER, BOOLEAN, STRING, UNIVERSAL_ARRAY, UNIVERSAL_OBJECT, NULL))) {
                return JSON;
            }
            if (unionTypes.contains(NUMBER)) {
                unionTypes.remove(NUMBER);
                unionTypes.add(INTEGER);
                unionTypes.add(FLOAT);
                unionTypes.add(DECIMAL);
            }
            return String.join(PIPE, unionTypes);
        }

        //TODO: Validate constraints on enums

        return schemaType.stream()
                .map(element -> {
                    try {
                        return generateStringRepresentation(element);
                    } catch (InvalidDataTypeException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.joining(PIPE));
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

        if (isCustomTypeNotRequired(minimum, exclusiveMinimum, maximum, exclusiveMaximum, multipleOf)) {
            return INTEGER;
        }

        if (isInvalidNumberLimit(minimum, exclusiveMinimum, maximum, exclusiveMaximum)) {
            return NEVER;
        }

        this.addJsonDataImport();
        String type = resolveNameConflicts(convertToPascalCase(name), this);

        List<String> annotationParts = new ArrayList<>();

        addIfNotNull(annotationParts, MINIMUM, minimum);
        addIfNotNull(annotationParts, EXCLUSIVE_MINIMUM, exclusiveMinimum);
        addIfNotNull(annotationParts, MAXIMUM, maximum);
        addIfNotNull(annotationParts, EXCLUSIVE_MAXIMUM, exclusiveMaximum);
        addIfNotNull(annotationParts, MULTIPLE_OF, multipleOf);

        String formattedAnnotation = getFormattedAnnotation(
                annotationParts, NUMBER_CONSTRAINTS, type, INTEGER);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        this.nodes.put(type, moduleNode);

        return type;
    }

    private String createNumber(String name, Schema schema) {
        Double minimum = schema.getMinimum();
        Double exclusiveMinimum = schema.getExclusiveMinimum();
        Double maximum = schema.getMaximum();
        Double exclusiveMaximum = schema.getExclusiveMaximum();
        Double multipleOf = schema.getMultipleOf();

        if (isCustomTypeNotRequired(minimum, exclusiveMinimum, maximum, exclusiveMaximum, multipleOf)) {
            return NUMBER;
        }

        if (isInvalidNumberLimit(minimum, exclusiveMinimum, maximum, exclusiveMaximum)) {
            return NEVER;
        }

        this.addJsonDataImport();
        String type = resolveNameConflicts(convertToPascalCase(name), this);

        List<String> annotationParts = new ArrayList<>();

        addIfNotNull(annotationParts, MINIMUM, minimum);
        addIfNotNull(annotationParts, EXCLUSIVE_MINIMUM, exclusiveMinimum);
        addIfNotNull(annotationParts, MAXIMUM, maximum);
        addIfNotNull(annotationParts, EXCLUSIVE_MAXIMUM, exclusiveMaximum);
        addIfNotNull(annotationParts, MULTIPLE_OF, multipleOf);

        String formattedAnnotation = getFormattedAnnotation(
                annotationParts, NUMBER_CONSTRAINTS, type, NUMBER);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        this.nodes.put(type, moduleNode);

        return type;
    }

    private String createString(String name, Schema schema) {
        String format = schema.getFormat();
        Long minLength = schema.getMinLength();
        Long maxLength = schema.getMaxLength();
        String pattern = schema.getPattern();

        if (isCustomTypeNotRequired(format, minLength, maxLength, pattern)) {
            return STRING;
        }

        this.addJsonDataImport();
        String type = resolveNameConflicts(convertToPascalCase(name), this);

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

        String formattedAnnotation = getFormattedAnnotation(annotationParts,
                STRING_CONSTRAINTS, type, STRING);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        this.nodes.put(type, moduleNode);

        return type;
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

        String type = resolveNameConflicts(convertToPascalCase(name), this);
        this.nodes.put(type, NodeParser.parseModuleMemberDeclaration(""));

        ArrayList<String> arrayItems = new ArrayList<>();

        if (prefixItems != null) {
            for (int i = 0; i < prefixItems.size(); i++) {
                Object item = prefixItems.get(i);
                arrayItems.add(this.convert(item, type + ITEM_SUFFIX + i));
            }
        }

        long startPosition = minItems == null ? 0L : minItems;
        long endPosition = maxItems == null ? Long.MAX_VALUE : maxItems;
        long annotationLimit = startPosition + MAX_UNION_TUPLE_TYPES;

        String restItem = JSON;
        if (items != null) {
            restItem = this.convert(items, type + NAME_REST_ITEM);
            if (restItem.contains(PIPE)) {
                restItem = OPEN_BRACKET + restItem + CLOSE_BRACKET;
            }
        }

        //TODO: Create sub-schemas before all the early return types for schema reference implementation.

        if ((endPosition < startPosition) || (restItem.equals(NEVER) && arrayItems.size() < startPosition)) {
            this.nodes.remove(type);
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
            this.nodes.remove(type);
            return String.join(PIPE, tupleList);
        }

        this.addJsonDataImport();
        List<String> annotationParts = new ArrayList<>();

        addIfNotNull(annotationParts, MIN_ITEMS, minItems);
        addIfNotNull(annotationParts, MAX_ITEMS, maxItems);
        addIfNotNull(annotationParts, UNIQUE_ITEMS, uniqueItems);

        if (contains != null) {
            String containsRecordName = resolveNameConflicts(type +
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
            String customTypeName = type + UNEVALUATED_ITEMS_SUFFIX;
            String typeName = this.convert(unevaluatedItems, customTypeName);
            annotationParts.add(UNEVALUATED_ITEMS + COLON + WHITE_SPACE +
                    resolveTypeNameForTypedesc(customTypeName, typeName, this));
        }

        String formattedAnnotation = getFormattedAnnotation(annotationParts,
                ARRAY_CONSTRAINTS, type,
                String.join(PIPE, tupleList));

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        this.nodes.put(type, moduleNode);

        return type;
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

        if (isCustomTypeNotRequired(additionalProperties, properties, patternProperties,
                dependentSchema, propertyNames,
                unevaluatedProperties, maxProperties, minProperties, dependentRequired, required)) {
            return UNIVERSAL_OBJECT;
        }

        if (maxProperties != null && minProperties != null && maxProperties < minProperties) {
            return NEVER;
        }

        String type = resolveNameConflicts(convertToPascalCase(name), this);
        this.nodes.put(type, NodeParser.parseModuleMemberDeclaration(""));

        List<String> objectAnnotations = new ArrayList<>();

        Map<String, GeneratorUtils.RecordField> recordFields = new HashMap<>();
        if (properties != null) {
            properties.forEach((key, value) -> {
                String fieldName = resolveNameConflicts(key, this);
                try {
                    GeneratorUtils.RecordField recordField =
                            new GeneratorUtils.RecordField(this.convert(value, fieldName), false);
                    recordFields.put(key, recordField);
                    if (value instanceof Schema fieldSchema && fieldSchema.getDefaultKeyword() != null) {
                        recordField.setDefaultValue(this.generateStringRepresentation(fieldSchema.getDefaultKeyword()));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        String restType = getRecordRestType(type, additionalProperties,
                unevaluatedProperties, this);

        if (patternProperties != null && !patternProperties.isEmpty()) {
            this.addJsonDataImport();

            List<String> propertyPatternTypes = new ArrayList<>();
            Set<String> patternTypes = new HashSet<>();

            String objectTypePrefix = convertToCamelCase(type);

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
            for (String repeatingType : restType.split("\\|")) {
                patternTypes.add(repeatingType.trim());
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
                            this.convert(propertyNamesSchema, type +
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

        String record = PUBLIC + WHITE_SPACE + TYPE + WHITE_SPACE + type + WHITE_SPACE +
                RECORD + OPEN_BRACES + PIPE + String.join(NEW_LINE, fields) +
                PIPE + CLOSE_BRACES + SEMI_COLON;

        if (!objectAnnotations.isEmpty()) {
            record = String.join(NEW_LINE, objectAnnotations) + NEW_LINE + record;
        }

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(record);
        this.nodes.put(type, moduleNode);
        return type;
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
