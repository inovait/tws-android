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

import android.webkit.CookieManager
import com.thewebsnippet.view.client.okhttp.cookie.SharedCookieJar
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SharedCookieJarTest {
    private val testUrl = "https://example.com".toHttpUrl()

    private lateinit var cookieManager: CookieManager
    private lateinit var cookieJar: SharedCookieJar

    @Before
    fun setUp() {
        cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.removeAllCookies(null)
        cookieManager.flush()

        cookieJar = SharedCookieJar(cookieManager)
    }

    @After
    fun tearDown() {
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }

    @Test
    fun ensureAllCookiesAreReturnedForRequest() = runTest {
        // Manually set the cookies
        cookieManager.setCookie(testUrl.toString(), "sessionId=abc123; Path=/")
        cookieManager.setCookie(testUrl.toString(), "userId=42; Path=/")
        cookieManager.flush()

        val cookies = cookieJar.loadForRequest(testUrl)

        assertEquals(2, cookies.size)

        val sessionId = cookies.find { it.name == "sessionId" }
        val userId = cookies.find { it.name == "userId" }

        assertNotNull(sessionId)
        assertEquals("abc123", sessionId?.value)

        assertNotNull(userId)
        assertEquals("42", userId?.value)
    }

    @Test
    fun ensureNoCookiesAreReturnedIfNonePresent() = runTest{
        val cookies = cookieJar.loadForRequest(testUrl)
        assertTrue(cookies.isEmpty())
    }

    @Test
    fun ensureMalformedCookiesAreIgnored() = runTest {
        cookieManager.setCookie(testUrl.toString(), "valid=ok; Path=/")
        cookieManager.setCookie(testUrl.toString(), "broken-cookie-without-equals-sign")
        cookieManager.flush()

        val cookies = cookieJar.loadForRequest(testUrl)

        assertEquals(1, cookies.size)
        assertEquals("valid", cookies[0].name)
        assertEquals("ok", cookies[0].value)
    }
}
