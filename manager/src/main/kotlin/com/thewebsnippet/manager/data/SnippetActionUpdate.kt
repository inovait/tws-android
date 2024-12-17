/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thewebsnippet.manager.data

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.thewebsnippet.data.TWSAttachment
import com.thewebsnippet.data.TWSEngine
import com.thewebsnippet.manager.data.ActionType.CREATED
import com.thewebsnippet.manager.data.ActionType.DELETED
import com.thewebsnippet.manager.data.ActionType.UPDATED

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
    @Json(name = "snippetCreated")
    CREATED,

    @Json(name = "snippetUpdated")
    UPDATED,

    @Json(name = "snippetDeleted")
    DELETED
}

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

internal fun List<TWSSnippetDto>.updateWith(action: SnippetUpdateAction): List<TWSSnippetDto> {
    return when (action.type) {
        CREATED -> insert(action.data)
        UPDATED -> update(action.data)
        DELETED -> remove(action.data)
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
                    props = data.props.orEmpty(),
                    engine = data.engine ?: TWSEngine.NONE
                )
            )
        }
    }
}

internal fun List<TWSSnippetDto>.update(data: ActionBody): List<TWSSnippetDto> {
    return map {
        if (it.id == data.id) {
            if (data.isEqual(it)) {
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
