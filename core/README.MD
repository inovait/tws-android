# Core

Direct gradle usage:

```kotlin
implementation("si.inova.tws:core:X.Y.Z")
implementation("si.inova.tws:core-data:X.Y.Z")
```

Version catalogs usage:

```toml
[versions]
tws = "X.Y.Z"
```

```toml
[libraries]
tws-core = { module = "si.inova.tws:core", version.ref = "tws" }
tws-core-data = { module = "si.inova.tws:core-data", version.ref = "tws" }
```

```kotlin
implementation(libs.tws.core)
implementation(libs.tws.core.data)
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.

# General Features

Library is a collection of core web view extension. Notable features include:

- Single Web View Snippet

- Multiple Web View Snippets - Snippets with tabs
