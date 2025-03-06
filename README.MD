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

To contribute to this set of libraries:

1. Checkout `develop`
2. Create new branch for your contribution
3. Commit your work. While commiting, use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/). Scope tag should
   be the name of the module you are updating.
4. Try to avoid breaking changes, but if it cannot be avoided, you must put `BREAKING CHANGE` in the footer of the commit message
   and explain the change.
5. Manually run instrumented tests (`./gradlew connectedCheck`) - we do not run those on CI at the moment.
6. Create merge request
7. When your PR is merged, new release will be generated automatically

   ## How to test locally

   ### TheWebSnippetSdk project

    1. In `version.txt` setup your own version
    2. Run `./gradlew publishtomavenlocal service:publishtomavenlocal`

   ### Your own project

    1. Setup your gradle for local repository use:

        ```
        pluginManagement {
        repositories {
                mavenLocal()
                ...
            }
            ...
        }

        dependencyResolutionManagement {
            repositories {
                mavenLocal()
                ...
            }
            ...
        }
        ```

    2. Add plugin and dependencies

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
       Where `X.Y.Z` is the version you specified in version.txt.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
