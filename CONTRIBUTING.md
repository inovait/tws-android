# Contributing

To contribute to this set of libraries:

1. Checkout `develop`
2. Create new branch for your contribution
3. Commit your work. While commiting, use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/). Scope tag should
   be the name of the module you are updating.
4. Try to avoid breaking changes, but if it cannot be avoided, you must put `BREAKING CHANGE` in the footer of the commit message
   and explain the change.
5. Create merge request.
6. When your PR is merged, new release will be generated automatically.

   ## How to test locally

      ### TheWebSnippetSdk project

          1. In `version.txt` setup your own version - do not push new versions to the PR (it will be automaticly updated by github action)
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
