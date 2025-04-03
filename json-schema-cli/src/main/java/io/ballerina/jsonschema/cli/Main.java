package io.ballerina.jsonschema.cli;

import io.ballerina.jsonschema.core.JsonSchemaToType;
import io.ballerina.jsonschema.core.Response;
import io.ballerina.jsonschema.core.SchemaUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Load the resource file
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("example.json");

        if (inputStream == null) {
            System.out.println("File not found!");
            return;
        }

        // Read the file as a string
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            String content = scanner.useDelimiter("\\A").next();
            System.out.println("File Content:\n" + content);
            Object schema = SchemaUtils.parseJsonSchema(content);
            Response result = JsonSchemaToType.convertBaseSchema(schema);
            System.out.println("HELLO");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
