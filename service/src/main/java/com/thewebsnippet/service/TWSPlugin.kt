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
package com.thewebsnippet.service

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.thewebsnippet.service.task.GenerateTokenTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register

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
