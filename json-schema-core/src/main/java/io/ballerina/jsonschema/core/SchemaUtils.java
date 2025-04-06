package io.ballerina.jsonschema.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaUtils {
    public static final List<String> SUPPORTED_DRAFTS = List.of("https://json-schema.org/draft/2020-12/schema");

    // Mapping the id to schema.
    static final Map<String, Schema> SCHEMA_LIST = new HashMap<>();

    // Mapping the id to created ballerina data types.
    static final Map<String, String> ID_TO_TYPE_MAP = new HashMap<>();

    static final Map<String, String> ANCHORS = new HashMap<>();
    static final Map<String, String> DYNAMIC_ANCHORS = new HashMap<>();

    public static Object parseJsonSchema(String jsonString) throws Exception {
        Object schema = null;

        Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
        jsonString = jsonString.trim();

        if (jsonString.isEmpty()) {
            throw new Exception("JSON schema is empty");
        }

        if (jsonString.equals("true")) {
            schema = (Boolean) true;
        } else if (jsonString.equals("false")) {
            schema = (Boolean) false;
        } else if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
            schema = gson.fromJson(jsonString, Schema.class);

            String id = ((Schema) schema).getIdKeyword();
            schemaIdCollector((Schema) schema, id, id);

//            if (((Schema) schema).schemaKeyword() == null ||
//                    !SUPPORTED_DRAFTS.contains(((Schema) schema).schemaKeyword())) {
//                throw new RuntimeException("Schema draft not supported");
//            }
        } else {
            throw new Exception("JSON schema is not valid");
        }
        return schema;
    }

    public static void schemaIdCollector(Schema schema, String id, String idPrefix) {
        SCHEMA_LIST.put(id, schema);

        if (schema.getIdKeyword() == null) {
            schema.setIdKeyword(id);
        } else {
            idPrefix = schema.getIdKeyword();
        }

        if (schema.getAnchorKeyword() != null) {
            if (ANCHORS.containsKey(schema.getAnchorKeyword())) {
                throw new RuntimeException("Anchor " + schema.getAnchorKeyword() + " is not unique");
            }
            ANCHORS.put(schema.getAnchorKeyword(), id);
            // TODO: There is a naming convention in JSON Schema.
        }

        if (schema.getDynamicAnchorKeyword() != null) {
            if (DYNAMIC_ANCHORS.containsKey(schema.getDynamicAnchorKeyword())) {
                throw new RuntimeException("Anchor " + schema.getDynamicAnchorKeyword() + " is not unique");
            }
            DYNAMIC_ANCHORS.put(schema.getDynamicAnchorKeyword(), id);
        }

        if (schema.getDefsKeyword() != null) {
            for (Map.Entry<String, Object> entry : schema.getDefsKeyword().entrySet()) {
                if (entry.getValue() instanceof Schema) {
                    schemaIdCollector((Schema) entry.getValue(), id + "/$defs/" + entry.getKey(), idPrefix);
                }
            }
        }

        if (schema.getItems() != null && schema.getItems() instanceof Schema) {
            schemaIdCollector((Schema) schema.getItems(), id + "/items", idPrefix);
        }

        if (schema.getContains() != null && schema.getContains() instanceof Schema) {
            schemaIdCollector((Schema) schema.getContains(), id + "/contains", idPrefix);
        }

        if (schema.getAdditionalProperties() != null) {
            if (schema.getAdditionalProperties() instanceof Schema) {
                schemaIdCollector((Schema) schema.getAdditionalProperties(), id + "/additionalProperties", idPrefix);
            } else if ((Boolean) schema.getAdditionalProperties()) {
                schema.setAdditionalProperties(null);
            }
        }

        if (schema.getUnevaluatedProperties() != null) {
            if (schema.getUnevaluatedProperties() instanceof Schema) {
                schemaIdCollector((Schema) schema.getUnevaluatedProperties(), id + "/unevaluatedProperties", idPrefix);
            } else if ((Boolean) schema.getAdditionalProperties()) {
                schema.setUnevaluatedItems(null);
            }
        }

        if (schema.getProperties() != null) {
            for (Map.Entry<String, Object> entry : (schema.getProperties()).entrySet()) {
                if (entry.getValue() instanceof Schema) {
                    schemaIdCollector((Schema) entry.getValue(), id + "/properties/" + entry.getKey(), idPrefix);
                }
            }
        }

        if (schema.getPatternProperties() != null) {
            for (Map.Entry<String, Object> entry : (schema.getPatternProperties()).entrySet()) {
                if (entry.getValue() instanceof Schema) {
                    schemaIdCollector((Schema) entry.getValue(), id + "/patternProperties/" + entry.getKey(), idPrefix);
                }
            }
        }

        if (schema.getDependentSchema() != null) {
            for (Map.Entry<String, Object> entry : (schema.getDependentSchema()).entrySet()) {
                if (entry.getValue() instanceof Schema) {
                    schemaIdCollector((Schema) entry.getValue(), id + "/dependentSchema/" + entry.getKey(), idPrefix);
                }
            }
        }

        if (schema.getPropertyNames() != null && schema.getPropertyNames() instanceof Schema) {
            schemaIdCollector((Schema) schema.getPropertyNames(), id + "/propertyNames", idPrefix);
        }

        if (schema.getIfKeyword() != null && schema.getIfKeyword() instanceof Schema) {
            schemaIdCollector((Schema) schema.getIfKeyword(), id + "/if", idPrefix);
        }

        if (schema.getThen() != null && schema.getThen() instanceof Schema) {
            schemaIdCollector((Schema) schema.getThen(), id + "/then", idPrefix);
        }

        if (schema.getElseKeyword() != null && schema.getElseKeyword() instanceof Schema) {
            schemaIdCollector((Schema) schema.getElseKeyword(), id + "/else", idPrefix);
        }

        if (schema.getNot() != null && schema.getNot() instanceof Schema) {
            schemaIdCollector((Schema) schema.getNot(), id + "/not", idPrefix);
        }

        if (schema.getContent() != null && schema.getContent() instanceof Schema) {
            schemaIdCollector((Schema) schema.getContent(), id + "/content", idPrefix);
        }

        if (schema.getUnevaluatedItems() != null && schema.getUnevaluatedItems() instanceof Schema) {
            schemaIdCollector((Schema) schema.getUnevaluatedItems(), id + "/unevaluatedItems", idPrefix);
        }

        if (schema.getRefKeyword() != null) {
            if (schema.getRefKeyword().startsWith(String.valueOf("#/"))) {
                schema.setRefKeyword(idPrefix + schema.getRefKeyword().substring(1));
            } else if (schema.getRefKeyword().startsWith(String.valueOf("/"))) {
                schema.setRefKeyword(idPrefix + schema.getRefKeyword());
            } else if (schema.getRefKeyword().startsWith(String.valueOf("#"))) {
                String anchorName = schema.getRefKeyword().substring(1);
                if (ANCHORS.containsKey(anchorName)) {
                    schema.setRefKeyword(ANCHORS.get(anchorName));
                } else {
                    throw new RuntimeException("Anchor " + anchorName + " not found");
                }
            } else if (!(schema.getRefKeyword().startsWith(String.valueOf("http://")) || schema.getRefKeyword()
                    .startsWith(String.valueOf("https://")))) { //TODO: Recheck this
                schema.setRefKeyword(idPrefix + "/" + schema.getRefKeyword());
            }
        }

        if (schema.getDynamicRefKeyword() != null) { // TODO: Complete this part
            if (schema.getDynamicRefKeyword().startsWith(String.valueOf("#/"))) {
                String anchorName = schema.getRefKeyword().substring(1);
                if (DYNAMIC_ANCHORS.containsKey(anchorName)) {
                    schema.setDynamicRefKeyword(DYNAMIC_ANCHORS.get(anchorName));
                } else {
                    throw new RuntimeException("Dynamic anchor " + anchorName + " not found");
                }
            } else {
                throw new RuntimeException("Dynamic anchor " + schema.getDynamicRefKeyword() + " not found");
            }
        }

        // Adding default values
        // TODO: Complete this if needed.
    }
}
