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

package com.thewebsnippet.manager

import android.content.Context

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

/**
 * Entry point for initializing and accessing the TWS SDK.
 *
 * This singleton provides a globally accessible [TWSManager] instance, which must be initialized
 * before use via [initialize]. Depending on your use case, you can provide a custom configuration
 * ([TWSConfiguration.Basic] or [TWSConfiguration.Shared]), or let the SDK load it from the
 * `AndroidManifest.xml` metadata.
 *
 * ### Example Usage
 * ```kotlin
 * // Default initialization using manifest metadata
 * TWSSdk.initialize(context)
 * val manager = TWSSdk.getInstance()
 *
 * // OR initialize with a specific configuration
 * val config = TWSConfiguration.Basic("your_project_id")
 * TWSSdk.initialize(context, config)
 * val manager = TWSSdk.getInstance()
 * ```
 *
 * Warning: Must be initialized before calling [getInstance], otherwise a no-op implementation is returned.
 */
object TWSSdk {
    private var manager: TWSManager = NoOpManager()

    /**
     * Retrieves the current [TWSManager] instance.
     *
     * @return The initialized [TWSManager], or a no-op implementation if [initialize] was not called.
     */
    fun getInstance(): TWSManager = manager

    /**
     * Initializes the SDK with an optional [TWSConfiguration].
     *
     * If no configuration is provided, it attempts to use the `projectId` defined in
     * the `AndroidManifest.xml` metadata.
     *
     * @param context The application context.
     * @param configuration Optional configuration for the SDK. If null, uses default metadata-based setup.
     */
    fun initialize(context: Context, configuration: TWSConfiguration? = null) {
        manager = configuration?.let {
            when (configuration) {
                is TWSConfiguration.Basic -> TWSFactory.get(context, configuration)
                is TWSConfiguration.Shared -> TWSFactory.get(context, configuration)
            }
        } ?: TWSFactory.get(context)

        manager.register()
    }
}
