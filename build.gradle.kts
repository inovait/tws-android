// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
   alias(libs.plugins.nexusPublish)
}

if (properties.containsKey("ossrhUsername")) {
   nexusStaging {
      username = property("ossrhUsername") as String
      password = property("ossrhPassword") as String
      packageGroup = "si.inova"
      stagingRepositoryId = property("ossrhRepId") as String
   }
}
