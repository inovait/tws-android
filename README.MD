# The Web Snippet

TWS SDK is a library, designed to make it easier and more powerful to add a web content to your Android apps, with a WebView on
steroids.
You can use it to combine web and native features, add web pages to an existing app, build a complete app using web content,
or mix web and native screens for a smoother user experience.

## Modules

### • [View](view/README.MD)
### • [Manager](manager/README.MD)
### • [Service](service/README.MD)

## Installation

In your `root-level` build.gradle:

```kotlin
plugin {
    id("com.thewebsnippet.service") version "X.Y.Z" apply false
}
```

In your `app-level` build.gradle:

```kotlin
plugin {
    id("com.thewebsnippet.service")
}

dependencies {
    implementation("com.thewebsnippet:view:X.Y.Z")
    implementation("com.thewebsnippet:manager:X.Y.Z")
}
```

Replace `X.Y.Z` with latest version. Check out [changelog](CHANGELOG.MD) to see what is the latest version.

## Versioning

This library uses [Semantic Versioning](https://semver.org/). Version number is updated automatically whenever new release is
built.

When updating, it is recommended that you check out [changelog](CHANGELOG.MD) first to see the changes, especially if there was
major version change.

## Contributing

Contributions are always welcome! To learn how you can contribute, please see the [Contributing Guide](CONTRIBUTING.md).

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
