# Interstitial

Direct gradle usage:

```kotlin
implementation("com.thewebsnippet:interstitial:X.Y.Z")
```

Version catalogs usage:

```toml
[versions]
tws = "X.Y.Z"
```

```toml
[libraries]
tws-interstitial = { module = "com.thewebsnippet:interstitial", version.ref = "tws" }
```

```kotlin
implementation(libs.tws.interstitial)
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.
