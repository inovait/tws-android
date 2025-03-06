# Manager

The Manager module of the TWS SDK is the backbone of dynamic web content handling.
It is designed to ensure that your web snippets are always up-to-date and seamlessly
integrated into your app. By leveraging WebSocket connections, the Manager module handles
everything from real-time updates to snippet visibility based on their state or expiration.

Direct gradle usage:

In your `root-level` build.gradle:

```kotlin
plugin {
    id("com.thewebsnippet.service") version "X.Y.Z" apply false
}
```

In your `app-level` build.gradle:

```kotlin

plugins {
    id("com.thewebsnippet.service")
}

dependencies {
    implementation("com.thewebsnippet:manager:X.Y.Z")
}
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.

## Configuration

You can configure your TWSManager via AndroidManifest metadata:

```xml

<application>
    <meta-data android:name="com.thewebsnippet.PROJECT_ID" android:value="your_project_id" />
</application>
```

And get manager instance like this:

```kotlin
val manager = TWSFactory.get(context)
```

Or provide configuration in code:

```kotlin
val config = TWSConfiguration.Basic("your_project_id")
val manager = TWSFactory.get(context, config)
```

# General Features

Library is a collection of core web view extension. Notable features include:

- Cloud Manager

- Real Time Updates Via Web Socket

- Snippet caching
