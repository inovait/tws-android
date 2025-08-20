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

package com.thewebsnippet.manager.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.manager.core.TWSFactory
import com.thewebsnippet.manager.core.TWSOutcome
import com.thewebsnippet.manager.core.mapData
import com.thewebsnippet.view.TWSView
import com.thewebsnippet.view.data.TWSLoadingState
import com.thewebsnippet.view.util.compose.SnippetLoadingView
import com.thewebsnippet.view.util.compose.error.SnippetErrorView

internal class TWSViewPopupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val projectId = intent.getStringExtra(EXTRA_PROJECT_ID)
        val snippetId = intent.getStringExtra(EXTRA_SNIPPET_ID)

        @Suppress("DEPRECATION")
        val snippet = intent.getParcelableExtra<TWSSnippet>(EXTRA_CAMPAIGN_SNIPPET)

        if ((projectId.isNullOrBlank() || snippetId.isNullOrBlank()) && snippet == null) {
            // both values are required

            finish()
            return
        }

        setContent {
            if (snippet != null) {
                TWSView(
                    modifier = Modifier.fillMaxSize(),
                    snippet = snippet
                )
            } else if (projectId != null && snippetId != null) {
                ContentWithManager(projectId, snippetId)
            }
        }
    }

    @Composable
    private fun ContentWithManager(projectId: String, snippetId: String) {
        val manager = remember {
            TWSFactory.get(projectId)
        }

        if (manager != null) {
            val pushSnippetOutcome = manager.snippets
                .collectAsStateWithLifecycle(TWSOutcome.Progress())
                .value
                .mapData { list -> list.find { it.id == snippetId } }

            SnippetPopupScreen(pushSnippetOutcome)
        } else {
            // display custom error?
            finish()
        }
    }

    @Composable
    private fun SnippetPopupScreen(pushSnippetOutcome: TWSOutcome<TWSSnippet?>) {
        val snippetData = pushSnippetOutcome.data
        when {
            snippetData != null -> {
                TWSView(
                    modifier = Modifier.fillMaxSize(),
                    snippet = snippetData
                )
            }

            pushSnippetOutcome is TWSOutcome.Progress -> {
                SnippetLoadingView(
                    modifier = Modifier.fillMaxSize(),
                    loadingState = TWSLoadingState.Loading(progress = 0f, isUserForceRefresh = false)
                )
            }

            pushSnippetOutcome is TWSOutcome.Error -> {
                SnippetErrorView(
                    errorMessage = pushSnippetOutcome.exception.message
                        ?: "Something went wrong"
                )
            }

            else -> {
                SnippetErrorView(errorMessage = "Snippet not found")
            }
        }
    }

    companion object {
        internal fun createIntent(context: Context, snippetId: String, projectId: String): Intent {
            return Intent(context, TWSViewPopupActivity::class.java).apply {
                putExtra(EXTRA_SNIPPET_ID, snippetId)
                putExtra(EXTRA_PROJECT_ID, projectId)
            }
        }

        internal fun createIntent(context: Context, snippet: TWSSnippet): Intent {
            return Intent(context, TWSViewPopupActivity::class.java).apply {
                putExtra(EXTRA_CAMPAIGN_SNIPPET, snippet)
            }
        }

        private const val EXTRA_SNIPPET_ID = "extra_snippet_id"
        private const val EXTRA_PROJECT_ID = "extra_project_id"
        private const val EXTRA_CAMPAIGN_SNIPPET = "extra_campaign_snippet"
    }
}
