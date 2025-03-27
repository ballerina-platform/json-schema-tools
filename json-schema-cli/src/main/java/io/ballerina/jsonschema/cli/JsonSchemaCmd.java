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

package io.ballerina.jsonschema.cli;

import io.ballerina.projects.util.ProjectUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import io.ballerina.cli.BLauncherCmd;

/**
 * Main class to implement "json-schema" command for ballerina.
 */
@CommandLine.Command(name = "jsonschema", description = "Generates Ballerina types for JSON-schema specification")
public class JsonSchemaCmd implements BLauncherCmd {
    private static final String CMD_NAME = "json-schema";
    private static final String FILE_OVERWRITE_PROMPT = "The file '%s' already exists at %s. Overwrite? (y/N): ";
    public static final String INVALID_BALLERINA_DIRECTORY_ERROR = "Invalid Ballerina package directory: %s, cannot find 'Ballerina.toml' file";
    public static final String INVALID_DIRECTORY_PATH = "Error: Invalid directory path has been provided. "
            + "Output path '%s' is a file";

    public static final String TYPES_FILE_NAME = "types.bal";
    private final PrintStream outStream;
    private final boolean exitWhenFinish;
    @CommandLine.Option(names = { "-h", "--help" }, hidden = true)
    private boolean helpFlag;

    public JsonSchemaCmd() {
        this.outStream = System.err;
        this.exitWhenFinish = true;
    }

    @Override
    public void execute() {
        // if (this.helpFlag) {
        // StringBuilder stringBuilder = new StringBuilder();
        // printLongDesc(stringBuilder);
        // outStream.println(stringBuilder);
        // return;
        // }
        // Path currentDir = Paths.get("").toAbsolutePath();

        // if (!ProjectUtils.isBallerinaProject(currentDir)) {
        // outStream.printf(INVALID_BALLERINA_DIRECTORY_ERROR + "%n", currentDir);
        // exitOnError();
        // return;
        // }

        outStream.println("HELLO WORLD");

        // ! YOU ARE WORKING HERE!!!!
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder outStream) {
        Class<?> clazz = JsonSchemaCmd.class;
        ClassLoader classLoader = clazz.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("cli-docs/json-schema-help.help"); // TODO: what are
                                                                                                     // these
                                                                                                     // references?
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
        }
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void exitOnError() {
        if (exitWhenFinish) {
            Runtime.getRuntime().exit(1);
        }
    }
}
