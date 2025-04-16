package io.ballerina.jsonschema.core;

public class SchemaExceptions {
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
}
