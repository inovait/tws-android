import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    alias(sampleLibs.plugins.android.application)
    alias(sampleLibs.plugins.kotlin.android)
    alias(sampleLibs.plugins.kotlin.compose)
    alias(sampleLibs.plugins.dokka)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

// configuration specific to this subproject.
// notice the use of Partial task
tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets {
        configureEach {
            includes.from("Module.md")
        }
    }
}

// Load properties in extras
apply(from = "properties.gradle.kts")

android {
    namespace = "si.inova.tws.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "si.inova.tws.sample"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        manifestPlaceholders["twsOrganizationId"] = extra.getString("organizationId")
        manifestPlaceholders["twsProjectId"] = extra.getString("projectId")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

hilt {
    enableAggregatingTask = false
}

dependencies {
    implementation(sampleLibs.androidx.core.ktx)
    implementation(sampleLibs.androidx.lifecycle.runtime.ktx)
    implementation(sampleLibs.androidx.activity.compose)
    implementation(platform(sampleLibs.androidx.compose.bom))
    implementation(sampleLibs.androidx.ui)
    implementation(sampleLibs.androidx.ui.graphics)
    implementation(sampleLibs.androidx.ui.tooling.preview)
    implementation(sampleLibs.androidx.material3)
    debugImplementation(sampleLibs.androidx.ui.tooling)
    debugImplementation(sampleLibs.androidx.ui.test.manifest)

    coreLibraryDesugaring(sampleLibs.desugarJdkLibs)
    implementation(sampleLibs.hilt.android)
    kapt(sampleLibs.hilt.android.compiler)
    implementation(sampleLibs.kotlin.immutableCollections)
    implementation(sampleLibs.androidx.hilt.navigation.compose)
    implementation(sampleLibs.tws.core)
    implementation(sampleLibs.tws.manager)
    implementation(sampleLibs.tws.interstitial)
}

fun ExtraPropertiesExtension.getString(key: String): String {
    return this[key]?.let { it as? String } ?: ""
}