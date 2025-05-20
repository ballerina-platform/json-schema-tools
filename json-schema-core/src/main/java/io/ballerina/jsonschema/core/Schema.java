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

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the deserialized Schema object corresponding to a JSON schema.
 *
 * @since 0.1.0
 */
class Schema {
    // Applicator
    @JsonAdapter(SchemaDeserializers.ListSchemaDeserializer.class)
    private List<Object> prefixItems = new ArrayList<>();

    @JsonAdapter(SchemaDeserializers.SchemaDeserializer.class)
    private Object items;

    @JsonAdapter(SchemaDeserializers.SchemaDeserializer.class)
    private Object contains;

    @JsonAdapter(SchemaDeserializers.SchemaDeserializer.class)
    private Object additionalProperties;

    @JsonAdapter(SchemaDeserializers.MapStringSchemaDeserializer.class)
    private Map<String, Object> properties;

    @JsonAdapter(SchemaDeserializers.MapStringSchemaDeserializer.class)
    private Map<String, Object> patternProperties;

    @JsonAdapter(SchemaDeserializers.MapStringSchemaDeserializer.class)
    private Map<String, Object> dependentSchema;

    @JsonAdapter(SchemaDeserializers.PropertyNameDeserializer.class)
    private Object propertyNames;

    @JsonAdapter(SchemaDeserializers.SchemaDeserializer.class)
    @SerializedName("if")
    private Object ifKeyword;

    @JsonAdapter(SchemaDeserializers.SchemaDeserializer.class)
    private Object then;

    @JsonAdapter(SchemaDeserializers.SchemaDeserializer.class)
    @SerializedName("else")
    private Object elseKeyword;

    @JsonAdapter(SchemaDeserializers.ListSchemaDeserializer.class)
    private List<Object> allOf = new ArrayList<>();

    @JsonAdapter(SchemaDeserializers.ListSchemaDeserializer.class)
    private List<Object> oneOf = new ArrayList<>();

    @JsonAdapter(SchemaDeserializers.ListSchemaDeserializer.class)
    private List<Object> anyOf = new ArrayList<>();

    @JsonAdapter(SchemaDeserializers.SchemaDeserializer.class)
    private Object not;

    // Content
    private String contentEncoding;
    private String contentMediaType;

    @JsonAdapter(SchemaDeserializers.SchemaDeserializer.class)
    private Object contentSchema;

    // Core
    @SerializedName("$id")
    private String idKeyword;

    @SerializedName("$schema")
    private String schemaKeyword;

    @SerializedName("$ref")
    private String refKeyword;

    @SerializedName("$anchor")
    private String anchorKeyword;

    @SerializedName("$dynamicRef")
    private String dynamicRefKeyword;

    @SerializedName("$dynamicAnchor")
    private String dynamicAnchorKeyword;

    @SerializedName("$vocabulary")
    private String vocabularyKeyword;

    @SerializedName("$comment")
    private String commentKeyword;

    @JsonAdapter(SchemaDeserializers.MapStringSchemaDeserializer.class)
    @SerializedName("$defs")
    private Map<String, Object> defsKeyword;

    // Format-annotation/ Format-assertion
    private String format;

    // Meta-data
    private String title;
    private String description;

    @SerializedName("default")
    private Object defaultKeyword;

    private Boolean deprecated;
    private Boolean readOnly;
    private Boolean writeOnly;

    private List<Object> examples;

    // Unevaluated
    @JsonAdapter(SchemaDeserializers.SchemaDeserializer.class)
    private Object unevaluatedItems;

    @JsonAdapter(SchemaDeserializers.SchemaDeserializer.class)
    private Object unevaluatedProperties;

    // Validation
    @JsonAdapter(SchemaDeserializers.TypeDeserializer.class)
    private List<String> type;

    @SerializedName("const")
    private Object constKeyword;

    @SerializedName("enum")
    private List<Object> enumKeyword;

    private Double multipleOf;
    private Double maximum;
    private Double exclusiveMaximum;
    private Double minimum;
    private Double exclusiveMinimum;

    private Long maxLength;
    private Long minLength;

    private String pattern;

    private Long maxItems;
    private Long minItems;

    private Boolean uniqueItems;

    private Long maxContains;
    private Long minContains;

    private Long maxProperties;
    private Long minProperties;

    private List<String> required;

