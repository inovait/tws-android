/*
 * Copyright 2024 INOVA IT d.o.o.
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

package si.inova.tws.manager.data

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Keep
data class WebSnippetDto(
    val id: String,
    val target: String,
    val organizationId: String,
    val projectId: String,
    val html: String? = null,
    val headers: Map<String, String>? = emptyMap(),
    val dynamicResources: List<DynamicResourceDto>? = emptyList(),
    val visibility: VisibilityDto? = null,
    val type: SnippetType = SnippetType.TAB,
    val status: SnippetStatus = SnippetStatus.ENABLED,
    val loadIteration: Int = 0
)

internal fun List<WebSnippetDto>.updateWith(action: SnippetUpdateAction): List<WebSnippetDto> {
    return when (action.type) {
        ActionType.CREATED -> insert(action.data)
        ActionType.UPDATED -> update(action.data)
        ActionType.DELETED -> remove(action.data)
    }
}

internal fun List<WebSnippetDto>.insert(data: ActionBody): List<WebSnippetDto> {
    return toMutableList().apply {
        if (data.target != null && data.organizationId != null && data.projectId != null) {
            add(
                WebSnippetDto(
                    id = data.id,
                    target = data.target,
                    headers = data.headers ?: emptyMap(),
                    organizationId = data.organizationId,
                    projectId = data.projectId,
                    type = data.type ?: SnippetType.TAB
                )
            )
        }
    }
}

internal fun List<WebSnippetDto>.update(data: ActionBody): List<WebSnippetDto> {
    return map {
        if (it.id == data.id) {
            it.copy(
                loadIteration = it.loadIteration + 1,
                target = data.target ?: it.target,
                headers = data.headers ?: it.headers,
                html = data.html ?: it.html
            )
        } else {
            it
        }
    }
}

internal fun List<WebSnippetDto>.remove(data: ActionBody): List<WebSnippetDto> {
    return filter { it.id != data.id }
}
