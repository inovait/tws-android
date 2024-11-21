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

package si.inova.tws.sample.examples.mustache

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import si.inova.tws.manager.TWSConfiguration
import si.inova.tws.manager.TWSFactory
import si.inova.tws.manager.TWSManager
import si.inova.tws.manager.TWSOutcome
import si.inova.tws.sample.R
import si.inova.tws.sample.components.ErrorView
import si.inova.tws.sample.components.LoadingView
import si.inova.tws.sample.components.TWSViewComponentWithPager

@Composable
fun TWSViewMustacheExample() {
    val context = LocalContext.current
    val manager =
        TWSFactory.get(
            context,
            TWSConfiguration.Basic(organizationId = organizationId, projectId = projectId, apiKey = "apiKey")
        )

    val localProps =
        mapOf(
            "welcome_email" to
                mapOf(
                    "name" to "Alice",
                    "company" to "TheWebSnippet",
                    "guide_url" to "https://mustache.github.io",
                    "community_name" to "TWS dev team",
                    "support_email" to "support@TWS.com"
                )
        )

    // Set local properties
    manager.set("howToMustache", localProps)

    TWSViewMustacheContent(manager)
}

@Composable
private fun TWSViewMustacheContent(
    manager: TWSManager
) {
    // Collect snippets for your project
    val content = manager.snippets.collectAsStateWithLifecycle(null).value

    content?.let {
        when {
            !content.data.isNullOrEmpty() -> {
                val data = content.data ?: return
                TWSViewComponentWithPager(data.toImmutableList())
            }

            content is TWSOutcome.Error -> {
                ErrorView(content.exception.message ?: stringResource(R.string.error_message))
            }

            content is TWSOutcome.Progress -> {
                LoadingView()
            }
        }
    }
}

private const val organizationId = "examples"
private const val projectId = "example2"
