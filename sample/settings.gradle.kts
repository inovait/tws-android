pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("${rootProject.projectDir}/libs")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("${rootProject.projectDir}/libs")
        }
    }
    versionCatalogs {
        create("sampleLibs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "sample"
include(":app")
 