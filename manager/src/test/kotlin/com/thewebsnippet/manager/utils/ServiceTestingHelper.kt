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
package com.thewebsnippet.manager.utils

import kotlinx.coroutines.CompletableDeferred

/**
 * Helper for creating fake retrofit services. It allows you to easily replace any call with either infinite loading
 * or error.
 *
 * To use this, you need to call [ServiceTestingHelper.intercept] in your fake
 * retrofit service's methods before you return the fake value.
 */
internal class ServiceTestingHelper : FakeService {
    private var nextInterception: InterceptionStyle? = null
    private var defaultInterception: InterceptionStyle = InterceptionStyle.None

    private var infiniteLoadCompletable: CompletableDeferred<Unit>? = null
    private var immediatelyComplete = false

    override var numServiceCalls: Int = 0

    override fun interceptNextCallWith(style: InterceptionStyle) {
        nextInterception = style
    }

    override fun interceptAllFutureCallsWith(style: InterceptionStyle) {
        defaultInterception = style
    }

    override fun completeInfiniteLoad() {
        val infiniteLoadCompletable = infiniteLoadCompletable
        if (infiniteLoadCompletable == null) {
            immediatelyComplete = true
        } else {
            infiniteLoadCompletable.complete(Unit)
            this.infiniteLoadCompletable = null

            immediatelyComplete = false
        }
    }

    suspend fun intercept() {
        val nextInterception = nextInterception ?: defaultInterception
        this.nextInterception = null

        numServiceCalls++

        return when (nextInterception) {
            is InterceptionStyle.None -> Unit
            is InterceptionStyle.InfiniteLoad -> {
                if (immediatelyComplete) {
                    immediatelyComplete = false
                    return
                }

                val completable = CompletableDeferred<Unit>()
                this.infiniteLoadCompletable = completable

                completable.await()
            }

            is InterceptionStyle.Error -> throw nextInterception.exception
        }
    }

    fun reset() {
        completeInfiniteLoad()
        nextInterception = InterceptionStyle.None
        immediatelyComplete = false
        numServiceCalls = 0
    }
}

sealed class InterceptionStyle {
    /**
     * Return value normally
     */
    object None : InterceptionStyle()

    /**
     * Suspend until [FakeService.completeInfiniteLoad] is called
     */
    object InfiniteLoad : InterceptionStyle()

    /**
     * Throw exception
     */
    data class Error(val exception: Throwable) : InterceptionStyle()
}

interface FakeService {
    /**
     * Number of load calls that have been made to this service
     */
    val numServiceCalls: Int

    /**
     * When set, next call to this service will behave in accordance with provided [InterceptionStyle].
     */
    fun interceptNextCallWith(style: InterceptionStyle)

    /**
     * When set, all future calls to this service will behave in accordance with provided [InterceptionStyle].
     */
    fun interceptAllFutureCallsWith(style: InterceptionStyle)

    /**
     * Complete any previous calls with [InterceptionStyle.InfiniteLoad] set
     */
    fun completeInfiniteLoad()
}
