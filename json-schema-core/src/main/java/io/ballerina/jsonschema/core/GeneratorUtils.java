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

import com.google.gson.internal.LinkedTreeMap;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Code Generator Utils for the Generator class.
 *
 * @since 0.1.0
 */
public class GeneratorUtils {
    public static final String IMPORT = "import";
    public static final String PUBLIC = "public";
    public static final String TYPE = "type";
    public static final String WHITE_SPACE = " ";
    public static final String AT = "@";
    public static final String OPEN_BRACKET = "(";
    public static final String CLOSE_BRACKET = ")";
    public static final String OPEN_BRACES = "{";
    public static final String CLOSE_BRACES = "}";
    public static final String COLON = ":";
    public static final String SEMI_COLON = ";";
    public static final String COMMA = ",";
    public static final String NEW_LINE = "\n";
    public static final String BACK_TICK = "`";
    public static final String REGEX_PREFIX = "re";
    public static final String PIPE = "|";
    public static final String REST = "...";
    public static final String OPEN_SQUARE_BRACKET = "[";
    public static final String CLOSE_SQUARE_BRACKET = "]";
    public static final String ZERO = "0";
    public static final String RECORD = "record";
    public static final String QUESTION_MARK = "?";
    public static final String EQUAL = "=";
    public static final String DOUBLE_QUOTATION = "\"";
    public static final String UNDERSCORE = "_";
    public static final String TAB = "\t";
    public static final String CONST = "const";

    public static final String INTEGER = "int";
    public static final String STRING = "string";
    public static final String FLOAT = "float";
    public static final String DECIMAL = "decimal";
    public static final String NUMBER = "int|float|decimal";
    public static final String BOOLEAN = "boolean";
    public static final String NEVER = "never";
    public static final String NULL = "()";
    public static final String JSON = "json";
    public static final String UNIVERSAL_ARRAY = "[json...]";
    public static final String EMPTY_ARRAY = "json[0]";
    public static final String UNIVERSAL_OBJECT = "record{|json...;|}";
    public static final String EMPTY_RECORD = "record{||}";

    public static final String BAL_JSON_DATA_MODULE = "ballerina/data.jsondata";

    public static final String ANNOTATION_MODULE = "jsondata";
    public static final String OBJECT_VALIDATION = "ObjectValidation";
    public static final String NUMBER_VALIDATION = "NumberValidation";
    public static final String STRING_VALIDATION = "StringValidation";
    public static final String ARRAY_VALIDATION = "ArrayValidation";
    public static final String PATTERN_RECORD = ANNOTATION_MODULE + COLON + "PatternPropertiesElement";
    public static final String VALUE = "value";

    public static final String ANNOTATION_FORMAT = "@%s:%s{%n\t%s%n}";
    public static final String TYPE_FORMAT = "public type %s %s;";
    public static final String FIELD_ANNOTATION_FORMAT = "@%s:%s{%n\tvalue: %s%n}";
    public static final String PATTERN_FORMAT = "%s %s = {%n\tpattern: re `%s`,%n\tvalue: %s%n};";

    public static final String MINIMUM = "minimum";
    public static final String EXCLUSIVE_MINIMUM = "exclusiveMinimum";
    public static final String MAXIMUM = "maximum";
    public static final String EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
    public static final String MULTIPLE_OF = "multipleOf";

    public static final String FORMAT = "format";
    public static final String MIN_LENGTH = "minLength";
    public static final String MAX_LENGTH = "maxLength";
    public static final String PATTERN = "pattern";

    public static final String MIN_ITEMS = "minItems";
    public static final String MAX_ITEMS = "maxItems";
    public static final String UNIQUE_ITEMS = "uniqueItems";
    public static final String CONTAINS = "contains";
    public static final String MIN_CONTAINS = "minContains";
    public static final String MAX_CONTAINS = "maxContains";
    public static final String UNEVALUATED_ITEMS = "unevaluatedItems";

    public static final String MAX_PROPERTIES = "maxProperties";
    public static final String MIN_PROPERTIES = "minProperties";
    public static final String PROPERTY_NAMES = "propertyNames";

