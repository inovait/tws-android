# Module core

The Core module of the TWS SDK provides a set of tools to seamlessly embed web
content into your Android applications using Jetpack Compose. It focuses on simplifying
the integration of web-based features and enhancing the user interface with dynamic and
customizable web views.

With the Core module, you can create, display, and interact with web snippets 
directly in your Compose-based layouts. It is designed to handle dynamic content, 
error states, loading indicators, and more, ensuring a smooth and polished user experience.

The Core module also includes web permission handling. Permissions requested by web pages, such as downloading/uploading files,
accessing the device's camera, and determining the user's location, are handled natively.

The following permissions need to be added to AndroidManifest.xml, if your web pages uses the following features:

- For camera permissions, include:
  `<uses-permission android:name="android.permission.CAMERA"/>`
  and
  `<uses-feature android:name="android.hardware.camera" android:required="false"/>`
- For accessing the user's location, include:
  `<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>`

## Installation

In your <b>module (app-level)</b> Gradle file (usually `<project>/<app-module>/build.gradle.kts` or `<project>/<app-module>/build.gradle`),
add the SDK dependency:

```gradle
dependencies {
    implementation 'com.thewebsnippet:core:1.0.0' // Contains UI Composable components for displaying web pages
}
```
