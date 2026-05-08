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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    public static final String EMPTY_ARRAY_CONSTANT = "[]";
    public static final String UNIVERSAL_OBJECT = "record{|json...;|}";
    public static final String EMPTY_RECORD = "record{||}";

    public static final String BAL_JSON_DATA_MODULE = "ballerina/data.jsondata";

    public static final String ANNOTATION_MODULE = "jsondata";
    public static final String OBJECT_CONSTRAINTS = "ObjectConstraints";
    public static final String NUMBER_CONSTRAINTS = "NumberConstraints";
    public static final String STRING_CONSTRAINTS = "StringConstraints";
    public static final String ARRAY_CONSTRAINTS = "ArrayConstraints";
    public static final String META_DATA = "MetaData";
    public static final String PATTERN_RECORD = ANNOTATION_MODULE + COLON + "PatternPropertiesElement";
    public static final String VALUE = "value";

    public static final String READ_ONLY = "@" + ANNOTATION_MODULE + ":ReadOnly";
    public static final String WRITE_ONLY = "@" + ANNOTATION_MODULE + ":WriteOnly";
    public static final String READ_ONLY_FIELD = "readonly ";
    public static final String DEPRECATED = "@deprecated";

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

    public static final String PREFIX_ITEMS = "prefixItems";
    public static final String ITEMS = "items";
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

    public static final String TITLE = "title";
    public static final String COMMENT = "comment";
    public static final String EXAMPLES = "examples";

    public static final String ALL_OF = "AllOf";
    public static final String ONE_OF = "OneOf";
    public static final String ANY_OF = "AnyOf";
    public static final String NOT = "Not";

    public static final String INVALID_TYPE_NAME_CHAR_PATTERN = "[^A-Za-z0-9_]";
    public static final String MULTIPLE_UNDERSCORES_PATTERN = "_+";
    public static final String STARTS_WITH_DIGIT_PATTERN = "^\\d.*";

    public static final String STRING_ENCODING = "StringEncodedData";
    public static final String CONTENT_ENCODING = "contentEncoding";
    public static final String CONTENT_MEDIA_TYPE = "contentMediaType";
    public static final String CONTENT_SCHEMA = "contentSchema";

    public static final String ITEM_SUFFIX = "Item";
    public static final String NAME_REST_ITEM = "RestItem";
    public static final String REST_TYPE = "RestType";
    public static final String ADDITIONAL_PROPS = "AdditionalProperties";
    public static final String DEPENDENT_SCHEMA = "DependentSchema";
    public static final String DEPENDENT_SCHEMAS = "dependentSchemas";
    public static final String DEPENDENT_REQUIRED = "DependentRequired";
    public static final String UNEVALUATED_PROPS = "UnevaluatedProperties";
    public static final String UNEVALUATED_ITEMS_ANNOT = "UnevaluatedItems";
    public static final String PATTERN_ELEMENT = "PatternElement";
    public static final String PATTERN_PROPERTIES = "PatternProperties";
    public static final String UNEVALUATED_ITEMS_SUFFIX = "UnevaluatedItems";
    public static final String PROPERTY_NAMES_SUFFIX = "PropertyNames";

    public static final String DUMMY_SCHEME = "placeholder:/";
    public static final String COMMENT_HEADER = "# ";
    public static final String CONST_MAPPING_PREFIX = "MAPPING_";
    public static final String EMPTY_ARRAY_CONST_NAME = "EMPTY_ARRAY";

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
        private String dependentSchemaType;
        private String defaultValue;
        private String description;
        private boolean readOnly;
        private boolean deprecated;

        RecordField(String type, boolean required) {
            this.type = type;
            this.required = required;
            this.dependentRequired = new ArrayList<>();
            this.dependentSchemaType = null;
            this.defaultValue = null;
            this.readOnly = false;
            this.deprecated = false;
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

        String getDependentSchemaType() {
            return dependentSchemaType;
        }

        void setDependentSchemaType(String dependentSchemaType) {
            this.dependentSchemaType = dependentSchemaType;
        }

        List<String> getDependentRequired() {
            return dependentRequired;
        }

        void setDependentRequired(List<String> dependentRequired) {
            this.dependentRequired = (dependentRequired != null) ? dependentRequired : new ArrayList<>();
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

        void setDescription(String description) {
            this.description = description;
        }

        String getDescription() {
            return description;
        }

        boolean isReadOnly() {
            return readOnly;
        }

        void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

        boolean isDeprecated() {
            return deprecated;
        }

        void setDeprecated(boolean deprecated) {
            this.deprecated = deprecated;
        }
    }

    static class ContextualTypeMember {
        private final String aliasSeed;
        private final String typeName;

        ContextualTypeMember(String aliasSeed, String typeName) {
            this.aliasSeed = aliasSeed;
            this.typeName = typeName;
        }

        String getAliasSeed() {
            return aliasSeed;
        }

        String getTypeName() {
            return typeName;
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

                List<String> dependentRequired = value.getDependentRequired();
                if (dependentRequired.isEmpty() || !keyRecord.isRequired()) {
                    continue;
                }

                for (String item : dependentRequired) {
                    RecordField itemRecord = recordFields.get(item);
                    if (!itemRecord.isRequired()) {
                        itemRecord.setRequired();
                        changeFlag = true;
                    }
                }
            }
        }
    }

    static String resolveTypeNameForTypedesc(String name, String typeName, Generator generator) {
        if (!typeName.contains(PIPE)) {
            return typeName;
        }
        String newType = resolveNameConflicts(name, generator);
        String typeDeclaration = String.format(TYPE_FORMAT, newType, typeName);
        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(typeDeclaration);
        generator.nodes.put(newType, moduleNode);
        return newType;
    }

    static String resolveAnnotationValueTypeName(String name, String typeName, Generator generator) {
        if (isPrimitiveBalType(typeName) || typeName.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            return typeName;
        }
        String newType = resolveNameConflicts(name, generator);
        String typeDeclaration = String.format(TYPE_FORMAT, newType, typeName);
        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(typeDeclaration);
        generator.nodes.put(newType, moduleNode);
        return newType;
    }

    static List<String> materializeContextualTypeAliases(List<ContextualTypeMember> members, Generator generator) {
        Map<String, Integer> typeCounts = new LinkedHashMap<>();
        for (ContextualTypeMember member : members) {
            typeCounts.merge(member.getTypeName(), 1, Integer::sum);
        }

        List<String> resolvedMembers = new ArrayList<>();
        for (ContextualTypeMember member : members) {
            String typeName = member.getTypeName();
            boolean shouldAlias = typeCounts.getOrDefault(typeName, 0) > 1 || typeName.contains(PIPE);
            if (!shouldAlias) {
                resolvedMembers.add(typeName);
                continue;
            }

            String newType = resolveNameConflicts(member.getAliasSeed(), generator);
            if (newType.equals(typeName)) {
                resolvedMembers.add(typeName);
                continue;
            }

            String typeDeclaration = String.format(TYPE_FORMAT, newType, typeName);
            ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(typeDeclaration);
            generator.nodes.put(newType, moduleNode);
            resolvedMembers.add(newType);
        }
        return resolvedMembers;
    }

    static ArrayList<String> processRecordFields(Map<String, RecordField> recordFields, Generator generator) {
        ArrayList<String> recordBody = new ArrayList<>();

        for (Map.Entry<String, RecordField> entry : recordFields.entrySet()) {
            String key = entry.getKey();
            RecordField value = entry.getValue();
            String fieldName = toBallerinaFieldIdentifier(key);

            ArrayList<String> fieldAnnotation = new ArrayList<>();

            if (value.getDescription() != null) {
                fieldAnnotation.addAll(toCommentLines(value.getDescription()));
            }

            String dependentSchemaType = value.getDependentSchemaType();
            if (dependentSchemaType != null) {
                generator.addJsonDataImport();
                String dependentSchemaString = String.format(
                        FIELD_ANNOTATION_FORMAT, ANNOTATION_MODULE, DEPENDENT_SCHEMA, dependentSchemaType);
                fieldAnnotation.add(dependentSchemaString);
            }

            List<String> dependentRequired = value.getDependentRequired();
            if (!dependentRequired.isEmpty()) {
                generator.addJsonDataImport();
                String dependentArray = dependentRequired.stream()
                        .map(GeneratorUtils::toBallerinaStringLiteral)
                        .collect(Collectors.joining(", ", "[", "]"));
                String dependentRequiredString = String.format(FIELD_ANNOTATION_FORMAT, ANNOTATION_MODULE,
                        DEPENDENT_REQUIRED, dependentArray);
                fieldAnnotation.add(dependentRequiredString);
            }

            String readOnly = value.isReadOnly() ? READ_ONLY_FIELD : "";
            String deprecated = value.isDeprecated() ? DEPRECATED + NEW_LINE : "";

            if (value.isRequired()) {
                if (value.getDefaultValue() != null) {
                    fieldAnnotation.add(deprecated + readOnly + String.join(WHITE_SPACE, value.getType(),
                            fieldName, EQUAL, value.getDefaultValue()));
                } else {
                    fieldAnnotation.add(deprecated + readOnly + value.getType() + WHITE_SPACE + fieldName);
                }
            } else {
                fieldAnnotation.add(deprecated + readOnly + value.getType() + WHITE_SPACE + fieldName + QUESTION_MARK);
            }

            recordBody.add(String.join(NEW_LINE, fieldAnnotation) + SEMI_COLON);

        }
        return recordBody;
    }

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

    static void addStringIfNotNull(List<String> list, String key, Object value) {
        if (value != null) {
            list.add(key + ": " + toBallerinaStringLiteral(String.valueOf(value)));
        }
    }

    static String toBallerinaStringLiteral(String raw) {
        StringBuilder builder = new StringBuilder(DOUBLE_QUOTATION);
        raw.codePoints().forEach(codePoint -> {
            switch (codePoint) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (Character.isISOControl(codePoint)) {
                        builder.append("\\u{")
                                .append(Integer.toHexString(codePoint).toUpperCase(Locale.ROOT))
                                .append("}");
                    } else {
                        builder.appendCodePoint(codePoint);
                    }
                }
            }
        });
        builder.append(DOUBLE_QUOTATION);
        return builder.toString();
    }

    static String toBallerinaFieldIdentifier(String raw) {
        if (raw.isEmpty()) {
            throw new IllegalArgumentException(
                    "JSON property names must be non-empty to generate Ballerina record fields"
            );
        }
        if (isValidUnquotedIdentifier(raw)) {
            return raw;
        }

        StringBuilder builder = new StringBuilder("'");
        raw.codePoints().forEach(codePoint -> {
            if (isValidQuotedIdentifierChar(codePoint)) {
                builder.appendCodePoint(codePoint);
            } else {
                builder.append("\\u{")
                        .append(Integer.toHexString(codePoint).toUpperCase(Locale.ROOT))
                        .append("}");
            }
        });
        return builder.toString();
    }

    static List<String> toCommentLines(String raw) {
        return Arrays.stream(raw.split("\\R", -1))
                .map(line -> COMMENT_HEADER + line)
                .toList();
    }

    static String getFormattedAnnotation(List<String> annotationParts,
                                         String annotationType, String typeName, String balType) {
        String annotation = String.join(COMMA + NEW_LINE + TAB, annotationParts);
        return String.format(ANNOTATION_FORMAT, ANNOTATION_MODULE, annotationType, annotation) + NEW_LINE +
                String.format(TYPE_FORMAT, typeName, balType);
    }

    static boolean isInvalidNumberLimit(Double minimum, Double exclusiveMinimum, Double maximum,
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
        String resolvedName;
        do {
            // Mapping_<index>
            resolvedName = CONST_MAPPING_PREFIX + generator.getNextConstIndex();
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

    private static boolean isValidUnquotedIdentifier(String raw) {
        int[] codePoints = raw.codePoints().toArray();
        if (codePoints.length == 0) {
            return false;
        }
        if (!isValidIdentifierInitialChar(codePoints[0])) {
            return false;
        }
        for (int i = 1; i < codePoints.length; i++) {
            if (!isValidQuotedIdentifierChar(codePoints[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidIdentifierInitialChar(int codePoint) {
        return codePoint == '_' || Character.isLetter(codePoint);
    }

    private static boolean isValidQuotedIdentifierChar(int codePoint) {
        return codePoint == '_' || Character.isLetterOrDigit(codePoint);
    }

    static String sanitizeName(String input) {
        if (input.matches(STARTS_WITH_DIGIT_PATTERN)) {
            input = UNDERSCORE + input;
        }
        input = input.replaceAll(INVALID_TYPE_NAME_CHAR_PATTERN, UNDERSCORE);
        input = input.replaceAll(MULTIPLE_UNDERSCORES_PATTERN, UNDERSCORE);
        return input;
    }

    static boolean areAllNullOrEmpty(Object... objects) {
        return Arrays.stream(objects).allMatch(obj ->
                obj == null
                        || (obj instanceof Collection<?> collection && collection.isEmpty())
                        || (obj instanceof Map<?, ?> map && map.isEmpty())
        );
    }

    static void addDiagnostic(JsonSchemaDiagnostic diagnostic, Generator generator) {
        generator.diagnostics.add(diagnostic);
    }

    static void stripOuterMetadataAndCoreKeywords(Schema schema) {
        schema.setIdKeyword(null);
        schema.setSchemaKeyword(null);
        schema.setAnchorKeyword(null);
        schema.setDynamicAnchorKeyword(null);
        schema.setVocabularyKeyword(null);
        schema.setDefsKeyword(new LinkedHashMap<>());
        schema.setTitle(null);
        schema.setDescription(null);
        schema.setDefaultKeyword(null);
        schema.setDeprecated(null);
        schema.setReadOnly(null);
        schema.setWriteOnly(null);
        schema.setExamples(new ArrayList<>());
        schema.setCommentKeyword(null);
    }

    static void stripReferenceWrapperToMetadataOnly(Schema schema) {
        schema.setRefKeyword(null);
        schema.setDynamicRefKeyword(null);
        schema.setPrefixItems(new ArrayList<>());
        schema.setItems(null);
        schema.setContains(null);
        schema.setAdditionalProperties(null);
        schema.setProperties(new LinkedHashMap<>());
        schema.setPatternProperties(new LinkedHashMap<>());
        schema.setDependentSchemas(new LinkedHashMap<>());
        schema.setPropertyNames(null);
        schema.setIfKeyword(null);
        schema.setThen(null);
        schema.setElseKeyword(null);
        schema.setOneOf(new ArrayList<>());
        schema.setAnyOf(new ArrayList<>());
        schema.setNot(null);
        schema.setContentEncoding(null);
        schema.setContentMediaType(null);
        schema.setContentSchema(null);
        schema.setFormat(null);
        schema.setUnevaluatedItems(null);
        schema.setUnevaluatedProperties(null);
        schema.setType(new ArrayList<>());
        schema.clearConstKeyword();
        schema.setEnumKeyword(null);
        schema.setMultipleOf(null);
        schema.setMaximum(null);
        schema.setExclusiveMaximum(null);
        schema.setMinimum(null);
        schema.setExclusiveMinimum(null);
        schema.setMaxLength(null);
        schema.setMinLength(null);
        schema.setPattern(null);
        schema.setMaxItems(null);
        schema.setMinItems(null);
        schema.setUniqueItems(null);
        schema.setMaxContains(null);
        schema.setMinContains(null);
        schema.setMaxProperties(null);
        schema.setMinProperties(null);
        schema.setRequired(new ArrayList<>());
        schema.setDependentRequired(new LinkedHashMap<>());
    }
}
