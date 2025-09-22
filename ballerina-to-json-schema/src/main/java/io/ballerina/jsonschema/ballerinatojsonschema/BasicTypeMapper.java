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

import static io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaConstants.K_TYPE;
import static io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaConstants.T_BOOLEAN;
import static io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaConstants.T_INTEGER;
import static io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaConstants.T_NULL;
import static io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaConstants.T_NUMBER;
import static io.ballerina.jsonschema.ballerinatojsonschema.JsonSchemaConstants.T_STRING;

public final class BasicTypeMapper {

    private BasicTypeMapper() {}

    /** Returns a schema node representing just the type (no $schema/$id/title). */
    public static JsonSchemaNode forBasicType(BallerinaBasicType ballerinaBasicType) {
        String jsonType = switch (ballerinaBasicType) {
            case INT -> T_INTEGER; // 2020-12: integer vs number
            case FLOAT, DECIMAL -> T_NUMBER; // only "number" exists
            case STRING -> T_STRING;
            case BOOLEAN -> T_BOOLEAN;
            case NULL -> T_NULL;
            default -> throw new IllegalArgumentException("Unsupported: " + ballerinaBasicType);
        };
        return JsonSchemaNode.empty().put(K_TYPE, jsonType);
    }
}
