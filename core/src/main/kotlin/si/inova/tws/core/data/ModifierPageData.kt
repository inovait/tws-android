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

/**
 * Abstract class representing a modifier page data that holds information
 * about different types of content injections (CSS or JavaScript).
 *
 * @property type The type of the modifier injection.
 */
abstract class ModifierPageData(open val type: ModifierInjectionType): Parcelable {
    abstract val inject: String?
}

/**
 * Data class representing a content injection that includes either JavaScript or CSS content.
 *
 * @property content The actual content (JavaScript or CSS) to be injected.
 * @property type The type of modifier injection (either CSS or JavaScript).
 *
 * @see ModifierInjectionType
 */
@Parcelize
data class ContentInjectData(val content: String, override val type: ModifierInjectionType) : ModifierPageData(type) {
    @IgnoredOnParcel
    override val inject = when (type) {
        ModifierInjectionType.CSS -> injectContentCss()
        ModifierInjectionType.JAVASCRIPT -> injectContentJs()
        else -> null
    }

    private fun injectContentJs(): String {
        return """<script type="text/javascript">$content</script>""".trimIndent()
    }

    private fun injectContentCss(): String {
        return """<style>$content</style>""".trimIndent()
    }
}

/**
 * Data class representing a URL injection that includes either a link to a JavaScript file or a CSS stylesheet.
 *
 * @property url The URL of the JavaScript or CSS file to be injected.
 * @property type The type of modifier injection (either CSS or JavaScript).
 */
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
        return """<script src="$url" type="text/javascript"></script>"""
    }
}

/**
 * Enum class representing the types of content injections that can be performed.
 */
enum class ModifierInjectionType {
    CSS,
    JAVASCRIPT,
    UNKNOWN;

    companion object {
        /**
         * Converts a string content type to a corresponding ModifierInjectionType.
         *
         * @param contentType The string content type (e.g., "text/css" or "text/javascript").
         * @return The corresponding ModifierInjectionType.
         */
        fun fromContentType(contentType: String): ModifierInjectionType {
            return when (contentType.lowercase(Locale.getDefault())) {
                "text/css" -> CSS
                "text/javascript" -> JAVASCRIPT
                else -> UNKNOWN
            }
        }
    }
}
