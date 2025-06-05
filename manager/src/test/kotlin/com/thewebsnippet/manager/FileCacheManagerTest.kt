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
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.thewebsnippet.manager.data.datasource.FileCacheManager
import com.thewebsnippet.manager.data.datasource.FileCacheManager.Companion.CACHE_DIR
import com.thewebsnippet.manager.domain.model.TWSSnippetDto
import com.thewebsnippet.manager.data.setup.twsMoshi
import com.thewebsnippet.manager.utils.FAKE_PROJECT_DTO
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito
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
