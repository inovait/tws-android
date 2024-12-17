/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package util

import com.android.build.api.dsl.AndroidResources
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.Installation
import com.android.build.api.dsl.ProductFlavor
import com.android.build.gradle.internal.dsl.InternalTestedExtension
import com.android.build.gradle.internal.utils.KOTLIN_ANDROID_PLUGIN_ID
import org.gradle.api.Action
import org.gradle.api.Project

/**
 * android {} block that can be used without applying specific android plugin
 */
fun Project.commonAndroid(
    block: Action<InternalTestedExtension<BuildFeatures, BuildType, DefaultConfig, ProductFlavor, AndroidResources, Installation>>
) {
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("android", block)
}

fun Project.isAndroidProject(): Boolean {
    return pluginManager.hasPlugin(KOTLIN_ANDROID_PLUGIN_ID)
}
