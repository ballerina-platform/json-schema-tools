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

package io.ballerina.jsonschema.ballerinatojsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Small wrapper around an ObjectNode for schema composition. */
public final class JsonSchemaNode {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectNode node;

    private JsonSchemaNode(ObjectNode node) {
        this.node = node;
    }

    public static JsonSchemaNode empty() {
        return new JsonSchemaNode(objectMapper.createObjectNode());
    }

    public JsonSchemaNode put(String field, String value) {
        node.put(field, value);
        return this;
    }

    public ObjectNode toObjectNode() {
        return node;
    }

    public String toPrettyString() {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static ObjectMapper mapper() {
        return objectMapper;
    }

    public static JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
