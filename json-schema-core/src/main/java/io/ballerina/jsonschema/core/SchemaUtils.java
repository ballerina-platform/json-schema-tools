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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaUtils {
    private static final String DRAFT2020_12 = "https://json-schema.org/draft/2020-12/schema";

    private static final List<String> SUPPORTED_DRAFTS = List.of(DRAFT2020_12);

    // Reference Ballerina types through schema ID.
    static final Map<String, String> ID_TO_TYPE_MAP = new HashMap<>();

    public static Object parseJsonSchema(String jsonString) throws Exception {
        Object schema = null;

        Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
        jsonString = jsonString.trim();

        if (jsonString.isEmpty()) {
            throw new Exception("JSON schema is empty");
        }

        if (jsonString.equals("true")) {
            schema = Boolean.TRUE;
        } else if (jsonString.equals("false")) {
            schema = Boolean.FALSE;
        } else if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
            schema = gson.fromJson(jsonString, Schema.class);

            if (((Schema) schema).getSchemaKeyword() == null ||
                    !SUPPORTED_DRAFTS.contains(((Schema) schema).getSchemaKeyword())) {
                throw new RuntimeException("Schema draft not supported");
            }
        } else {
            throw new Exception("JSON schema is not valid");
        }
        return schema;
    }
}
