# Manager

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
    <meta-data android:name="com.thewebsnippet.ORGANIZATION_ID" android:value="your_organization_id" />
    <meta-data android:name="com.thewebsnippet.PROJECT_ID" android:value="your_project_id" />
</application>
```

Or provide configuration in code:

```kotlin
val config = TWSConfiguration.Basic("your_organization_id", "your_project_id")
val manager = TWSFactory.get(context, config)
```

# General Features

Library is a collection of core web view extension. Notable features include:

- Cloud Manager

- Real Time Updates Via Web Socket

- Snippet caching
