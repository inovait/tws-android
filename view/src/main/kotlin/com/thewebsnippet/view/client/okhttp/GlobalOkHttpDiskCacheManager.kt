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
package com.thewebsnippet.view.client.okhttp

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import androidx.annotation.WorkerThread
import okhttp3.Cache
import java.io.File
import kotlin.math.roundToLong

/**
 * Class that manages creation of global OKHttp's [Cache].
 *
 * Cache size is based on Android's provided [StorageManager.getCacheQuotaBytes] method.
 * That way we can achieve best balance between having big cache and not eating up user's disk space.
 *
 * To create cache, just access [cache] object. Please note that the operations makes some blocking disk accesses and thus, first
 * call to [cache] MUST be done on the worker thread.
 *
 * @param context The Android [Context] used to access system services and the app's cache directory.
 * @param errorReporter The [ErrorReporter] used to log exceptions during cache initialization or operations.
 * @param fallbackCacheSize Cache size in bytes if we cannot get the quota from the Android system (for example due to
 *                               older API level)
 * @param cacheSubfolderName Name of the subfolder inside cache folder where OkHttp cache will be created
 * @param cacheQuotaFraction Fraction of the total cache quota that can be used for OkHttp Disk cache.
 */
internal class GlobalOkHttpDiskCacheManager(
    private val context: Context,
    private val errorReporter: ErrorReporter,
    private val fallbackCacheSize: Long = DEFAULT_FALLBACK_CACHE_SIZE_BYTES,
    private val cacheSubfolderName: String = DEFAULT_CACHE_SUBFOLDER,
    private val cacheQuotaFraction: Float = DEFAULT_CACHE_QUOTA_PERCENTAGE
) {
    @get:WorkerThread
    val cache: Cache by lazy {
        if (Thread.currentThread().name == "main") {
            error("Disk cache must not be initialized on the main thread")
        }

        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
            ?: error("Could not get storage service")

        val cacheSize = determineCacheSizeBytes(storageManager)
        createCache(storageManager, cacheSize)
    }

    @WorkerThread
    private fun determineCacheSizeBytes(storageManager: StorageManager): Long {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Older device with unknown cache quota. Just use default size all the time.
            fallbackCacheSize
        } else {
            try {
                val cacheQuota = storageManager.getCacheQuotaBytes(
                    storageManager.getUuidForPath(context.cacheDir)
                )

                (cacheQuota * cacheQuotaFraction).roundToLong()
            } catch (e: Exception) {
                errorReporter.report(e)
                // Cache determining error. Just fallback to default value
                fallbackCacheSize
            }
        }
    }

    private fun createCache(storageManager: StorageManager, cacheSizeBytes: Long): Cache {
        val absoluteCacheFolder = File(context.cacheDir, cacheSubfolderName)
        absoluteCacheFolder.mkdirs()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            storageManager.setCacheBehaviorGroup(absoluteCacheFolder, true)
        }

        return Cache(absoluteCacheFolder, cacheSizeBytes)
    }
}

private const val DEFAULT_FALLBACK_CACHE_SIZE_BYTES = 20_000_000L // 20 MB
private const val DEFAULT_CACHE_SUBFOLDER = "okdisk"

// Using third of our cache quota for OK HTTP disk cache requests seems reasonable.
private const val DEFAULT_CACHE_QUOTA_PERCENTAGE = 0.3f
