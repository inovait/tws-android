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
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
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

        repositories {
            maven {
                setUrl(layout.buildDirectory.dir("staging-deploy"))
            }
        }
    }
}

fun Project.configureForJReleaser() {
    if (!properties.containsKey("mavenUsername")) return
    extensions.configure<org.gradle.plugins.signing.SigningExtension>("signing") {
        sign(extensions.getByName<org.gradle.api.publish.PublishingExtension>("publishing").publications)
    }

    // Workaround for the https://github.com/gradle/gradle/issues/26091
    tasks.withType<AbstractPublishToMaven>().configureEach {
        val signingTasks = tasks.withType<Sign>()
        mustRunAfter(signingTasks)
    }

    extensions.configure<JReleaserExtension>("jreleaser") {
        release {
            github {
                enabled.set(true)
                skipRelease.set(false)
                skipTag.set(false)
                overwrite.set(true)
            }
        }

        gitRootSearch.set(true)

        signing {
            active.set(Active.ALWAYS)
            armored.set(true)
            mode.set(Signing.Mode.FILE)
            publicKey.set(property("publicKeyPath") as String)
            secretKey.set(property("privateKeyPath") as String)
        }

        deploy {
            maven {
                pomchecker {
                    version.set("1.14.0")
                    failOnWarning.set(true)
                    failOnError.set(true)
                }
                mavenCentral {
                    create("sonatype") {
                        active.set(Active.ALWAYS)

                        retryDelay.set(JRELEASER_RETRIES_SECONDS)
                        maxRetries.set(JRELEASER_MAX_RETRIES)

                        namespace.set("com.thewebsnippet")
                        url.set("https://central.sonatype.com/api/v1/publisher")
                        stagingRepository("build/staging-deploy")

                        authorization.set(Http.Authorization.BASIC)
                        username.set(property("mavenUsername") as String)
                        password.set(property("mavenPassword") as String)

                        applyMavenCentralRules.set(false)
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

private const val JRELEASER_RETRIES_SECONDS = 60
private const val JRELEASER_MAX_RETRIES = 100
