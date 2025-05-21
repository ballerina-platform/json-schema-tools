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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema Deserializers of the Schema class.
 *
 * @since 0.1.0
 */
public class SchemaDeserializers {
    static class ListSchemaDeserializer implements JsonDeserializer<List<Object>> {
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

    static class MapStringSchemaDeserializer implements JsonDeserializer<Map<String, Object>> {
        @Override
        public Map<String, Object> deserialize(JsonElement jsonElement, Type type,
                                               JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Map<String, Object> resultMap = new LinkedHashMap<>();
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

    static class PropertyNameDeserializer implements JsonDeserializer<Object> {
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

    static class SchemaDeserializer implements JsonDeserializer<Object> {
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

    static class TypeDeserializer implements JsonDeserializer<List<String>> {
        @Override
        public List<String> deserialize(JsonElement jsonElement, Type type,
                                             JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
            if (jsonElement.isJsonArray()) {
                return jsonDeserializationContext.deserialize(jsonElement, ArrayList.class);
            }
            if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
                return new ArrayList<>(List.of(jsonElement.getAsString()));
            }
            throw new JsonParseException("Expected a string or an array of strings, but got: " + jsonElement);
        }
    }
}
