/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import util.publishLibrary

plugins {
    androidLibraryModule
    kotlin("kapt")
}

android {
    namespace = "si.inova.tws.manager"

    testOptions {
        unitTests.all {
            it.useJUnit()
        }
    }
}

publishLibrary(
    userFriendlyName = "tws-manager",
    description = "A collection of manager and network connection",
    githubPath = "manager",
)

dependencies {
    api(libs.kotlinova.core)
    implementation(libs.kotlinova.retrofit)
    implementation(libs.androidx.core.ktx)
    implementation(libs.dispatch)
    implementation(libs.retrofit.moshi)
    implementation(libs.retrofit.scalars)
    implementation(libs.certificateTransparency)
    implementation(libs.moshi.kotlin)
    implementation(libs.inject)
    implementation(libs.moshi.adapters)

    kapt(libs.moshi.codegen)

    testImplementation(libs.kotlinova.core.test)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.mockito)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinova.retrofit.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}
