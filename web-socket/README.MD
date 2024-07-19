# Web Socket

Direct gradle usage:

```kotlin
implementation("si.inova.tws:web-socket:X.Y.Z")
```

Version catalogs usage:

```toml
[versions]
tws = "X.Y.Z"
```

```toml
[libraries]
tws-ws = { module = "si.inova.tws:core", version.ref = "tws" }
```

```kotlin
implementation(libs.tws.ws)
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.

# General Features

Library is a collection of web socket connection for updating web snippets. Notable features include:

- Connection to desired web socket url 

- Updating web snippets over web socket
