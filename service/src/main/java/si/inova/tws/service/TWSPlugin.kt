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

package si.inova.tws.service

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register
import si.inova.tws.service.task.GenerateTokenTask

/**
 * TWSPlugin is a Gradle plugin that automates the generation of tokens for Android application variants.
 *
 * The plugin performs the following steps:
 * 1. Retrieves the `ApplicationAndroidComponentsExtension` to interact with Android build variants.
 * 2. Iterates over all Android build variants.
 * 3. For each variant, it:
 *    - Registers a task (`GenerateTokenTask`) dynamically named based on the variant name.
 *    - Configures the task to take specific input files, including:
 *      - A variant-specific configuration file (e.g., `src/<variant-name>/tws-service.json`).
 *      - A fallback configuration file (`tws-service.json`) located at the project root.
 *    - Adds the task's output directory as a generated source directory to the variant's resources.
 *
 */
class TWSPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val androidComponents =
            project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            variant.sources.res?.let {
                val resCreationTask =
                    project.tasks.register<GenerateTokenTask>("create${variant.name.capitalized()}$TASK_NAME")

                resCreationTask.configure {
                    inputFiles.add(project.layout.projectDirectory.file("src/${variant.name}/$FILE_NAME"))
                    inputFiles.add(project.layout.projectDirectory.file(FILE_NAME))
                }

                it.addGeneratedSourceDirectory(
                    resCreationTask,
                    GenerateTokenTask::outputDirectory
                )
            }
        }
    }

    companion object {
        private const val TASK_NAME = "TWSTokenGenerator"
        private const val FILE_NAME = "tws-service.json"
    }
}
