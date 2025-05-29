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

package com.thewebsnippet.view.client.okhttp

import com.thewebsnippet.view.client.okhttp.web.RedirectHandlerImpl
import com.thewebsnippet.view.fake.FakeCookieSaver
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RedirectHandlerImplTest {
    private lateinit var redirectHandler: RedirectHandlerImpl
    private lateinit var mockWebServer: MockWebServer
    private lateinit var cookieSaver: FakeCookieSaver

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        cookieSaver = FakeCookieSaver()

        redirectHandler = RedirectHandlerImpl(
            client = OkHttpClient.Builder().followRedirects(false).followSslRedirects(false).build(),
            cookieSaver = cookieSaver
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `execute follows relative redirects and saves cookies`() {
        val baseUrl = mockWebServer.url("/").toString()

        // First response: 302 redirect
        val redirectResponse = MockResponse()
            .setResponseCode(302)
            .addHeader("Location", "/final")
            .addHeader("Set-Cookie", "first=session1")

        // Final response: 200 OK
        val finalResponse = MockResponse()
            .setResponseCode(200)
            .addHeader("Set-Cookie", "second=session2")
            .setBody("<html>done</html>")

        mockWebServer.enqueue(redirectResponse)
        mockWebServer.enqueue(finalResponse)

        val request = Request.Builder().url(baseUrl).build()
        val response = redirectHandler.execute(request)

        assertEquals(200, response.code)
        assertTrue(response.body?.string()?.contains("done") == true)

        // Check that both cookies were saved
        assertEquals(2, cookieSaver.savedCookies.size)

        val firstCookies = cookieSaver.savedCookies[0].second
        val secondCookies = cookieSaver.savedCookies[1].second

        assertTrue(firstCookies.any { it.contains("first=session1") })
        assertTrue(secondCookies.any { it.contains("second=session2") })
    }

    @Test
    fun `execute follows absolute redirects and saves cookies`() {
        val baseUrl = mockWebServer.url("/").toString()

        // Step 1: Respond with absolute redirect
        val redirectResponse = MockResponse()
            .setResponseCode(302)
            .addHeader("Location", "$baseUrl/final")
            .addHeader("Set-Cookie", "first=session1")

        // Step 2: Final response
        val finalResponse = MockResponse()
            .setResponseCode(200)
            .addHeader("Set-Cookie", "second=session2")
            .setBody("<html>redirected absolute</html>")

        mockWebServer.enqueue(redirectResponse)
        mockWebServer.enqueue(finalResponse)

        val request = Request.Builder()
            .url("$baseUrl/start")
            .build()

        val response = redirectHandler.execute(request)

        assertEquals(200, response.code)
        assertTrue(response.body?.string()?.contains("redirected absolute") == true)

        // Check that both cookies were saved
        assertEquals(2, cookieSaver.savedCookies.size)

        val firstCookies = cookieSaver.savedCookies[0].second
        val secondCookies = cookieSaver.savedCookies[1].second

        assertTrue(firstCookies.any { it.contains("first=session1") })
        assertTrue(secondCookies.any { it.contains("second=session2") })
    }

    @Test
    fun `execute follows multiple absolute redirects and saves all cookies`() {
        val baseUrl = mockWebServer.url("/").toString()

        // Step 1: First redirect with a cookie
        val firstRedirect = MockResponse()
            .setResponseCode(302)
            .addHeader("Location", "$baseUrl/step2")
            .addHeader("Set-Cookie", "first=session1")

        // Step 2: Second redirect with another cookie
        val secondRedirect = MockResponse()
            .setResponseCode(302)
            .addHeader("Location", "$baseUrl/final")
            .addHeader("Set-Cookie", "second=session2")

        // Step 3: Final response with another cookie
        val finalResponse = MockResponse()
            .setResponseCode(200)
            .addHeader("Set-Cookie", "third=session3")
            .setBody("<html>final destination</html>")

        mockWebServer.enqueue(firstRedirect)
        mockWebServer.enqueue(secondRedirect)
        mockWebServer.enqueue(finalResponse)

        val request = Request.Builder()
            .url("$baseUrl/start")
            .build()

        val response = redirectHandler.execute(request)

        assertEquals(200, response.code)
        assertTrue(response.body?.string()?.contains("final destination") == true)

        // Check that all cookies from redirects were saved
        assertEquals(3, cookieSaver.savedCookies.size)

        val firstCookies = cookieSaver.savedCookies[0].second
        val secondCookies = cookieSaver.savedCookies[1].second
        val thirdCookies = cookieSaver.savedCookies[2].second

        assertTrue(firstCookies.any { it.contains("first=session1") })
        assertTrue(secondCookies.any { it.contains("second=session2") })
        assertTrue(thirdCookies.any { it.contains("third=session3") })
    }

    @Test
    fun `execute follows mixed absolute and relative redirects and saves all cookies`() {
        val baseUrl = mockWebServer.url("/").toString()

        // Step 1: Absolute redirect with cookie
        val firstRedirect = MockResponse()
            .setResponseCode(302)
            .addHeader("Location", "$baseUrl/step2")
            .addHeader("Set-Cookie", "first=session1")

        // Step 2: Relative redirect with cookie
        val secondRedirect = MockResponse()
            .setResponseCode(302)
            .addHeader("Location", "/final")
            .addHeader("Set-Cookie", "second=session2")

        // Step 3: Final response with cookie
        val finalResponse = MockResponse()
            .setResponseCode(200)
            .addHeader("Set-Cookie", "third=session3")
            .setBody("<html>mixed redirect complete</html>")

        mockWebServer.enqueue(firstRedirect)
        mockWebServer.enqueue(secondRedirect)
        mockWebServer.enqueue(finalResponse)

        val request = Request.Builder()
            .url("$baseUrl/start")
            .build()

        val response = redirectHandler.execute(request)

        assertEquals(200, response.code)
        assertTrue(response.body?.string()?.contains("mixed redirect complete") == true)

        // Ensure all cookies were saved
        assertEquals(3, cookieSaver.savedCookies.size)

        val firstCookies = cookieSaver.savedCookies[0].second
        val secondCookies = cookieSaver.savedCookies[1].second
        val thirdCookies = cookieSaver.savedCookies[2].second

        assertTrue(firstCookies.any { it.contains("first=session1") })
        assertTrue(secondCookies.any { it.contains("second=session2") })
        assertTrue(thirdCookies.any { it.contains("third=session3") })
    }
}
