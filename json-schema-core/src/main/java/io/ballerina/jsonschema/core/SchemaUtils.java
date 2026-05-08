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
import java.nio.charset.StandardCharsets;
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
        Gson gson = new GsonBuilder()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .registerTypeAdapter(Schema.class, new SchemaDeserializers.SchemaObjectDeserializer())
                .create();
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
            baseUri = resolveSchemaUri(baseUri, schema.getIdKeyword());
            if (idToSchemaMap.containsKey(baseUri)) {
                throw new RuntimeException("Schema id \"" + schema.getIdKeyword() + "\" is not unique");
            }
            idToSchemaMap.put(baseUri, schema);
        }
        if (schema.getAnchorKeyword() != null) {
            // TODO: validate the anchor expression
            URI resolvedUri = resolveSchemaUri(baseUri, "#" + schema.getAnchorKeyword());
            if (idToSchemaMap.containsKey(resolvedUri)) {
                throw new RuntimeException("Schema anchor \"" + schema.getAnchorKeyword() + "\" is not unique");
            }
            idToSchemaMap.put(resolvedUri, schema);
        }
        if (schema.getDynamicAnchorKeyword() != null) {
            // TODO: validate the anchor expression
            URI resolvedUri = resolveSchemaUri(baseUri, "#" + schema.getDynamicAnchorKeyword());
            if (idToSchemaMap.containsKey(resolvedUri)) {
                throw new RuntimeException("Schema anchor \"" + schema.getDynamicAnchorKeyword() + "\" is not unique");
            }
            idToSchemaMap.put(resolvedUri, schema);
        }

        if (!schema.getPrefixItems().isEmpty()) {
            for (Object obj : schema.getPrefixItems()) {
                fetchSchemaId(obj, baseUri, idToSchemaMap);
            }
        }

        if (schema.getItems() != null) {
            fetchSchemaId(schema.getItems(), baseUri, idToSchemaMap);
        }

        if (schema.getContains() != null) {
            fetchSchemaId(schema.getContains(), baseUri, idToSchemaMap);
        }

        if (schema.getAdditionalProperties() != null) {
            fetchSchemaId(schema.getAdditionalProperties(), baseUri, idToSchemaMap);
        }

        if (!schema.getProperties().isEmpty()) {
            for (Map.Entry<String, Object> entry : schema.getProperties().entrySet()) {
                fetchSchemaId(entry.getValue(), baseUri, idToSchemaMap);
            }
        }

        if (!schema.getPatternProperties().isEmpty()) {
            for (Map.Entry<String, Object> entry : schema.getPatternProperties().entrySet()) {
                fetchSchemaId(entry.getValue(), baseUri, idToSchemaMap);
            }
        }

        if (!schema.getDependentSchemas().isEmpty()) {
            fetchSchemaId(schema.getDependentSchemas(), baseUri, idToSchemaMap);
        }

        if (schema.getPropertyNames() != null) {
            fetchSchemaId(schema.getPropertyNames(), baseUri, idToSchemaMap);
        }

        if (schema.getIfKeyword() != null) {
            fetchSchemaId(schema.getIfKeyword(), baseUri, idToSchemaMap);
        }

        if (schema.getThen() != null) {
            fetchSchemaId(schema.getThen(), baseUri, idToSchemaMap);
        }

        if (schema.getElseKeyword() != null) {
            fetchSchemaId(schema.getElseKeyword(), baseUri, idToSchemaMap);
        }

        if (!schema.getAllOf().isEmpty()) {
            for (Object obj : schema.getAllOf()) {
                fetchSchemaId(obj, baseUri, idToSchemaMap);
            }
        }

        if (!schema.getAnyOf().isEmpty()) {
            for (Object obj : schema.getAnyOf()) {
                fetchSchemaId(obj, baseUri, idToSchemaMap);
            }
        }

        if (!schema.getOneOf().isEmpty()) {
            for (Object obj : schema.getOneOf()) {
                fetchSchemaId(obj, baseUri, idToSchemaMap);
            }
        }

        if (schema.getNot() != null) {
            fetchSchemaId(schema.getNot(), baseUri, idToSchemaMap);
        }

        if (schema.getContentSchema() != null) {
            fetchSchemaId(schema.getContentSchema(), baseUri, idToSchemaMap);
        }

        if (!schema.getDefsKeyword().isEmpty()) {
            for (Map.Entry<String, Object> entry : schema.getDefsKeyword().entrySet()) {
                fetchSchemaId(entry.getValue(), baseUri, idToSchemaMap);
            }
        }

        if (schema.getUnevaluatedItems() != null) {
            fetchSchemaId(schema.getUnevaluatedItems(), baseUri, idToSchemaMap);
        }

        if (schema.getUnevaluatedProperties() != null) {
            fetchSchemaId(schema.getUnevaluatedProperties(), baseUri, idToSchemaMap);
        }
    }

    public static void convertToAbsoluteUri(Object schemaObject, URI baseUri) {
        if (!(schemaObject instanceof Schema schema)) {
            return;
        }

        // Change the base URI for the sub schemas
        if (schema.getIdKeyword() != null) {
            baseUri = resolveSchemaUri(baseUri, schema.getIdKeyword());
            schema.setIdKeyword(baseUri.toString());
        }

        // Resolving $ref and $dynamicRef
        String refKeyword = schema.getRefKeyword();
        if (refKeyword != null) {
            if (refKeyword.equals("#")) {
                schema.setRefKeyword(baseUri.toString());
            } else {
                schema.setRefKeyword(resolveSchemaUri(baseUri, schema.getRefKeyword()).toString());
            }
        }

        String dynamicRefKeyword = schema.getDynamicRefKeyword();
        if (dynamicRefKeyword != null) {
            if (dynamicRefKeyword.equals("#")) {
                schema.setDynamicRefKeyword(baseUri.toString());
            } else {
                schema.setDynamicRefKeyword(resolveSchemaUri(baseUri, schema.getDynamicRefKeyword()).toString());
            }
        }

        if (!schema.getPrefixItems().isEmpty()) {
            for (Object obj : schema.getPrefixItems()) {
                convertToAbsoluteUri(obj, baseUri);
            }
        }

        if (schema.getItems() != null) {
            convertToAbsoluteUri(schema.getItems(), baseUri);
        }

        if (schema.getContains() != null) {
            convertToAbsoluteUri(schema.getContains(), baseUri);
        }

        if (schema.getAdditionalProperties() != null) {
            convertToAbsoluteUri(schema.getAdditionalProperties(), baseUri);
        }

        if (!schema.getProperties().isEmpty()) {
            for (Map.Entry<String, Object> entry : schema.getProperties().entrySet()) {
                convertToAbsoluteUri(entry.getValue(), baseUri);
            }
        }

        if (!schema.getPatternProperties().isEmpty()) {
            for (Map.Entry<String, Object> entry : schema.getPatternProperties().entrySet()) {
                convertToAbsoluteUri(entry.getValue(), baseUri);
            }
        }

        if (!schema.getDependentSchemas().isEmpty()) {
            convertToAbsoluteUri(schema.getDependentSchemas(), baseUri);
        }

        if (schema.getPropertyNames() != null) {
            convertToAbsoluteUri(schema.getPropertyNames(), baseUri);
        }

        if (schema.getIfKeyword() != null) {
            convertToAbsoluteUri(schema.getIfKeyword(), baseUri);
        }

        if (schema.getThen() != null) {
            convertToAbsoluteUri(schema.getThen(), baseUri);
        }

        if (schema.getElseKeyword() != null) {
            convertToAbsoluteUri(schema.getElseKeyword(), baseUri);
        }

        if (!schema.getAllOf().isEmpty()) {
            for (Object obj : schema.getAllOf()) {
                convertToAbsoluteUri(obj, baseUri);
            }
        }

        if (!schema.getAnyOf().isEmpty()) {
            for (Object obj : schema.getAnyOf()) {
                convertToAbsoluteUri(obj, baseUri);
            }
        }

        if (!schema.getOneOf().isEmpty()) {
            for (Object obj : schema.getOneOf()) {
                convertToAbsoluteUri(obj, baseUri);
            }
        }

        if (schema.getNot() != null) {
            convertToAbsoluteUri(schema.getNot(), baseUri);
        }

        if (schema.getContentSchema() != null) {
            convertToAbsoluteUri(schema.getContentSchema(), baseUri);
        }

        if (!schema.getDefsKeyword().isEmpty()) {
            for (Map.Entry<String, Object> entry : schema.getDefsKeyword().entrySet()) {
                convertToAbsoluteUri(entry.getValue(), baseUri);
            }
        }

        if (schema.getUnevaluatedItems() != null) {
            convertToAbsoluteUri(schema.getUnevaluatedItems(), baseUri);
        }

        if (schema.getUnevaluatedProperties() != null) {
            convertToAbsoluteUri(schema.getUnevaluatedProperties(), baseUri);
        }
    }

    public static Object getSchemaById(Map<URI, Schema> idToSchemaMap, String uri) throws Exception {
        URI targetUri = URI.create(uri);
        if (idToSchemaMap.containsKey(targetUri)) {
            return idToSchemaMap.get(targetUri);
        }

        URI documentUri = removeFragment(targetUri);
        Schema schema = idToSchemaMap.get(documentUri);
        if (schema == null) {
            throw new RuntimeException("No matching schema found for " + uri);
        }

        String fragment = decodeFragment(targetUri);
        if (fragment == null || fragment.isEmpty()) {
            return schema;
        }
        if (!fragment.startsWith("/")) {
            URI anchorUri = URI.create(documentUri.toString() + "#" + fragment);
            if (idToSchemaMap.containsKey(anchorUri)) {
                return idToSchemaMap.get(anchorUri);
            }
            throw new RuntimeException("No matching schema found for " + uri);
        }

        ArrayList<String> pathList = decodeJsonPointerTokens(fragment.substring(1));
        return getSchemaByKeyword(schema, pathList);
    }

    private static URI resolveSchemaUri(URI baseUri, String reference) {
        URI candidate = URI.create(reference);
        if (candidate.isAbsolute()) {
            return candidate;
        }
        if (baseUri.isOpaque() && reference.startsWith("#")) {
            return URI.create(removeFragment(baseUri).toString() + reference);
        }

        URI resolved = baseUri.resolve(candidate);
        if ("file".equals(baseUri.getScheme()) && baseUri.getAuthority() == null && resolved.getAuthority() == null) {
            String resolvedString = resolved.toString();
            if (resolvedString.startsWith("file:/") && !resolvedString.startsWith("file:///")) {
                return URI.create("file:///" + resolvedString.substring("file:/".length()));
            }
        }
        return resolved;
    }

    private static URI removeFragment(URI uri) {
        if (uri.getRawFragment() == null) {
            return uri;
        }
        return URI.create(uri.toString().substring(0, uri.toString().indexOf('#')));
    }

    private static String decodeFragment(URI uri) {
        String rawFragment = uri.getRawFragment();
        if (rawFragment == null) {
            return null;
        }
        return decodeUriComponent(rawFragment);
    }

    private static ArrayList<String> decodeJsonPointerTokens(String pointerPath) {
        ArrayList<String> pathList = new ArrayList<>(Arrays.asList(pointerPath.split("/")));
        for (int i = 0; i < pathList.size(); i++) {
            pathList.set(i, pathList.get(i).replace("~1", "/").replace("~0", "~"));
        }
        return pathList;
    }

    private static String decodeUriComponent(String value) {
        byte[] buffer = new byte[value.length()];
        int bufferIndex = 0;
        StringBuilder decoded = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current != '%') {
                if (bufferIndex > 0) {
                    decoded.append(new String(buffer, 0, bufferIndex, StandardCharsets.UTF_8));
                    bufferIndex = 0;
                }
                decoded.append(current);
                continue;
            }

            if (i + 2 >= value.length()) {
                throw new IllegalArgumentException("Invalid percent-encoding in URI component: " + value);
            }
            int decodedByte = Integer.parseInt(value.substring(i + 1, i + 3), 16);
            buffer[bufferIndex++] = (byte) decodedByte;
            i += 2;
        }

        if (bufferIndex > 0) {
            decoded.append(new String(buffer, 0, bufferIndex, StandardCharsets.UTF_8));
        }
        return decoded.toString();
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
                case "dependentSchemas" -> {
                    return fetchSchemaForMap(schema.getDependentSchemas(), pathList);
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
