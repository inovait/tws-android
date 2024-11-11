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

package si.inova.tws.manager

import android.content.Context
import jakarta.inject.Singleton
import java.lang.ref.WeakReference
import java.util.WeakHashMap

@Singleton
object TWSFactory {
    private val instances = WeakHashMap<String, WeakReference<TWSManager>>()

    fun get(context: Context, configuration: TWSConfiguration.Basic): TWSManager {
        return createOrGet(context, "${configuration.organizationId}_${configuration.projectId}", configuration)
    }

    fun get(context: Context, configuration: TWSConfiguration.Shared): TWSManager {
        return createOrGet(context, configuration.sharedId, configuration)
    }

    fun get(tag: String): TWSManager? {
        return instances[tag]?.get()
    }

    private fun createOrGet(
        context: Context,
        tag: String,
        configuration: TWSConfiguration
    ): TWSManager {
        val existingInstance = instances[tag]?.get()

        return if (existingInstance != null) {
            existingInstance
        } else {
            val newInstance = TWSManagerImpl(context, tag, configuration)
            instances[tag] = WeakReference(newInstance)
            newInstance
        }
    }
}

sealed class TWSConfiguration(open val apiKey: String) {
    data class Basic(
        val organizationId: String,
        val projectId: String,
        override val apiKey: String
    ) : TWSConfiguration(apiKey)

    data class Shared(
        val sharedId: String,
        override val apiKey: String
    ) : TWSConfiguration(apiKey)
}
