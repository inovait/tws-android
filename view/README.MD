# View

The View module of the TWS SDK provides a set of tools to seamlessly embed web content into your Android applications using
Jetpack Compose. It focuses on simplifying the integration of web-based features and enhancing the user interface with dynamic and
customizable web views.
With the View module, you can create, display, and interact with web snippets directly in your Compose-based layouts. It is
designed to handle dynamic content, error states, loading indicators, and more, ensuring a smooth and polished user experience.

Direct gradle usage:

```kotlin
dependencies {
    implementation("com.thewebsnippet:view:X.Y.Z")
}
```

Version catalogs usage:

```toml
[versions]
tws = "X.Y.Z"
```

```toml
[libraries]
tws-view = { module = "com.thewebsnippet:view", version.ref = "tws" }
```

```kotlin
implementation(libs.tws.view)
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.

# General Features

Library is a collection of core web view extension. Notable features include:

- Single Web View Snippet

- URL intercepting

- Resource injection (eg. JavaScript, CSS)

- Mustache template processing

- Native Web Permission handling

- File downloads/uploads