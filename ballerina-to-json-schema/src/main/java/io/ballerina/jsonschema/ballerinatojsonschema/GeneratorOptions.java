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

import java.net.URI;
import java.util.Optional;

/** Options for root schema generation. */
public final class GeneratorOptions {
    private final String schemaUri; // $schema
    private final String title;
    private final URI id; // $id

    private GeneratorOptions(Builder builder) {
        this.schemaUri = builder.schemaUri;
        this.title = builder.title;
        this.id = builder.id;
    }

    public Optional<String> schemaUri() {
        return Optional.ofNullable(schemaUri);
    }

    public Optional<String> title() {
        return Optional.ofNullable(title);
    }
    public Optional<URI> id() {
        return Optional.ofNullable(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String schemaUri = JsonSchemaConstants.DRAFT_2020_12_SCHEMA;
        private String title;
        private URI id;

        public Builder schemaUri(String uri) {
            this.schemaUri = uri; return this;
        }

        public Builder title(String title) {
            this.title = title; return this;
        }

        public Builder id(URI id) {
            this.id = id; return this;
        }

        public GeneratorOptions build() {
            return new GeneratorOptions(this);
        }
    }
}
