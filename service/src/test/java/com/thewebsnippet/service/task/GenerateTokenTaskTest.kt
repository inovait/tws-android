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

        val generatedFile = File(outputDir.asFile, "values/si_inova_tws_service_mappingfield.xml")
        assert(generatedFile.exists())

        val content = generatedFile.readText()
        assert(content.contains("<string name=\"com.thewebsnippet.service.jwt\""))
        assert(
            content.contains("eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMy1wcml2YXRlX2tleV9pZC0xMjMifQ.eyJleHAiOjk5OTk5OTk5OTk5OTksImlzcyI6InRlc3QiLCJjbGllbnRfaWQiOiJ0ZXN0In0.Li1S4QA3Xvrd5y51i7GwRax8qZSZREv7WGlQYmaDRgOZf61oKKzsDGz1d8Ve5HYOhBxOR_s1qGsolwKG_j_Y2zxqW9GsLxBin2rJ0HMEEF4Sm0Af-LtyOG6Cn_kjA8mHYqft2v7W3Byeja4SBYeXgsz8VpTmnVGlhUpXZhgaxPQ"),
        )
    }
}
