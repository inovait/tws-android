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

import androidx.annotation.Keep
import com.squareup.moshi.Json

/**
 * Represents the processing engine for rendering web content in the TWSView.
 *
 * This enum defines the available content processing engines, determining how the input content
 * is processed and displayed in the SDK's TWSView.
 */
@Keep
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
