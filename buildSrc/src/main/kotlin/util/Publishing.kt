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
package util

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.withType
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.model.Active
import org.jreleaser.model.Http
import org.jreleaser.model.Signing

fun Project.publishLibrary(
    userFriendlyName: String,
    description: String,
    githubPath: String,
    artifactName: String = project.name
) {
    setProjectMetadata(userFriendlyName, description, githubPath)
    configureForJReleaser()
    forceArtifactName(artifactName)
}

private fun Project.setProjectMetadata(
    userFriendlyName: String,
    description: String,
    githubPath: String
) {
    extensions.configure<org.gradle.api.publish.PublishingExtension>("publishing") {
        publications.withType<MavenPublication> {
            pom {
                name.set(userFriendlyName)
                this.description.set(description)
                val projectGitUrl = "https://github.com/inovait/tws-android-sdk"
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

        repositories {
            maven {
                setUrl(layout.buildDirectory.dir("staging-deploy"))
            }
        }
    }
}

fun Project.configureForJReleaser() {
    if (!properties.containsKey("username")) return

    extensions.configure<JReleaserExtension>("jreleaser") {
        signing {
            active.set(Active.ALWAYS)
            armored.set(true)
            mode.set(Signing.Mode.FILE)
            publicKey.set(property("publicKeyPath") as String)
            secretKey.set(property("privateKeyPath") as String)
        }

        deploy {
            maven {
                mavenCentral {
                    register("maven-central") {
                        active.set(Active.ALWAYS)

                        namespace.set("com.thewebsnippet")
                        url.set("https://s01.oss.sonatype.org/service/local")
                        stagingRepository("target/staging-deploy")

                        authorization.set(Http.Authorization.BASIC)
                        username.set(property("username") as String)
                        password.set(property("password") as String)

                        applyMavenCentralRules.set(true)
                    }
                }
            }
        }
    }
}

private fun Project.forceArtifactName(artifactName: String) {
    afterEvaluate {
        extensions.configure<org.gradle.api.publish.PublishingExtension>("publishing") {
            publications.withType<MavenPublication> {
                artifactId = artifactId.replace(project.name, artifactName)
            }
        }
    }
}
