/*
 * Copyright 2025 INOVA IT d.o.o.
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

        AuthPreferenceImpl(testDataStore, this)
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

        AuthPreferenceImpl(testDataStore, this)
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
