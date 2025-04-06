package io.ballerina.jsonschema.core;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TypeDeserializer implements JsonDeserializer<ArrayList<String>> {
    @Override
    public ArrayList<String> deserialize(JsonElement jsonElement, Type type,
                                         JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        if (jsonElement.isJsonArray()) {
            return jsonDeserializationContext.deserialize(jsonElement, ArrayList.class);
        }
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
            return new ArrayList<>(List.of(jsonElement.getAsString()));
        }
        throw new JsonParseException("Expected a string or an array of strings");
    }
}

class PropertyNameDeserializer implements JsonDeserializer<Object> {
    @Override
    public Object deserialize(JsonElement jsonElement, Type type,
                              JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean()) {
            return jsonElement.getAsBoolean();
        }
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            jsonObject.addProperty("type", "string");
            return jsonDeserializationContext.deserialize(jsonObject, Schema.class);
        }
        throw new JsonParseException("Expected a boolean or an object");
    }
}

class SchemaDeserializer implements JsonDeserializer<Object> {
    @Override
    public Object deserialize(JsonElement jsonElement, Type type,
                              JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean()) {
            return jsonElement.getAsBoolean();
        }
        if (jsonElement.isJsonObject()) {
            return jsonDeserializationContext.deserialize(jsonElement, Schema.class);
        }
        throw new JsonParseException("Expected a boolean or an object");
    }
}

class ListSchemaDeserializer implements JsonDeserializer<List<Object>> {
    @Override
    public List<Object> deserialize(JsonElement jsonElement, Type type,
                                    JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (!jsonElement.isJsonArray()) {
            throw new JsonParseException("Expected an array");
        }
        List<Object> list = new ArrayList<>();
        for (JsonElement element : jsonElement.getAsJsonArray()) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
                list.add(element.getAsBoolean());
            } else {
                list.add(jsonDeserializationContext.deserialize(element, Schema.class));
            }
        }
        return list;
    }
}

class MapStringSchemaDeserializer implements JsonDeserializer<Map<String, Object>> {
    @Override
    public Map<String, Object> deserialize(JsonElement jsonElement, Type type,
                                           JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isBoolean()) {
                resultMap.put(entry.getKey(), entry.getValue().getAsBoolean());
            } else {
                Schema schema = jsonDeserializationContext.deserialize(entry.getValue(), Schema.class);
                resultMap.put(entry.getKey(), schema);
            }
        }
        return resultMap;
    }
}


class Schema {

    // Applicator
    @JsonAdapter(ListSchemaDeserializer.class)
    private List<Object> prefixItems;

    @JsonAdapter(SchemaDeserializer.class)
    private Object items;

    @JsonAdapter(SchemaDeserializer.class)
    private Object contains;

    @JsonAdapter(SchemaDeserializer.class)
    private Object additionalProperties;

    @JsonAdapter(MapStringSchemaDeserializer.class)
    private Map<String, Object> properties;

    @JsonAdapter(MapStringSchemaDeserializer.class)
    private Map<String, Object> patternProperties;

    @JsonAdapter(MapStringSchemaDeserializer.class)
    private Map<String, Object> dependentSchema;

    @JsonAdapter(PropertyNameDeserializer.class)
    private Object propertyNames;

    @JsonAdapter(SchemaDeserializer.class)
    @SerializedName("if")
    private Object ifKeyword;

    @JsonAdapter(SchemaDeserializer.class)
    private Object then;

    @JsonAdapter(SchemaDeserializer.class)
    @SerializedName("else")
    private Object elseKeyword;

    @JsonAdapter(ListSchemaDeserializer.class)
    private List<Object> allOf;

    @JsonAdapter(ListSchemaDeserializer.class)
    private List<Object> oneOf;

    @JsonAdapter(ListSchemaDeserializer.class)
    private List<Object> anyOf;

    @JsonAdapter(SchemaDeserializer.class)
    private Object not;

    // Content
    private String contentEncoding;
    private String contentMediaType;

    @JsonAdapter(SchemaDeserializer.class)
    private Object content;

    // Core
    private String idKeyword;
    private String schemaKeyword;
    private String refKeyword;
    private String anchorKeyword;
    private String dynamicRefKeyword;
    private String dynamicAnchorKeyword;
    private String vocabularyKeyword;
    private String commentKeyword;

    @JsonAdapter(MapStringSchemaDeserializer.class)
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
    @JsonAdapter(SchemaDeserializer.class)
    private Object unevaluatedItems;

    @JsonAdapter(SchemaDeserializer.class)
    private Object unevaluatedProperties;

    // Validation
    @JsonAdapter(TypeDeserializer.class)
    private ArrayList<String> type;

    @SerializedName("const")
    private Object constKeyword;

    @SerializedName("enum")
    private ArrayList<Object> enumKeyword;

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

    // TODO: Extra properties support.

    // Constructors
    public Schema(
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
            String contentEncoding,
            String contentMediaType,
            Object content,
            String idKeyword,
            String schemaKeyword,
            String refKeyword,
            String anchorKeyword,
            String dynamicRefKeyword,
            String dynamicAnchorKeyword,
            String vocabularyKeyword,
            String commentKeyword,
            Map<String, Object> defsKeyword,
            String format,
            String title,
            String description,
            Object defaultKeyword,
            Boolean deprecated,
            Boolean readOnly,
            Boolean writeOnly,
            List<Object> examples,
            Object unevaluatedItems,
            Object unevaluatedProperties,
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
        this.content = content;
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

    public Schema() {}

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
        this.prefixItems = prefixItems;
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
        this.allOf = allOf;
    }

    public List<Object> getOneOf() {
        return oneOf;
    }

    public void setOneOf(List<Object> oneOf) {
        this.oneOf = oneOf;
    }

    public List<Object> getAnyOf() {
        return anyOf;
    }

    public void setAnyOf(List<Object> anyOf) {
        this.anyOf = anyOf;
    }

    public Object getNot() {
        return not;
    }

    public void setNot(Object not) {
        this.not = not;
    }

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

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

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

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

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

    public ArrayList<String> getType() {
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

    public ArrayList<Object> getEnumKeyword() {
        return enumKeyword;
    }

    public void setEnumKeyword(ArrayList<Object> enumKeyword) {
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
}
