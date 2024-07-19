# Core - data

Direct gradle usage:

```kotlin
implementation("si.inova.tws:core-data:X.Y.Z")
```

Version catalogs usage:

```toml
[versions]
tws = "X.Y.Z"
```

```toml
[libraries]
tws-core-data = { module = "si.inova.tws:core-data", version.ref = "tws" }
```

```kotlin
implementation(libs.tws.core.data)
```

Replace `X.Y.Z` with latest version. Check out [changelog](../../CHANGELOG.MD) to see what is the latest version.

# General Features

Library is a collection of core data objects:

- WebContent
- WebSnippetData
