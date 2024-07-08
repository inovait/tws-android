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

package si.inova.tws.core

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import si.inova.tws.core.data.rememberSaveableWebViewState
import si.inova.tws.core.data.rememberWebViewNavigator
import si.inova.tws.core.lifecycle.DoOnScreenReset
import si.inova.tws.core.lifecycle.LocalScreenResetNotifier
import si.inova.tws.core.lifecycle.ScreenResetNotifier
import timber.log.Timber

/**
 *
 * Extension of WebSnippetComponent to contains multiple WebViews and navigate with bottom tab
 *
 * If only one item is in [targets] bottom tab is not visible
 *
 * @param targets A list, which contains list of data to show in WebView
 * @param modifier A compose modifier
 * @param mainTabIndex Which tab to show when screen is loaded
 * @param scrollableTabRow If scrollable bottom tab
 * @param displayErrorViewOnError Show custom error content
 * if there is error during loading WebView content
 * @param errorViewContent If [displayErrorViewOnError] is set to true
 * show this compose error content
 * @param displayPlaceholderWhileLoading Show custom loading animation while loading WebView content
 * @param loadingPlaceholderContent If [displayPlaceholderWhileLoading] is set to true
 * show this compose loading content
 * @param interceptOverrideUrl Optional callback, how to handle intercepted urls,
 * return true if do not want to navigate to the new url and
 * return false if navigation to the new url is intact
 * @param topBar Optional callback, for showing TopBar, if left empty TopBar is not shown
 * @param resetScreenOnTabReselect If set to true,
 * [DoOnScreenReset] is triggered when tab is reselected
 */
@Composable
fun TabsWebSnippetComponent(
    targets: ImmutableList<WebSnippetData>,
    modifier: Modifier = Modifier,
    mainTabIndex: Int = 0,
    scrollableTabRow: Boolean = false,
    displayErrorViewOnError: Boolean = false,
    errorViewContent: @Composable () -> Unit = { FullScreenErrorView() },
    displayPlaceholderWhileLoading: Boolean = true,
    loadingPlaceholderContent: @Composable () -> Unit = { FullScreenLoadingView() },
    interceptOverrideUrl: (String) -> Boolean = { false },
    topBar: @Composable (String?) -> Unit = { },
    resetScreenOnTabReselect: Boolean = true,
) {
    CompositionLocalProvider(LocalScreenResetNotifier provides ScreenResetNotifier()) {
        val screenResetNotifier = LocalScreenResetNotifier.current
        val webViewStatesMap = targets.map { rememberSaveableWebViewState(key = it.id) }
        val navigatorsMap = targets.map { rememberWebViewNavigator(it.id) }

        val lastSelectedTabIndex = remember { mutableIntStateOf(mainTabIndex) }

        var tabIndex by rememberSaveable(targets.size.toString()) {
            mutableIntStateOf(
                if (targets.size <= mainTabIndex) {
                    Timber.e(
                        "targetUrl size should be > then mainTabIndex: " +
                            "targetUrls.size = ${targets.size} mainTabIndex = $mainTabIndex"
                    )
                    0
                } else {
                    lastSelectedTabIndex.intValue
                }
            )
        }

        // If number of pages decreases
        LaunchedEffect(targets.size) {
            if (tabIndex >= targets.size) {
                tabIndex = 0
                lastSelectedTabIndex.intValue = 0
            }
        }

        BackHandler(tabIndex != mainTabIndex) {
            tabIndex = if (targets.size <= mainTabIndex) 0 else mainTabIndex
        }

        Scaffold(
            modifier = modifier,
            topBar = {
                topBar(webViewStatesMap[tabIndex.coerceAtMost(targets.size - 1)].pageTitle)
            },
            bottomBar = {
                BottomTabRow(
                    scrollableTabRow,
                    tabIndex,
                    targets
                ) {
                    if (tabIndex == it && resetScreenOnTabReselect) {
                        screenResetNotifier.requestScreenReset()
                    } else {
                        tabIndex = it
                    }
                    lastSelectedTabIndex.intValue = it
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Crossfade(
                        targetState = tabIndex.coerceAtMost(targets.size - 1),
                        label = "Animation while changing tabs"
                    ) { targetIndex ->
                        // can crash because of the animation if tab is deleted
                        val coercedIndex = targetIndex.coerceAtMost(targets.size - 1)
                        WebSnippetComponent(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            target = targets[coercedIndex],
                            webViewState = webViewStatesMap[coercedIndex],
                            navigator = navigatorsMap[coercedIndex],
                            displayErrorViewOnError = displayErrorViewOnError,
                            errorViewContent = errorViewContent,
                            displayPlaceholderWhileLoading = displayPlaceholderWhileLoading,
                            loadingPlaceholderContent = loadingPlaceholderContent,
                            interceptOverrideUrl = interceptOverrideUrl,
                        )
                    }

                }
            }
        }
    }
}

@Composable
private fun BottomTabRow(
    scrollableTabRow: Boolean,
    tabIndex: Int,
    targets: ImmutableList<WebSnippetData>,
    onClick: (Int) -> Unit
) {
    if (targets.size <= 1) return
    if (scrollableTabRow) {
        ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 0.dp) {
            Tab(targets, tabIndex, onClick)
        }
    } else {
        TabRow(selectedTabIndex = tabIndex) {
            Tab(targets, tabIndex, onClick)
        }
    }
}

@Composable
private fun Tab(targets: ImmutableList<WebSnippetData>, tabIndex: Int, onClick: (Int) -> Unit) {
    targets.forEachIndexed { index, _ ->
        Tab(text = { Text(text = index.toString()) }, selected = tabIndex == index, onClick = {
            onClick(index)
        })
    }
}

@Composable
private fun FullScreenErrorView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            painter = painterResource(id = R.drawable.image_load_failed),
            contentDescription = "Web view error image",
        )

        Text(
            modifier = Modifier
                .padding(all = 16.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.oops_loading_failed),
            style = TextStyle(color = Color.Black)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FullScreenLoadingView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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

@Composable
@Preview
private fun TabsWebSnippetComponentPreview() {
    TabsWebSnippetComponent(
        persistentListOf(
            WebSnippetData(id = "id1", url = "https://www.google.com/"),
            WebSnippetData(id = "id2", url = "https://www.google.com/"),
            WebSnippetData(id = "id3", url = "https://www.google.com/")
        ),
        displayErrorViewOnError = true,
        displayPlaceholderWhileLoading = true
    )
}
