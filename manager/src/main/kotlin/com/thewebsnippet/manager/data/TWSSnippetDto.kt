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
