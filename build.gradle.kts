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
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial

// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.nexusPublish)
    alias(libs.plugins.dokka)
}

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

if (properties.containsKey("ossrhUsername")) {
    nexusStaging {
        username = property("ossrhUsername") as String
        password = property("ossrhPassword") as String
        packageGroup = "com.thewebsnippet"
        stagingRepositoryId = property("ossrhRepId") as String
    }
}

// Configure submodules, can be configured separately also
allprojects {
    apply(plugin = "org.jetbrains.dokka")
    tasks.withType<DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            documentedVisibilities = setOf(DokkaConfiguration.Visibility.PUBLIC)
            reportUndocumented = true
        }
    }
}

// Configures only the parent MultiModule task, this will not affect submodules
tasks.dokkaHtmlMultiModule {
    moduleName.set("TheWebSnippet SDK")

    moduleVersion = File(rootDir, "version.txt").readText().trim()

    // Include custom index.md file
    includes.from("docs/index.md")
    // Include custom assets
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file("docs/appIcon.png"))
        customStyleSheets = listOf(
            file("docs/logo-styles.css"), file("docs/hide-kotlin-playground.css"),
            file("docs/hide-filter.css"), file("docs/fonts.css")
        )
        footerMessage = "Copyright 2024 INOVA IT d.o.o."
    }
}
