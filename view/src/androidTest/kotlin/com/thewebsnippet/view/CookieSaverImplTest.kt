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
import com.thewebsnippet.view.client.okhttp.cookie.CookieSaver
import com.thewebsnippet.view.client.okhttp.cookie.CookieSaverImpl
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CookieSaverImplTest {
    private lateinit var cookieSaver: CookieSaver
    private lateinit var cookieManager: CookieManager

    @Before
    fun setup() {
        cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)

        cookieSaver = CookieSaverImpl(cookieManager)
    }

    @Test
    fun ensureFullCookieIsStoredToWebViewStore() = runBlocking {
        assert(!cookieManager.hasCookies())

        val cookies = listOf("sessionId=abc123; Path=/", "userId=42; Path=/")
        cookieSaver.saveCookies(testUrl, cookies)

        val stored = cookieManager.getCookie(testUrl.toString())

        assertNotNull("Expected cookies to be stored", stored)
        assertTrue("Expected sessionId cookie", stored!!.contains("sessionId=abc123"))
        assertTrue("Expected userId cookie", stored.contains("userId=42"))
    }

    @Test
    fun ensureCookiesAreAppendedInsteadOfReplaced() = runBlocking {
        cookieSaver.saveCookies(testUrl, listOf("a=1; Path=/"))
        cookieSaver.saveCookies(testUrl, listOf("b=2; Path=/"))

        val stored = cookieManager.getCookie(testUrl.toString())

        assertTrue(stored?.contains("a=1") == true)
        assertTrue(stored?.contains("b=2") == true)
    }
}

private val testUrl = "https://example.com".toHttpUrl()