    private static final String INVALID_CHARS_PATTERN = ".*[!@$%^&*()_\\-|/\\\\\\s\\d].*";
    private static final String DIGIT_PATTERN = ".*\\d.*";
    private static final String STARTS_WITH_DIGIT_PATTERN = "^\\d.*";
    private static final String SLASH_PATTERN = "[/\\\\]";
    private static final String WHITESPACE_PATTERN = "\\s";
    private static final String SPECIAL_CHARS_PATTERN = "[!@$%^&*()_\\-|]";

    private static final String ITEM_SUFFIX = "Item";
    private static final String NAME_REST_ITEM = "RestItem";
    private static final String REST_TYPE = "RestType";
    private static final String ADDITIONAL_PROPS = "AdditionalProperties";
    public static final String DEPENDENT_SCHEMA = "DependentSchema";
    public static final String DEPENDENT_REQUIRED = "DependentRequired";
    public static final String UNEVALUATED_PROPS = "UnevaluatedProperties";
    public static final String PATTERN_ELEMENT = "PatternElement";
    public static final String PATTERN_PROPERTIES = "PatternProperties";
    public static final String UNEVALUATED_ITEMS_SUFFIX = "UnevaluatedItems";
    public static final String PROPERTY_NAMES_SUFFIX = "PropertyNames";

    private static final ArrayList<String> STRING_FORMATS = new ArrayList<>(
            Arrays.asList("date", "time", "date-time", "duration", "regex", "email", "idn-email", "hostname",
                    "idn-hostname", "ipv4", "ipv6", "json-pointer", "relative-json-pointer", "uri",
                    "uri-reference", "uri-template", "iri", "iri-reference", "uuid")
    );
    private static final ArrayList<String> BAL_PRIMITIVE_TYPES = new ArrayList<>(
            Arrays.asList(INTEGER, STRING, BOOLEAN, NEVER, NULL, JSON)
    );

    // The Union concatenated no. of Tuples will be denoted by annotations beyond this limit
    private static final int ARRAY_ANNOTATION_SIZE_LIMIT = 5;
    // The Tuple size will be denoted by annotations beyond this limit
    private static final int ARRAY_ANNOTATION_MIN_LIMIT = 10;

    private static class RecordField {
        private String type;
        private boolean required;

        private List<String> dependentRequired;
        private String dependentSchema;

        public RecordField(String type, boolean required) {
            this.type = type;
            this.required = required;
            this.dependentRequired = new ArrayList<>();
            this.dependentSchema = null;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public String getDependentSchema() {
            return dependentSchema;
        }

        public void setDependentSchema(String dependentSchema) {
            this.dependentSchema = dependentSchema;
        }

        public List<String> getDependentRequired() {
            return dependentRequired;
        }

        public void setDependentRequired(List<String> dependentRequired) {
            this.dependentRequired = dependentRequired;
        }

        public void addDependentRequired(String dependentRequired) {
            this.dependentRequired.add(dependentRequired);
        }
    }

    public static String createType(String name, Schema schema, Object type, Generator generator) throws Exception {
        if (type == null) {
            return NULL;
        }
        if (type == Boolean.class) {
            return BOOLEAN;
        }
        if (type == Long.class) {
            return createInteger(name, schema.getMinimum(), schema.getExclusiveMinimum(), schema.getMaximum(),
                    schema.getExclusiveMaximum(), schema.getMultipleOf(), generator);
        }
        if (type == Double.class) {
            return createNumber(name, schema.getMinimum(), schema.getExclusiveMinimum(), schema.getMaximum(),
                    schema.getExclusiveMaximum(), schema.getMultipleOf(), generator);
        }
        if (type == String.class) {
            return createString(name, schema.getFormat(), schema.getMinLength(), schema.getMaxLength(),
                    schema.getPattern(), generator);
        }
        if (type == ArrayList.class) {
            return createArray(name, schema.getPrefixItems(), schema.getItems(), schema.getContains(),
                    schema.getMinItems(), schema.getMaxItems(), schema.getUniqueItems(), schema.getMaxContains(),
                    schema.getMinContains(), schema.getUnevaluatedItems(), generator);
        }
        if (type == LinkedTreeMap.class) {
            return createObject(name, schema.getAdditionalProperties(), schema.getProperties(),
                    schema.getPatternProperties(), schema.getDependentSchema(), schema.getPropertyNames(),
                    schema.getUnevaluatedProperties(), schema.getMaxProperties(), schema.getMinProperties(),
                    schema.getDependentRequired(), schema.getRequired(), generator);
        }
        throw new RuntimeException("Type currently not supported");
    }

