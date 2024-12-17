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
import org.gradle.accessors.dm.LibrariesForLibs
import util.commonAndroid

val libs = the<LibrariesForLibs>()

plugins {
    id("io.gitlab.arturbosch.detekt")
}

commonAndroid {
    lint {
        lintConfig = file("$rootDir/config/android-lint.xml")
        abortOnError = true

        warningsAsErrors = true
        sarifReport = true
    }
}

tasks.withType<com.android.build.gradle.internal.lint.AndroidLintTask>().configureEach {
    finalizedBy(":reportMerge")
}

detekt {
    config.setFrom("$rootDir/config/detekt.yml")
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.compilerWarnings)
    detektPlugins(libs.detekt.compose)
}
