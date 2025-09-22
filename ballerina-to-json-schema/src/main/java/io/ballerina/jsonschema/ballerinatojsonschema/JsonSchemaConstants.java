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

public final class JsonSchemaConstants {
    private JsonSchemaConstants() {}

    // Official 2020-12 meta-schema (dialect)
    public static final String DRAFT_2020_12_SCHEMA = "https://json-schema.org/draft/2020-12/schema";

    // Common keys
    public static final String K_SCHEMA = "$schema";
    public static final String K_ID = "$id";
    public static final String K_TITLE = "title";
    public static final String K_TYPE = "type";

    // JSON Schema primitive names (2020-12)
    public static final String T_OBJECT  = "object";
    public static final String T_ARRAY   = "array";
    public static final String T_STRING  = "string";
    public static final String T_BOOLEAN = "boolean";
    public static final String T_INTEGER = "integer";
    public static final String T_NUMBER  = "number";
    public static final String T_NULL    = "null";
}
