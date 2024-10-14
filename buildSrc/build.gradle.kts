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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    `kotlin-dsl`
    alias(libs.plugins.versions)
    alias(libs.plugins.detekt)
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

detekt {
    config.setFrom("$projectDir/../config/detekt.yml")
}

dependencies {
    implementation(libs.androidGradleCacheFix)
    implementation(libs.android.agp)
    implementation(libs.kotlin.plugin)
    implementation(libs.v.checker.plugin)
    implementation(libs.detekt.plugin)

    // Workaround to have libs accessible (from https://github.com/gradle/gradle/issues/15383)
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.compilerWarnings)
    detektPlugins(libs.detekt.compose)
}

tasks.withType<DependencyUpdatesTask> {
    gradleReleaseChannel = "current"

    rejectVersionIf {
        candidate.version.contains("alpha", ignoreCase = true) ||
            candidate.version.contains("beta", ignoreCase = true) ||
            candidate.version.contains("RC", ignoreCase = true) ||
            candidate.version.contains("M", ignoreCase = true) ||
            candidate.version.contains("eap", ignoreCase = true)
    }

    reportfileName = "versions"
    outputFormatter = "json"
}

tasks.register("pre-commit-hook", Copy::class) {
    from("$rootDir/../config/hooks/")
    into("$rootDir/../.git/hooks")
}

afterEvaluate {
    tasks.getByName("jar").dependsOn("pre-commit-hook")
}
