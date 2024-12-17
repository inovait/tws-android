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
