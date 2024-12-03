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

package com.thewebsnippet.sample.examples.permissions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thewebsnippet.core.TWSView
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.manager.TWSManager
import com.thewebsnippet.manager.TWSOutcome
import com.thewebsnippet.manager.mapData
import com.thewebsnippet.sample.R
import com.thewebsnippet.sample.components.ErrorView
import com.thewebsnippet.sample.components.LoadingView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * A composable function that renders a screen showcasing how download, upload, camera and
 * location permissions are handled in [TWSView].
 *
 * Following permissions and features need to be present in the AndroidManifest:
 * - For downloading and uploading files:
 *     `<uses-permission android:name="android.permission.INTERNET" /`>
 * - For camera access:
 *     `<uses-permission android:name="android.permission.CAMERA"/>`
 *     `<uses-feature android:name="android.hardware.camera" android:required="false" />`
 * - For users location:
 *     `<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />`
 *
 * @param twsPermissionsViewModel A viewModel that provides access to the [TWSOutcome].
 * @sample com.thewebsnippet.sample.examples.permissions.TWSViewPermissionsExample
 */
@Composable
fun TWSViewPermissionsExample(
    twsPermissionsViewModel: TWSPermissionsViewModel = hiltViewModel()
) {
    val permissionSnippetState = twsPermissionsViewModel.twsSnippetsFlow.collectAsStateWithLifecycle(null).value
        ?.mapData { state ->
            state.firstOrNull { it.id == "permissions" }
    } ?: return

    val snippet = permissionSnippetState.data
    when {
        snippet != null -> {
            TWSView(
                snippet = snippet,
                loadingPlaceholderContent = { LoadingView() },
                errorViewContent = { ErrorView(it) },
            )
        }

        permissionSnippetState is TWSOutcome.Error -> {
            ErrorView(permissionSnippetState.exception.message ?: stringResource(R.string.error_message))
        }

        permissionSnippetState is TWSOutcome.Progress -> {
            LoadingView()
        }
    }
}

/**
 * @param manager Global instance of [TWSManager].
 * @property twsSnippetsFlow A Flow collecting [TWSOutcome] state from the manager.
 */
@HiltViewModel
class TWSPermissionsViewModel @Inject constructor(
    manager: TWSManager
) : ViewModel() {
    // Collecting TWSManager.snippets, which returns the current state, which
    // exposes TWSOutcome.Error, TWSOutcome.Progress or TWSOutcome.Success state.
    val twsSnippetsFlow: Flow<TWSOutcome<List<TWSSnippet>>> = manager.snippets
}
