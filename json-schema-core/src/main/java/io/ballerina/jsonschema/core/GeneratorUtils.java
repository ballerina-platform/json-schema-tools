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

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.jsonschema.core.diagnostic.JsonSchemaDiagnostic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public static final String OBJECT_CONSTRAINTS = "ObjectConstraints";
    public static final String NUMBER_CONSTRAINTS = "NumberConstraints";
    public static final String STRING_CONSTRAINTS = "StringConstraints";
    public static final String ARRAY_CONSTRAINTS = "ArrayConstraints";
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

    public static final String INVALID_CHARS_PATTERN = ".*[!@$%^&*()_\\-|/\\\\\\s\\d].*";
    public static final String DIGIT_PATTERN = ".*\\d.*";
    public static final String STARTS_WITH_DIGIT_PATTERN = "^\\d.*";
    public static final String SLASH_PATTERN = "[/\\\\]";
    public static final String WHITESPACE_PATTERN = "\\s";
    public static final String SPECIAL_CHARS_PATTERN = "[!@$%^&*()_\\-|]";

    public static final String ITEM_SUFFIX = "Item";
    public static final String NAME_REST_ITEM = "RestItem";
    public static final String REST_TYPE = "RestType";
    public static final String ADDITIONAL_PROPS = "AdditionalProperties";
    public static final String DEPENDENT_SCHEMA = "DependentSchema";
    public static final String DEPENDENT_REQUIRED = "DependentRequired";
    public static final String UNEVALUATED_PROPS = "UnevaluatedProperties";
    public static final String PATTERN_ELEMENT = "PatternElement";
    public static final String PATTERN_PROPERTIES = "PatternProperties";
    public static final String UNEVALUATED_ITEMS_SUFFIX = "UnevaluatedItems";
    public static final String PROPERTY_NAMES_SUFFIX = "PropertyNames";

    static final ArrayList<String> STRING_FORMATS = new ArrayList<>(
            Arrays.asList("date", "time", "date-time", "duration", "regex", "email", "idn-email", "hostname",
                    "idn-hostname", "ipv4", "ipv6", "json-pointer", "relative-json-pointer", "uri",
                    "uri-reference", "uri-template", "iri", "iri-reference", "uuid")
    );
    static final List<String> BAL_PRIMITIVE_TYPES = new ArrayList<>(
            Arrays.asList(INTEGER, BOOLEAN, NULL, NEVER, JSON, STRING)
    );

    static class RecordField {
        private String type;
        private boolean required;

        private List<String> dependentRequired;
        private String dependentSchema;
        private String defaultValue;

        RecordField(String type, boolean required) {
            this.type = type;
            this.required = required;
            this.dependentRequired = new ArrayList<>();
            this.dependentSchema = null;
            this.defaultValue = null;
        }

        String getType() {
            return type;
        }

        void setType(String type) {
            this.type = type;
        }

        boolean isRequired() {
            return required;
        }

        void setRequired() {
            this.required = true;
        }

        String getDependentSchema() {
            return dependentSchema;
        }

        void setDependentSchema(String dependentSchema) {
            this.dependentSchema = dependentSchema;
        }

        List<String> getDependentRequired() {
            return dependentRequired;
        }

        void setDependentRequired(List<String> dependentRequired) {
            this.dependentRequired = dependentRequired;
        }

        void addDependentRequired(String dependentRequired) {
            this.dependentRequired.add(dependentRequired);
        }

        void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        String getDefaultValue() {
            return defaultValue;
        }
    }

    static void processRequiredFields(Map<String, RecordField> recordFields) {
        boolean changeFlag = true;
        while (changeFlag) {
            changeFlag = false;
            for (Map.Entry<String, RecordField> entry : recordFields.entrySet()) {
                String key = entry.getKey();
                RecordField keyRecord = recordFields.get(key);
                RecordField value = entry.getValue();

                if (value.getDependentRequired() == null || !keyRecord.isRequired()) {
                    continue;
                }

                // If there are dependentRequired items iterate through them
                for (String dependentRequired : value.getDependentRequired()) {
                    if (!recordFields.get(dependentRequired).isRequired()) {
                        recordFields.get(dependentRequired).setRequired();
                        changeFlag = true;
                    }
                }
            }
        }
    }

    // Create a new type with the union of typedesc and return the new type name.
    static String resolveTypeNameForTypedesc(String name, String typeName, Generator generator) {
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

    static ArrayList<String> processRecordFields(Map<String, RecordField> recordFields, Generator generator) {
        ArrayList<String> recordBody = new ArrayList<>();

        for (Map.Entry<String, RecordField> entry : recordFields.entrySet()) {
            String key = entry.getKey();
            RecordField value = entry.getValue();

            ArrayList<String> fieldAnnotation = new ArrayList<>();

            if (value.getDependentSchema() != null) {
                addImports(BAL_JSON_DATA_MODULE, generator);
                String dependentSchema = String.format(FIELD_ANNOTATION_FORMAT, ANNOTATION_MODULE, DEPENDENT_SCHEMA,
                        value.getDependentSchema());
                addImports(BAL_JSON_DATA_MODULE, generator);
                fieldAnnotation.add(dependentSchema);
            }

            if (value.getDependentRequired() != null && !value.getDependentRequired().isEmpty()) {
                addImports(BAL_JSON_DATA_MODULE, generator);
                String dependentArray = value.getDependentRequired().stream()
                        .map(name -> DOUBLE_QUOTATION + name + DOUBLE_QUOTATION)
                        .collect(Collectors.joining(", ", "[", "]"));
                String dependentRequired = String.format(FIELD_ANNOTATION_FORMAT, ANNOTATION_MODULE,
                        DEPENDENT_REQUIRED, dependentArray);
                fieldAnnotation.add(dependentRequired);
            }

            if (value.isRequired()) {
                if (value.getDefaultValue() != null) {
                    fieldAnnotation.add(String.join(WHITE_SPACE, value.getType(), key, EQUAL,
                            value.getDefaultValue()));
                } else {
                    fieldAnnotation.add(value.getType() + WHITE_SPACE + key);
                }
            } else {
                fieldAnnotation.add(value.getType() + WHITE_SPACE + key + QUESTION_MARK);
            }

            recordBody.add(String.join(NEW_LINE, fieldAnnotation) + SEMI_COLON);
        }

        return recordBody;
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

    // Handle union values by enclosing them in parentheses.
    static String handleUnion(String type) {
        if (type.contains(PIPE) && !type.startsWith(OPEN_BRACKET)) {
            return OPEN_BRACKET + type + CLOSE_BRACKET;
        }
        return type;
    }

    static boolean isPrimitiveBalType(String type) {
        return BAL_PRIMITIVE_TYPES.contains(type);
    }

    static void addIfNotNull(List<String> list, String key, Object value) {
        if (value != null) {
            list.add(key + ": " + value);
        }
    }

    static String getFormattedAnnotation(List<String> annotationParts,
                                         String annotationType, String typeName, String balType) {
        String annotation = String.join(COMMA + NEW_LINE + TAB, annotationParts);
        return String.format(ANNOTATION_FORMAT, ANNOTATION_MODULE, annotationType, annotation) + NEW_LINE +
                String.format(TYPE_FORMAT, typeName, balType);
    }

    static boolean isNumberLimitInvalid(Double minimum, Double exclusiveMinimum, Double maximum,
                                        Double exclusiveMaximum) {
        return (minimum != null && maximum != null && maximum < minimum) ||
                (minimum != null && exclusiveMaximum != null && exclusiveMaximum <= minimum) ||
                (exclusiveMinimum != null && maximum != null && maximum <= exclusiveMinimum) ||
                (exclusiveMinimum != null && exclusiveMaximum != null && exclusiveMaximum <= exclusiveMinimum);
    }

    static String resolveNameConflicts(String name, Generator generator) {
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

    static String resolveConstMapping(Generator generator) {
        String name = "MAPPING_";
        String resolvedName;
        do {
            resolvedName = name + generator.getNextConstIndex();
        } while (generator.nodes.containsKey(resolvedName));
        return resolvedName;
    }

    static String convertToPascalCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    static String convertToCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    static String sanitizeName(String input) {
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

    static boolean isCustomTypeNotRequired(Object... objects) {
        return Arrays.stream(objects).allMatch(Objects::isNull);
    }

    static void addImports(String module, Generator generator) {
        String importDeclaration = IMPORT + WHITE_SPACE + module + SEMI_COLON;
        if (!generator.imports.contains(importDeclaration)) {
            generator.imports.add(importDeclaration);
        }
    }

    static void addDiagnostic(JsonSchemaDiagnostic diagnostic, Generator generator) {
        generator.diagnostics.add(diagnostic);
    }
}
