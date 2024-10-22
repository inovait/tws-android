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

import org.jetbrains.dokka.gradle.DokkaTaskPartial
import util.publishLibrary

plugins {
    androidLibraryModule
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
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

android {
    namespace = "si.inova.tws.core"

    buildFeatures {
        compose = true
        androidResources = true
    }

    testOptions {
        unitTests.all {
            it.useJUnit()
        }
    }
}

publishLibrary(
    userFriendlyName = "tws-core",
    description = "A collection of core utilities",
    githubPath = "core"
)

dependencies {
    api(projects.data)

    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlin.immutableCollections)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.coil.compose)
    implementation(libs.kotlinova.retrofit)
    implementation(libs.kotlinova.core)
    implementation(libs.inject)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.mustache)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
