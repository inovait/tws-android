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
