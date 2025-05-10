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

    public static void fetchSchemaId(Object schemaObject, URI baseUri, Map<URI, Schema> idToSchemaMap) {
        if (!(schemaObject instanceof Schema schema)) {
            return;
        }

        if (schema.getIdKeyword() != null) {
            URI uri = URI.create(schema.getIdKeyword());
            if (idToSchemaMap.containsKey(uri)) {
                throw new RuntimeException("Schema id \"" + schema.getIdKeyword() + "\" is not unique");
            }
            idToSchemaMap.put(uri, schema);
            baseUri = uri;
        }
        if (schema.getAnchorKeyword() != null) {
            // TODO: validate the anchor syntax
            URI uri = URI.create("#" + schema.getAnchorKeyword());
            if (idToSchemaMap.containsKey(uri)) {
                throw new RuntimeException("Schema anchor \"" + schema.getAnchorKeyword() + "\" is not unique");
            }
            idToSchemaMap.put(baseUri.resolve(uri), schema);
        }
        if (schema.getDynamicAnchorKeyword() != null) {
            // TODO: validate the anchor syntax
            URI uri = URI.create("#" + schema.getDynamicAnchorKeyword());
            if (idToSchemaMap.containsKey(uri)) {
                throw new RuntimeException("Schema anchor \"" + schema.getDynamicAnchorKeyword() + "\" is not unique");
            }
            idToSchemaMap.put(baseUri.resolve(uri), schema);
        }

        // prefixItems
        if (schema.getPrefixItems() != null) {
            for (Object obj : schema.getPrefixItems()) {
                fetchSchemaId(obj, baseUri, idToSchemaMap);
            }
        }

        // items
        if (schema.getItems() != null) {
            fetchSchemaId(schema.getItems(), baseUri, idToSchemaMap);
        }

        // contains
        if (schema.getContains() != null) {
            fetchSchemaId(schema.getContains(), baseUri, idToSchemaMap);
        }

        // additionalProperties
        if (schema.getAdditionalProperties() != null) {
            fetchSchemaId(schema.getAdditionalProperties(), baseUri, idToSchemaMap);
        }

        // properties
        if (schema.getProperties() != null) {
            for (Map.Entry<String, Object> entry : schema.getProperties().entrySet()) {
                fetchSchemaId(entry.getValue(), baseUri, idToSchemaMap);
            }
        }

        // patternProperties
        if (schema.getPatternProperties() != null) {
            for (Map.Entry<String, Object> entry : schema.getPatternProperties().entrySet()) {
                fetchSchemaId(entry.getValue(), baseUri, idToSchemaMap);
            }
        }

        // dependentSchema
        if (schema.getDependentSchema() != null) {
            fetchSchemaId(schema.getDependentSchema(), baseUri, idToSchemaMap);
        }

        // propertyNames
        if (schema.getPropertyNames() != null) {
            fetchSchemaId(schema.getPropertyNames(), baseUri, idToSchemaMap);
        }

        // if
        if (schema.getIfKeyword() != null) {
            fetchSchemaId(schema.getIfKeyword(), baseUri, idToSchemaMap);
        }

        // then
        if (schema.getThen() != null) {
            fetchSchemaId(schema.getThen(), baseUri, idToSchemaMap);
        }

        // else
        if (schema.getElseKeyword() != null) {
            fetchSchemaId(schema.getElseKeyword(), baseUri, idToSchemaMap);
        }

        // allOf
        if (schema.getAllOf() != null) {
            for (Object obj : schema.getAllOf()) {
                fetchSchemaId(obj, baseUri, idToSchemaMap);
            }
        }

        // anyOf
        if (schema.getAnyOf() != null) {
            for (Object obj : schema.getAnyOf()) {
                fetchSchemaId(obj, baseUri, idToSchemaMap);
            }
        }

        // oneOf
        if (schema.getOneOf() != null) {
            for (Object obj : schema.getOneOf()) {
                fetchSchemaId(obj, baseUri, idToSchemaMap);
            }
        }

        // not
        if (schema.getNot() != null) {
            fetchSchemaId(schema.getNot(), baseUri, idToSchemaMap);
        }

        // content
        if (schema.getContent() != null) {
            fetchSchemaId(schema.getContent(), baseUri, idToSchemaMap);
        }

        // $defs
        if (schema.getDefsKeyword() != null) {
            for (Map.Entry<String, Object> entry : schema.getDefsKeyword().entrySet()) {
                fetchSchemaId(entry.getValue(), baseUri, idToSchemaMap);
            }
        }

        // unevaluatedItems
        if (schema.getUnevaluatedItems() != null) {
            fetchSchemaId(schema.getUnevaluatedItems(), baseUri, idToSchemaMap);
        }

        // unevaluatedProperties
        if (schema.getUnevaluatedProperties() != null) {
            fetchSchemaId(schema.getUnevaluatedProperties(), baseUri, idToSchemaMap);
        }
    }

    public static void convertToAbsoluteUri(Object schemaObject, URI baseUri) {
        if (!(schemaObject instanceof Schema schema)) {
            return;
        }

        // Resolving $ref and $dynamicRef
        if (schema.getRefKeyword() != null) {
            URI uri = URI.create(schema.getRefKeyword());
            schema.setRefKeyword(baseUri.resolve(uri).toString());
        }
        if (schema.getDynamicRefKeyword() != null) {
            URI uri = URI.create(schema.getDynamicRefKeyword());
            schema.setDynamicRefKeyword(baseUri.resolve(uri).toString());
        }

        // Change the base URI for the sub schemas
        if (schema.getIdKeyword() != null) {
            baseUri = URI.create(schema.getIdKeyword());
        }

        // prefixItems
        if (schema.getPrefixItems() != null) {
            for (Object obj : schema.getPrefixItems()) {
                convertToAbsoluteUri(obj, baseUri);
            }
        }

        // items
        if (schema.getItems() != null) {
            convertToAbsoluteUri(schema.getItems(), baseUri);
        }

        // contains
        if (schema.getContains() != null) {
            convertToAbsoluteUri(schema.getContains(), baseUri);
        }

        // additionalProperties
        if (schema.getAdditionalProperties() != null) {
            convertToAbsoluteUri(schema.getAdditionalProperties(), baseUri);
        }

        // properties
        if (schema.getProperties() != null) {
            for (Map.Entry<String, Object> entry : schema.getProperties().entrySet()) {
                convertToAbsoluteUri(entry.getValue(), baseUri);
            }
        }

        // patternProperties
        if (schema.getPatternProperties() != null) {
            for (Map.Entry<String, Object> entry : schema.getPatternProperties().entrySet()) {
                convertToAbsoluteUri(entry.getValue(), baseUri);
            }
        }

        // dependentSchema
        if (schema.getDependentSchema() != null) {
            convertToAbsoluteUri(schema.getDependentSchema(), baseUri);
        }

        // propertyNames
        if (schema.getPropertyNames() != null) {
            convertToAbsoluteUri(schema.getPropertyNames(), baseUri);
        }

        // if
        if (schema.getIfKeyword() != null) {
            convertToAbsoluteUri(schema.getIfKeyword(), baseUri);
        }

        // then
        if (schema.getThen() != null) {
            convertToAbsoluteUri(schema.getThen(), baseUri);
        }

        // else
        if (schema.getElseKeyword() != null) {
            convertToAbsoluteUri(schema.getElseKeyword(), baseUri);
        }

        // allOf
        if (schema.getAllOf() != null) {
            for (Object obj : schema.getAllOf()) {
                convertToAbsoluteUri(obj, baseUri);
            }
        }

        // anyOf
        if (schema.getAnyOf() != null) {
            for (Object obj : schema.getAnyOf()) {
                convertToAbsoluteUri(obj, baseUri);
            }
        }

        // oneOf
        if (schema.getOneOf() != null) {
            for (Object obj : schema.getOneOf()) {
                convertToAbsoluteUri(obj, baseUri);
            }
        }

        // not
        if (schema.getNot() != null) {
            convertToAbsoluteUri(schema.getNot(), baseUri);
        }

        // content
        if (schema.getContent() != null) {
            convertToAbsoluteUri(schema.getContent(), baseUri);
        }

        // $defs
        if (schema.getDefsKeyword() != null) {
            for (Map.Entry<String, Object> entry : schema.getDefsKeyword().entrySet()) {
                convertToAbsoluteUri(entry.getValue(), baseUri);
            }
        }

        // unevaluatedItems
        if (schema.getUnevaluatedItems() != null) {
            convertToAbsoluteUri(schema.getUnevaluatedItems(), baseUri);
        }

        // unevaluatedProperties
        if (schema.getUnevaluatedProperties() != null) {
            convertToAbsoluteUri(schema.getUnevaluatedProperties(), baseUri);
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
