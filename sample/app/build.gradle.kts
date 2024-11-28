import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dokka)
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    coreLibraryDesugaring(libs.desugarJdkLibs)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.kotlin.immutableCollections)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.tws.core)
    implementation(libs.tws.manager)
    implementation(libs.tws.interstitial)
}

fun ExtraPropertiesExtension.getString(key: String): String {
    return this[key]?.let { it as? String } ?: ""
}