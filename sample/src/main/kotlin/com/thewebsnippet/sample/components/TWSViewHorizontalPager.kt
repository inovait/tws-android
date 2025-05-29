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
package com.thewebsnippet.sample.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.sample.ui.theme.SampleTheme
import com.thewebsnippet.view.TWSView
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

/**
 * A composable function that displays a full screen [HorizontalPager], that holds the list of [TWSSnippet].
 *
 * @param data A list of [TWSSnippet] that will get displayed in a [HorizontalPager].
 */
@Composable
internal fun TWSViewComponentWithPager(data: ImmutableList<TWSSnippet>) {
    val pagerState = rememberPagerState { data.size }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal pager for displaying snippets
        HorizontalPager(
            modifier = Modifier.weight(1f),
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            TWSView(
                modifier = Modifier.fillMaxSize(),
                snippet = data[page],
                loadingPlaceholderContent = { LoadingView() },
                errorViewContent = sampleErrorView()
            )
        }

        HorizontalDivider(color = Color.Black)

        Row(modifier = Modifier.fillMaxWidth()) {
            // Navigate back button
            NavigateIndicator(Icons.AutoMirrored.Filled.ArrowBack, pagerState.currentPage > 0) {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }

            // Current page indicator
            PageIndicators(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                currentPage = pagerState.currentPage,
                pageCount = data.size
            )

            // Navigate forward button
            NavigateIndicator(Icons.AutoMirrored.Filled.ArrowForward, pagerState.currentPage < data.size - 1) {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        }
    }
}

/**
 * @param imageVector An [ImageVector] displayed in [IconButton].
 * @param isEnabled Boolean that controls if the button is enabled.
 * @param modifier A [Modifier] used for layout and styling of the [IconButton].
 * @param goToPrevious A function that controls on click behaviour for [IconButton].
 */
@Composable
private fun NavigateIndicator(
    imageVector: ImageVector,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    goToPrevious: () -> Unit = {}
) {
    IconButton(
        modifier = modifier.padding(horizontal = 16.dp),
        enabled = isEnabled,
        onClick = goToPrevious
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "Navigate to page"
        )
    }
}

/**
 * @param currentPage A number indicating the current page number.
 * @param pageCount A number of all the pages.
 * @param modifier A [Modifier] use to control the layout and style of the [PageIndicators].
 */
@Composable
private fun PageIndicators(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(if (currentPage == it) Color.DarkGray else Color.LightGray)
                    .size(12.dp)
            )
        }
    }
}

@Composable
@Preview
private fun PagerPreview() {
    SampleTheme {
        TWSViewComponentWithPager(
            persistentListOf(
                TWSSnippet(id = "1", "www.example1.com"),
                TWSSnippet(id = "2", "www.example2.com"),
                TWSSnippet(id = "3", "www.example3.com")
            )
        )
    }
}
