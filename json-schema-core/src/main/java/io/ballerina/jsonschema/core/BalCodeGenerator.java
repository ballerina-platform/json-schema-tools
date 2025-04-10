package io.ballerina.jsonschema.core;

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeParser;

import java.util.Arrays;

public class BalCodeGenerator {
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

    public static final String BAL_JSON_SCHEMA_DATA_MODULE = "ballerina/data.jsondata";
    public static final String ANNOTATION_MODULE = "jsondata";
    public static final String NUMBER_ANNOTATION = AT + ANNOTATION_MODULE + COLON + "NumberValidation";
    public static final String STRING_ANNOTATION = AT + ANNOTATION_MODULE + COLON + "StringValidation";
    public static final String ARRAY_ANNOTATION = AT + ANNOTATION_MODULE + COLON + "ArrayValidation";
    public static final String OBJECT_ANNOTATION = AT + ANNOTATION_MODULE + COLON + "ObjectValidation";
    public static final String DEPENDENT_SCHEMA_ANNOTATION = AT + ANNOTATION_MODULE + COLON + DEPENDENT_SCHEMA;
    public static final String DEPENDENT_REQUIRED_ANNOTATION = AT + ANNOTATION_MODULE + COLON + DEPENDENT_REQUIRED;
    public static final String ONE_OF_ANNOTATION = AT + "OneOf";

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

    public static String createType(String name, Schema schema, Object type, Generator generator) {
        if (type == Long.class) {
            return createInteger(name, schema.getMinimum(), schema.getExclusiveMinimum(), schema.getMaximum(),
                    schema.getExclusiveMaximum(), schema.getMultipleOf(), generator);
        }
        //TODO: Complete for other data types
        return "INCOMPLETE";
    }

    public static String createInteger(String name, Double minimum, Double exclusiveMinimum, Double maximum,
                                       Double exclusiveMaximum, Double multipleOf, Generator generator) {
        if (minimum == null && maximum == null && exclusiveMaximum == null &&
                exclusiveMinimum == null && multipleOf == null) {
            return INTEGER;
        }

        if ((minimum != null && maximum != null && maximum < minimum) ||
                (minimum != null && exclusiveMaximum != null && exclusiveMaximum <= minimum) ||
                (exclusiveMinimum != null && maximum != null && maximum <= exclusiveMinimum) ||
                (exclusiveMinimum != null && exclusiveMaximum != null && exclusiveMaximum <= exclusiveMinimum)) {
            return NEVER;
        }

        generator.addImports(BAL_JSON_SCHEMA_DATA_MODULE);
        String finalName = resolveNameConflicts(convertToPascalCase(name), generator);

        StringBuilder annotation = new StringBuilder();
        annotation.append(NUMBER_ANNOTATION).append(OPEN_BRACES);

        if (minimum != null) {
            annotation.append(MINIMUM).append(COLON).append(minimum).append(COMMA);
        }
        if (exclusiveMinimum != null) {
            annotation.append(EXCLUSIVE_MINIMUM).append(COLON).append(exclusiveMinimum).append(COMMA);
        }
        if (maximum != null) {
            annotation.append(MAXIMUM).append(COLON).append(maximum).append(COMMA);
        }
        if (exclusiveMaximum != null) {
            annotation.append(EXCLUSIVE_MAXIMUM).append(COLON).append(exclusiveMaximum).append(COMMA);
        }
        if (multipleOf != null) {
            annotation.append(MULTIPLE_OF).append(COLON).append(multipleOf).append(COMMA);
        }

        annotation.deleteCharAt(annotation.length() - 1).append(CLOSE_BRACES).append(NEW_LINE);
        annotation.append(PUBLIC).append(WHITE_SPACE).append(TYPE).append(WHITE_SPACE).append(finalName)
                .append(WHITE_SPACE).append(INTEGER).append(SEMI_COLON);

        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(annotation.toString());
        generator.nodes.put(finalName, moduleNode);

        return finalName;
    }

    public static String resolveNameConflicts(String name, Generator generator) {
        String baseName = sanitizeName(name);
        int counter = 1;
        while (generator.nodes.containsKey(name)) {
            name = baseName + counter;
            counter++;
        }
        return name;
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
        for (String placeholder : Arrays.asList(SLASH_PATTERN, WHITESPACE_PATTERN, SPECIAL_CHARS_PATTERN)) {
            input = input.replaceAll(placeholder, UNDERSCORE);
        }
        return input;
    }
}
