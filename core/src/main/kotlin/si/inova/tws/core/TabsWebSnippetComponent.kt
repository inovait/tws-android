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

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import si.inova.tws.core.data.TabIcon
import si.inova.tws.core.data.WebSnippetData
import si.inova.tws.core.data.view.WebViewState
import si.inova.tws.core.data.view.rememberSaveableWebViewState
import si.inova.tws.core.data.view.rememberWebViewNavigator
import si.inova.tws.core.lifecycle.DoOnScreenReset
import si.inova.tws.core.lifecycle.LocalScreenResetNotifier
import si.inova.tws.core.lifecycle.ScreenResetNotifier
import si.inova.tws.core.util.compose.SnippetErrorView
import si.inova.tws.core.util.compose.SnippetLoadingView
import si.inova.tws.core.util.compose.TabIconHandler
import si.inova.tws.core.util.onScreenReset

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
 * @param onScreenReset Optional callback, set what to do on screen reset
 * @param googleLoginRedirectUrl open new intent for google login url
 * @param tabsContainerColor the color used for the background of this tab row.
 * @param tabsContentColor the preferred color for content inside this tab row. Defaults to either the matching content color for containerColor, or to the current LocalContentColor if containerColor is not a color from the theme.
 * @param tabIconHandler set how to handle tab icon (resources or url)
 */
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // WebView can be scrollable and do not want to limit padding
fun TabsWebSnippetComponent(
    targets: ImmutableList<WebSnippetData>,
    modifier: Modifier = Modifier,
    mainTabIndex: Int = 0,
    scrollableTabRow: Boolean = false,
    displayErrorViewOnError: Boolean = false,
    errorViewContent: @Composable () -> Unit = { SnippetErrorView(true) },
    displayPlaceholderWhileLoading: Boolean = true,
    loadingPlaceholderContent: @Composable () -> Unit = { SnippetLoadingView(true) },
    interceptOverrideUrl: (String) -> Boolean = { false },
    topBar: @Composable (String?) -> Unit = { },
    resetScreenOnTabReselect: Boolean = true,
    onScreenReset: (WebViewState) -> Unit = { it.webView?.onScreenReset() },
    googleLoginRedirectUrl: String? = null,
    tabsContainerColor: Color = TabRowDefaults.primaryContainerColor,
    tabsContentColor: Color = TabRowDefaults.primaryContentColor,
    tabIconHandler: @Composable (TabIcon) -> Unit = { it.TabIconHandler() }
) {
    CompositionLocalProvider(LocalScreenResetNotifier provides ScreenResetNotifier()) {
        val screenResetNotifier = LocalScreenResetNotifier.current
        val webViewStatesMap = targets.map { rememberSaveableWebViewState(key = "${it.id}-${it.url}") }
        val navigatorsMap = targets.map { rememberWebViewNavigator(it.id) }

        val lastSelectedTabIndex = remember { mutableIntStateOf(mainTabIndex) }

        var tabIndex by rememberSaveable(targets.size.toString()) {
            mutableIntStateOf(
                if (targets.size <= mainTabIndex) {
                    Log.e(
                        "OutOfBounds",
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

        val targetScreens: List<@Composable () -> Unit> = targets.mapIndexed { i, data ->
            {
                val loadFirstTime = rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(tabIndex) {
                    if (i == tabIndex) {
                        loadFirstTime.value = true
                        webViewStatesMap[i].webView?.onResume()
                    } else {
                        webViewStatesMap[i].webView?.onPause()
                    }
                }

                if (loadFirstTime.value) {
                    DoOnScreenReset {
                        onScreenReset(webViewStatesMap[i])
                    }

                    WebSnippetComponent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        target = data,
                        webViewState = webViewStatesMap[i],
                        navigator = navigatorsMap[i],
                        displayErrorViewOnError = displayErrorViewOnError,
                        errorViewContent = errorViewContent,
                        displayPlaceholderWhileLoading = displayPlaceholderWhileLoading,
                        loadingPlaceholderContent = loadingPlaceholderContent,
                        interceptOverrideUrl = interceptOverrideUrl,
                        googleLoginRedirectUrl = googleLoginRedirectUrl
                    )
                }
            }
        }

        Scaffold(
            modifier = modifier,
            topBar = {
                topBar(webViewStatesMap[tabIndex.coerceAtMost(targets.size - 1)].pageTitle)
            },
            bottomBar = {
                BottomTabRow(
                    tabsContainerColor = tabsContainerColor,
                    tabsContentColor = tabsContentColor,
                    scrollableTabRow = scrollableTabRow,
                    tabIndex = tabIndex,
                    targets = targets,
                    onClick = {
                        if (tabIndex == it && resetScreenOnTabReselect) {
                            screenResetNotifier.requestScreenReset()
                        } else {
                            tabIndex = it
                        }
                        lastSelectedTabIndex.intValue = it
                    },
                    tabIconHandler = tabIconHandler
                )
            }
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                targetScreens.forEachIndexed { index, screen ->
                    val animatedZIndex by animateFloatAsState(
                        targetValue = if (index == tabIndex) 1f else 0f,
                        animationSpec = tween(durationMillis = 500),
                        label = "Animation while changing tabs"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(animatedZIndex)
                            .zIndex(if (index == tabIndex) 1f else 0f)
                    ) {
                        screen()
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomTabRow(
    tabsContainerColor: Color,
    tabsContentColor: Color,
    scrollableTabRow: Boolean,
    tabIndex: Int,
    targets: ImmutableList<WebSnippetData>,
    onClick: (Int) -> Unit,
    tabIconHandler: @Composable (TabIcon) -> Unit = { it.TabIconHandler() }
) {
    if (targets.size <= 1) return
    if (scrollableTabRow) {
        ScrollableTabRow(
            containerColor = tabsContainerColor,
            contentColor = tabsContentColor,
            selectedTabIndex = tabIndex, edgePadding = 0.dp
        ) {
            Tab(targets, tabIndex, onClick, tabIconHandler)
        }
    } else {
        TabRow(
            containerColor = tabsContainerColor,
            contentColor = tabsContentColor,
            selectedTabIndex = tabIndex
        ) {
            Tab(targets, tabIndex, onClick, tabIconHandler)
        }
    }
}

@Composable
private fun Tab(
    targets: ImmutableList<WebSnippetData>,
    tabIndex: Int,
    onClick: (Int) -> Unit,
    tabIconHandler: @Composable (TabIcon) -> Unit = { it.TabIconHandler() }
) {
    targets.forEachIndexed { index, data ->
        Tab(
            icon = data.tabContentResources?.icon?.let {
                { tabIconHandler(it) }
            },
            text = if (data.tabContentResources?.name != null) {
                { Text(text = data.tabContentResources.name) }
            } else if (data.tabContentResources?.icon == null) {
                { Text(text = index.toString()) }
            } else {
                null
            },
            selected = tabIndex == index, onClick = {
                onClick(index)
            }
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
