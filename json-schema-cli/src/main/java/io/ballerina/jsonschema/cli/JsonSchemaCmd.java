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

package io.ballerina.jsonschema.cli;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.jsonschema.core.Generator;
import io.ballerina.jsonschema.core.Response;
import io.ballerina.jsonschema.core.SchemaUtils;
import io.ballerina.projects.util.ProjectUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

/**
 * Main class to implement "json-schema" command for Ballerina.
 *
 * @since 0.1.0
 */
@CommandLine.Command(name = "json-schema", description = "Generates Ballerina types for JSON-schema specification")
public class JsonSchemaCmd implements BLauncherCmd {
    private static final String CMD_NAME = "json-schema";

    private static final String INVALID_BALLERINA_DIRECTORY =
            "Invalid Ballerina package directory: %s, cannot find 'Ballerina.toml' file.";
    private static final String INVALID_DIRECTORY_PATH = "Error: Invalid directory path has been provided. "
            + "Output path '%s' is a file";
    private static final String OUTPUT_FILE_NAME = "types.bal";
    private static final String FILE_OVERWRITE_PROMPT = "The file '%s' already exists at %s. Overwrite? (Y/N): ";
    private static final String AUTO_GENERATED_MESSAGE = """
            // AUTO-GENERATED FILE. DO NOT MODIFY.
            // This file is auto-generated by the Ballerina json-schema tool.""";
    private static final String EMPTY_STRING = "";
    private static final String MODULES = "modules";

    private final PrintStream outStream;
    private final boolean exitOnError;

    public JsonSchemaCmd() {
        this.outStream = System.err;
        this.exitOnError = true;
    }

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean helpFlag;

    @CommandLine.Parameters(description = "Input file path of the JSON schema", arity = "0..1")
    private String inputPath = "";

    @CommandLine.Option(names = {"-m", "--module"}, description = "The name of the module in which the Ballerina " +
            "record types are generated.")
    private String outputPath = "";

    @Override
    public void execute() {
        if (this.helpFlag) {
            StringBuilder stringBuilder = new StringBuilder();
            printLongDesc(stringBuilder);
            outStream.println(stringBuilder);
            return;
        }
        if (this.inputPath == null || this.inputPath.isEmpty()) {
            outStream.println("A JSON schema file path is required to generate the types");
            outStream.println("e.g., $ bal json-schema <json schema source file path>");
            exitOnError();
            return;
        }
        Path currentDir = Paths.get("").toAbsolutePath();
        if (!ProjectUtils.isBallerinaProject(currentDir)) {
            outStream.printf(INVALID_BALLERINA_DIRECTORY + "%n", currentDir);
            exitOnError();
            return;
        }
        if (!ProjectUtils.validateModuleName(outputPath)) {
            outStream.println("ERROR: invalid module name : '" + outputPath + "' :\n" +
                    "module name can only contain alphanumerics, underscores and periods");
            exitOnError();
            return;
        }
        if (!ProjectUtils.validateNameLength(outputPath)) {
            outStream.println("ERROR: invalid module name : '" + outputPath + "' :\n" +
                    "maximum length of module name is 256 characters");
            exitOnError();
            return;
        }
        Path outputDirPath = Paths.get(outputPath).toAbsolutePath();
        if (!Objects.equals(outputPath, EMPTY_STRING)) {
            Path basePath = Paths.get("modules").toAbsolutePath();
            outputDirPath = basePath.resolve(outputPath).normalize();
        }
        if (Files.exists(outputDirPath) && !Files.isDirectory(outputDirPath)) {
            outStream.printf(INVALID_DIRECTORY_PATH + "%n", outputPath);
            exitOnError();
            return;
        }
        try {
            if (!Files.isDirectory(Path.of(inputPath))) {
                handleSingleFile(outputDirPath, inputPath);
            } else {
                //TODO: Implement for Directories
                outStream.println("Creating Ballerina types for multiple schema files is not yet supported");
            }
        } catch (IOException e) {
            outStream.println("Error occurred while accessing the file. " + e.getLocalizedMessage());
        } catch (SchemaUtils.InvalidJsonSchemaException | SchemaUtils.EmptyJsonSchemaException e) {
            outStream.println("Error: " + e.getLocalizedMessage());
        } catch (Exception e) {
            outStream.println("Error: " + e.getLocalizedMessage());
            exitOnError();
        }
    }

