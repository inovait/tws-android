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

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import java.io.File

class GenerateTokenTaskTest {

    @Test
    fun `GenerateTokenTask should create a valid XML file with the JWT token`() {
        val projectDir = File("src/test").apply { mkdirs() }

        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        val task = project.tasks.create("generateToken", GenerateTokenTask::class.java)

        val outputDir = project.layout.projectDirectory.file("output")

        task.inputFiles.add(project.layout.projectDirectory.file("tws-service.json"))
        task.outputDirectory.set(outputDir.asFile)

        task.taskAction()

        println("dsdsd: uwu")

        val generatedFile = File(outputDir.asFile, "values/com_thewebsnippet_service_mappingfield.xml")
        assert(generatedFile.exists())

        val content = generatedFile.readText()
        assert(content.contains("<string name=\"com.thewebsnippet.service.jwt\""))
        assert(
            content.contains(
                "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMy1wcml2YXRlX2tleV9pZC0xMjMifQ." +
                    "eyJleHAiOjk5OTk5OTk5OTk5OTksImlzcyI6InRlc3QiLCJjbGllbnRfaWQiOiJ0ZXN0In0." +
                    "Li1S4QA3Xvrd5y51i7GwRax8qZSZREv7WGlQYmaDRgOZf61oKKzsDGz1d8Ve5HYOhBxOR_s1qGsolwKG_j" +
                    "_Y2zxqW9GsLxBin2rJ0HMEEF4Sm0Af-LtyOG6Cn_kjA8mHYqft2v7W3Byeja4SBYeXgsz8VpTmnVGlhUpXZhgaxPQ"
            )
        )
    }
}
