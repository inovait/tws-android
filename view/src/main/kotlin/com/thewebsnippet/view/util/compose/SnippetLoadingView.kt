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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thewebsnippet.view.data.TWSLoadingState

/**
 * A composable function that displays a loading indicator for snippets until main frame content is successfully loaded.
 *
 * @param loadingState The current loading state containing information about progress,
 * whether the main frame is loaded, and if the user has forced a refresh.
 * If [TWSLoadingState.Loading.isUserForceRefresh] is true, the loading view is skipped.
 * @param modifier A [Modifier] to configure the layout or styling of the loading view.
 */
@Composable
fun SnippetLoadingView(
    loadingState: TWSLoadingState.Loading,
    modifier: Modifier = Modifier
) {
    // stop showing loading view after mainframe is loaded or if user forced refresh (pull to refresh indicator is shown)
    if (loadingState.isUserForceRefresh) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Preview
@Composable
private fun SnippetLoadingFullScreenPreview() {
    SnippetLoadingView(
        loadingState = TWSLoadingState.Loading(progress = 0.8f, isUserForceRefresh = false),
        modifier = Modifier.fillMaxHeight()
    )
}
