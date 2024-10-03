/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.tws.core.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The WebSnippetData class serves as a data model used to encapsulate information necessary for displaying web
 * content within a WebSnippetComponent or TabWebSnippetComponent. It holds essential properties such as the URL,
 * HTTP headers, and additional configuration options such as modifiers and tab content resources.
 *
 * @property id A unique identifier for the web snippet. This ID helps distinguish between different snippets
 * and can be used for internal references or tracking.
 * @property url The URL of the web page or content to be loaded in the WebView. This is the core property
 * that directs the web snippet to load specific web content.
 * @property headers A map of additional HTTP headers to include when loading the web content. This can be used to add custom
 * authentication, tracking headers, or other metadata needed for the request.
 * @property dynamicModifiers A list of ModifierPageData objects that define dynamic modifications or behaviors that will be
 * applied to the web content. These could represent things like adjusting styles, scripts, or interacting with specific
 * parts of the page. Note that those coule be applied only if used in combination with OkHttpTwsWebViewClient in WebView.
 * @property tabContentResources Optional tab-specific resources that can be applied to the web snippet. Useful only fot
 * TabWebSnippetComponent, where this parameter defines the content of the tab.
 * @property loadIteration  An integer used to force reloading of the web content when needed. When this value changes, the
 * WebView will reload the specified URL. This can be useful in scenarios where dynamic content needs to be refreshed
 * without changing the actual URL.
 */
@Parcelize
data class WebSnippetData(
    val id: String,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val dynamicModifiers: List<ModifierPageData> = emptyList(),
    val tabContentResources: TabContentResources? = null,
    val loadIteration: Int = 0
) : Parcelable
