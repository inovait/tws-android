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

package si.inova.tws.manager.cache

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import si.inova.tws.data.WebSnippetDto
import si.inova.tws.manager.singleton.twsMoshi
import java.io.File

internal class FileCacheManager(context: Context, tag: String) : CacheManager {
    private val moshi: Moshi by lazy { twsMoshi() }
    private val cacheDir = File(context.cacheDir, "$CACHE_DIR-$tag")

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdir()
        }
    }

    override fun save(key: String, data: List<WebSnippetDto>) {
        try {
            val type = Types.newParameterizedType(List::class.java, WebSnippetDto::class.java)
            val jsonAdapter = moshi.adapter<List<WebSnippetDto>>(type)

            File(cacheDir, key).writeText(jsonAdapter.toJson(data))
        } catch (e: Exception) {
            Log.e(TAG_ERROR_SAVE_CACHE, e.message, e)
        }
    }

    override fun load(key: String): List<WebSnippetDto>? {
        val type = Types.newParameterizedType(List::class.java, WebSnippetDto::class.java)
        val jsonAdapter = moshi.adapter<List<WebSnippetDto>>(type)

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
