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
import com.thewebsnippet.data.TWSSnippet
import kotlinx.parcelize.RawValue

@JsonClass(generateAdapter = true)
@Keep
internal data class TWSSnippetDto(
    val id: String,
    val target: String,
    val organizationId: String,
    val projectId: String,
    val visibility: VisibilityDto? = null,
    val headers: Map<String, String>? = emptyMap(),
    val dynamicResources: List<TWSAttachment> = emptyList(),
    val props: Map<String, @RawValue Any> = emptyMap(),
    val engine: TWSEngine = TWSEngine.NONE,
    val loadIteration: Int = 0
)

internal fun TWSSnippetDto.toTWSSnippet(localProps: Map<String, Any>) = TWSSnippet(
    id = this.id,
    target = this.target,
    headers = this.headers.orEmpty(),
    dynamicResources = this.dynamicResources,
    props = this.props + localProps,
    engine = this.engine,
    loadIteration = this.loadIteration
)

internal fun List<TWSSnippetDto>.toTWSSnippetList(localProps: Map<String, Map<String, Any>>) = map {
    it.toTWSSnippet(localProps[it.id].orEmpty())
}
