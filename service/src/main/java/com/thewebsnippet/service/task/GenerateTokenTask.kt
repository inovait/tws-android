/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
 *
 * @throws IllegalStateException If no valid `tws-service.json` file is found.
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
        writeTokenToXml(valuesFile, token)
    }

    @Suppress("StringTemplateIndent") // for not complex XML we can just write directly in file
    private fun writeTokenToXml(file: File, token: String) {
        val content = """
            <?xml version="1.0" encoding="utf-8"?>
            <resources xmlns:tools="http://schemas.android.com/tools">
                <string name="com.thewebsnippet.service.jwt" tools:ignore="UnusedResources,TypographyDashes" translatable="false">
                    $token
                </string>
            </resources>
        """.trimIndent()

        file.writeText(content)
    }

    internal companion object {
        private const val FILE_NAME = "si_inova_tws_service_mappingfield.xml"
    }
}