    public static String createInteger(String name, Double minimum, Double exclusiveMinimum, Double maximum,
                                       Double exclusiveMaximum, Double multipleOf, Generator generator) {
        if (minimum == null && maximum == null && exclusiveMaximum == null &&
                exclusiveMinimum == null && multipleOf == null) {
            return INTEGER;
        }

        if (invalidNumberLimits(minimum, exclusiveMinimum, maximum, exclusiveMaximum)) {
            return NEVER;
        }

        generator.addImports(BAL_JSON_DATA_MODULE);
        String finalType = resolveNameConflicts(convertToPascalCase(name), generator);

        List<String> annotationParts = new ArrayList<>();

        addIfNotNull(annotationParts, MINIMUM, minimum);
        addIfNotNull(annotationParts, EXCLUSIVE_MINIMUM, exclusiveMinimum);
        addIfNotNull(annotationParts, MAXIMUM, maximum);
        addIfNotNull(annotationParts, EXCLUSIVE_MAXIMUM, exclusiveMaximum);
        addIfNotNull(annotationParts, MULTIPLE_OF, multipleOf);

        String formattedAnnotation = getFormattedAnnotation(annotationParts, NUMBER_VALIDATION, finalType, INTEGER);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        generator.nodes.put(finalType, moduleNode);

        return finalType;
    }

    public static String createNumber(String name, Double minimum, Double exclusiveMinimum, Double maximum,
                                      Double exclusiveMaximum, Double multipleOf, Generator generator) {
        if (minimum == null && maximum == null && exclusiveMaximum == null &&
                exclusiveMinimum == null && multipleOf == null) {
            return NUMBER;
        }

        if (invalidNumberLimits(minimum, exclusiveMinimum, maximum, exclusiveMaximum)) {
            return NEVER;
        }

        generator.addImports(BAL_JSON_DATA_MODULE);
        String finalType = resolveNameConflicts(convertToPascalCase(name), generator);

        List<String> annotationParts = new ArrayList<>();

        addIfNotNull(annotationParts, MINIMUM, minimum);
        addIfNotNull(annotationParts, EXCLUSIVE_MINIMUM, exclusiveMinimum);
        addIfNotNull(annotationParts, MAXIMUM, maximum);
        addIfNotNull(annotationParts, EXCLUSIVE_MAXIMUM, exclusiveMaximum);
        addIfNotNull(annotationParts, MULTIPLE_OF, multipleOf);

        String formattedAnnotation = getFormattedAnnotation(annotationParts, NUMBER_VALIDATION, finalType, NUMBER);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        generator.nodes.put(finalType, moduleNode);

        return finalType;
    }

    public static String createString(String name, String format, Long minLength, Long maxLength,
                                      String pattern, Generator generator) {
        if (format == null && minLength == null && maxLength == null && pattern == null) {
            return STRING;
        }

        generator.addImports(BAL_JSON_DATA_MODULE);
        String finalType = resolveNameConflicts(convertToPascalCase(name), generator);

        List<String> annotationParts = new ArrayList<>();

        if (format != null) {
            if (!STRING_FORMATS.contains(format)) {
                throw new IllegalArgumentException("Invalid format: " + format);
            }
            annotationParts.add(FORMAT + COLON + DOUBLE_QUOTATION + format + DOUBLE_QUOTATION);
        }

        addIfNotNull(annotationParts, MIN_LENGTH, minLength);
        addIfNotNull(annotationParts, MAX_LENGTH, maxLength);

        if (pattern != null) {
            annotationParts.add(PATTERN + COLON + REGEX_PREFIX + BACK_TICK + pattern + BACK_TICK);
        }

        String formattedAnnotation = getFormattedAnnotation(annotationParts, STRING_VALIDATION, finalType, STRING);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        generator.nodes.put(finalType, moduleNode);

        return finalType;
    }

