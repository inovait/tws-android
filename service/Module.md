# Module service

A service plugin is a tool that connects your organization's services to your mobile app seamlessly with manager SDK.

## <b>Step 1:</b> Add the Manager SDK to your app

In your <b>module (app-level)</b> Gradle file (usually `<project>/<app-module>/build.gradle.kts` or
`<project>/<app-module>/build.gradle`), add the dependency for the Manager library for Android.

```gradle
dependencies {
    implementation 'com.thewebsnippet:manager:1.0.0' // Contains TWSManager for loading and refreshing snippets in real time
}
```

## <b>Step 2:</b> Add the Service Gradle plugin to your app

1. In your <b>root-level (project-level)</b> Gradle file (`<project>/build.gradle.kts` or `<project>/build.gradle`), add the
   Service Gradle plugin to the plugins block:

    ```gradle
    plugin {
       id("com.thewebsnippet.service") version "1.0.0" apply false
    }
    ```
   <br>

2. In your <b>module (app-level)</b> Gradle file (usually `<project>/<app-module>/build.gradle.kts` or
   `<project>/<app-module>/build.gradle`), add the Service Gradle plugin:

    ```gradle
    plugin {
       id("com.thewebsnippet.service")
    }
    ```

## <b>Step 3:</b> Add the `tws-service.json` file

- Add `tws-service.json` into variant-specific configuration file (e.g., `src/<variant-name>/tws-service.json`).
- `tws-service.json` have fallback configuration file (`tws-service.json`) located at the <b>project root</b>.