    private Map<String, List<String>> dependentRequired;

    // TODO: Extra keyword are currently not supported by attributes

    // Constructors
    public Schema(
            // Applicator
            List<Object> prefixItems,
            Object items,
            Object contains,
            Object additionalProperties,
            Map<String, Object> properties,
            Map<String, Object> patternProperties,
            Map<String, Object> dependentSchema,
            Object propertyNames,
            Object ifKeyword,
            Object then,
            Object elseKeyword,
            List<Object> allOf,
            List<Object> oneOf,
            List<Object> anyOf,
            Object not,

            // Content
            String contentEncoding,
            String contentMediaType,
            Object contentSchema,

            // Core
            String idKeyword,
            String schemaKeyword,
            String refKeyword,
            String anchorKeyword,
            String dynamicRefKeyword,
            String dynamicAnchorKeyword,
            String vocabularyKeyword,
            String commentKeyword,
            Map<String, Object> defsKeyword,

            // Format-annotation/ Format-assertion
            String format,

            // Meta-data
            String title,
            String description,
            Object defaultKeyword,
            Boolean deprecated,
            Boolean readOnly,
            Boolean writeOnly,
            ArrayList<Object> examples,

            // Unevaluated
            Object unevaluatedItems,
            Object unevaluatedProperties,

            // Validation
            ArrayList<String> type,
            Object constKeyword,
            ArrayList<Object> enumKeyword,
            Double multipleOf,
            Double maximum,
            Double exclusiveMaximum,
            Double minimum,
            Double exclusiveMinimum,
            Long maxLength,
            Long minLength,
            String pattern,
            Long maxItems,
            Long minItems,
            Boolean uniqueItems,
            Long maxContains,
            Long minContains,
            Long maxProperties,
            Long minProperties,
            List<String> required,
            Map<String, List<String>> dependentRequired
    ) {
        this.prefixItems = prefixItems;
        this.items = items;
        this.contains = contains;
        this.additionalProperties = additionalProperties;
        this.properties = properties;
        this.patternProperties = patternProperties;
        this.dependentSchema = dependentSchema;
        this.propertyNames = propertyNames;
        this.ifKeyword = ifKeyword;
        this.then = then;
        this.elseKeyword = elseKeyword;
        this.allOf = allOf;
        this.oneOf = oneOf;
        this.anyOf = anyOf;
        this.not = not;
        this.contentEncoding = contentEncoding;
        this.contentMediaType = contentMediaType;
        this.contentSchema = contentSchema;
        this.idKeyword = idKeyword;
        this.schemaKeyword = schemaKeyword;
        this.refKeyword = refKeyword;
        this.anchorKeyword = anchorKeyword;
        this.dynamicRefKeyword = dynamicRefKeyword;
        this.dynamicAnchorKeyword = dynamicAnchorKeyword;
        this.vocabularyKeyword = vocabularyKeyword;
        this.commentKeyword = commentKeyword;
        this.defsKeyword = defsKeyword;
        this.format = format;
        this.title = title;
        this.description = description;
        this.defaultKeyword = defaultKeyword;
        this.deprecated = deprecated;
        this.readOnly = readOnly;
        this.writeOnly = writeOnly;
        this.examples = examples;
        this.unevaluatedItems = unevaluatedItems;
        this.unevaluatedProperties = unevaluatedProperties;
        this.type = type;
        this.constKeyword = constKeyword;
        this.enumKeyword = enumKeyword;
        this.multipleOf = multipleOf;
        this.maximum = maximum;
        this.exclusiveMaximum = exclusiveMaximum;
        this.minimum = minimum;
        this.exclusiveMinimum = exclusiveMinimum;
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.pattern = pattern;
        this.maxItems = maxItems;
        this.minItems = minItems;
        this.uniqueItems = uniqueItems;
        this.maxContains = maxContains;
        this.minContains = minContains;
        this.maxProperties = maxProperties;
        this.minProperties = minProperties;
        this.required = required;
        this.dependentRequired = dependentRequired;
    }

    public Schema() {
    }

    // Applicator
    public Object getItems() {
        return items;
    }

    public void setItems(Object items) {
        this.items = items;
    }

    public List<Object> getPrefixItems() {
        return prefixItems;
    }

    public void setPrefixItems(List<Object> prefixItems) {
        this.prefixItems = (prefixItems != null) ? prefixItems : new ArrayList<>();
    }

