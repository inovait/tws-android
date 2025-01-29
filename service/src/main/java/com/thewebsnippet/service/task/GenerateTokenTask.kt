/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thewebsnippet.service.task

import com.google.gson.Gson
import com.thewebsnippet.service.data.ServiceAccount
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * A Gradle task that generates a JWT (JSON Web Token) based on the contents of a service account JSON file.
 * The JWT is then written to an XML file in a specified output directory.
 *
 * - The task reads input files (`tws-service.json`) to find the service account configuration.
 * - It generates a JWT using the `generateJWT` function.
 * - The token is saved as a string resource in an XML file for further use in the project.
 *
 * The task uses Gradle's @CacheableTask annotation to enable build cache, avoiding re-execution
 * if the inputs and outputs have not changed.
 *
 * Properties:
 * - `outputDirectory`: Directory where the generated XML file will be saved.
 * - `inputFiles`: List of input JSON files to locate the service account configuration.
 *
 * Task Action:
 * - Reads the first valid service account JSON file from the input list.
 * - Parses the JSON into a `ServiceAccount` object.
 * - Generates a JWT using the service account's private key.
 * - Writes the token into an XML file in the `values` subdirectory of the specified output directory.
 */
@CacheableTask
internal abstract class GenerateTokenTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFiles
    abstract val inputFiles: ListProperty<RegularFile>

    @TaskAction
    fun taskAction() {
        val specifiedFile = inputFiles.get().map { it.asFile }.firstOrNull {
            it.exists()
        }

        if (specifiedFile == null) {
            println("ERROR: No valid tws-service.json in: ${inputFiles.get()}")
            return
        }

        val jsonContent = specifiedFile.readText()

        val service = Gson().fromJson(jsonContent, ServiceAccount::class.java)
        val token = generateJWT(service)

        val twsDir = outputDirectory.dir("values").get().asFile

        if (!twsDir.exists()) {
            twsDir.mkdirs()
        }

        val valuesFile = File(twsDir, FILE_NAME)
        writeTokenToXml(valuesFile, token, service.baseUrl)
    }

    @Suppress("StringTemplateIndent") // for not complex XML we can just write directly in file
    private fun writeTokenToXml(file: File, token: String, baseUrl: String) {
        val content = """
            <?xml version="1.0" encoding="utf-8"?>
            <resources xmlns:tools="http://schemas.android.com/tools">
                <string name="com.thewebsnippet.service.jwt" tools:ignore="UnusedResources,TypographyDashes" translatable="false">
                    $token
                </string>
                <string name="com.thewebsnippet.service.base.url" tools:ignore="UnusedResources,TypographyDashes" translatable="false">
                    $baseUrl
                </string>
            </resources>
        """.trimIndent()

        file.writeText(content)
    }

    internal companion object {
        private const val FILE_NAME = "com_thewebsnippet_service_mappingfield.xml"
    }
}
