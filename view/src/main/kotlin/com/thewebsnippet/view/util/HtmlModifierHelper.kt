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
package com.thewebsnippet.view.util

import android.os.Build
import com.samskivert.mustache.Mustache
import com.thewebsnippet.data.TWSAttachment
import com.thewebsnippet.data.TWSAttachmentType
import com.thewebsnippet.data.TWSEngine
import com.thewebsnippet.view.BuildConfig

internal class HtmlModifierHelper {

    /**
     * Modify the HTML content by injecting CSS, JavaScript, and applying Mustache templates.
     *
     * @param htmlContent The raw HTML content to modify.
     * @param dynamicModifiers A list of [TWSAttachment] representing both CSS and JavaScript to inject.
     * @param mustacheProps A map of properties used for Mustache templating.
     * @param engine The type of engine used for parsing HTML content.
     * @return The modified HTML content.
     */
    fun modifyContent(
        htmlContent: String,
        dynamicModifiers: List<TWSAttachment>,
        mustacheProps: Map<String, Any>,
        engine: TWSEngine? = null
    ): String {
        // Filter the dynamic modifiers into CSS and JS lists
        val cssModifiers = dynamicModifiers.filter { it.contentType == TWSAttachmentType.CSS }
        val jsModifiers = dynamicModifiers.filter { it.contentType == TWSAttachmentType.JAVASCRIPT }

        return htmlContent
            .processAsMustache(mustacheProps, engine)
            .insertCss(cssModifiers)
            .insertJs(jsModifiers)
    }

    private fun String.insertCss(cssModifiers: List<TWSAttachment>): String {
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

    private fun String.insertJs(jsModifiers: List<TWSAttachment>): String {
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