    public Object getContains() {
        return contains;
    }

    public void setContains(Object contains) {
        this.contains = contains;
    }

    public Object getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Object additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getPatternProperties() {
        return patternProperties;
    }

    public void setPatternProperties(Map<String, Object> patternProperties) {
        this.patternProperties = patternProperties;
    }

    public Map<String, Object> getDependentSchema() {
        return dependentSchema;
    }

    public void setDependentSchema(Map<String, Object> dependentSchema) {
        this.dependentSchema = dependentSchema;
    }

    public Object getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(Object propertyNames) {
        this.propertyNames = propertyNames;
    }

    public Object getIfKeyword() {
        return ifKeyword;
    }

    public void setIfKeyword(Object ifKeyword) {
        this.ifKeyword = ifKeyword;
    }

    public Object getThen() {
        return then;
    }

    public void setThen(Object then) {
        this.then = then;
    }

    public Object getElseKeyword() {
        return elseKeyword;
    }

    public void setElseKeyword(Object elseKeyword) {
        this.elseKeyword = elseKeyword;
    }

    public List<Object> getAllOf() {
        return allOf;
    }

    public void setAllOf(List<Object> allOf) {
        this.allOf = (allOf != null) ? allOf : new ArrayList<>();
    }

    public List<Object> getOneOf() {
        return oneOf;
    }

    public void setOneOf(List<Object> oneOf) {
        this.oneOf = (oneOf != null) ? oneOf : new ArrayList<>();
    }

    public List<Object> getAnyOf() {
        return anyOf;
    }

    public void setAnyOf(List<Object> anyOf) {
        this.anyOf = (anyOf != null) ? anyOf : new ArrayList<>();
    }

    public Object getNot() {
        return not;
    }

    public void setNot(Object not) {
        this.not = not;
    }

    // Content
    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentMediaType() {
        return contentMediaType;
    }

    public void setContentMediaType(String contentMediaType) {
        this.contentMediaType = contentMediaType;
    }

    public Object getContentSchema() {
        return contentSchema;
    }

    public void setContentSchema(Object contentSchema) {
        this.contentSchema = contentSchema;
    }

    // Core
    public String getIdKeyword() {
        return idKeyword;
    }

    public void setIdKeyword(String idKeyword) {
        this.idKeyword = idKeyword;
    }

    public String getSchemaKeyword() {
        return schemaKeyword;
    }

    public void setSchemaKeyword(String schemaKeyword) {
        this.schemaKeyword = schemaKeyword;
    }

    public String getRefKeyword() {
        return refKeyword;
    }

    public void setRefKeyword(String refKeyword) {
        this.refKeyword = refKeyword;
    }

    public String getAnchorKeyword() {
        return anchorKeyword;
    }

    public void setAnchorKeyword(String anchorKeyword) {
        this.anchorKeyword = anchorKeyword;
    }

    public String getDynamicRefKeyword() {
        return dynamicRefKeyword;
    }

    public void setDynamicRefKeyword(String dynamicRefKeyword) {
        this.dynamicRefKeyword = dynamicRefKeyword;
    }

    public String getDynamicAnchorKeyword() {
        return dynamicAnchorKeyword;
    }

    public void setDynamicAnchorKeyword(String dynamicAnchorKeyword) {
        this.dynamicAnchorKeyword = dynamicAnchorKeyword;
    }

    public String getVocabularyKeyword() {
        return vocabularyKeyword;
    }

    public void setVocabularyKeyword(String vocabularyKeyword) {
        this.vocabularyKeyword = vocabularyKeyword;
    }

    public String getCommentKeyword() {
        return commentKeyword;
    }

    public void setCommentKeyword(String commentKeyword) {
        this.commentKeyword = commentKeyword;
    }

    public Map<String, Object> getDefsKeyword() {
        return defsKeyword;
    }

    public void setDefsKeyword(Map<String, Object> defsKeyword) {
        this.defsKeyword = defsKeyword;
    }

    // Format-annotation/ Format-assertion
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    // Meta-data
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getDefaultKeyword() {
        return defaultKeyword;
    }

    public void setDefaultKeyword(Object defaultKeyword) {
        this.defaultKeyword = defaultKeyword;
    }

    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getWriteOnly() {
        return writeOnly;
    }

