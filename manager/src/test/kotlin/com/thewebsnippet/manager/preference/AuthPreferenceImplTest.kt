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

import android.content.Context
import android.content.res.Resources
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.cash.turbine.test
import com.thewebsnippet.manager.utils.testScopeWithDispatcherProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class AuthPreferenceImplTest {
    private val testContext: Context = mockk()

    private val testScope = testScopeWithDispatcherProvider()

    private val mockResources: Resources = mockk()

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var testDataStore: DataStore<Preferences>

    @Before
    fun setUp() {
        every { testContext.resources } returns mockResources
        every { testContext.packageName } returns "com.thewebsnippet"
        every { mockResources.getIdentifier("com.thewebsnippet.service.jwt", "string", "com.thewebsnippet") } returns 123
        every { testContext.getString(123) } returns "JWT-test"

        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_store.preferences_pb") }
        )

        JWT.safeInit(testContext)
    }

    @Test
    fun `init should clear preferences and set JWT if JWT does not match`() = testScope.runTest {
        testDataStore.edit {
            it[AuthPreferenceImpl.DATASTORE_JWT] = "different_jwt"
            it[AuthPreferenceImpl.DATASTORE_REFRESH_TOKEN] = "existing_refresh_token"
        }
        runCurrent()

        AuthPreferenceImpl(this, testDataStore)
        advanceUntilIdle()

        // Verify that preferences were cleared and JWT was updated
        testDataStore.data.test {
            val preferences = awaitItem()

            assert(preferences[AuthPreferenceImpl.DATASTORE_JWT] == JWT.token)
            assert(preferences[AuthPreferenceImpl.DATASTORE_REFRESH_TOKEN] == null)

            cancelAndIgnoreRemainingEvents()
        }

        // To destroy DataStore scope.
        coroutineContext.cancelChildren()
    }

    @Test
    fun `init should not clear preferences if JWT matches`() = testScope.runTest {
        testDataStore.edit {
            it[AuthPreferenceImpl.DATASTORE_JWT] = "JWT-test"
            it[AuthPreferenceImpl.DATASTORE_REFRESH_TOKEN] = "existing_refresh_token"
        }
        runCurrent()

        AuthPreferenceImpl(this, testDataStore)
        advanceUntilIdle()

        // Verify that preferences were cleared and JWT was updated
        testDataStore.data.test {
            val preferences = awaitItem()

            assert(preferences[AuthPreferenceImpl.DATASTORE_JWT] == JWT.token)
            assert(preferences[AuthPreferenceImpl.DATASTORE_REFRESH_TOKEN] == "existing_refresh_token")

            cancelAndIgnoreRemainingEvents()
        }

        // To destroy DataStore scope.
        coroutineContext.cancelChildren()
    }
}
