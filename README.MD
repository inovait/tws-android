# The Web Snippet

Expanded WebView to create your own application from your webpage.

## Modules

### • [Core](core/README.MD)
### • [Web Socket](web-socket/README.MD)

## Versioning

This library uses [Semantic Versioning](https://semver.org/). Version number is updated automatically whenever new release is
built.

When updating, it is recommended that you check out [changelog](CHANGELOG.MD) first to see the changes, especially if there was
major version change.

## Contributing

To contribute to this set of libraries:

1. Checkout `main`
2. Create new branch for your contribution
3. Commit your work. While commiting, use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/). Scope tag should
   be the name of the module you are updating.
4. Try to avoid breaking changes, but if it cannot be avoided, you must put `BREAKING CHANGE` in the footer of the commit mesasage
   and explain the change.
5. Manually run instrumented tests (`./gradlew connectedCheck`) - we do not run those on CI at the moment.
6. Create merge request
7. When your PR is merged, new release will be generated automatically

## How to test locally

### TheWebSnippetSdk project

1. in `version.txt` setup your own version
2. run `./gradlew publishtomavenlocal`

### Your own project

1. setup your gradle for local repository use

    ```
    dependencyResolutionManagement {
        repositories {
            mavenLocal()
            ...
        }
        ...
    }
    ```

2. import library

- `implementation("si.inova.tws:core-ui:[version]")`
- `implementation("si.inova.tws:core-data:[version]")`
- `implementation("si.inova.tws:web-socket:[version]")`
