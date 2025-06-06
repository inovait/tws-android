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
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import util.publishLibrary

plugins {
    androidLibraryModule
    kotlin("kapt")
    alias(libs.plugins.dokka)
    id(libs.plugins.jreleaser.get().pluginId)
}

// configuration specific to this subproject.
// notice the use of Partial task
tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets {
        configureEach {
            includes.from("Module.md")
        }
    }
}

afterEvaluate {
    tasks["dokkaHtmlPartial"].dependsOn(tasks.getByName("kaptReleaseKotlin"), tasks.getByName("kaptDebugKotlin"))
}

android {
    namespace = "com.thewebsnippet.manager"

    testOptions {
        unitTests.all {
            it.useJUnit()
        }
    }
}

publishLibrary(
    userFriendlyName = "tws-manager",
    description = "A collection of manager and network connection",
    githubPath = "manager",
)

dependencies {
    api(projects.data)
    implementation(projects.view)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.foundation)
    implementation(libs.dispatch)
    implementation(libs.retrofit.moshi)
    implementation(libs.retrofit.scalars)
    implementation(libs.certificateTransparency)
    implementation(libs.moshi.kotlin)
    implementation(libs.inject)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp)
    implementation(libs.data.store)

    kapt(libs.moshi.codegen)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.mockito)
    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}
