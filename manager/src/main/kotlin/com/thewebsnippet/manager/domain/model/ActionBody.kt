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

package com.thewebsnippet.manager.domain.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import com.thewebsnippet.data.TWSAttachment
import com.thewebsnippet.data.TWSEngine

@Keep
@JsonClass(generateAdapter = true)
internal data class ActionBody(
    val id: String,
    val organizationId: String? = null,
    val projectId: String? = null,
    val target: String? = null,
    val headers: Map<String, String>? = emptyMap(),
    val visibility: VisibilityDto? = null,
    val dynamicResources: List<TWSAttachment>? = emptyList(),
    val props: Map<String, Any>? = emptyMap(),
    val engine: TWSEngine? = TWSEngine.NONE
)

internal fun ActionBody.isEqual(snippet: TWSSnippetDto): Boolean {
    return target == snippet.target &&
        headers == snippet.headers &&
        visibility == snippet.visibility &&
        dynamicResources == snippet.dynamicResources &&
        props == snippet.props &&
        engine == snippet.engine
}
