package com.thewebsnippet.view.client.okhttp

import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.view.fake.HtmlModifierHelperFake
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SnippetWebLoadImplTest {

    private lateinit var helper: SnippetWebLoadImpl
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        helper = SnippetWebLoadImpl(
            okHttpClient = lazy { OkHttpClient.Builder().build() },
            htmlModifier = HtmlModifierHelperFake()
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `response should return correct metadata when response is successful`() = runTest {
        val baseUrl = server.url("/").toString()
        val snippet = TWSSnippet("test-id", baseUrl)

        val response = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "text/html; charset=UTF-8")
            .setBody("<html>testHtml</html>")

        server.enqueue(response)

        val result = helper.response(snippet)

        assertEquals(baseUrl, result.url)
        assertEquals("text/html", result.mimeType)
        assertEquals("UTF-8", result.encode)
        assertEquals("Modify Added \n<html>testHtml</html>", result.html)
    }
}
