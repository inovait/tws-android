// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
   alias(libs.plugins.nexusPublish)
   alias(libs.plugins.dokka)
}

tasks.dokkaHtmlMultiModule {
   moduleName.set("Dokka MultiModule Example")
}

subprojects {
   plugins.apply("org.jetbrains.dokka")
}

if (properties.containsKey("ossrhUsername")) {
   nexusStaging {
      username = property("ossrhUsername") as String
      password = property("ossrhPassword") as String
      packageGroup = "si.inova"
      stagingRepositoryId = property("ossrhRepId") as String
   }
}
