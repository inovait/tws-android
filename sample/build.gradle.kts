import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(sampleLibs.plugins.android.application) apply false
    alias(sampleLibs.plugins.kotlin.android) apply false
    alias(sampleLibs.plugins.kotlin.compose) apply false
    alias(sampleLibs.plugins.dokka)
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}
tasks.dokkaHtmlMultiModule {
    moduleName.set("Sample application")
    outputDirectory.set(file("../build/dokka/htmlMultiModule/sample"))
    includes.from("../docs/sample.md")

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file("../docs/appIcon.png"))
        customStyleSheets = listOf(file("../docs/logo-styles.css"))
    }
}