    public void setWriteOnly(Boolean writeOnly) {
        this.writeOnly = writeOnly;
    }

    public List<Object> getExamples() {
        return examples;
    }

    public void setExamples(List<Object> examples) {
        this.examples = examples;
    }

    // Unevaluated
    public Object getUnevaluatedItems() {
        return unevaluatedItems;
    }

    public void setUnevaluatedItems(Object unevaluatedItems) {
        this.unevaluatedItems = unevaluatedItems;
    }

    public Object getUnevaluatedProperties() {
        return unevaluatedProperties;
    }

    public void setUnevaluatedProperties(Object unevaluatedProperties) {
        this.unevaluatedProperties = unevaluatedProperties;
    }

    // Validation
    public List<String> getType() {
        return type;
    }

    public void setType(ArrayList<String> type) {
        this.type = type;
    }

    public Object getConstKeyword() {
        return constKeyword;
    }

    public void setConstKeyword(Object constKeyword) {
        this.constKeyword = constKeyword;
    }

    public List<Object> getEnumKeyword() {
        return enumKeyword;
    }

    public void setEnumKeyword(List<Object> enumKeyword) {
        this.enumKeyword = enumKeyword;
    }

    public Double getMultipleOf() {
        return multipleOf;
    }

    public void setMultipleOf(Double multipleOf) {
        this.multipleOf = multipleOf;
    }

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public Double getExclusiveMaximum() {
        return exclusiveMaximum;
    }

