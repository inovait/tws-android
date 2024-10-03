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
import androidx.compose.foundation.layout.padding
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
 * TabsWebSnippetComponent is a custom composable function designed to handle multiple WebView instances in a tabbed layout.
 * It displays content in WebViews, where each WebView corresponds to a tab in the tab navigation.
 * This component can handle multiple WebViews, manage their states, and switch between them through a tab-based navigation
 * bar at the bottom of the screen.
 *
 * This component can also display a top bar, handle URL interception, show loading placeholders, display custom error views,
 * and reset screen states when tabs are reselected. It supports both fixed and scrollable tab rows. If [targets] contains only
 * one item, bottom tab row is not displayed.
 *
 * @param targets A list of WebSnippetData, where each item represents the data to be displayed in a WebView.
 * This list determines the number of tabs displayed.
 * @param modifier A compose modifier
 * @param mainTabIndex The index of the tab that is initially selected and displayed when the component loads. Defaults to 0.
 * @param scrollableTabRow Whether the bottom tab bar should be scrollable or fixed.
 * If set to true, the tabs will scroll horizontally when there are too many to fit on the screen.
 * @param displayErrorViewOnError Whether to show a custom error view if loading the WebView content fails.
 * If set to true, the provided errorViewContent will be displayed in case of errors.
 * @param errorViewContent A custom composable that defines the UI content to display when there's an error
 * loading WebView content. Used only if [displayErrorViewOnError] is set to true.
 * @param displayPlaceholderWhileLoading If set to true, a placeholder or loading animation will be
 *  * shown while the WebView content is loading.
 * @param loadingPlaceholderContent A custom composable that defines the UI content to show while the WebView content is loading.
 *  Used only if [displayPlaceholderWhileLoading] is set to true.
 * @param interceptOverrideUrl A lambda function that is invoked when a URL in WebView will be loaded.
 * Returning true prevents navigation to the new URL (and allowing you to define custom behavior for specific urls),
 * while returning false allows it to proceed.
 * @param topBar A composable function to define a top bar above the WebView.
 * The string parameter provides the current WebView’s title. If no top bar is needed, leave this function empty.
 * @param resetScreenOnTabReselect If set to true, the [onScreenReset] method will be invoked when tab is reselected.
 * @param onScreenReset A callback invoked to handle actions when a screen is reset.
 * The WebViewState of the current tab is passed as a parameter, allowing you to customize the reset behavior.
 * Defaults to scrolling WebView's content to top.
 * @param googleLoginRedirectUrl A URL to which user is redirected after successful Google login. This will allow us to redirect
 * user back to the app after login in Custom Tabs has been completed.
 * @param tabsContainerColor The background color for the tab row.
 * @param tabsContentColor The content color used inside the tab row (e.g., text and icons).
 * @param tabIconHandler A composable function that defines how to display icons in the tab row.
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
            topBar = { topBar(webViewStatesMap[tabIndex.coerceAtMost(targets.size - 1)].pageTitle) },
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
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = it.calculateTopPadding(), bottom = it.calculateBottomPadding())
            ) {
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
