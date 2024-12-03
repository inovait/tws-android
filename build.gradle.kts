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
      packageGroup = "si.inova"
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

   // Include custom index.md file
   includes.from("docs/index.md")
   // Include custom assets
   pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
      customAssets = listOf(file("docs/appIcon.png"))
      customStyleSheets = listOf(file("docs/logo-styles.css"))
   }
}
