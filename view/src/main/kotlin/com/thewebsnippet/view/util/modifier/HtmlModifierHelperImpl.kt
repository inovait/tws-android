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
package com.thewebsnippet.view.util.modifier

import android.os.Build
import com.samskivert.mustache.Mustache
import com.thewebsnippet.data.TWSAttachment
import com.thewebsnippet.data.TWSAttachmentType
import com.thewebsnippet.data.TWSEngine
import com.thewebsnippet.view.BuildConfig

internal class HtmlModifierHelperImpl : HtmlModifierHelper {
    /**
     * Modify the HTML content by injecting CSS, JavaScript, and applying Mustache templates.
     *
     * @param htmlContent The raw HTML content to modify.
     * @param dynamicModifiers A list of [TWSAttachment] representing both CSS and JavaScript to inject.
     * @param mustacheProps A map of properties used for Mustache templating.
     * @param engine The type of engine used for parsing HTML content.
     * @return The modified HTML content.
     */
    override fun modifyContent(
        htmlContent: String,
        dynamicModifiers: List<TWSAttachment>,
        mustacheProps: Map<String, Any>,
        engine: TWSEngine?
    ): String {
        // Filter the dynamic modifiers into CSS and JS lists
        val cssModifiers = dynamicModifiers.filter { it.contentType == TWSAttachmentType.CSS }
        val jsModifiers = dynamicModifiers.filter { it.contentType == TWSAttachmentType.JAVASCRIPT }

        return htmlContent
            .processAsMustache(mustacheProps, engine)
            .insertCss(cssModifiers)
            .insertJs(jsModifiers)
    }

    override fun getMimeTypeAndEncoding(contentType: String): Pair<String, String> {
        val mimeType = contentType.substringBefore(";").trim()
        val encoding = contentType.substringAfter("charset=", "UTF-8").trim()

        return Pair(mimeType, encoding)
    }

    private fun String.insertCss(cssModifiers: List<TWSAttachment>): String {
        val combinedCssInjection = cssModifiers.joinToString(separator = "") { it.inject.orEmpty() }.trimIndent()

        return if (contains("</head>")) {
            replaceFirst("</head>", """$combinedCssInjection</head>""")
        } else if (contains("</body>")) {
            replaceFirst("</body>", """$combinedCssInjection</body>""")
        } else {
            val htmlTagRegex = Regex("<html(\\s[^>]*)?>", RegexOption.IGNORE_CASE)
            if (htmlTagRegex.containsMatchIn(this)) {
                replaceFirst(htmlTagRegex, """$0$combinedCssInjection""")
            } else {
                "$combinedCssInjection$this"
            }
        }
    }

    private fun String.insertJs(jsModifiers: List<TWSAttachment>): String {
        val combinedJsInjection = STATIC_INJECT_DATA + jsModifiers.joinToString(separator = "") {
            it.inject.orEmpty()
        }.trimIndent()

        return if (contains("<head>")) {
            replaceFirst("<head>", """<head>$combinedJsInjection""")
        } else if (contains("<body>")) {
            replaceFirst("<body>", """<body>$combinedJsInjection""")
        } else {
            val htmlTagRegex = Regex("<html(\\s[^>]*)?>", RegexOption.IGNORE_CASE)
            if (htmlTagRegex.containsMatchIn(this)) {
                replaceFirst(htmlTagRegex, """$0$combinedJsInjection""")
            } else {
                "$combinedJsInjection$this"
            }
        }
    }

    private fun String.processAsMustache(
        mustacheProps: Map<String, Any>,
        engine: TWSEngine?
    ): String {
        if (engine != TWSEngine.MUSTACHE) return this

        return Mustache.compiler()
            .defaultValue("")
            .escapeHTML(false)
            .compile(this)
            .execute(mustacheProps + MUSTACHE_SYSTEM_DEFAULTS)
    }

    companion object {
        private val STATIC_INJECT_DATA = """<script type="text/javascript">var tws_injected = true;</script>""".trimIndent()

        private val MUSTACHE_SYSTEM_DEFAULTS = mapOf(
            "version" to BuildConfig.TWS_VERSION,
            "device" to mapOf(
                "vendor" to Build.MANUFACTURER,
                "name" to Build.DEVICE
            ),
            "os" to mapOf(
                "version" to Build.VERSION.RELEASE
            )
        )
    }
}
