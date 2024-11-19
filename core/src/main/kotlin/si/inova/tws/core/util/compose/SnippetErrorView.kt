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

package si.inova.tws.core.util.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A composable function that displays an error view for snippets, which can adapt to either full-screen
 * or card-sized layouts based on the provided configuration.
 *
 * @param errorMessage The error message to display in the view.
 * @param fullScreen A Boolean flag indicating whether the view should occupy the full screen (`true`)
 * or a smaller card-like height (`false`).
 */
@Composable
internal fun SnippetErrorView(
    errorMessage: String,
    fullScreen: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (fullScreen) Modifier.fillMaxHeight() else Modifier.height(200.dp))
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            modifier = Modifier.align(Alignment.CenterHorizontally).size(64.dp),
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

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
fun SnippetErrorViewFullScreenPreview() {
    SnippetErrorView(errorMessage = "No internet connection", fullScreen = true)
}

@Preview
@Composable
fun SnippetErrorViewCardPreview() {
    SnippetErrorView(errorMessage = "No internet connection", fullScreen = false)
}

@Preview
@Composable
fun SnippetErrorViewFullScreenLongTextPreview() {
    SnippetErrorView(errorMessage = "Section close tag with mismatched open tag 'title' != 'person @line 559", fullScreen = true)
}

@Preview
@Composable
fun SnippetErrorViewCardLongTextPreview() {
    SnippetErrorView(errorMessage = "Section close tag with mismatched open tag 'title' != 'person @line 559", fullScreen = false)
}
