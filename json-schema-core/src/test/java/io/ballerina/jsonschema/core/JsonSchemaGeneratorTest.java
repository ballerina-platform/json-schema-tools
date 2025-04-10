package io.ballerina.jsonschema.core;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonSchemaGeneratorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String JSON_SCHEMA_DIR = "jsonschema";
    private static final String EXPECTED_DIR = "expected";

    @DataProvider(name = "jsonSchemaProvider")
    public Object[][] provideTestPaths() {
        return new Object[][]{{"1_simple_int_schema.json", "1_simple_int_schema.bal"},
                {"2_constrained_int_schema.json", "2_constrained_int_schema.bal"}};
    }

    @Test(dataProvider = "jsonSchemaProvider")
    public void testJsonSchemaToRecord(String jsonFilePath, String balFilePath) throws Exception {
        validate(RES_DIR.resolve(JSON_SCHEMA_DIR).resolve(jsonFilePath),
                RES_DIR.resolve(EXPECTED_DIR).resolve(balFilePath), new Generator());
    }

    private void validate(Path sample, Path expected, Generator generator) throws Exception {
        String jsonSchemaFileContent = Files.readString(sample);
        Object schema = SchemaUtils.parseJsonSchema(jsonSchemaFileContent);
        Response result = generator.convertBaseSchema(schema);
        Assert.assertTrue(result.getDiagnostics().isEmpty(), "Diagnostics should be empty");
        String expectedValue = Files.readString(expected);
        Assert.assertEquals(result.getTypes(), expectedValue, "Generated types do not match expected output");
    }
}
