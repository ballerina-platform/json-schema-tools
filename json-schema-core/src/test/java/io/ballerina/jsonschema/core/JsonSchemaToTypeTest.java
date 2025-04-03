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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testng.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static io.ballerina.jsonschema.core.SchemaUtils.parseJsonSchema;

public class JsonSchemaToTypeTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String JSON_SCHEMA_DIR = "jsonschema";
    private static final String EXPECTED_DIR = "expected";

    private static Stream<Object[]> provideTestPaths() {
        return Stream.of(
                new Object[] {"1_simple_int_schema.json", "1_simple_int_schema.bal"},
                new Object[] {"2_simple_int_schema.json", "2_simple_int_schema.bal"}
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestPaths")
    void testXsdToRecord(String xmlFilePath, String balFilePath) throws Exception {
        validate(RES_DIR.resolve(JSON_SCHEMA_DIR).resolve(xmlFilePath),
        RES_DIR.resolve(EXPECTED_DIR).resolve(balFilePath));
    }

    private void validate(Path sample, Path expected) throws Exception {
        String jsonSchemaFileContent = Files.readString(sample);
        Object schema = parseJsonSchema(jsonSchemaFileContent);
        Response result = JsonSchemaToType.convertBaseSchema(schema);
        Assert.assertTrue(result.getDiagnostics().isEmpty());
        String expectedValue = Files.readString(expected);
        Assert.assertEquals(result.getTypes(), expectedValue);
    }
}
