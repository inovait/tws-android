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

package com.thewebsnippet.view.client.okhttp.web

import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.view.data.ResponseMetaData

/**
 * Defines the contract for loading and transforming a TWS snippet into structured web content.
 *
 * Implementations are responsible for:
 * - Executing HTTP requests (including redirects if needed)
 * - Parsing and modifying HTML content
 * - Returning metadata about the final result
 */
interface SnippetWebLoader {
    /**
     * Loads a given [TWSSnippet], performs necessary network and transformation logic,
     * and returns the resulting HTML and metadata.
     *
     * @param snippet The snippet to load, including target URL, headers, props, and dynamic resources.
     * @return [ResponseMetaData] containing the final URL, MIME type, encoding, and transformed HTML content.
     */
    suspend fun response(snippet: TWSSnippet): ResponseMetaData
}
