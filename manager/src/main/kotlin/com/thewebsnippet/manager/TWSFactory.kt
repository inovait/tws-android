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
package com.thewebsnippet.manager

import android.content.Context
import android.content.pm.PackageManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.thewebsnippet.manager.TWSConfiguration.Basic
import com.thewebsnippet.manager.TWSConfiguration.Shared
import com.thewebsnippet.manager.preference.AuthPreferenceImpl
import com.thewebsnippet.manager.preference.JWT
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.lang.ref.WeakReference
import java.util.WeakHashMap

/**
 * A factory for creating and managing instances of [TWSManager].
 *
 * This factory uses a [WeakHashMap] to cache instances of [TWSManager] associated with specific tags.
 * Instances are stored as weak references, meaning they will be automatically cleared by the garbage
 * collector when they are no longer in use, ensuring memory efficiency.
 *
 * #### Prerequisite: `tws-service.json`
 * To use the [TWSManager], ensure you have the required `tws-service.json` configuration file.
 *
 * 1. Generate `tws-service.json` and associate it with your TWS account
 * 2. Place the `tws-service.json` file in the app module's root directory or the root directory
 *    of your desired flavor.
 *
 * #### Example Directory Structure:
 * ```
 * /app
 * ├── src
 * │   ├── main
 * │   │   ├── tws-service.json
 * │   │   └── AndroidManifest.xml
 * │   └── flavorName
 * │       ├── tws-service.json
 * │       └── AndroidManifest.xml
 * ```
 *
 */
@Singleton
object TWSFactory {
    private val instances = WeakHashMap<String, WeakReference<TWSManager>>()
    private val Context.authPreferences: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

    /**
     * Retrieves a [TWSManager] instance for the default configuration, which is created using metadata
     * from the application's manifest.
     *
     * To create [TWSManager] without providing configuration manually,
     * you must define the required metadata in your `AndroidManifest.xml` file:
     * ```xml
     * <application>
     *     <!-- Other application elements -->
     *     <meta-data
     *         android:name="com.thewebsnippet.PROJECT_ID"
     *         android:value="your_project_id_here" />
     * </application>
     * ```
     *
     * @param context The application context.
     * @return A [TWSManager] instance associated with the configuration.
     * @throws IllegalStateException if required metadata is missing in the Android Manifest.
     */
    fun get(context: Context): TWSManager {
        val configuration = Basic(getMetaData(context))

        return createOrGet(context, configuration.projectId, configuration)
    }

    /**
     * Retrieves a [TWSManager] instance for a custom [TWSConfiguration.Basic].
     *
     * @param context The application context.
     * @param configuration The basic configuration containing project ID.
     * @return A [TWSManager] instance associated with the provided configuration.
     */
    fun get(context: Context, configuration: Basic): TWSManager {
        return createOrGet(context, configuration.projectId, configuration)
    }

    /**
     * Retrieves a [TWSManager] instance for a custom [TWSConfiguration.Shared].
     *
     * @param context The application context.
     * @param configuration The shared configuration containing the shared ID.
     * @return A [TWSManager] instance associated with the provided configuration.
     */
    fun get(context: Context, configuration: Shared): TWSManager {
        return createOrGet(context, configuration.sharedId, configuration)
    }

    /**
     * Retrieves an existing [TWSManager] instance by its tag, if available.
     *
     * @param tag The unique tag associated with a [TWSManager] instance.
     * @return The [TWSManager] instance if it exists in the cache, or `null` otherwise.
     */
    fun get(tag: String): TWSManager? {
        return instances[tag]?.get()
    }

    private fun createOrGet(
        context: Context,
        tag: String,
        configuration: TWSConfiguration
    ): TWSManager {
        val existingInstance = instances[tag]?.get()

        return if (existingInstance != null) {
            existingInstance
        } else {
            JWT.safeInit(context)

            val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val newInstance = TWSManagerImpl(
                context = context,
                tag = tag,
                configuration = configuration,
                auth = AuthPreferenceImpl(applicationScope, context.authPreferences)
            )

            instances[tag] = WeakReference(newInstance)
            newInstance
        }
    }

    private fun getMetaData(context: Context): String {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        return appInfo.metaData?.getString(PROJECT_ID_METADATA)
            ?: error("Missing metadata in Android Manifest. Please check if $PROJECT_ID_METADATA is provided.")
    }
}

/**
 * Represents configuration for a [TWSManager].
 * There are two possible configurations [Basic] and [Shared].
 */
sealed class TWSConfiguration {
    /**
     * Basic configuration for a [TWSManager].
     * Configured for a specific project.
     * [TWSManager] configured with Basic configuration will have access to all snippets in that organizations project,
     * a valid tws-service.json is provided.
     *
     * @param projectId The ID of the project.
     */
    data class Basic(val projectId: String) : TWSConfiguration()

    /**
     * Shared configuration for a [TWSManager].
     * Configuration for a single snippet.
     * [TWSManager] configured with Shared configuration has access to only a single snippet with that sharedId.
     *
     * @param sharedId The shared identifier for accessing shared snippet.
     */
    data class Shared(val sharedId: String) : TWSConfiguration()
}

private const val PROJECT_ID_METADATA = "com.thewebsnippet.PROJECT_ID"
private const val DATASTORE_NAME = "authPreferences"
