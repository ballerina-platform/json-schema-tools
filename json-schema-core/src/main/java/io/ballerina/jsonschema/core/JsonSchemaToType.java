package io.ballerina.jsonschema.core;

import io.ballerina.jsonschema.core.diagnostic.JsonSchemaDiagnostic;

import java.util.ArrayList;
import java.util.List;

public class JsonSchemaToType {
    private static final List<JsonSchemaDiagnostic> diagnostics = new ArrayList<>();

    public static Response convert(Object jsonSchema) {
        return new Response("public type Schema int;", diagnostics);
    }
}
