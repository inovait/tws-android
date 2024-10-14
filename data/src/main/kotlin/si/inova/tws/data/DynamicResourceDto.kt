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

package si.inova.tws.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import si.inova.tws.data.ModifierInjectionType.Companion.fromContentType
import java.util.Locale

/**
 * Data class representing a dynamic resource that can be injected into a WebView before the page is loaded.
 *
 * @param url The path to the file to inject into the WebView.
 * @param contentType The type of file to inject,
 * either "text/css" for CSS files or "text/javascript" for JavaScript files or other.
 *
 * @constructor Creates a [DynamicResourceDto] with the provided [url] and [contentType].
 * The [type] is automatically inferred from [contentType] using [ModifierInjectionType].
 * Depending on the [type], the corresponding injection code for CSS or JavaScript is generated.
 *
 * - [type] The type of the resource, inferred from [contentType], which can be CSS, JavaScript, or UNKNOWN.
 * - [inject] The generated HTML code snippet for injecting the resource into a WebView, depending on the [type].
 */
@JsonClass(generateAdapter = true)
@Parcelize
@Keep
data class DynamicResourceDto(
    val url: String,
    val contentType: String
) : Parcelable {
    @IgnoredOnParcel
    val type: ModifierInjectionType = contentType.fromContentType()

    @IgnoredOnParcel
    val inject = when (type) {
        ModifierInjectionType.CSS -> injectUrlCss()
        ModifierInjectionType.JAVASCRIPT -> injectUrlJs()
        ModifierInjectionType.UNKNOWN -> null
    }
}

private fun DynamicResourceDto.injectUrlCss(): String {
    return """<link rel="stylesheet" href="$url">""".trimIndent()
}

private fun DynamicResourceDto.injectUrlJs(): String {
    return """<script src="$url" type="text/javascript"></script>"""
}

/**
 * Enum class representing the types of content injections that can be performed.
 */
enum class ModifierInjectionType {
    @Json(name = "text/css")
    CSS,

    @Json(name = "text/javascript")
    JAVASCRIPT,

    UNKNOWN;

    companion object {
        /**
         * Converts a string content type to a corresponding ModifierInjectionType.
         *
         * [this] The string content type (e.g., "text/css" or "text/javascript").
         * @return The corresponding ModifierInjectionType.
         */
        internal fun String.fromContentType(): ModifierInjectionType {
            return when (lowercase(Locale.getDefault())) {
                "text/css" -> CSS
                "text/javascript" -> JAVASCRIPT
                else -> UNKNOWN
            }
        }
    }
}
