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
                {"2_constrained_int_schema.json", "2_constrained_int_schema.bal"},
                {"3_true_schema.json", "3_true_schema.bal"},
                {"4_false_schema.json", "4_false_schema.bal"},
                {"5_invalid_int_schema.json", "5_invalid_int_schema.bal"},
                {"6_constrained_number_schema.json", "6_constrained_number_schema.bal"},
                {"7_enum_with_string_schema.json", "7_enum_with_string_schema.bal"},
                {"8_enum_with_integer_schema.json", "8_enum_with_integer_schema.bal"},
                {"9_null_schema.json", "9_null_schema.bal"},
                {"10_nested_enum_schema.json", "10_nested_enum_schema.bal"},
                {"11_nested_enum_with_type_schema.json", "11_nested_enum_with_type_schema.bal"},
                {"12_nested_enum_with_multiple_types.json", "12_nested_enum_with_multiple_types.bal"},
                {"13_enum_with_number_type.json", "13_enum_with_number_type.bal"},
                {"14_enum_with_const.json", "14_enum_with_const.bal"},
                {"15_invalid_enum_with_const.json", "15_invalid_enum_with_const.bal"}
        };
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
