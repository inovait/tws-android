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
import com.thewebsnippet.manager.fakes.function.FakeTWSSnippetFunction
import com.thewebsnippet.manager.manager.snippet.ProjectResponse
import com.thewebsnippet.manager.manager.snippet.SnippetLoadingManager
import com.thewebsnippet.manager.manager.snippet.SnippetLoadingManagerImpl
import com.thewebsnippet.manager.utils.FAKE_PROJECT_DTO
import com.thewebsnippet.manager.utils.FAKE_SHARED_PROJECT
import com.thewebsnippet.manager.utils.MILLISECONDS_DATE
import com.thewebsnippet.manager.utils.testScopeWithDispatcherProvider
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response
import java.time.Instant
import java.util.Date

internal class SnippetLoadingManagerImplTest {
    private val scope = testScopeWithDispatcherProvider()

    private val fakeFunctions = FakeTWSSnippetFunction()

    private lateinit var impl: SnippetLoadingManager

    private val context = Mockito.mock(Context::class.java)

    @Test
    fun `Load project`() = scope.runTest {
        impl = SnippetLoadingManagerImpl(
            context = context,
            configuration = TWSConfiguration.Basic("proj"),
            functions = fakeFunctions
        )

        fakeFunctions.returnedProject =
            Response.success(
                FAKE_PROJECT_DTO,
                Headers.Builder().set("date", Date(MILLISECONDS_DATE)).build()
            )

        fakeFunctions.getWebSnippets("proj")

        assert(
            impl.load() == ProjectResponse(
                project = FAKE_PROJECT_DTO,
                responseDate = Instant.ofEpochMilli(MILLISECONDS_DATE)
            )
        )
    }

    @Test
    fun `Load shared snippet`() = scope.runTest {
        impl = SnippetLoadingManagerImpl(
            context = context,
            configuration = TWSConfiguration.Shared("sharedId"),
            functions = fakeFunctions
        )

        fakeFunctions.returnedSharedSnippet = FAKE_SHARED_PROJECT

        fakeFunctions.returnedProject =
            Response.success(
                FAKE_PROJECT_DTO,
                Headers.Builder().set("date", Date(MILLISECONDS_DATE)).build()
            )

        fakeFunctions.getSharedSnippetData(FAKE_SHARED_PROJECT.shareToken)

        assert(
            impl.load() == ProjectResponse(
                project = FAKE_PROJECT_DTO,
                responseDate = Instant.ofEpochMilli(MILLISECONDS_DATE)
            )
        )
    }
}
