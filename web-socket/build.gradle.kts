import util.publishLibrary

plugins {
   androidLibraryModule
}

android {
   namespace = "si.inova.tws.web_socket"

   buildFeatures {
      androidResources = true
   }
}

publishLibrary(
   userFriendlyName = "tws-web-socket",
   description = "A collection of web socket connection",
   githubPath = "web-socket"
)

dependencies {
   api(projects.core.data)

   implementation(libs.androidx.activity.compose)
   implementation(libs.kotlin.immutableCollections)
   implementation(libs.androidx.compose.ui.tooling.preview)
   implementation(libs.compose.foundation)
   implementation(libs.androidx.compose.material3)
   implementation(libs.timber)
   implementation(libs.androidx.browser)
   implementation(libs.moshi.kotlin)
   implementation(libs.okhttp)

   testImplementation(libs.junit)
   androidTestImplementation(libs.runner)
   androidTestImplementation(libs.espresso.core)
}
