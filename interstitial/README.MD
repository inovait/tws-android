# Interstitial

Direct gradle usage:

```kotlin
implementation("si.inova.tws:interstitial:X.Y.Z")
```

Version catalogs usage:

```toml
[versions]
tws = "X.Y.Z"
```

```toml
[libraries]
tws-interstitial = { module = "si.inova.tws:interstitial", version.ref = "tws" }
```

```kotlin
implementation(libs.tws.interstitial)
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.
