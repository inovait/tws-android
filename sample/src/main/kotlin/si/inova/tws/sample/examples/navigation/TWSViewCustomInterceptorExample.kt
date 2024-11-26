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

package si.inova.tws.sample.examples.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import si.inova.tws.core.TWSView
import si.inova.tws.data.TWSSnippet
import si.inova.tws.manager.TWSManager
import si.inova.tws.sample.components.ErrorView
import si.inova.tws.sample.components.LoadingView
import javax.inject.Inject

/**
 * A composable function that renders a screen used for navigation, showcasing the use of [TWSViewDemoInterceptor] to handle URLs natively.
 *
 * @param navController A [NavController] used to handle navigation between screens.
 * @param twsInterceptorViewModel A viewModel that provides access to the list of [TWSSnippet].
 */
@Composable
fun TWSViewCustomInterceptorExample(
    navController: NavController,
    twsInterceptorViewModel: TWSInterceptorViewModel = hiltViewModel()
) {
    val homeSnippet = twsInterceptorViewModel.twsSnippetsFlow.collectAsStateWithLifecycle(null).value
        ?.firstOrNull { it.id == "customInterceptors" } ?: return

    TWSView(
        snippet = homeSnippet,
        loadingPlaceholderContent = { LoadingView() },
        errorViewContent = { ErrorView(it) },
        // Handling urls natively
        interceptUrlCallback = TWSViewDemoInterceptor { navController.navigate(it) }
    )
}

/**
 * @param manager Global instance of [TWSManager].
 * @property twsSnippetsFlow A Flow collecting the list of available [TWSSnippet], either cached or up to date.
 */
@HiltViewModel
class TWSInterceptorViewModel @Inject constructor(
    manager: TWSManager
) : ViewModel() {
    val twsSnippetsFlow: Flow<List<TWSSnippet>?> = manager.snippets()
}
