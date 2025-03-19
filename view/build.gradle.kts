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
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
    id("io.deepmedia.tools.deployer") version "0.17.0"
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
    namespace = "com.thewebsnippet.view"

    buildFeatures {
        compose = true
        androidResources = true
        buildConfig = true
    }

    testOptions {
        unitTests.all {
            it.useJUnit()
        }
    }

    defaultConfig {
        buildConfigField("String", "TWS_VERSION", "\"${version}\"")
    }
}

publishLibrary(
    userFriendlyName = "tws-view",
    description = "A collection of core webview utilities",
    githubPath = "view"
)

deployer {
    centralPortalSpec {
        auth.user.set(secret("MAVEN_CENTRAL_USERNAME"))
        auth.password.set(secret("MAVEN_CENTRAL_PASSWORD"))

        signing.key.set(secret("PGP_PUBLIC"))
        signing.password.set(secret("PGP_PRIVATE"))
        allowMavenCentralSync = false
    }
}

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
    implementation(libs.inject)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.mustache)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