    private void handleSingleFile(Path outputDirPath, String fileName) throws Exception {
        if (Files.notExists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
        }
        Path filePath = Path.of(fileName);
        if (!Files.exists(filePath)) {
            outStream.println(fileName + " file does not exist.");
            exitOnError();
            return;
        }
        if (!fileName.endsWith(".json")) {
            outStream.println("The provided file is not a JSON file. Please provide a valid JSON file.");
            exitOnError();
            return;
        }
        String jsonFileContent = Files.readString(filePath);
        Object schema = SchemaUtils.parseJsonSchema(jsonFileContent);

        Generator generator = new Generator();
        Response result = generator.convertBaseSchema(schema);
        if (!result.getDiagnostics().isEmpty()) {
            result.getDiagnostics().forEach(jsonSchemaDiagnostic ->
                    outStream.println(jsonSchemaDiagnostic.toString()));
            exitOnError();
            return;
        }
        writeSourceToFiles(outputDirPath, result, OUTPUT_FILE_NAME);
    }

    private void writeSourceToFiles(Path outputPath, Response response, String outputName) throws IOException {
        Path clientPath = outputPath.resolve(outputName);
        String fileName = clientPath.getFileName().toString();
        if (Files.exists(clientPath)) {
            outStream.printf(FILE_OVERWRITE_PROMPT, fileName, getModuleName(clientPath));
            String overwriteAccess = new Scanner(System.in).nextLine().trim().toLowerCase();
            if (overwriteAccess.equals("y")) {
                generateFile(response.getTypes(), clientPath, fileName);
            } else {
                outStream.printf("The operation is cancelled %n");
            }
        } else {
            generateFile(response.getTypes(), clientPath, fileName);
        }
    }

    private static String getModuleName(Path clientPath) {
        String outputModule;
        String destinationPath = clientPath.toString();
        if (destinationPath.contains(MODULES)) {
            int startIndex = destinationPath.indexOf(MODULES);
            int endIndex = destinationPath.lastIndexOf("/");
            if (endIndex > startIndex) {
                outputModule = destinationPath.substring(startIndex, endIndex);
            } else {
                outputModule = destinationPath.substring(startIndex);
            }
        } else {
            outputModule = "the default module";
        }
        return outputModule;
    }

    private void generateFile(String content, Path clientPath, String fileName) throws IOException {
        Files.writeString(clientPath, addAutoGeneratedMessage(content));
        String outputModule = getModuleName(clientPath);
        outStream.printf("The '%s' file is written to %s %n", fileName, outputModule);
    }

    private String addAutoGeneratedMessage(String content) {
        return AUTO_GENERATED_MESSAGE + "\n\n" + content;
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder outStream) {
        Class<?> clazz = JsonSchemaCmd.class;
        ClassLoader classLoader = clazz.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("cli-docs/json-schema-help.help");

        if (inputStream != null) {
            try (InputStreamReader inputStreamREader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(inputStreamREader)) {
                String content = br.readLine();
                outStream.append(content);
                while ((content = br.readLine()) != null) {
                    outStream.append('\n').append(content);
                }
            } catch (IOException e) {
                outStream.append("Helper text is not available.");
            }
        } else {
            outStream.append("Helper text resource not found.");
        }
    }

    @Override
    @Deprecated
    public void printUsage(StringBuilder stringBuilder) {
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void exitOnError() {
        if (exitOnError) {
            Runtime.getRuntime().exit(1);
        }
    }
}
