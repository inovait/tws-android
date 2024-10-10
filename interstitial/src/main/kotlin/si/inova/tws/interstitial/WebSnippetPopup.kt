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
import si.inova.tws.data.WebSnippetDto

/**
 * Interface that provides methods for opening web snippet popups in the form of interstitials.
 * The popups can be opened with a specific web snippet or with a list of snippets, and the status
 * bar and navigation bar colors can be customized.
 */
interface WebSnippetPopup {
    companion object {
        const val WEB_SNIPPET_DATA = "webSnippetData"
        const val WEB_SNIPPET_ID = "webSnippetId"
        const val MANAGER_TAG = "managerTag"
        const val STATUS_BAR_COLOR = "statusBarColor"
        const val NAVIGATION_BAR_COLOR = "navigationBarColor"

        /**
         * Opens a single web snippet interstitial activity.
         *
         * @param context The context from which the activity is started.
         * @param popup The WebSnippetData containing the web content to be shown in the full screen popup.
         * @param statusBarColor The optional color for the status bar in hexadecimal format.
         * @param navigationBarColor The optional color for the navigation bar in hexadecimal format.
         */
        fun open(
            context: Context,
            popup: WebSnippetDto,
            statusBarColor: String? = null,
            navigationBarColor: String? = null
        ) {
            context.startActivity(
                Intent(context, WebSnippetInterstitialActivity::class.java).apply {
                    putExtra(WEB_SNIPPET_DATA, popup)
                    putExtra(STATUS_BAR_COLOR, statusBarColor)
                    putExtra(NAVIGATION_BAR_COLOR, navigationBarColor)
                }
            )
        }

        /**
         * Opens multiple web snippet interstitial activities with a list of popups.
         * Each snippet will be shown in a separate activity.
         *
         * @param context The context from which the activities are started.
         * @param popups A list of WebSnippetData objects, each containing web content to be shown.
         * @param statusBarColor The optional color for the status bar in hexadecimal format.
         * @param navigationBarColor The optional color for the navigation bar in hexadecimal format.
         */
        fun open(
            context: Context,
            popups: List<WebSnippetDto>,
            statusBarColor: String? = null,
            navigationBarColor: String? = null
        ) {
            val intents = popups.map {
                Intent(context, WebSnippetInterstitialActivity::class.java).apply {
                    putExtra(WEB_SNIPPET_DATA, it)
                    putExtra(STATUS_BAR_COLOR, statusBarColor)
                    putExtra(NAVIGATION_BAR_COLOR, navigationBarColor)
                }
            }.toTypedArray()
            context.startActivities(intents)
        }

        /**
         * Opens a web snippet interstitial activity by snippet ID and optionally a manager tag.
         *
         * @param context The context from which the activity is started.
         * @param id The ID of the web snippet to be shown in the popup.
         * @param tag The optional manager tag to identify the popup manager.
         * If no tag is provided, default tag will be used to obtain shared manager instance.
         * @param statusBarColor The optional color for the status bar in hexadecimal format.
         * @param navigationBarColor The optional color for the navigation bar in hexadecimal format.
         */
        fun open(
            context: Context,
            id: String,
            tag: String? = null,
            statusBarColor: String? = null,
            navigationBarColor: String? = null
        ) {
            context.startActivity(
                Intent(context, WebSnippetInterstitialActivity::class.java).apply {
                    putExtra(WEB_SNIPPET_ID, id)
                    putExtra(MANAGER_TAG, tag)
                    putExtra(STATUS_BAR_COLOR, statusBarColor)
                    putExtra(NAVIGATION_BAR_COLOR, navigationBarColor)
                }
            )
        }

        /**
         * Opens multiple web snippet interstitial activities by their IDs.
         * Each snippet will be shown in a separate activity.
         *
         * @param context The context from which the activities are started.
         * @param ids A list of web snippet IDs to be shown in separate popups.
         * @param tag The optional manager tag to identify the popup manager.
         * If no tag is provided, default tag will be used to obtain shared manager instance.
         * @param statusBarColor The optional color for the status bar in hexadecimal format.
         * @param navigationBarColor The optional color for the navigation bar in hexadecimal format.
         */
        fun open(
            context: Context,
            ids: List<String>,
            tag: String? = null,
            statusBarColor: String? = null,
            navigationBarColor: String? = null
        ) {
            val intents = ids.map {
                Intent(context, WebSnippetInterstitialActivity::class.java).apply {
                    putExtra(WEB_SNIPPET_ID, it)
                    putExtra(MANAGER_TAG, tag)
                    putExtra(STATUS_BAR_COLOR, statusBarColor)
                    putExtra(NAVIGATION_BAR_COLOR, navigationBarColor)
                }
            }.toTypedArray()
            context.startActivities(intents)
        }
    }
}
