/*
 * Copyright 2021 The Android Open Source Project
 * Modifications Copyright 2024 INOVA IT d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file has been modified from its original version.
 */
package com.thewebsnippet.view.data

import org.jetbrains.annotations.CheckReturnValue

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
     * @property mainFrameLoaded Describes a loading state between `onPageStarted` and `onPageFinished` events
     */
    abstract val mainFrameLoaded: Boolean

    /**
     * Describes a WebView that has not yet loaded for the first time.
     */
    data object Initializing : TWSLoadingState() {
        override val mainFrameLoaded: Boolean = false
    }

    /**
     * Describes a WebView that will be reloaded as a result of a users pull to refresh action.
     */
    data object ForceRefreshInitiated : TWSLoadingState() {
        override val mainFrameLoaded: Boolean = false
    }

    /**
     * Describes a WebView.progress for all resources and page
     *
     * @property progress Current load progress (typically 0.0 to 1.0), provided by the webview.
     * @property mainFrameLoaded Describes a loading state between `onPageStarted` and `onPageFinished` events
     * @property isUserForceRefresh True if the load was initiated by the user explicitly refreshing the page.
     */
    data class Loading(
        val progress: Float,
        override val mainFrameLoaded: Boolean,
        val isUserForceRefresh: Boolean
    ) : TWSLoadingState()

    /**
     * Describes a webview that has finished loading content.
     */
    data object Finished : TWSLoadingState() {
        override val mainFrameLoaded: Boolean = true
    }
}

/**
 * Updates the [TWSLoadingState.mainFrameLoaded] flag, returning a new [TWSLoadingState.Loading] instance
 * only if the state is currently [TWSLoadingState.Loading] and the value needs changing.
 * Otherwise (no change or not in Loading state), returns the original instance.
 */
@CheckReturnValue
internal fun TWSLoadingState.copyMainFrame(loaded: Boolean): TWSLoadingState {
    if (mainFrameLoaded == loaded) return this

    return when (this) {
        is TWSLoadingState.Loading -> copy(mainFrameLoaded = loaded)
        is TWSLoadingState.Finished,
        is TWSLoadingState.ForceRefreshInitiated,
        is TWSLoadingState.Initializing -> this
    }
}
