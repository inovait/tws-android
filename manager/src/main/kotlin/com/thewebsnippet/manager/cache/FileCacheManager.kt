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
package com.thewebsnippet.manager.cache

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.thewebsnippet.manager.data.TWSSnippetDto
import com.thewebsnippet.manager.setup.twsMoshi
import java.io.File

internal class FileCacheManager(context: Context, tag: String) : CacheManager {
    private val moshi: Moshi by lazy { twsMoshi() }
    private val cacheDir = File(context.cacheDir, "$CACHE_DIR-$tag")

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdir()
        }
    }

    override fun save(key: String, data: List<TWSSnippetDto>) {
        try {
            val type = Types.newParameterizedType(List::class.java, TWSSnippetDto::class.java)
            val jsonAdapter = moshi.adapter<List<TWSSnippetDto>>(type)

            File(cacheDir, key).writeText(jsonAdapter.toJson(data))
        } catch (e: Exception) {
            Log.e(TAG_ERROR_SAVE_CACHE, e.message, e)
        }
    }

    override fun load(key: String): List<TWSSnippetDto>? {
        val type = Types.newParameterizedType(List::class.java, TWSSnippetDto::class.java)
        val jsonAdapter = moshi.adapter<List<TWSSnippetDto>>(type)

        val file = File(cacheDir, key)
        return try {
            if (file.exists()) {
                val json = file.readText()
                jsonAdapter.fromJson(json)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(CACHE_DIR, e.message, e)
            null
        }
    }

    override fun clear() {
        cacheDir.deleteRecursively()
    }

    companion object {
        private const val TAG_ERROR_SAVE_CACHE = "SaveCache"
        internal const val CACHE_DIR = "tws_cache"
    }
}
