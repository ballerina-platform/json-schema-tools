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
        return new Object[][]{
                {"1_simple_int.json", "1_simple_int.bal"},
                {"2_constrained_int.json", "2_constrained_int.bal"},
                {"3_true.json", "3_true.bal"},
                {"4_false.json", "4_false.bal"},
                {"5_invalid_int.json", "5_invalid_int.bal"},
                {"6_constrained_number.json", "6_constrained_number.bal"},
                {"7_enum_with_string.json", "7_enum_with_string.bal"},
                {"8_enum_with_integer.json", "8_enum_with_integer.bal"},
                {"9_null.json", "9_null.bal"},
                {"10_nested_enum.json", "10_nested_enum.bal"},
                {"11_nested_enum_with_type.json", "11_nested_enum_with_type.bal"},
                {"12_nested_enum_with_multiple_types.json", "12_nested_enum_with_multiple_types.bal"},
                {"13_enum_with_number_type.json", "13_enum_with_number_type.bal"},
                {"14_enum_with_const.json", "14_enum_with_const.bal"},
                {"15_invalid_enum_with_const.json", "15_invalid_enum_with_const.bal"},
                {"16_object_const.json", "16_object_const.bal"},
                {"17_constrained_string.json", "17_constrained_string.bal"},
//                {"18_no_type.json", "18_no_type.bal"},  // Invalid test until object type support is implemented
                {"19_universal_array.json", "19_universal_array.bal"},
                {"20_array_with_multiple_types.json", "20_array_with_multiple_types.bal"},
                {"21_array_with_constrained_types.json", "21_array_with_constrained_types.bal"},
                {"22_array_with_multiple_elements.json", "22_array_with_multiple_elements.bal"},
                {"23_array_with_multiple_elements.json", "23_array_with_multiple_elements.bal"},
                {"24_array_with_multiple_elements.json", "24_array_with_multiple_elements.bal"},
                {"25_array_with_multiple_elements.json", "25_array_with_multiple_elements.bal"},
                {"26_array_with_multiple_elements.json", "26_array_with_multiple_elements.bal"},
                {"27_array_with_multiple_elements.json", "27_array_with_multiple_elements.bal"},
                {"28_array_with_multiple_elements.json", "28_array_with_multiple_elements.bal"},
                {"29_array_with_multiple_elements.json", "29_array_with_multiple_elements.bal"},
                {"30_array_with_multiple_elements.json", "30_array_with_multiple_elements.bal"},
                {"31_array_with_unique_items.json", "31_array_with_unique_items.bal"},
                {"32_array_with_contains.json", "32_array_with_contains.bal"},
                {"33_array_with_contains.json", "33_array_with_contains.bal"},
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
