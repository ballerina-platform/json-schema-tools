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

import com.fasterxml.jackson.databind.node.ObjectNode;

import static io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaConstants.K_ID;
import static io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaConstants.K_SCHEMA;
import static io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaConstants.K_TITLE;

/** Public API: Stage 1 generation. */
public final class JsonSchemaGenerator {

    /** Generate a root JSON Schema document for a basic type. */
    public ObjectNode generateBasicTypeSchema(BallerinaBasicType type, GeneratorOptions options) {
        JsonSchemaNode root = BasicTypeMapper.forBasicType(type);

        // Add $schema (dialect) at the root — 2020-12
        options.schemaUri().ifPresent(uri -> root.toObjectNode().put(K_SCHEMA, uri));
        options.id().ifPresent(id -> root.toObjectNode().put(K_ID, id.toString()));
        options.title().ifPresent(title -> root.toObjectNode().put(K_TITLE, title));

        return root.toObjectNode();
    }
}
