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
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Represents a dynamic resource that can be injected into a WebView.
 *
 * @param url The URL of the resource to inject.
 * @param contentType The type of resource, such as CSS or JavaScript.
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class TWSAttachment(
    val url: String,
    val contentType: TWSAttachmentType
) : Parcelable {
    @IgnoredOnParcel
    val inject = when (contentType) {
        TWSAttachmentType.CSS -> injectUrlCss()
        TWSAttachmentType.JAVASCRIPT -> injectUrlJs()
        TWSAttachmentType.OTHER -> null
    }
}

private fun TWSAttachment.injectUrlCss(): String {
    return """<link rel="stylesheet" href="$url">""".trimIndent()
}

private fun TWSAttachment.injectUrlJs(): String {
    return """<script src="$url" type="text/javascript"></script>"""
}
