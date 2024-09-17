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

package si.inova.tws.core.data

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Locale

abstract class ModifierPageData(open val type: ModifierInjectionType): Parcelable {
    abstract val inject: String?
}

@Parcelize
data class ContentInjectData(val content: String, override val type: ModifierInjectionType) : ModifierPageData(type) {
    @IgnoredOnParcel
    override val inject = when (type) {
        ModifierInjectionType.CSS -> injectContentCss()
        ModifierInjectionType.JAVASCRIPT -> content.trimIndent()
        else -> null
    }

    private fun injectContentCss(): String {
        return """<style> $content</style>""".trimIndent()
    }
}

@Parcelize
data class UrlInjectData(val url: String, override val type: ModifierInjectionType) : ModifierPageData(type) {
    @IgnoredOnParcel
    override val inject = when (type) {
        ModifierInjectionType.CSS -> injectUrlCss()
        ModifierInjectionType.JAVASCRIPT -> injectUrlJs()
        else -> null
    }

    private fun injectUrlCss(): String {
        return """<link rel="stylesheet" href="$url">""".trimIndent()
    }

    private fun injectUrlJs(): String {
        return """<script src="$url"/>""".trimIndent()
    }
}

enum class ModifierInjectionType {
    CSS,
    JAVASCRIPT,
    UNKNOWN;

    companion object {
        fun fromContentType(contentType: String): ModifierInjectionType {
            return when (contentType.lowercase(Locale.getDefault())) {
                "text/css" -> CSS
                "text/javascript" -> JAVASCRIPT
                else -> UNKNOWN
            }
        }
    }
}
