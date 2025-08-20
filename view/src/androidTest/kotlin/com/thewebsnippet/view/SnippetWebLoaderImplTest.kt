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

package com.thewebsnippet.view

import android.content.Context
import android.webkit.CookieManager
import androidx.test.platform.app.InstrumentationRegistry
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.view.client.okhttp.cookie.CookieSaverImpl
import com.thewebsnippet.view.client.okhttp.web.RedirectHandlerImpl
import com.thewebsnippet.view.client.okhttp.web.SnippetWebLoaderImpl
import com.thewebsnippet.view.client.okhttp.web.webViewHttpClient
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class SnippetWebLoaderImplTest {
    private lateinit var snippetLoad: SnippetWebLoaderImpl
    private lateinit var context: Context
    private lateinit var cookieManager: CookieManager
    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.setAcceptCookie(true)

        server = MockWebServer()
        server.start()

        snippetLoad = SnippetWebLoaderImpl(
            redirectHandler = lazy {
                RedirectHandlerImpl(
                    client = webViewHttpClient(context),
                    cookieSaver = CookieSaverImpl(cookieManager)
                )
            }
        )
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun ensureCookiesAreSyncedToWebView() = runTest {
        val baseUrl = server.url("/").toString()

        // Prepare response with cookie
        val response = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "text/html; charset=UTF-8")
            .setHeader("Set-Cookie", "test_cookie=test_value; Path=/; Domain=${server.hostName}")
            .setBody("<html><body>Hello</body></html>")

        server.enqueue(response)

        // Validate that no cookies are saved in web view cookie store
        assert(cookieManager.getCookie(baseUrl).isNullOrEmpty())

        val snippet = TWSSnippet(id = "testId", target = baseUrl)
        // Fetch snippet target content (and cookie), this is NOT fetched in webview
        snippetLoad.response(snippet.target, snippet.headers)

        // Assert that cookie has been synced with web views cookie store
        assert(cookieManager.getCookie(baseUrl).contains("test_cookie=test_value"))
    }

    @Test
    fun ensureUnsupportedCookieExtensionsAreSynced() = runTest {
        val baseUrl = server.url("/").toString()

        // Prepare response with cookie
        val response = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "text/html; charset=UTF-8")
            .setHeader("Set-Cookie", "test_cookie=test_value; Path=/; Domain=${server.hostName}; Secure; HttpOnly; SameSite=None")
            .setBody("<html><body>Hello</body></html>")

        server.enqueue(response)

        // Validate that no cookies are saved in web view cookie store
        assert(cookieManager.getCookie(baseUrl).isNullOrEmpty())

        val snippet = TWSSnippet(id = "testId", target = baseUrl)
        // Fetch snippet target content (and cookie), this is NOT fetched in webview
        snippetLoad.response(snippet.target, snippet.headers)

        // Assert that cookie has been synced with web views cookie store
        assert(cookieManager.getCookie(baseUrl).contains("test_cookie=test_value"))
    }
}
