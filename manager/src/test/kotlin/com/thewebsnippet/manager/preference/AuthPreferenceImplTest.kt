/*
 * Copyright 2025 INOVA IT d.o.o.
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

package com.thewebsnippet.manager.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import com.thewebsnippet.manager.data.preference.AuthPreferenceImpl
import com.thewebsnippet.manager.fakes.preference.FakeTWSBuild
import com.thewebsnippet.manager.utils.testScopeWithDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class AuthPreferenceImplTest {
    private val testScope = testScopeWithDispatcherProvider()

    private lateinit var testDataStore: DataStore<Preferences>

    @Before
    fun setUp() {
        testDataStore = MemoryDataStore(defaultValue = emptyPreferences())
    }

    @Test
    fun `init should clear preferences and set JWT if JWT does not match`() = testScope.runTest {
        testDataStore.edit {
            it[AuthPreferenceImpl.DATASTORE_JWT] = "different_jwt"
            it[AuthPreferenceImpl.DATASTORE_REFRESH_TOKEN] = "existing_refresh_token"
        }
        runCurrent()

        // Create a temporary directory
        val tempDirPath = Files.createTempDirectory("test_cache")
        val tempCacheDir = tempDirPath.toFile()

        AuthPreferenceImpl(mock(), testDataStore, mock(), tempCacheDir, this, FakeTWSBuild)
        advanceUntilIdle()

        // Verify that preferences were cleared and JWT was updated
        testDataStore.data.test {
            val preferences = awaitItem()

            assert(preferences[AuthPreferenceImpl.DATASTORE_JWT] == FakeTWSBuild.token)
            assert(preferences[AuthPreferenceImpl.DATASTORE_REFRESH_TOKEN] == null)

            cancelAndIgnoreRemainingEvents()
        }

        // To destroy DataStore scope.
        coroutineContext.cancelChildren()

        // Cleanup temp dir
        tempCacheDir.deleteRecursively()
    }

    @Test
    fun `init should not clear preferences if JWT matches`() = testScope.runTest {
        testDataStore.edit {
            it[AuthPreferenceImpl.DATASTORE_JWT] = "JWT-test"
            it[AuthPreferenceImpl.DATASTORE_REFRESH_TOKEN] = "existing_refresh_token"
        }
        runCurrent()

        // Create a temporary directory
        val tempDirPath = Files.createTempDirectory("test_cache")
        val tempCacheDir = tempDirPath.toFile()

        AuthPreferenceImpl(mock(), testDataStore, mock(), tempCacheDir, this, FakeTWSBuild)
        advanceUntilIdle()

        // Verify that preferences were cleared and JWT was updated
        testDataStore.data.test {
            val preferences = awaitItem()

            assert(preferences[AuthPreferenceImpl.DATASTORE_JWT] == FakeTWSBuild.token)
            assert(preferences[AuthPreferenceImpl.DATASTORE_REFRESH_TOKEN] == "existing_refresh_token")

            cancelAndIgnoreRemainingEvents()
        }

        // To destroy DataStore scope.
        coroutineContext.cancelChildren()

        // Cleanup temp dir
        tempCacheDir.deleteRecursively()
    }
}

class MemoryDataStore<T>(defaultValue: T) : DataStore<T> {
    override val data = MutableStateFlow(defaultValue)

    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        data.value = transform(data.value)
        return data.value
    }
}
