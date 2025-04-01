/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
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

import io.ballerina.jsonschema.core.diagnostic.JsonSchemaDiagnostic;

import java.util.ArrayList;
import java.util.List;

public class Response {
    private String types;
    private List<JsonSchemaDiagnostic> diagnostics;

    Response(String type, List<JsonSchemaDiagnostic> diagnostics) {
        this.types = type;
        this.diagnostics = diagnostics;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public List<JsonSchemaDiagnostic> getDiagnostics() {
        return new ArrayList<>(diagnostics);
    }

    public void setDiagnostics(List<JsonSchemaDiagnostic> diagnostics) {
        this.diagnostics = new ArrayList<>(diagnostics);
    }
}
