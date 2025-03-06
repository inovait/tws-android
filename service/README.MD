# Service

Plugin that uses tws-service.json to generate your access token, which is required for authenticating your organization, when
initializing TWSManager.

In your `root-level` build.gradle:

```kotlin
plugins {
    id("com.thewebsnippet.service") version "X.Y.Z" apply false
}
```

In your `app-level` build.gradle:

```kotlin
plugins {
    id("com.thewebsnippet.service")
}
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.