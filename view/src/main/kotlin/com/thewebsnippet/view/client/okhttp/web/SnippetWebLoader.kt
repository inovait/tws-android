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

import com.thewebsnippet.view.data.ResponseMetaData

/**
 * Defines the contract for loading and resolving an url into structured content.
 *
 * Implementations are responsible for:
 * - Executing HTTP requests using the given target URL and headers
 * - Following redirects until a final, resolved URL is reached
 * - Optionally parsing or transforming the received content
 * - Returning metadata describing the final result
 *
 * This abstraction decouples higher-level WebView logic from the underlying
 * HTTP/redirect handling and content resolution.
 */
interface SnippetWebLoader {
    /**
     * Loads the given [target] URL, applying any provided [headers],
     * and resolves the final state after redirects.
     *
     * Implementations may also transform or enrich the response before returning.
     *
     * @param target  The initial URL to load.
     * @param headers Optional HTTP headers to include in the request.
     * @return [ResponseMetaData] containing the final resolved URL, MIME type,
     *         encoding, and optionally transformed content.
     */
    suspend fun response(target: String, headers: Map<String, String> = emptyMap()): ResponseMetaData
}