    public void setExclusiveMaximum(Double exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    public Double getExclusiveMinimum() {
        return exclusiveMinimum;
    }

    public void setExclusiveMinimum(Double exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    public Long getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Long maxLength) {
        this.maxLength = maxLength;
    }

    public Long getMinLength() {
        return minLength;
    }

    public void setMinLength(Long minLength) {
        this.minLength = minLength;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Long getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(Long maxItems) {
        this.maxItems = maxItems;
    }

    public Long getMinItems() {
        return minItems;
    }

    public void setMinItems(Long minItems) {
        this.minItems = minItems;
    }

    public Boolean getUniqueItems() {
        return uniqueItems;
    }

    public void setUniqueItems(Boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    public Long getMaxContains() {
        return maxContains;
    }

    public void setMaxContains(Long maxContains) {
        this.maxContains = maxContains;
    }

    public Long getMinContains() {
        return minContains;
    }

    public void setMinContains(Long minContains) {
        this.minContains = minContains;
    }

    public Long getMaxProperties() {
        return maxProperties;
    }

    public void setMaxProperties(Long maxProperties) {
        this.maxProperties = maxProperties;
    }

    public Long getMinProperties() {
        return minProperties;
    }

    public void setMinProperties(Long minProperties) {
        this.minProperties = minProperties;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public Map<String, List<String>> getDependentRequired() {
        return dependentRequired;
    }

    public void setDependentRequired(Map<String, List<String>> dependentRequired) {
        this.dependentRequired = dependentRequired;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Schema schema)) {
            return false;
        }

        return Objects.equals(prefixItems, schema.prefixItems) &&
                Objects.equals(items, schema.items) &&
                Objects.equals(contains, schema.contains) &&
                Objects.equals(additionalProperties, schema.additionalProperties) &&
                Objects.equals(properties, schema.properties) &&
                Objects.equals(patternProperties, schema.patternProperties) &&
                Objects.equals(dependentSchema, schema.dependentSchema) &&
                Objects.equals(propertyNames, schema.propertyNames) &&
                Objects.equals(ifKeyword, schema.ifKeyword) &&
                Objects.equals(then, schema.then) &&
                Objects.equals(elseKeyword, schema.elseKeyword) &&
                Objects.equals(allOf, schema.allOf) &&
                Objects.equals(oneOf, schema.oneOf) &&
                Objects.equals(anyOf, schema.anyOf) &&
                Objects.equals(not, schema.not) &&
                Objects.equals(contentEncoding, schema.contentEncoding) &&
                Objects.equals(contentMediaType, schema.contentMediaType) &&
                Objects.equals(contentSchema, schema.contentSchema) &&
                Objects.equals(idKeyword, schema.idKeyword) &&
                Objects.equals(schemaKeyword, schema.schemaKeyword) &&
                Objects.equals(refKeyword, schema.refKeyword) &&
                Objects.equals(anchorKeyword, schema.anchorKeyword) &&
                Objects.equals(dynamicRefKeyword, schema.dynamicRefKeyword) &&
                Objects.equals(dynamicAnchorKeyword, schema.dynamicAnchorKeyword) &&
                Objects.equals(vocabularyKeyword, schema.vocabularyKeyword) &&
                Objects.equals(commentKeyword, schema.commentKeyword) &&
                Objects.equals(defsKeyword, schema.defsKeyword) &&
                Objects.equals(format, schema.format) &&
                Objects.equals(title, schema.title) &&
                Objects.equals(description, schema.description) &&
                Objects.equals(defaultKeyword, schema.defaultKeyword) &&
                Objects.equals(deprecated, schema.deprecated) &&
                Objects.equals(readOnly, schema.readOnly) &&
                Objects.equals(writeOnly, schema.writeOnly) &&
                Objects.equals(examples, schema.examples) &&
                Objects.equals(unevaluatedItems, schema.unevaluatedItems) &&
                Objects.equals(unevaluatedProperties, schema.unevaluatedProperties) &&
                Objects.equals(type, schema.type) &&
                Objects.equals(constKeyword, schema.constKeyword) &&
                Objects.equals(enumKeyword, schema.enumKeyword) &&
                Objects.equals(multipleOf, schema.multipleOf) &&
                Objects.equals(maximum, schema.maximum) &&
                Objects.equals(exclusiveMaximum, schema.exclusiveMaximum) &&
                Objects.equals(minimum, schema.minimum) &&
                Objects.equals(exclusiveMinimum, schema.exclusiveMinimum) &&
                Objects.equals(maxLength, schema.maxLength) &&
                Objects.equals(minLength, schema.minLength) &&
                Objects.equals(pattern, schema.pattern) &&
                Objects.equals(maxItems, schema.maxItems) &&
                Objects.equals(minItems, schema.minItems) &&
                Objects.equals(uniqueItems, schema.uniqueItems) &&
                Objects.equals(maxContains, schema.maxContains) &&
                Objects.equals(minContains, schema.minContains) &&
                Objects.equals(maxProperties, schema.maxProperties) &&
                Objects.equals(minProperties, schema.minProperties) &&
                Objects.equals(required, schema.required) &&
                Objects.equals(dependentRequired, schema.dependentRequired);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                prefixItems, items, contains, additionalProperties, properties, patternProperties,
                dependentSchema, propertyNames, ifKeyword, then, elseKeyword, allOf, oneOf, anyOf, not,
                contentEncoding, contentMediaType, contentSchema, idKeyword, schemaKeyword, refKeyword,
                anchorKeyword, dynamicRefKeyword, dynamicAnchorKeyword, vocabularyKeyword,
                commentKeyword, defsKeyword, format, title, description, defaultKeyword, deprecated,
                readOnly, writeOnly, examples, unevaluatedItems, unevaluatedProperties, type,
                constKeyword, enumKeyword, multipleOf, maximum, exclusiveMaximum, minimum,
                exclusiveMinimum, maxLength, minLength, pattern, maxItems, minItems, uniqueItems,
                maxContains, minContains, maxProperties, minProperties, required, dependentRequired
        );
    }

    public static Object deepCopy(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Boolean || obj instanceof String || obj instanceof Number) {
            return obj;
        }

        if (obj instanceof List originalList) {
            List<Object> copiedList = new ArrayList<>();
            for (Object element : originalList) {
                copiedList.add(deepCopy(element));
            }
            return copiedList;
        }

        if (obj instanceof Map) {
            Map<String, Object> originalMap = (Map<String, Object>) obj;
            Map<String, Object> copiedMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
                copiedMap.put(entry.getKey(), deepCopy(entry.getValue()));
            }
            return copiedMap;
        }

