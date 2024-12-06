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

package com.thewebsnippet.manager

import android.content.Context
import android.content.pm.PackageManager
import com.thewebsnippet.manager.preference.AuthPreference
import jakarta.inject.Singleton
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
     *         android:name="com.thewebsnippet.ORGANIZATION_ID"
     *         android:value="your_organization_id_here" />
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
        val configuration = TWSConfiguration.Basic(
            getMetaData(context, ORGANIZATION_ID_METADATA),
            getMetaData(context, PROJECT_ID_METADATA)
        )

        return createOrGet(context, "${configuration.organizationId}_${configuration.projectId}", configuration)
    }

    /**
     * Retrieves a [TWSManager] instance for a custom [TWSConfiguration.Basic].
     *
     * @param context The application context.
     * @param configuration The basic configuration containing organization and project IDs.
     * @return A [TWSManager] instance associated with the provided configuration.
     */
    fun get(context: Context, configuration: TWSConfiguration.Basic): TWSManager {
        return createOrGet(context, "${configuration.organizationId}_${configuration.projectId}", configuration)
    }

    /**
     * Retrieves a [TWSManager] instance for a custom [TWSConfiguration.Shared].
     *
     * @param context The application context.
     * @param configuration The shared configuration containing the shared ID.
     * @return A [TWSManager] instance associated with the provided configuration.
     */
    fun get(context: Context, configuration: TWSConfiguration.Shared): TWSManager {
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
            AuthPreference.safeInit(context)

            val newInstance = TWSManagerImpl(context, tag, configuration)
            instances[tag] = WeakReference(newInstance)
            newInstance
        }
    }

    private fun getMetaData(context: Context, key: String): String {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        return appInfo.metaData?.getString(key)
            ?: error("Missing metadata in Android Manifest. Please check if $key is provided.")
    }
}

/**
 * Represents configuration for a [TWSManager].
 */
sealed class TWSConfiguration {
    /**
     * Basic configuration for a [TWSManager].
     *
     * @param organizationId The ID of the organization.
     * @param projectId The ID of the project.
     */
    data class Basic(
        val organizationId: String,
        val projectId: String,
    ) : TWSConfiguration()

    /**
     * Shared configuration for a [TWSManager].
     *
     * @param sharedId The shared identifier for accessing shared snippet.
     */
    data class Shared(
        val sharedId: String,
    ) : TWSConfiguration()
}

private const val ORGANIZATION_ID_METADATA = "com.thewebsnippet.ORGANIZATION_ID"
private const val PROJECT_ID_METADATA = "com.thewebsnippet.PROJECT_ID"
