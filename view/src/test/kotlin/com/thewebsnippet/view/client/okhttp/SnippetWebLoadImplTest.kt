package com.thewebsnippet.view.client.okhttp

import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.view.fake.HtmlModifierHelperFake
import com.thewebsnippet.view.util.modifier.HtmlModifierHelper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SnippetWebLoadImplTest {

    private lateinit var helper: SnippetWebLoadImpl

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var htmlModifierHelper: HtmlModifierHelper

    @Before
    fun setUp() {
        okHttpClient = mockk()
        htmlModifierHelper = HtmlModifierHelperFake()

        helper = SnippetWebLoadImpl(lazy { okHttpClient }, htmlModifierHelper)
    }

    @Test
    fun `response should return correct metadata when response is successful`() = runTest {
        val snippet = TWSSnippet("test-id", "https://www.example.com")

        val requestSlot = slot<Request>()

        val mockResponse = mockk<Response>()
        val mockResponseBody = mockk<ResponseBody>()

        every { okHttpClient.newCall(capture(requestSlot)).execute() } returns mockResponse
        every { mockResponse.request } returns Request.Builder().url(snippet.target).build()
        every { mockResponse.header("Content-Type") } returns "text/html; charset=UTF-8"
        every { mockResponse.body } returns mockResponseBody
        every { mockResponseBody.string() } returns "testHtml"
        every { mockResponse.close() } just Runs

        val result = helper.response(snippet)

        assertEquals("https://www.example.com/", result.url)
        assertEquals("text/html", result.mimeType)
        assertEquals("UTF-8", result.encode)
        assertEquals("Modify Added \ntestHtml", result.html)
    }
}
