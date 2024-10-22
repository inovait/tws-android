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

package si.inova.tws.core.util

import com.samskivert.mustache.Mustache
import si.inova.tws.data.DynamicResourceDto
import si.inova.tws.data.ModifierInjectionType

class HtmlModifierHelper {

    /**
     * Modify the HTML content by injecting CSS, JavaScript, and applying Mustache templates.
     *
     * @param htmlContent The raw HTML content to modify.
     * @param dynamicModifiers A list of [DynamicResourceDto] representing both CSS and JavaScript to inject.
     * @param mustacheProps A map of properties used for Mustache templating.
     * @return The modified HTML content.
     */
    fun modifyContent(
        htmlContent: String,
        dynamicModifiers: List<DynamicResourceDto>,
        mustacheProps: Map<String, Any>
    ): String {
        // Filter the dynamic modifiers into CSS and JS lists
        val cssModifiers = dynamicModifiers.filter { it.type == ModifierInjectionType.CSS }
        val jsModifiers = dynamicModifiers.filter { it.type == ModifierInjectionType.JAVASCRIPT }

        return htmlContent.processAsMustache(mustacheProps).insertCss(cssModifiers).insertJs(jsModifiers)
    }

    private fun String.insertCss(cssModifiers: List<DynamicResourceDto>): String {
        val combinedCssInjection = cssModifiers.joinToString(separator = "") { it.inject.orEmpty() }.trimIndent()

        return if (contains("</head>")) {
            replaceFirst("</head>", """$combinedCssInjection</head>""")
        } else {
            val htmlTagRegex = Regex("<html(\\s[^>]*)?>", RegexOption.IGNORE_CASE)
            if (htmlTagRegex.containsMatchIn(this)) {
                replaceFirst(htmlTagRegex, """$0$combinedCssInjection""")
            } else {
                "$combinedCssInjection$this"
            }
        }
    }

    private fun String.insertJs(jsModifiers: List<DynamicResourceDto>): String {
        val combinedJsInjection = STATIC_INJECT_DATA + jsModifiers.joinToString(separator = "") {
            it.inject.orEmpty()
        }.trimIndent()

        return if (contains("<head>")) {
            replaceFirst("<head>", """<head>$combinedJsInjection""")
        } else {
            val htmlTagRegex = Regex("<html(\\s[^>]*)?>", RegexOption.IGNORE_CASE)
            if (htmlTagRegex.containsMatchIn(this)) {
                replaceFirst(htmlTagRegex, """$0$combinedJsInjection""")
            } else {
                "$combinedJsInjection$this"
            }
        }
    }

    private fun String.processAsMustache(mustacheProps: Map<String, Any>): String {
        return if (mustacheProps.isEmpty()) {
            this
        } else {
            Mustache.compiler()
                .defaultValue("")
                .escapeHTML(false)
                .compile(this)
                .execute(mustacheProps)
        }
    }

    companion object {
        private val STATIC_INJECT_DATA = """<script type="text/javascript">var tws_injected = true;</script>""".trimIndent()
    }
}
