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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Util methods to handle Schema keywords.
 *
 * @since 0.1.0
 */
public class SchemaUtils {
    private static final String DRAFT_2020_12 = "https://json-schema.org/draft/2020-12/schema";

    private static final List<String> SUPPORTED_DRAFTS = List.of(DRAFT_2020_12);

    public static void fetchSchemaId(Object schemaObject, Map<URI, Schema> idToSchemaMap) {
        if (!(schemaObject instanceof Schema schema)) {
            return;
        }

        if (schema.getIdKeyword() != null) {
            URI uri = URI.create(schema.getIdKeyword());
            if (idToSchemaMap.containsKey(uri)) {
                throw new RuntimeException("Schema id \"" + schema.getIdKeyword() + "\" is not unique");
            }
            idToSchemaMap.put(uri, schema);
        }

        // prefixItems
        if (schema.getPrefixItems() != null) {
            for (Object obj : schema.getPrefixItems()) {
                fetchSchemaId(obj, idToSchemaMap);
            }
        }

        // items
        if (schema.getItems() != null) {
            fetchSchemaId(schema.getItems(), idToSchemaMap);
        }

        // contains
        if (schema.getContains() != null) {
            fetchSchemaId(schema.getContains(), idToSchemaMap);
        }

        // additionalProperties
        if (schema.getAdditionalProperties() != null) {
            fetchSchemaId(schema.getAdditionalProperties(), idToSchemaMap);
        }

        // properties
        if (schema.getProperties() != null) {
            for (Map.Entry<String, Object> entry : schema.getProperties().entrySet()) {
                fetchSchemaId(entry.getValue(), idToSchemaMap);
            }
        }

        // patternProperties
        if (schema.getPatternProperties() != null) {
            for (Map.Entry<String, Object> entry : schema.getPatternProperties().entrySet()) {
                fetchSchemaId(entry.getValue(), idToSchemaMap);
            }
        }

        // dependentSchema
        if (schema.getDependentSchema() != null) {
            fetchSchemaId(schema.getDependentSchema(), idToSchemaMap);
        }

        // propertyNames
        if (schema.getPropertyNames() != null) {
            fetchSchemaId(schema.getPropertyNames(), idToSchemaMap);
        }

        // if
        if (schema.getIfKeyword() != null) {
            fetchSchemaId(schema.getIfKeyword(), idToSchemaMap);
        }

        // then
        if (schema.getThen() != null) {
            fetchSchemaId(schema.getThen(), idToSchemaMap);
        }

        // else
        if (schema.getElseKeyword() != null) {
            fetchSchemaId(schema.getElseKeyword(), idToSchemaMap);
        }

        // allOf
        if (schema.getAllOf() != null) {
            for (Object obj : schema.getAllOf()) {
                fetchSchemaId(obj, idToSchemaMap);
            }
        }

        // anyOf
        if (schema.getAnyOf() != null) {
            for (Object obj : schema.getAnyOf()) {
                fetchSchemaId(obj, idToSchemaMap);
            }
        }

        // oneOf
        if (schema.getOneOf() != null) {
            for (Object obj : schema.getOneOf()) {
                fetchSchemaId(obj, idToSchemaMap);
            }
        }

        // not
        if (schema.getNot() != null) {
            fetchSchemaId(schema.getNot(), idToSchemaMap);
        }

        // content
        if (schema.getContent() != null) {
            fetchSchemaId(schema.getContent(), idToSchemaMap);
        }

        // $defs
        if (schema.getDefsKeyword() != null) {
            for (Map.Entry<String, Object> entry : schema.getDefsKeyword().entrySet()) {
                fetchSchemaId(entry.getValue(), idToSchemaMap);
            }
        }

        // unevaluatedItems
        if (schema.getUnevaluatedItems() != null) {
            fetchSchemaId(schema.getUnevaluatedItems(), idToSchemaMap);
        }

        // unevaluatedProperties
        if (schema.getUnevaluatedProperties() != null) {
            fetchSchemaId(schema.getUnevaluatedProperties(), idToSchemaMap);
        }
    }

    public static Object parseJsonSchema(String jsonString) throws Exception {
        Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
        jsonString = jsonString.trim();

        if (jsonString.isEmpty()) {
            throw new EmptyJsonSchemaException("JSON schema is empty");
        }

        if (jsonString.equals("true")) {
            return Boolean.TRUE;
        }
        if (jsonString.equals("false")) {
            return Boolean.FALSE;
        }
        if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
            Schema tmpschema = gson.fromJson(jsonString, Schema.class);

            if (tmpschema.getSchemaKeyword() == null ||
                    !SUPPORTED_DRAFTS.contains(tmpschema.getSchemaKeyword())) {
                throw new RuntimeException("Schema draft not supported");
            }
            return tmpschema;
        }
        throw new InvalidJsonSchemaException("JSON schema is not valid");
    }

    public static class InvalidJsonSchemaException extends Exception {
        public InvalidJsonSchemaException(String message) {
            super(message);
        }

        public InvalidJsonSchemaException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class EmptyJsonSchemaException extends Exception {
        public EmptyJsonSchemaException(String message) {
            super(message);
        }

        public EmptyJsonSchemaException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidDataTypeException extends Exception {
        public InvalidDataTypeException(String message) {
            super(message);
        }

        public InvalidDataTypeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
