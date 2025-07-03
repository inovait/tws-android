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

package com.thewebsnippet.manager.data.datasource

import android.util.Log
import com.thewebsnippet.manager.core.TWSConfiguration
import com.thewebsnippet.manager.data.function.TWSSnippetFunction
import com.thewebsnippet.manager.domain.datasource.RemoteCampaignLoader
import com.thewebsnippet.manager.domain.model.EventBody
import com.thewebsnippet.manager.domain.model.TWSSnippetDto

internal class RemoteCampaignLoaderImpl(
    private val configuration: TWSConfiguration,
    private val functions: TWSSnippetFunction
) : RemoteCampaignLoader {

    override suspend fun logEventAndGetCampaignSnippets(
        name: String
    ): List<TWSSnippetDto> {
        return try {
            val projectId = (configuration as? TWSConfiguration.Basic)?.projectId
                ?: return emptyList()
            val snippets = functions.logEventAndGetCampaignSnippets(
                projectId,
                EventBody(event = name)
            ).snippets

            snippets
        } catch (e: Exception) {
            Log.e("RemoteCampaignLoaderImpl", "Error logging event", e)
            emptyList()
        }
    }
}
