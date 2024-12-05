
<div style="text-align: center;width: 100%;">
    <h1>TheWebSnippet SDK</h1>
    <img src="images/appIcon.png" alt="My Custom Icon" style="display: block; margin: 32px auto; max-width: 100%; height: auto;" />
</div>

## Overview
This documentation will guide you through implementing TheWebSnippet SDK into your own app.

## Description
The TWS SDK is a library, designed to make it easier and more powerful to add web
content to your Android apps with a WebView on steroids. You can use it to combine web
and native features, add web pages to an existing app, build a complete app using web content, 
or mix web and native screens for a smoother user experience.

The TWS SDK goes beyond a standard WebView. It lets you customize content with features like 
custom HTTP headers, CSS, and JavaScript injections, giving you full control over how your
web content looks and works. It also supports Mustache templates, so you can modify HTML 
dynamically based on app data.

The SDK makes handling files simple, including uploading and downloading files directly
through the app. It also takes care of permissions for features like location, camera,
and file storage, so you don’t have to worry about managing them yourself.

With an active internet connection, the TWSManager ensures your web snippets are always up-to-date. 
Developers can change app content on the fly without rebuilding or updating the app. Even 
offline, the SDK will still work smoothly, letting users access content without interruption.

## Quick Tutorial
### Installation

In your <b>root-level (project-level)</b> Gradle file (`<project>/build.gradle.kts` or `<project>/build.gradle`), add the Service
Gradle
plugin to the plugins block:

```gradle
plugin {
   id("com.thewebsnippet.service") version "1.0.0" apply false
}
```

<br>

In your <b>module (app-level)</b> Gradle file (usually `<project>/<app-module>/build.gradle.kts` or
`<project>/<app-module>/build.gradle`),
add the Service Gradle plugin and SDK dependencies:

```gradle
plugin {
   id("com.thewebsnippet.service")
}

dependencies {
    implementation 'com.thewebsnippet:core:1.0.0' // Contains UI Composable components for displaying web pages
    implementation 'com.thewebsnippet:manager:1.0.0' // Contains TWSManager for loading and refreshing snippets in real time
}
```

### Step 1: Add a TWS configuration file

Download the TWS configuration file (`tws-services.json`) from our web page and add it to your app.
Move your config file into the module (app-level) root directory of your app or in your desired flavour.

### Step 2: Provide metadata for TWS SDK

Before using the TWS SDK, ensure you set up metadata keys for organization and project in AndroidManifest.xml. 
These metadata keys allow the SDK to identify the correct organization and project context when initializing your TWSManager:

```xml
<application>
   <meta-data android:name="com.thewebsnippet.ORGANIZATION_ID"
           android:value="your_organization_id" />
   <meta-data android:name="com.thewebsnippet.PROJECT_ID"
           android:value="your_project_id" />
</application>
```

### Step 3: Using TWSView to display snippet

Set up TWSView to display a specific snippet. Here’s how to collect snippets and display "home" snippet:

```kotlin
val manager = TWSFactory.get(context)

setContent {
   val projectSnippets = manager.snippets.collectAsStateWithLifecycle(null).value

   when (projectSnippets) {
      is TWSOutcome.Success -> {
         val home = projectSnippets.data.first { it.id == "home" }
         TWSView(snippet = home)
      }

      else -> {
         // Handle errors or loading state here
      }
   }
}
```
