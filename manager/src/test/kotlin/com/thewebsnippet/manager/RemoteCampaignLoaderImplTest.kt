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

package com.thewebsnippet.manager

import com.thewebsnippet.manager.core.TWSConfiguration
import com.thewebsnippet.manager.data.datasource.RemoteCampaignLoaderImpl
import com.thewebsnippet.manager.domain.model.TWSSnippetDto
import com.thewebsnippet.manager.fakes.function.FakeTWSSnippetFunction
import com.thewebsnippet.manager.utils.FAKE_SNIPPET_ONE
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

internal class RemoteCampaignLoaderImplTest {
    private lateinit var fakeFunctions: FakeTWSSnippetFunction
    private lateinit var remoteCampaignLoader: RemoteCampaignLoaderImpl

    @Before
    fun setup() {
        fakeFunctions = FakeTWSSnippetFunction()

        remoteCampaignLoader = RemoteCampaignLoaderImpl(
            context = mock(),
            configuration = TWSConfiguration.Basic("id"),
            functions = fakeFunctions
        )
    }

    @Test
    fun `logEventAndGetCampaignSnippets returns empty snippets for specific trigger if no triggers defined`() = runTest {
        val result = remoteCampaignLoader.logEventAndGetCampaignSnippets("purchase")

        assertEquals(emptyList<TWSSnippetDto>(), result)
    }

    @Test
    fun `logEventAndGetCampaignSnippets returns empty snippets for specific trigger if triggers defined`() = runTest {
        fakeFunctions.returnedSnippetsForTrigger = mapOf("purchase1" to listOf(FAKE_SNIPPET_ONE))
        val result = remoteCampaignLoader.logEventAndGetCampaignSnippets("purchase")

        assertEquals(emptyList<TWSSnippetDto>(), result)
    }

    @Test
    fun `logEventAndGetCampaignSnippets returns empty snippets for specific trigger if correct trigger defined`() = runTest {
        fakeFunctions.returnedSnippetsForTrigger = mapOf("purchase" to listOf(FAKE_SNIPPET_ONE))
        val result = remoteCampaignLoader.logEventAndGetCampaignSnippets("purchase")

        assertEquals(listOf(FAKE_SNIPPET_ONE), result)
    }

    @Test
    fun `logEventAndGetCampaignSnippets returns empty list when configuration has no projectId`() = runTest {
        fakeFunctions.returnedSnippetsForTrigger = mapOf("purchase" to listOf(FAKE_SNIPPET_ONE))

        remoteCampaignLoader = RemoteCampaignLoaderImpl(
            context = mock(),
            configuration = TWSConfiguration.Shared("token"),
            functions = fakeFunctions
        )

        val result = remoteCampaignLoader.logEventAndGetCampaignSnippets("purchase")
        assertEquals(emptyList<TWSSnippetDto>(), result)
    }
}
