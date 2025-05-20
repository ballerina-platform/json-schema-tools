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
import java.util.ArrayList;
import java.util.Arrays;
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
            // TODO: validate the anchor expression
            URI uri = URI.create("#" + schema.getAnchorKeyword());
            if (idToSchemaMap.containsKey(uri)) {
                throw new RuntimeException("Schema anchor \"" + schema.getAnchorKeyword() + "\" is not unique");
            }
            idToSchemaMap.put(baseUri.resolve(uri), schema);
        }
        if (schema.getDynamicAnchorKeyword() != null) {
            // TODO: validate the anchor expression
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
        if (!schema.getAllOf().isEmpty()) {
            for (Object obj : schema.getAllOf()) {
                fetchSchemaId(obj, baseUri, idToSchemaMap);
            }
        }

        // anyOf
        if (!schema.getAnyOf().isEmpty()) {
            for (Object obj : schema.getAnyOf()) {
                fetchSchemaId(obj, baseUri, idToSchemaMap);
            }
        }

        // oneOf
        if (!schema.getOneOf().isEmpty()) {
            for (Object obj : schema.getOneOf()) {
                fetchSchemaId(obj, baseUri, idToSchemaMap);
            }
        }

        // not
        if (schema.getNot() != null) {
            fetchSchemaId(schema.getNot(), baseUri, idToSchemaMap);
        }

        // content
        if (schema.getContentSchema() != null) {
            fetchSchemaId(schema.getContentSchema(), baseUri, idToSchemaMap);
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
        String refKeyword = schema.getRefKeyword();
        if (refKeyword != null) {
            if (refKeyword.equals("#")) {
                schema.setRefKeyword(baseUri.toString());
            } else {
                URI uri = URI.create(schema.getRefKeyword());
                schema.setRefKeyword(baseUri.resolve(uri).toString());
            }
        }

        String dynamicRefKeyword = schema.getDynamicRefKeyword();
        if (dynamicRefKeyword != null) {
            if (dynamicRefKeyword.equals("#")) {
                schema.setDynamicRefKeyword(baseUri.toString());
            } else {
                URI uri = URI.create(schema.getDynamicRefKeyword());
                schema.setDynamicRefKeyword(baseUri.resolve(uri).toString());
            }
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
        if (!schema.getAllOf().isEmpty()) {
            for (Object obj : schema.getAllOf()) {
                convertToAbsoluteUri(obj, baseUri);
            }
        }

        // anyOf
        if (!schema.getAnyOf().isEmpty()) {
            for (Object obj : schema.getAnyOf()) {
                convertToAbsoluteUri(obj, baseUri);
            }
        }

        // oneOf
        if (!schema.getOneOf().isEmpty()) {
            for (Object obj : schema.getOneOf()) {
                convertToAbsoluteUri(obj, baseUri);
            }
        }

        // not
        if (schema.getNot() != null) {
            convertToAbsoluteUri(schema.getNot(), baseUri);
        }

        // content
        if (schema.getContentSchema() != null) {
            convertToAbsoluteUri(schema.getContentSchema(), baseUri);
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

    public static Object getSchemaById(Map<URI, Schema> idToSchemaMap, String uri) throws Exception {
        URI id = URI.create(uri);
        if (idToSchemaMap.containsKey(id)) {
            return idToSchemaMap.get(id);
        }

        URI nearestUri = URI.create("");

        for (URI key : idToSchemaMap.keySet()) {
            if (uri.startsWith(key.toString()) && key.toString().length() > nearestUri.toString().length()) {
                nearestUri = key;
            }
        }

        if (nearestUri.equals(URI.create(""))) {
            throw new RuntimeException("No matching schema found for " + uri);
        }

        Schema schema = idToSchemaMap.get(nearestUri);

        String basePath = nearestUri.toString();
        String relativePath = uri.substring(basePath.length());

        if (!relativePath.startsWith("#/")) {
            throw new RuntimeException("Invalid path: " + basePath);
        }

        relativePath = relativePath.substring(2);
        ArrayList<String> pathList = new ArrayList<>(Arrays.asList(relativePath.split("/")));

        return getSchemaByKeyword(schema, pathList);
    }

    public static Object getSchemaByKeyword(Object schemaObject, ArrayList<String> pathList) throws Exception {
        if (pathList.isEmpty()) {
            if (schemaObject instanceof Schema || schemaObject instanceof Boolean) {
                return schemaObject;
            }
            throw new RuntimeException("Path does not refer to a schema");
        }

        if (!(schemaObject instanceof Schema schema)) {
            throw new RuntimeException("Invalid path: " + String.join("/", pathList));
        }

        String nextPath = pathList.removeFirst();

        try {
            switch (nextPath) {
                case "prefixItems" -> {
                    return fetchSchemaForList(schema.getPrefixItems(), pathList);
                }
                case "items" -> {
                    return getSchemaByKeyword(schema.getItems(), pathList);
                }
                case "contains" -> {
                    return getSchemaByKeyword(schema.getContains(), pathList);
                }
                case "additionalProperties" -> {
                    return getSchemaByKeyword(schema.getAdditionalProperties(), pathList);
                }
                case "properties" -> {
                    return fetchSchemaForMap(schema.getProperties(), pathList);
                }
                case "patternProperties" -> {
                    return fetchSchemaForMap(schema.getPatternProperties(), pathList);
                }
                case "dependentSchema" -> {
                    return fetchSchemaForMap(schema.getDependentSchema(), pathList);
                }
                case "propertyNames" -> {
                    return getSchemaByKeyword(schema.getPropertyNames(), pathList);
                }
                case "if" -> {
                    return getSchemaByKeyword(schema.getIfKeyword(), pathList);
                }
                case "then" -> {
                    return getSchemaByKeyword(schema.getThen(), pathList);
                }
                case "else" -> {
                    return getSchemaByKeyword(schema.getElseKeyword(), pathList);
                }
                case "allOf" -> {
                    return fetchSchemaForList(schema.getAllOf(), pathList);
                }
                case "oneOf" -> {
                    return fetchSchemaForList(schema.getOneOf(), pathList);
                }
                case "anyOf" -> {
                    return fetchSchemaForList(schema.getAnyOf(), pathList);
                }
                case "not" -> {
                    return getSchemaByKeyword(schema.getNot(), pathList);
                }
                case "contentSchema" -> {
                    return getSchemaByKeyword(schema.getContentSchema(), pathList);
                }
                case "$defs" -> {
                    return fetchSchemaForMap(schema.getDefsKeyword(), pathList);
                }
                case "unevaluatedItems" -> {
                    return getSchemaByKeyword(schema.getUnevaluatedItems(), pathList);
                }
                case "unevaluatedProperties" -> {
                    return getSchemaByKeyword(schema.getUnevaluatedProperties(), pathList);
                }
                default -> throw new RuntimeException("Invalid path: " + String.join("/", pathList));
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid path: " + String.join("/", pathList));
        }
        // TODO: Implement for undefined keywords.
    }

    public static Object fetchSchemaForList(Object objectList, ArrayList<String> pathList) throws Exception {
        List<Object> itemList = (List<Object>) objectList;
        String key = pathList.removeFirst();
        long index = Long.parseLong(key);
        return getSchemaByKeyword(itemList.get((int) index), pathList);
    }

    public static Object fetchSchemaForMap(Object objectMap, ArrayList<String> pathList) throws Exception {
        Map<String, Object> map = (Map<String, Object>) objectMap;
        String key = pathList.removeFirst();
        return getSchemaByKeyword(map.get(key), pathList);
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
