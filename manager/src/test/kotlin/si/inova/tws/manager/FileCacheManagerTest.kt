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

package si.inova.tws.manager

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito
import si.inova.tws.manager.cache.FileCacheManager
import si.inova.tws.manager.cache.FileCacheManager.Companion.CACHE_DIR
import si.inova.tws.manager.data.TWSSnippetDto
import si.inova.tws.manager.singleton.twsMoshi
import si.inova.tws.manager.utils.FAKE_PROJECT_DTO
import java.io.File

class FileCacheManagerTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var fileCacheManager: FileCacheManager
    private lateinit var context: Context
    private lateinit var cacheDir: File
    private val moshi: Moshi = twsMoshi()

    @Before
    fun setUp() {
        context = Mockito.mock(Context::class.java)
        cacheDir = tempFolder.newFolder(CACHE_MANAGER_TEST_FOLDER)
        Mockito.`when`(context.cacheDir).thenReturn(cacheDir)

        fileCacheManager = FileCacheManager(context, FILE_CACHE_MANAGER_TAG)
    }

    @After
    fun tearDown() {
        tempFolder.delete()
    }

    @Test
    fun `Save should write data to cache file`() {
        val savedFile = File(cacheDir, "$CACHE_DIR-$FILE_CACHE_MANAGER_TAG")
        fileCacheManager.save(CACHED_SNIPPETS_KEY, FAKE_PROJECT_DTO.snippets)

        assert(savedFile.exists())

        val type = Types.newParameterizedType(List::class.java, TWSSnippetDto::class.java)
        val jsonAdapter = moshi.adapter<List<TWSSnippetDto>>(type)
        val savedData = jsonAdapter.fromJson(savedFile.resolve(CACHED_SNIPPETS_KEY).readText())

        assert(savedData == FAKE_PROJECT_DTO.snippets)
    }

    @Test
    fun `Load should return null when cache file does not exist`() {
        val loadedData = fileCacheManager.load(CACHED_SNIPPETS_KEY)
        assert(loadedData == null)
    }

    @Test
    fun `Clear should delete all cached files`() {
        val savedFile = File(cacheDir, "$CACHE_DIR-$FILE_CACHE_MANAGER_TAG")
        fileCacheManager.save(CACHED_SNIPPETS_KEY, FAKE_PROJECT_DTO.snippets)

        assert(!savedFile.listFiles().isNullOrEmpty())

        fileCacheManager.clear()

        assert(cacheDir.listFiles().isNullOrEmpty())
    }

    @Test
    fun `Saving to cache and retrieving to cache should occur with same items`() {
        assert(fileCacheManager.load(CACHED_SNIPPETS_KEY) == null)
        fileCacheManager.save(CACHED_SNIPPETS_KEY, FAKE_PROJECT_DTO.snippets)
        assert(fileCacheManager.load(CACHED_SNIPPETS_KEY) == FAKE_PROJECT_DTO.snippets)
        fileCacheManager.clear()
        assert(fileCacheManager.load(CACHED_SNIPPETS_KEY) == null)
    }
}

private const val CACHE_MANAGER_TEST_FOLDER = "CacheManagerTestFolder"
private const val CACHED_SNIPPETS_KEY = "CachedKey"
private const val FILE_CACHE_MANAGER_TAG = "FileCacheManagerTag"
