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
package com.thewebsnippet.view.data

/**
 * TWSLoadingState is a sealed class for constraining possible loading states.
 *
 * NOTE: This class is a modified version of the original loading state representation
 * from the Accompanist WebView library. Modifications were made to add support for force-refresh
 * states initiated by user actions and more detailed loading progress tracking.
 * -----
 * See [Loading] and [Finished].
 */
sealed class TWSLoadingState {
    /**
     * Describes a WebView that has not yet loaded for the first time.
     */
    data object Initializing : TWSLoadingState()

    /**
     * Describes a WebView that will be reloaded as a result of a users pull to refresh action.
     */
    data object ForceRefreshInitiated : TWSLoadingState()

    /**
     * Describes a webview between `onPageStarted` and `onPageFinished` events, contains a
     * [progress] property which is updated by the webview and [isUserForceRefresh] property
     * which marks if page is refreshed because of the user action.
     */
    data class Loading(val progress: Float, val isUserForceRefresh: Boolean) : TWSLoadingState()

    /**
     * Describes a webview that has finished loading content.
     */
    data object Finished : TWSLoadingState()
}
