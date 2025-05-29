
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
plugins {
    `java-gradle-plugin`
    `maven-publish`
    `kotlin-dsl`
    alias(libs.plugins.detekt)
    alias(libs.plugins.gradle.publish)
}

detekt {
    config.setFrom("$projectDir/../config/detekt.yml")
}

group = "com.thewebsnippet"
version = File(rootDir, "../version.txt").readText().trim()

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    val userFriendlyName = "service"
    val description = "Setup for manager"
    val githubPath = "service"

    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = project.group.toString()
            artifactId = "service"
            version = project.version.toString()

            pom {
                name.set(userFriendlyName)
                this.description.set(description)
                val projectGitUrl = "https://github.com/inovait/tws-android"
                url.set("$projectGitUrl/tree/main/$githubPath")
                inceptionYear.set("2024")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("$projectGitUrl/issues")
                }
                scm {
                    connection.set("scm:git:$projectGitUrl")
                    developerConnection.set("scm:git:$projectGitUrl")
                    url.set(projectGitUrl)
                }
                developers {
                    developer {
                        name.set("Inova IT")
                        url.set("https://inova.si/")
                    }
                }
            }
        }
    }
}

gradlePlugin {
    website = "https://www.thewebsnippet.com"
    vcsUrl = "https://github.com/inovait/tws-android"

    plugins {
        create("service") {
            id = "com.thewebsnippet.service"
            implementationClass = "com.thewebsnippet.service.TWSPlugin"
            displayName = "TWS Service gradle plugin"
            description = "Plugin for parsing tws-service.json, works along with com.thewebsnippet dependency"
            tags = listOf("tws", "service", "parser", "json")
        }
    }
}

dependencies {
    implementation(libs.android.agp)
    implementation(libs.google.api.client) {
        exclude("io.grpc")
    }

    testImplementation(libs.junit)

    compileOnly(libs.detekt.plugin)

    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.compilerWarnings)
    detektPlugins(libs.detekt.compose)
}
