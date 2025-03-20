import org.jreleaser.model.Active
import org.jreleaser.model.Http
import org.jreleaser.model.Signing as JSigning
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
    signing
    alias(libs.plugins.detekt)
    alias(libs.plugins.jreleaser)
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

    repositories {
        maven {
            setUrl(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

if (properties.containsKey("mavenUsername")) {
    signing {
        sign(publishing.publications)
    }

    // Workaround for the https://github.com/gradle/gradle/issues/26091
    tasks.withType<AbstractPublishToMaven>().configureEach {
        val signingTasks = tasks.withType<Sign>()
        mustRunAfter(signingTasks)
    }

    jreleaser {
        release {
            github {
                enabled.set(true)
                skipRelease.set(true)
                skipTag.set(true)
            }
        }

        gitRootSearch.set(true)

        signing {
            active.set(Active.ALWAYS)
            armored.set(true)
            mode.set(JSigning.Mode.FILE)
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

gradlePlugin {
    plugins {
        create("service") {
            id = "com.thewebsnippet.service"
            implementationClass = "com.thewebsnippet.service.TWSPlugin"
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
