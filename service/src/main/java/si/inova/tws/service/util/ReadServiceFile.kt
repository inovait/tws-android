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

package si.inova.tws.service.util

import com.google.gson.Gson
import si.inova.tws.service.data.ServiceAccount
import java.io.File

/**
 * Reads a service account configuration file based on the project directory and build type.
 *
 * This function attempts to locate the configuration file in the following order:
 * 1. A build-type-specific file at "src/{buildType}/tws-service.json".
 * 2. A default file at "tws-service.json" in the project directory.
 *
 * If neither file exists, it throws an IllegalStateException. Once the file is located,
 * it reads the JSON content and deserializes it into a `ServiceAccount` object using Gson.
 *
 * Example:
 * Given the following project structure:
 *
 * app/
 * ├── tws-services.json                    // Default file
 * ├── src/dogfood/tws-services.json        // Build-type-specific file for "dogfood"
 * ├── src/release/tws-services.json        // Build-type-specific file for "release"
 *
 * For buildType = "dogfood", the function will read "src/dogfood/tws-services.json".
 * For buildType = "release", the function will read "src/release/tws-services.json".
 * If buildType = null or if no build-type-specific file exists, it will fall back to "tws-services.json".
 *
 * @param projectDir The root directory of the project.
 * @param buildType The build type (e.g., "dogfood" or "release"). Can be null.
 * @return A `ServiceAccount` object deserialized from the configuration file.
 * @throws IllegalStateException If no valid configuration file is found.
 */
internal fun readServiceFile(projectDir: File, buildType: String?): ServiceAccount {
    val specificFile = File(projectDir, "src/$buildType/tws-service.json")
    val defaultFile = File(projectDir, "tws-service.json")

    val file = if (specificFile.exists()) specificFile else defaultFile

    if (!file.exists()) {
        throw IllegalStateException("No valid tws-services.json file found for build type: $buildType")
    }

    val jsonContent = file.readText()
    return Gson().fromJson(jsonContent, ServiceAccount::class.java)
}
