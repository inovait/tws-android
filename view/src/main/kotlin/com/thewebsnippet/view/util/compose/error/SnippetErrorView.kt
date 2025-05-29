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
package com.thewebsnippet.view.util.compose.error

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thewebsnippet.view.R

/**
 * A composable function signature for displaying error content in the WebView.
 *
 * @param message The error message to display.
 * @param callback An optional [ErrorRefreshCallback] to trigger a retry action.
 * @param refreshable A flag indicating whether the error view should include a refresh button.
 */
typealias SnippetErrorContent = @Composable (
    message: String,
    callback: ErrorRefreshCallback?,
    refreshable: Boolean
) -> Unit

/**
 * Provides a default implementation of [SnippetErrorContent] using [SnippetErrorView].
 *
 * @param modifier A [Modifier] used to customize the layout of the default error view.
 * @return A [SnippetErrorContent] composable function that renders a default error UI.
 */
fun defaultErrorView(modifier: Modifier): SnippetErrorContent = { message, callback, refreshable ->
    SnippetErrorView(
        errorMessage = message,
        modifier = modifier,
        isRefreshable = refreshable,
        refreshCallback = callback
    )
}

/**
 * A composable function that displays a centered error message with optional retry functionality.
 *
 * @param errorMessage The error message to display within the view.
 * @param modifier A [Modifier] to customize the layout or behavior of the error view.
 * @param isRefreshable Whether to show a retry button.
 * @param refreshCallback Optional callback to trigger a refresh when retry is clicked.
 */
@Composable
fun SnippetErrorView(
    errorMessage: String,
    modifier: Modifier = Modifier,
    isRefreshable: Boolean = false,
    refreshCallback: ErrorRefreshCallback? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(64.dp),
            imageVector = Icons.Default.Warning,
            contentDescription = "Web view error image",
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 48.dp, vertical = 16.dp)
                .align(Alignment.CenterHorizontally),
            text = errorMessage,
            textAlign = TextAlign.Center,
            style = TextStyle(color = Color.Black)
        )

        if (isRefreshable && refreshCallback != null) {
            Button(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                onClick = { refreshCallback.refresh() }
            ) {
                Text(stringResource(R.string.refresh))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun SnippetErrorViewFullScreenPreview() {
    SnippetErrorView(modifier = Modifier.fillMaxHeight(), errorMessage = "No internet connection")
}

@Preview
@Composable
private fun SnippetErrorViewFullScreenLongTextPreview() {
    SnippetErrorView(
        modifier = Modifier.fillMaxHeight(),
        errorMessage = "Section close tag with mismatched open tag 'title' != 'person @line 559"
    )
}

@Preview
@Composable
private fun SnippetErrorViewFullScreenWithRefreshPreview() {
    SnippetErrorView(
        modifier = Modifier.fillMaxHeight(),
        errorMessage = "No internet connection",
        isRefreshable = true,
        refreshCallback = {}
    )
}
