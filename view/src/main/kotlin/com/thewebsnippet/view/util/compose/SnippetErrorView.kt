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
package com.thewebsnippet.view.util.compose

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
 * A composable function that displays an error view for snippets.
 *
 * @param errorMessage The error message to display within the view.
 * @param modifier A [Modifier] for customizing the layout or behavior of the error view.
 */
@Composable
fun SnippetErrorView(
    errorMessage: String,
    modifier: Modifier = Modifier
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
