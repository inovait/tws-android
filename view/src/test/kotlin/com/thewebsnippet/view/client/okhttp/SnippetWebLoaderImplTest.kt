package com.thewebsnippet.view.client.okhttp

import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.view.client.okhttp.web.SnippetWebLoaderImpl
import com.thewebsnippet.view.fake.FakeRedirectHandler
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SnippetWebLoaderImplTest {

    private lateinit var helper: SnippetWebLoaderImpl
    private lateinit var server: MockWebServer
    private lateinit var fakeRedirectHandler: FakeRedirectHandler

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        fakeRedirectHandler = FakeRedirectHandler()

        helper = SnippetWebLoaderImpl(redirectHandler = lazy { fakeRedirectHandler })
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `response should return correct metadata when response is successful`() = runTest {
        fakeRedirectHandler.fakeBody = "<html>testHtml</html>"

        val baseUrl = server.url("/").toString()
        val snippet = TWSSnippet("test-id", baseUrl)

        val result = helper.response(snippet)

        assertEquals(baseUrl, result.url)
    }
}
