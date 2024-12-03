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

plugins {
    `java-gradle-plugin`
    `maven-publish`
    `kotlin-dsl`
}

group = "com.thewebsnippet"
version = File(rootDir, "version.txt").readText().trim()

publishing {
    publications {
        create<MavenPublication>("gradlePlugin") {
            groupId = project.group.toString()
            artifactId = "service"
            version = project.version.toString()
        }
    }
}

gradlePlugin {
    plugins {
        create("service") {
            id = "com.thewebsnippet.service"
            implementationClass = "com.thewebsnippet.service.TWSPlugin"
        }
    }
}

dependencies {
    implementation(libs.android.agp)
    implementation(libs.google.api.client)

    testImplementation(libs.junit)
}
