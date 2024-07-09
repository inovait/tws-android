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

// We cannot use common buildSrc in this project, since buildSrc depends on this project.
// So we use copied configuration from there

// https://youtrack.jetbrains.com/issue/KTIJ-19369
@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
   id("java-library")
   alias(libs.plugins.kotlin.jvm)
   id("java-gradle-plugin")
   signing
   id("maven-publish")
}

group = "si.inova.tws"
version = File(rootDir, "../version.txt").readText().trim()

kotlin {
   jvmToolchain(17)
}

if (properties.containsKey("ossrhUsername")) {
   signing {
      sign(publishing.publications)
   }

   // Workaround for the https://github.com/gradle/gradle/issues/26091
   tasks.withType<AbstractPublishToMaven>().configureEach {
      val signingTasks = tasks.withType<Sign>()
      mustRunAfter(signingTasks)
   }

   publishing {
      repositories {
         maven {
            val repositoryId = property("ossrhRepId") ?: error("Missing property: ossrhRepId")
            setUrl("https://oss.sonatype.org/service/local/staging/deployByRepositoryId/$repositoryId/")
            credentials {
               username = property("ossrhUsername") as String
               password = property("ossrhPassword") as String
            }
         }
      }
   }
}

java {
   withJavadocJar()
   withSourcesJar()

   sourceCompatibility = JavaVersion.VERSION_17
   targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
   api(gradleApi())

   implementation(libs.tomlj)

   // Declare those dependencies as compile only to ensure they do not leak to consumers that do not need them
   compileOnly(libs.android.agp)
}
