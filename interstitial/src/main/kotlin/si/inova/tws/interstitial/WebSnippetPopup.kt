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

package si.inova.tws.interstitial

import android.content.Context
import android.content.Intent
import si.inova.tws.core.data.WebSnippetData

interface WebSnippetPopup {
    companion object {
        const val WEB_SNIPPET_DATA = "webSnippetData"
        const val WEB_SNIPPET_ID = "webSnippetId"
        const val MANAGER_TAG = "managerTag"

        fun open(context: Context, popup: WebSnippetData) {
            context.startActivity(
                Intent(context, WebSnippetInterstitialActivity::class.java).apply {
                    putExtra(WEB_SNIPPET_DATA, popup)
                }
            )
        }

        fun open(context: Context, popups: List<WebSnippetData>) {
            val intents = popups.map {
                Intent(context, WebSnippetInterstitialActivity::class.java).apply {
                    putExtra(WEB_SNIPPET_DATA, it)
                }
            }.toTypedArray()
            context.startActivities(intents)
        }

        fun open(context: Context, id: String, tag: String? = null) {
            context.startActivity(
                Intent(context, WebSnippetInterstitialActivity::class.java).apply {
                    putExtra(WEB_SNIPPET_ID, id)
                    putExtra(MANAGER_TAG, tag)
                }
            )
        }

        fun open(context: Context, ids: List<String>, tag: String? = null) {
            val intents = ids.map {
                Intent(context, WebSnippetInterstitialActivity::class.java).apply {
                    putExtra(WEB_SNIPPET_ID, it)
                    putExtra(MANAGER_TAG, tag)
                }
            }.toTypedArray()
            context.startActivities(intents)
        }
    }
}