        if (obj instanceof Schema schema) {
            Schema copiedSchema = new Schema();

            copiedSchema.setPrefixItems((ArrayList<Object>) deepCopy(schema.getPrefixItems()));
            copiedSchema.setItems(deepCopy(schema.getItems()));
            copiedSchema.setContains(deepCopy(schema.getContains()));
            copiedSchema.setAdditionalProperties(deepCopy(schema.getAdditionalProperties()));
            copiedSchema.setProperties((Map<String, Object>) deepCopy(schema.getProperties()));
            copiedSchema.setPatternProperties((Map<String, Object>) deepCopy(schema.getPatternProperties()));
            copiedSchema.setDependentSchema((Map<String, Object>) deepCopy(schema.getDependentSchema()));
            copiedSchema.setPropertyNames(deepCopy(schema.getPropertyNames()));
            copiedSchema.setIfKeyword(deepCopy(schema.getIfKeyword()));
            copiedSchema.setThen(deepCopy(schema.getThen()));
            copiedSchema.setElseKeyword(deepCopy(schema.getElseKeyword()));
            copiedSchema.setAllOf((List<Object>) deepCopy(schema.getAllOf()));
            copiedSchema.setOneOf((List<Object>) deepCopy(schema.getOneOf()));
            copiedSchema.setAnyOf((List<Object>) deepCopy(schema.getAnyOf()));
            copiedSchema.setNot(deepCopy(schema.getNot()));

            copiedSchema.setContentEncoding(schema.getContentEncoding());
            copiedSchema.setContentMediaType(schema.getContentMediaType());
            copiedSchema.setContentSchema(deepCopy(schema.getContentSchema()));

            copiedSchema.setIdKeyword(schema.getIdKeyword());
            copiedSchema.setSchemaKeyword(schema.getSchemaKeyword());
            copiedSchema.setRefKeyword(schema.getRefKeyword());
            copiedSchema.setAnchorKeyword(schema.getAnchorKeyword());
            copiedSchema.setDynamicRefKeyword(schema.getDynamicRefKeyword());
            copiedSchema.setDynamicAnchorKeyword(schema.getDynamicAnchorKeyword());
            copiedSchema.setVocabularyKeyword(schema.getVocabularyKeyword());
            copiedSchema.setCommentKeyword(schema.getCommentKeyword());
            copiedSchema.setDefsKeyword((Map<String, Object>) deepCopy(schema.getDefsKeyword()));

            copiedSchema.setFormat(schema.getFormat());

            copiedSchema.setTitle(schema.getTitle());
            copiedSchema.setDescription(schema.getDescription());
            copiedSchema.setDefaultKeyword(deepCopy(schema.getDefaultKeyword()));
            copiedSchema.setDeprecated(schema.getDeprecated());
            copiedSchema.setReadOnly(schema.getReadOnly());
            copiedSchema.setWriteOnly(schema.getWriteOnly());
            copiedSchema.setExamples((List<Object>) deepCopy(schema.getExamples()));

            copiedSchema.setUnevaluatedItems(deepCopy(schema.getUnevaluatedItems()));
            copiedSchema.setUnevaluatedProperties(deepCopy(schema.getUnevaluatedProperties()));

            copiedSchema.setType(schema.getType() == null ? null : new ArrayList<>(schema.getType()));
            copiedSchema.setConstKeyword(deepCopy(schema.getConstKeyword()));
            copiedSchema.setEnumKeyword(schema.getEnumKeyword() == null ?
                    null : new ArrayList<>(schema.getEnumKeyword()));

            copiedSchema.setMultipleOf(schema.getMultipleOf());
            copiedSchema.setMaximum(schema.getMaximum());
            copiedSchema.setExclusiveMaximum(schema.getExclusiveMaximum());
            copiedSchema.setMinimum(schema.getMinimum());
            copiedSchema.setExclusiveMinimum(schema.getExclusiveMinimum());

            copiedSchema.setMaxLength(schema.getMaxLength());
            copiedSchema.setMinLength(schema.getMinLength());
            copiedSchema.setPattern(schema.getPattern());

            copiedSchema.setMaxItems(schema.getMaxItems());
            copiedSchema.setMinItems(schema.getMinItems());
            copiedSchema.setUniqueItems(schema.getUniqueItems());
            copiedSchema.setMaxContains(schema.getMaxContains());
            copiedSchema.setMinContains(schema.getMinContains());

            copiedSchema.setMaxProperties(schema.getMaxProperties());
            copiedSchema.setMinProperties(schema.getMinProperties());
            copiedSchema.setRequired(schema.getRequired() == null ? null : new ArrayList<>(schema.getRequired()));
            copiedSchema.setDependentRequired(
                    schema.getDependentRequired() == null ? null : new LinkedHashMap<>(schema.getDependentRequired())
            );

            return copiedSchema;
        }

        throw new UnsupportedOperationException("Unsupported type for deep copy: " + obj.getClass());
    }

}
