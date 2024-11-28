import org.jetbrains.dokka.gradle.DokkaTaskPartial

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.dokka)
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
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