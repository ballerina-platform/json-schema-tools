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
import java.util.List;
import java.util.Map;

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
    public static final String VALUE = "value";
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
    public static final String UNIVERSAL_ARRAY = "json[]";
    public static final String UNIVERSAL_OBJECT = "record{|json...;|}";
    public static final String DEFAULT_SCHEMA_NAME = "Schema";
    public static final String DEPENDENT_SCHEMA = "dependentSchema";
    public static final String DEPENDENT_REQUIRED = "dependentRequired";

    public static final String BAL_REGEX_MODULE = "ballerina/lang.regexp";
    public static final String BAL_JSON_SCHEMA_DATA_MODULE = "ballerina/data.jsondata";

    public static final String ANNOTATION_MODULE = "jsondata";
    public static final String NUMBER_ANNOTATION = AT + ANNOTATION_MODULE + COLON + "NumberValidation";
    public static final String STRING_ANNOTATION = AT + ANNOTATION_MODULE + COLON + "StringValidation";
    public static final String ARRAY_ANNOTATION = AT + ANNOTATION_MODULE + COLON + "ArrayValidation";
    public static final String OBJECT_ANNOTATION = AT + ANNOTATION_MODULE + COLON + "ObjectValidation";
    public static final String DEPENDENT_SCHEMA_ANNOTATION = AT + ANNOTATION_MODULE + COLON + DEPENDENT_SCHEMA;
    public static final String DEPENDENT_REQUIRED_ANNOTATION = AT + ANNOTATION_MODULE + COLON + DEPENDENT_REQUIRED;
    public static final String ONE_OF_ANNOTATION = AT + "OneOf";

    public static final String ANNOTATION_FORMAT = "%s {%n\t%s%n}%npublic type %s %s;";

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

    private static final ArrayList<String> STRING_FORMATS = new ArrayList<>(
            Arrays.asList("date", "time", "date-time", "duration", "regex", "email", "idn-email", "hostname",
                    "idn-hostname", "ipv4", "ipv6", "json-pointer", "relative-json-pointer", "uri",
                    "uri-reference", "uri-template", "iri", "iri-reference", "uuid")
    );

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

        if (invalidLimits(minimum, exclusiveMinimum, maximum, exclusiveMaximum)) {
            return NEVER;
        }

        generator.addImports(BAL_JSON_SCHEMA_DATA_MODULE);
        String finalType = resolveNameConflicts(convertToPascalCase(name), generator);

        List<String> annotationParts = new ArrayList<>();

        addIfNotNull(annotationParts, MINIMUM, minimum);
        addIfNotNull(annotationParts, EXCLUSIVE_MINIMUM, exclusiveMinimum);
        addIfNotNull(annotationParts, MAXIMUM, maximum);
        addIfNotNull(annotationParts, EXCLUSIVE_MAXIMUM, exclusiveMaximum);
        addIfNotNull(annotationParts, MULTIPLE_OF, multipleOf);

        String formattedAnnotation = getFormattedAnnotation(annotationParts, NUMBER_ANNOTATION, finalType, INTEGER);

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

        if (invalidLimits(minimum, exclusiveMinimum, maximum, exclusiveMaximum)) {
            return NEVER;
        }

        generator.addImports(BAL_JSON_SCHEMA_DATA_MODULE);
        String finalType = resolveNameConflicts(convertToPascalCase(name), generator);

        List<String> annotationParts = new ArrayList<>();

        addIfNotNull(annotationParts, MINIMUM, minimum);
        addIfNotNull(annotationParts, EXCLUSIVE_MINIMUM, exclusiveMinimum);
        addIfNotNull(annotationParts, MAXIMUM, maximum);
        addIfNotNull(annotationParts, EXCLUSIVE_MAXIMUM, exclusiveMaximum);
        addIfNotNull(annotationParts, MULTIPLE_OF, multipleOf);

        String formattedAnnotation = getFormattedAnnotation(annotationParts, NUMBER_ANNOTATION, finalType, NUMBER);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        generator.nodes.put(finalType, moduleNode);

        return finalType;
    }

    public static String createString(String name, String format, Long minLength, Long maxLength,
                                      String pattern, Generator generator) {
        if (format == null && minLength == null && maxLength == null && pattern == null) {
            return STRING;
        }

        generator.addImports(BAL_JSON_SCHEMA_DATA_MODULE);
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
            generator.addImports(BAL_REGEX_MODULE);
            annotationParts.add(PATTERN + COLON + REGEX_PREFIX + BACK_TICK + pattern + BACK_TICK);
        }

        String formattedAnnotation = getFormattedAnnotation(annotationParts, STRING_ANNOTATION, finalType, STRING);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(formattedAnnotation);
        generator.nodes.put(finalType, moduleNode);

        return finalType;
    }

    public static String createArray(String name, List<Object> prefixItems, Object items, Object contains,
                                     Long minItems, Long maxItems, Boolean uniqueItems, Long maxContains,
                                     Long minContains, Object unevaluatedItems, Generator generator) throws Exception {
        // TODO: Implement array type generation
        return UNIVERSAL_ARRAY;
    }

    public static String createObject(String name, Object additionalProperties, Map<String, Object> properties,
                                      Map<String, Object> patternProperties, Map<String, Object> dependentSchema,
                                      Object propertyNames, Object unevaluatedProperties, Long maxProperties,
                                      Long minProperties, Map<String, List<String>> dependentRequired,
                                      List<String> required, Generator generator) {
        // TODO: Implement object type generation
        return UNIVERSAL_OBJECT;
    }

    private static void addIfNotNull(List<String> list, String key, Object value) {
        if (value != null) {
            list.add(key + ": " + value);
        }
    }

    private static String getFormattedAnnotation(List<String> annotationParts,
                                                 String annotationType, String typeName, String type) {
        String annotation = String.join("," + NEW_LINE + TAB, annotationParts);
        return String.format(ANNOTATION_FORMAT, annotationType, annotation, typeName, type);
    }

    private static boolean invalidLimits(Double minimum, Double exclusiveMinimum, Double maximum,
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
}
