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

import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import org.junit.Test
import retrofit2.Response
import si.inova.tws.manager.utils.FAKE_PROJECT_DTO
import si.inova.tws.manager.utils.FAKE_SHARED_PROJECT
import si.inova.tws.manager.fakes.FakeTWSFunctions
import si.inova.tws.manager.snippet.ProjectResponse
import si.inova.tws.manager.snippet.SnippetLoadingManager
import si.inova.tws.manager.snippet.SnippetLoadingManagerImpl
import si.inova.tws.manager.utils.MILLISECONDS_DATE
import si.inova.tws.manager.utils.testScopeWithDispatcherProvider
import java.time.Instant
import java.util.Date

internal class SnippetLoadingManagerImplTest {
    private val scope = testScopeWithDispatcherProvider()

    private val fakeFunctions = FakeTWSFunctions()

    private lateinit var impl: SnippetLoadingManager

    @Test
    fun `Load project`() = scope.runTest {
        impl = SnippetLoadingManagerImpl(
            configuration = TWSConfiguration.Basic("org", "proj", "key"),
            functions = fakeFunctions
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
            configuration = TWSConfiguration.Shared("sharedId", "key"),
            functions = fakeFunctions
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
