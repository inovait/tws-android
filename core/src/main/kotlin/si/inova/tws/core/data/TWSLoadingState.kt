/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * This file contains modifications based on code from the Accompanist WebView library.
 * Original Copyright (c) 2021 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice, this permission notice, and the following additional notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * -----
 * Portions of this file are derived from the Accompanist WebView library,
 * which is available at https://github.com/google/accompanist/blob/main/web/src/main/java/com/google/accompanist/web/WebView.kt.
 * Copyright (c) 2021 The Android Open Source Project. Licensed under Apache License, Version 2.0.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * -----
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.tws.core.data

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
