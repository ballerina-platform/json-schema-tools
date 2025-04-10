package io.ballerina.jsonschema.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaUtils {
    public static final List<String> SUPPORTED_DRAFTS = List.of("https://json-schema.org/draft/2020-12/schema");

    static final Map<String, String> ID_TO_TYPE_MAP = new HashMap<>();

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

            if (((Schema) schema).getSchemaKeyword() == null ||
                    !SUPPORTED_DRAFTS.contains(((Schema) schema).getSchemaKeyword())) {
                throw new RuntimeException("Schema draft not supported");
            }
        } else {
            throw new Exception("JSON schema is not valid");
        }
        return schema;
    }
}
