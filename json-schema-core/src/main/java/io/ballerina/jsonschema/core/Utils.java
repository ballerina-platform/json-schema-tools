package io.ballerina.jsonschema.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

import java.util.List;

public class Utils {
    public static final List<String> SUPPORTED_DRAFTS = List.of("https://json-schema.org/draft/2020-12/schema");

    public static Object parseJsonSchema(String jsonString) throws Exception {
        Object schema;
        try {
            Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
            jsonString = jsonString.trim();

            if (jsonString.isEmpty()) {
                throw new Exception("JSON schema is empty");
            }

            if (jsonString.equals("true")) {
                schema = true;
            } else if (jsonString.equals("false")) {
                schema = false;
            } else if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
                schema = gson.fromJson(jsonString, Schema.class);

                if (((Schema) schema).schemaKeyword() == null ||
                        !SUPPORTED_DRAFTS.contains(((Schema) schema).schemaKeyword())) {
                    throw new RuntimeException("Schema draft not supported");
                }
            } else {
                throw new Exception("JSON schema is not valid");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return schema;
    }
}
