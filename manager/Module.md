# Module manager

The Manager module of the TWS SDK is the backbone of dynamic web content handling.
It is designed to ensure that your web snippets are always up-to-date and seamlessly
integrated into your app. By leveraging WebSocket connections, the Manager module handles
everything from real-time updates to snippet visibility based on their state or expiration.

## Installation

Add Service Gradle plugin to the __root-level__ Gradle file of your project:

```gradle
plugin {
   id("com.thewebsnippet.service") version "{{version}}" apply false
}
```
<br>
Apply Service Gradle plugin and add SDK dependencies to the __app-level__ Gradle file of your project:

```gradle
plugin {
   id("com.thewebsnippet.service")
}

dependencies {
    implementation("com.thewebsnippet:manager:{{version}}") // Contains TWSManager for loading and refreshing snippets in real time
}
```