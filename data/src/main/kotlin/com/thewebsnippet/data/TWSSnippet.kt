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
package com.thewebsnippet.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Represents a web snippet that can be rendered in a TWSView.
 *
 * @param id A unique identifier for the snippet.
 * @param target The URL of the snippets content.
 * @param headers custom HTTP headers to include with the request.
 * @param dynamicResources A list of resources (CSS/JS) to inject.
 * @param props Custom properties for the snippet. These properties can be used for a variety of purposes,
 * such as displaying a snippet's title, sorting snippets by a sort key, or providing additional metadata
 * for snippet handling. If the [engine] is set to [TWSEngine.MUSTACHE], all provided properties are also used
 * for Mustache template processing while displaying snippet in TWSView.
 * @param engine Specifies how the snippet content is processed.
 * @param loadIteration A counter to manually trigger a redraw of the snippet in `TWSView`.
 * Useful when the HTML content changes but the snippet itself remains unchanged.
 * Incrementing this forces `TWSView` to redraw and reflect the updated HTML.
 */
@Parcelize
data class TWSSnippet(
    val id: String,
    val target: String,
    val headers: Map<String, String> = emptyMap(),
    val dynamicResources: List<TWSAttachment> = emptyList(),
    val props: Map<String, @RawValue Any> = emptyMap(),
    val engine: TWSEngine = TWSEngine.NONE,
    val loadIteration: Int = 0
) : Parcelable
