# Manager

Direct gradle usage:

```kotlin
implementation("si.inova.tws:manager:X.Y.Z")
```

Version catalogs usage:

```toml
[versions]
tws = "X.Y.Z"
```

```toml
[libraries]
tws-manager = { module = "si.inova.tws:manager", version.ref = "tws" }
```

```kotlin
implementation(libs.tws.manager)
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.

# General Features

Library is a collection of core web view extension. Notable features include:

- Cloud Manager

- Web Socket
