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

import com.squareup.moshi.Json

/**
 * Represents the processing engine for rendering web content in the TWSView.
 *
 * This enum defines the available content processing engines, determining how the input content
 * is processed and displayed in the SDK's TWSView.
 */
enum class TWSEngine {
    /**
     * Content will be processed using the Mustache templating engine. The content is rendered dynamically
     * by interpolating the provided properties (`props`) into the Mustache template syntax.
     * This allows for generating custom HTML content based on the data passed to the engine.
     * For more details on Mustache syntax, refer to [Mustache Documentation](https://mustache.github.io/).
     */
    @Json(name = "mustache")
    MUSTACHE,

    /**
     * Content will be displayed as normal static HTML without any additional processing.
     */
    @Json(name = "none")
    NONE,

    /**
     * Indicates that the specified processing engine is not supported by the current SDK version.
     * If this value is encountered, the behavior will default to the same as `NONE`.
     */
    OTHER
}
