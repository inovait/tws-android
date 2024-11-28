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
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response
import si.inova.tws.manager.fakes.FakeAuthPreference
import si.inova.tws.manager.fakes.function.FakeTWSSnippetFunction
import si.inova.tws.manager.manager.snippet.ProjectResponse
import si.inova.tws.manager.manager.snippet.SnippetLoadingManager
import si.inova.tws.manager.manager.snippet.SnippetLoadingManagerImpl
import si.inova.tws.manager.preference.AuthPreference
import si.inova.tws.manager.utils.FAKE_PROJECT_DTO
import si.inova.tws.manager.utils.FAKE_SHARED_PROJECT
import si.inova.tws.manager.utils.MILLISECONDS_DATE
import si.inova.tws.manager.utils.testScopeWithDispatcherProvider
import java.time.Instant
import java.util.Date

internal class SnippetLoadingManagerImplTest {
    private val scope = testScopeWithDispatcherProvider()

    private val fakeFunctions = FakeTWSSnippetFunction()

    private lateinit var impl: SnippetLoadingManager

    private val fakeAuthPreference: AuthPreference = FakeAuthPreference()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = Mockito.mock(Context::class.java)
    }

    @Test
    fun `Load project`() = scope.runTest {
        impl = SnippetLoadingManagerImpl(
            context = context,
            configuration = TWSConfiguration.Basic("org", "proj", "key"),
            functions = fakeFunctions,
            authPreference = fakeAuthPreference
        )

        fakeFunctions.returnedProject =
            Response.success(
                FAKE_PROJECT_DTO,
                Headers.Builder().set("date", Date(MILLISECONDS_DATE)).build()
            )

        assert(
            impl.load() == ProjectResponse(
                project = FAKE_PROJECT_DTO,
                responseDate = Instant.ofEpochMilli(MILLISECONDS_DATE),
                null
            )
        )
    }

    @Test
    fun `Load shared snippet`() = scope.runTest {
        impl = SnippetLoadingManagerImpl(
            context = context,
            configuration = TWSConfiguration.Shared("sharedId", "key"),
            functions = fakeFunctions,
            authPreference = fakeAuthPreference
        )

        fakeFunctions.returnedSharedSnippet = FAKE_SHARED_PROJECT

        fakeFunctions.returnedProject =
            Response.success(
                FAKE_PROJECT_DTO,
                Headers.Builder().set("date", Date(MILLISECONDS_DATE)).build()
            )

        assert(
            impl.load() == ProjectResponse(
                project = FAKE_PROJECT_DTO,
                responseDate = Instant.ofEpochMilli(MILLISECONDS_DATE),
                FAKE_SHARED_PROJECT.snippet.id
            )
        )
    }
}
