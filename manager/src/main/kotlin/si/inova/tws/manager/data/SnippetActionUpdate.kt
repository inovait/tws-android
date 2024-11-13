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
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import si.inova.tws.data.TWSAttachment
import si.inova.tws.data.TWSEngine

/**
 * Data class representing an action to be returned on a WebSocket update.
 *
 * @property type The type of action being performed, represented by [ActionType].
 * @property data The content of the action, represented by [ActionBody], which contains the relevant information for the update.
 *
 */
@Keep
@JsonClass(generateAdapter = true)
internal data class SnippetUpdateAction(
    val type: ActionType,
    val data: ActionBody
)

/**
 *
 * Defines the type of action performed on a snippet.
 *
 * - [CREATED] indicates that a new snippet has been created.
 * - [UPDATED] indicates that an existing snippet has been updated.
 * - [DELETED] indicates that an existing snippet has been removed.
 */
@Keep
@JsonClass(generateAdapter = false)
internal enum class ActionType {
    @Json(name = "SNIPPET_CREATED")
    CREATED,

    @Json(name = "SNIPPET_UPDATED")
    UPDATED,

    @Json(name = "SNIPPET_DELETED")
    DELETED
}

@Keep
@JsonClass(generateAdapter = true)
internal data class ActionBody(
    val id: String,
    val organizationId: String? = null,
    val projectId: String? = null,
    val target: String? = null,
    val headers: Map<String, String>? = null,
    val visibility: VisibilityDto? = null,
    val dynamicResources: List<TWSAttachment>? = null,
    val props: Map<String, Any>? = null,
    val engine: TWSEngine? = null
)

internal fun ActionBody.isEmpty(): Boolean {
    return target == null && headers == null && visibility == null && dynamicResources == null && props == null && engine == null
}

internal fun List<TWSSnippetDto>.updateWith(action: SnippetUpdateAction): List<TWSSnippetDto> {
    return when (action.type) {
        ActionType.CREATED -> insert(action.data)
        ActionType.UPDATED -> update(action.data)
        ActionType.DELETED -> remove(action.data)
    }
}

internal fun List<TWSSnippetDto>.insert(data: ActionBody): List<TWSSnippetDto> {
    return toMutableList().apply {
        if (data.target != null) {
            add(
                TWSSnippetDto(
                    id = data.id,
                    target = data.target,
                    headers = data.headers.orEmpty(),
                    organizationId = data.organizationId.orEmpty(),
                    projectId = data.projectId.orEmpty(),
                    visibility = data.visibility,
                    dynamicResources = data.dynamicResources.orEmpty(),
                    props = data.props.orEmpty()
                )
            )
        }
    }
}

internal fun List<TWSSnippetDto>.update(data: ActionBody): List<TWSSnippetDto> {
    return map {
        if (it.id == data.id) {
            if (data.isEmpty()) {
                // html has changed, increase load iteration
                it.copy(loadIteration = it.loadIteration + 1)
            } else {
                // a property has changed
                it.copy(
                    target = data.target ?: it.target,
                    headers = data.headers ?: it.headers,
                    visibility = data.visibility ?: it.visibility,
                    dynamicResources = data.dynamicResources ?: it.dynamicResources,
                    props = data.props ?: it.props,
                    engine = data.engine ?: it.engine
                )
            }
        } else {
            it
        }
    }
}

internal fun List<TWSSnippetDto>.remove(data: ActionBody): List<TWSSnippetDto> {
    return filter { it.id != data.id }
}
