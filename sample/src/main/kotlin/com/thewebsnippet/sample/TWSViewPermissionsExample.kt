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
package com.thewebsnippet.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.manager.TWSManager
import com.thewebsnippet.manager.TWSOutcome
import com.thewebsnippet.manager.mapData
import com.thewebsnippet.sample.components.ErrorView
import com.thewebsnippet.sample.components.LoadingView
import com.thewebsnippet.view.TWSView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Demonstrates how to handle web permissions and file upload/download natively within `TWSView`.
 *
 * Include the necessary permissions in `AndroidManifest.xml` based on your web page's requirements:
 * For opening Camera from your web page:
 *   ```xml
 *   <uses-permission android:name="android.permission.CAMERA"/>
 *   <uses-feature android:name="android.hardware.camera" android:required="false"/>
 *   ```
 * For requesting Location permission:
 *   ```xml
 *   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
 *   ```
 *
 * In this example, a snippet with all mentioned functionalities is prepared, where you can test
 * permissions, gallery and file upload/download. Include/exclude permissions from `AndroidManifest.xml`
 * and see, how behavior changes.
 *
 * You can see a working example [here](https://github.com/inovait/tws-android/blob/develop/sample/src/main/kotlin/com/thewebsnippet/sample/TWSViewPermissionsExample.kt).
 * Download the Sample app from our web page to explore this functionality interactively.
 *
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

/** @suppress: viewmodel should not be documented */
@HiltViewModel
class TWSPermissionsViewModel @Inject constructor(
    manager: TWSManager
) : ViewModel() {
    val twsSnippetsFlow: Flow<TWSOutcome<List<TWSSnippet>>> = manager.snippets
}
