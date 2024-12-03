# Module manager

The Manager module of the TWS SDK is the backbone of dynamic web content handling.
It is designed to ensure that your web snippets are always up-to-date and seamlessly
integrated into your app. By leveraging WebSocket connections, the Manager module handles
everything from real-time updates to snippet visibility based on their state or expiration.

## Installation

In your <b>root-level (project-level)</b> Gradle file (`<project>/build.gradle.kts` or `<project>/build.gradle`), add the Service Gradle
plugin to the plugins block:

```gradle
plugin {
   id("com.thewebsnippet.service") version "1.0.0" apply false
}
```
<br>

In your <b>module (app-level)</b> Gradle file (usually `<project>/<app-module>/build.gradle.kts` or `<project>/<app-module>/build.gradle`),
add the Service Gradle plugin and SDK dependency:

```gradle
plugin {
   id("com.thewebsnippet")
}

dependencies {
    implementation 'com.thewebsnippet:manager:1.0.0' // Contains TWSManager for loading and refreshing snippets in real time
}
```