    public static String createArray(String name, List<Object> prefixItems, Object items, Object contains,
                                     Long minItems, Long maxItems, Boolean uniqueItems, Long maxContains,
                                     Long minContains, Object unevaluatedItems, Generator generator) throws Exception {
        String finalType = resolveNameConflicts(convertToPascalCase(name), generator);
        generator.nodes.put(finalType, NodeParser.parseModuleMemberDeclaration(""));

        ArrayList<String> arrayItems = new ArrayList<>();
        ArrayList<String> tupleList = new ArrayList<>();

        if (prefixItems != null) {
            for (int i = 0; i < prefixItems.size(); i++) {
                Object item = prefixItems.get(i);
                arrayItems.add(generator.convert(item, finalType + ITEM_SUFFIX + i));
            }
        }

        long startPosition = (minItems == null) ? 0L : minItems;
        long endPosition = (maxItems == null) ? Long.MAX_VALUE : maxItems;
        long annotationLimit = startPosition + ARRAY_ANNOTATION_SIZE_LIMIT;

        String restItem = "";
        if (items != null) {
            restItem = generator.convert(items, finalType + NAME_REST_ITEM);
            if (restItem.contains(PIPE)) {
                restItem = OPEN_BRACKET + restItem + CLOSE_BRACKET;
            }
        } else {
            restItem = JSON;
        }

        // TODO: Create sub-schemas before all of these early return types for schema reference implementation.

        if ((endPosition < startPosition) || (restItem.equals(NEVER) && arrayItems.size() < startPosition)) {
            generator.nodes.remove(finalType);
            return NEVER;
        }

        // Determine the rest item type
        if (!restItem.equals(NEVER)) {
            if (arrayItems.size() < startPosition) {
                if (startPosition < ARRAY_ANNOTATION_MIN_LIMIT) {
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
            generator.nodes.remove(finalType);
            return String.join(PIPE, tupleList);
        }

        generator.addImports(BAL_JSON_DATA_MODULE);
        List<String> annotationParts = new ArrayList<>();

        addIfNotNull(annotationParts, MIN_ITEMS, minItems);
        addIfNotNull(annotationParts, MAX_ITEMS, maxItems);
        addIfNotNull(annotationParts, UNIQUE_ITEMS, uniqueItems);

        if (contains != null) {
            String containsRecordName = resolveNameConflicts(finalType +
                    convertToPascalCase(CONTAINS), generator);
            String newType = generator.convert(contains, containsRecordName);

            if (newType.contains(PIPE)) {
                // Ballerina typedesc doesn't allow union types. Hence, we need to create a new type definition
                String typeDef = String.format(TYPE_FORMAT, containsRecordName, newType);
                ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(typeDef);
                generator.nodes.put(containsRecordName, moduleNode);
                newType = containsRecordName;
            }

            List<String> containsAnnotationParts = new ArrayList<>();

            containsAnnotationParts.add(CONTAINS + COLON + WHITE_SPACE + newType);
            if (minContains == null) {
                containsAnnotationParts.add(MIN_CONTAINS + COLON + WHITE_SPACE + ZERO);
            } else {
                containsAnnotationParts.add(MIN_CONTAINS + COLON + WHITE_SPACE + minContains);
            }

            addIfNotNull(containsAnnotationParts, MAX_CONTAINS, maxContains);

            String combined = String.join(COMMA, containsAnnotationParts);
            annotationParts.add(CONTAINS + COLON + WHITE_SPACE + OPEN_BRACES + combined + CLOSE_BRACES);
        }

        if (unevaluatedItems != null) {
            String customTypeName = finalType + UNEVALUATED_ITEMS_SUFFIX;
            String typeName = generator.convert(unevaluatedItems, customTypeName);
            annotationParts.add(UNEVALUATED_ITEMS + COLON + WHITE_SPACE +
                    resolveTypeNameForTypedesc(customTypeName, typeName, generator));
        }

        String formattedAnnotation = getFormattedAnnotation(annotationParts, ARRAY_VALIDATION, finalType,
                String.join(PIPE, tupleList));

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        generator.nodes.put(finalType, moduleNode);

        return finalType;
    }

    public static String createObject(String name, Object additionalProperties, Map<String, Object> properties,
                                      Map<String, Object> patternProperties, Map<String, Object> dependentSchema,
                                      Object propertyNames, Object unevaluatedProperties, Long maxProperties,
                                      Long minProperties, Map<String, List<String>> dependentRequired,
                                      List<String> required, Generator generator) throws Exception {
        if (Boolean.FALSE.equals(propertyNames)) {
            return EMPTY_RECORD;
        }

        if (customTypeNotRequired(additionalProperties, properties, patternProperties, dependentSchema, propertyNames,
                unevaluatedProperties, maxProperties, minProperties, dependentRequired, required)) {
            return UNIVERSAL_OBJECT;
        }

        if (maxProperties != null && minProperties != null && maxProperties < minProperties) {
            return NEVER;
        }

        String finalType = resolveNameConflicts(convertToPascalCase(name), generator);
        generator.nodes.put(finalType, NodeParser.parseModuleMemberDeclaration(""));

        List<String> objectAnnotations = new ArrayList<>();

        Map<String, RecordField> recordFields = new HashMap<>();
        if (properties != null) {
            properties.forEach((key, value) -> {
                String fieldName = resolveNameConflicts(key, generator);
                try {
                    recordFields.put(key, new RecordField(generator.convert(value, fieldName), false));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        String restType = getRecordRestType(finalType, additionalProperties, unevaluatedProperties, generator);

        if (patternProperties != null && !patternProperties.isEmpty()) {
            generator.addImports(BAL_JSON_DATA_MODULE);

            List<String> propertyPatternTypes = new ArrayList<>();
            Set<String> patternTypes = new HashSet<>();

            String objectTypePrefix = convertToCamelCase(finalType);

            for (Map.Entry<String, Object> entry : patternProperties.entrySet()) {
                String elementName = resolveNameConflictsWithSuffix(objectTypePrefix + PATTERN_ELEMENT, generator);
                String elementValue = resolveNameConflicts(elementName + "Type", generator);

                String key = entry.getKey();
                Object value = entry.getValue();

                String generatedType = generator.convert(value, elementValue);

                String recordObject = String.format(PATTERN_FORMAT, PATTERN_RECORD, elementName, key, generatedType);

                ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(recordObject);
                generator.nodes.put(elementName, moduleNode);

                propertyPatternTypes.add(elementName);
                patternTypes.add(generatedType);
            }

            String resolvedRestType = resolveTypeNameForTypedesc(REST_TYPE, restType, generator);

            String restTypeAnnotation = String.format(ANNOTATION_FORMAT, ANNOTATION_MODULE, ADDITIONAL_PROPS,
                    VALUE + COLON + resolvedRestType);
            objectAnnotations.add(restTypeAnnotation);

            String patternElementsArray =
                    OPEN_SQUARE_BRACKET + String.join(COMMA, propertyPatternTypes) + CLOSE_SQUARE_BRACKET;
            String patternAnnotation = String.format(ANNOTATION_FORMAT, ANNOTATION_MODULE, PATTERN_PROPERTIES,
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
            List<String> objectProperties = new ArrayList<>();

            addIfNotNull(objectProperties, MIN_PROPERTIES, minProperties);
            addIfNotNull(objectProperties, MAX_PROPERTIES, maxProperties);

            if (propertyNames != null) {
                if (propertyNames instanceof Schema propertyNamesSchema) {
                    propertyNamesSchema.setType(new ArrayList<>(List.of("string")));
                    objectProperties.add(PROPERTY_NAMES + ": " +
                            generator.convert(propertyNamesSchema, finalType + PROPERTY_NAMES_SUFFIX));
                } else if (Boolean.FALSE.equals(propertyNames)) {
                    //TODO: Remove created unwanted type objects
                    generator.nodes.remove(finalType);
                    return NEVER; // TODO: Test this
                } else {
                    objectProperties.add(PROPERTY_NAMES + ": " + STRING);
                }
            }

            String minMaxAnnotation = String.format(ANNOTATION_FORMAT, ANNOTATION_MODULE, OBJECT_VALIDATION,
                    String.join(", ", objectProperties));
            objectAnnotations.add(minMaxAnnotation);
        }

        if (restType.equals(NEVER) && required != null) {
            try {
                required.forEach((key) -> {
                    if (!recordFields.containsKey(key)) {
                        throw new IllegalStateException("Returning NEVER");
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
                    recordFields.put(key, new RecordField(finalRestType, true));
                } else {
                    recordFields.get(key).setRequired(true);
                }
            });
        }

        // Add dependent schema fields that are not specified in the properties' keyword.
        if ((dependentSchema != null) && (!restType.equals(NEVER))) {
            String finalRestType = restType;
            dependentSchema.forEach((key, value) -> {
                if (!recordFields.containsKey(key)) {
                    recordFields.put(key, new RecordField(finalRestType, false));
                }
                try {
                    String schemaName =
                            resolveNameConflicts(convertToPascalCase(key) + DEPENDENT_SCHEMA, generator);
                    String dependentSchemaType = generator.convert(value, schemaName);

                    if (!dependentSchemaType.equals(schemaName) && !isTypeBalPrimitive(dependentSchemaType)) {
                        dependentSchemaType = resolveTypeNameForTypedesc(schemaName, dependentSchemaType, generator);
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
                    recordFields.put(key, new RecordField(finalRestType, false));
                }

                value.forEach((dependentKey) -> {
                    recordFields.get(key).addDependentRequired(dependentKey);

                    if (!recordFields.containsKey(dependentKey)) {
                        recordFields.put(dependentKey, new RecordField(finalRestType, false));
                    }
                });
            });
            processRequiredFields(recordFields);
        }

        ArrayList<String> fields = processRecordFields(recordFields, generator);

        if (!restType.equals(NEVER)) {
            fields.add(handleUnion(restType) + REST + SEMI_COLON);
        }

        //TODO: Add a min max property validation with a rest type check.

        String record = PUBLIC + WHITE_SPACE + TYPE + WHITE_SPACE + finalType + WHITE_SPACE +
                RECORD + OPEN_BRACES + PIPE + String.join(NEW_LINE, fields) + PIPE + CLOSE_BRACES + SEMI_COLON;

        if (!objectAnnotations.isEmpty()) {
            record = String.join(NEW_LINE, objectAnnotations) + NEW_LINE + record;
        }

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(record);
        generator.nodes.put(finalType, moduleNode);
        return finalType;
    }

    private static void processRequiredFields(Map<String, RecordField> recordFields) {
        boolean changeFlag = true;
        while (changeFlag) {
            changeFlag = false;
            for (Map.Entry<String, RecordField> entry : recordFields.entrySet()) {
                String key = entry.getKey();
                RecordField keyRecord = recordFields.get(key);
                RecordField value = entry.getValue();
                if (value.getDependentRequired() != null && keyRecord.isRequired()) {
                    // If there are dependentRequired items
                    for (String dependentRequired : value.getDependentRequired()) {
                        // Iterate through the dependentRequired items
                        if (keyRecord.isRequired() && !recordFields.get(dependentRequired).isRequired()) {
                            recordFields.get(dependentRequired).setRequired(true);
                            changeFlag = true;
                        }
                    }
                }
            }
        }
    }

    // Create a new type with the union of typedesc and return the new type name.
    private static String resolveTypeNameForTypedesc(String name, String typeName, Generator generator) {
        if (!typeName.contains(PIPE)) {
            return typeName;
        }
        String newType = resolveNameConflicts(name, generator);
        String typeDeclaration = PUBLIC + WHITE_SPACE + TYPE + WHITE_SPACE + newType + WHITE_SPACE + typeName +
                SEMI_COLON;
        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(typeDeclaration);
        generator.nodes.put(newType, moduleNode);
        return newType;
    }

    private static ArrayList<String> processRecordFields(Map<String, RecordField> recordFields, Generator generator) {
        ArrayList<String> recordBody = new ArrayList<>();

        for (Map.Entry<String, RecordField> entry : recordFields.entrySet()) {
            String key = entry.getKey();
            RecordField value = entry.getValue();

            ArrayList<String> fieldAnnotation = new ArrayList<>();

            if (value.getDependentSchema() != null) {
                generator.addImports(BAL_JSON_DATA_MODULE);
                String dependentSchema = String.format(FIELD_ANNOTATION_FORMAT, ANNOTATION_MODULE, DEPENDENT_SCHEMA,
                        value.getDependentSchema());
                generator.addImports(BAL_JSON_DATA_MODULE);
                fieldAnnotation.add(dependentSchema);
            }

            if (value.getDependentRequired() != null && !value.getDependentRequired().isEmpty()) {
                generator.addImports(BAL_JSON_DATA_MODULE);
                String dependentArray = value.getDependentRequired().stream()
                        .map(name -> DOUBLE_QUOTATION + name + DOUBLE_QUOTATION)
                        .collect(Collectors.joining(", ", "[", "]"));
                String dependentRequired = String.format(FIELD_ANNOTATION_FORMAT, ANNOTATION_MODULE,
                        DEPENDENT_REQUIRED, dependentArray);
                fieldAnnotation.add(dependentRequired);
            }

            if (value.isRequired()) {
                fieldAnnotation.add(value.getType() + WHITE_SPACE + key);
            } else {
                fieldAnnotation.add(value.getType() + WHITE_SPACE + key + QUESTION_MARK);
            }

            recordBody.add(String.join(NEW_LINE, fieldAnnotation) + SEMI_COLON);
        }

        return recordBody;
    }

    private static String getRecordRestType(String name, Object additionalProperties, Object unevaluatedProperties,
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

    // Handle union values by enclosing them in parentheses.
    private static String handleUnion(String type) {
        if (type.contains(PIPE) && !type.startsWith(OPEN_BRACKET)) {
            return OPEN_BRACKET + type + CLOSE_BRACKET;
        }
        return type;
    }

    private static boolean isTypeBalPrimitive(String type) {
        return BAL_PRIMITIVE_TYPES.contains(type);
    }

    private static void addIfNotNull(List<String> list, String key, Object value) {
        if (value != null) {
            list.add(key + ": " + value);
        }
    }

    private static String getFormattedAnnotation(List<String> annotationParts,
                                                 String annotationType, String typeName, String balType) {
        String annotation = String.join(COMMA + NEW_LINE + TAB, annotationParts);
        return String.format(ANNOTATION_FORMAT, ANNOTATION_MODULE, annotationType, annotation) + NEW_LINE +
                String.format(TYPE_FORMAT, typeName, balType);
    }

    private static boolean invalidNumberLimits(Double minimum, Double exclusiveMinimum, Double maximum,
                                               Double exclusiveMaximum) {
        return (minimum != null && maximum != null && maximum < minimum) ||
                (minimum != null && exclusiveMaximum != null && exclusiveMaximum <= minimum) ||
                (exclusiveMinimum != null && maximum != null && maximum <= exclusiveMinimum) ||
                (exclusiveMinimum != null && exclusiveMaximum != null && exclusiveMaximum <= exclusiveMinimum);
    }

    public static String resolveNameConflicts(String name, Generator generator) {
        String baseName = sanitizeName(name);
        String resolvedName = baseName;
        int counter = 1;

        while (generator.nodes.containsKey(resolvedName)) {
            StringBuilder sb = new StringBuilder(baseName);
            sb.append(counter);
            resolvedName = sb.toString();
            counter++;
        }
        return resolvedName;
    }

    public static String resolveNameConflictsWithSuffix(String name, Generator generator) {
        String baseName = sanitizeName(name);
        StringBuilder sb = new StringBuilder(baseName);
        sb.append(1);
        String resolvedName = sb.toString();
        int counter = 2;

        while (generator.nodes.containsKey(resolvedName)) {
            sb = new StringBuilder(baseName);
            sb.append(counter);
            resolvedName = sb.toString();
            counter++;
        }
        return resolvedName;
    }

    public static String convertToPascalCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static String convertToCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public static String sanitizeName(String input) {
        if (!input.matches(INVALID_CHARS_PATTERN)
                || (input.matches(DIGIT_PATTERN) && !input.matches(STARTS_WITH_DIGIT_PATTERN))) {
            return input;
        }
        if (input.matches(STARTS_WITH_DIGIT_PATTERN)) {
            input = UNDERSCORE + input;
        }
        for (String placeholder : List.of(SLASH_PATTERN, WHITESPACE_PATTERN, SPECIAL_CHARS_PATTERN)) {
            input = input.replaceAll(placeholder, UNDERSCORE);
        }
        return input;
    }

    private static boolean customTypeNotRequired(Object... objects) {
        for (Object obj : objects) {
            if (obj != null) {
                return false;
            }
        }
        return true;
    }
}
