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

import dispatch.core.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Creates a [TestScope] with a [DispatcherProvider] installed that provides TestScope's test dispatchers as all dispatchers.
 */
internal fun testScopeWithDispatcherProvider(context: CoroutineContext = EmptyCoroutineContext): TestScope {
    val testDispatcher = StandardTestDispatcher()
    val testDispatcherProvider = SingleDispatcherProvider(testDispatcher)
    return TestScope(context + testDispatcherProvider + testDispatcher)
}

/**
 * Dispatcher provider that provides a single dispatcher as every other
 */
internal class SingleDispatcherProvider(private val dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val default: CoroutineDispatcher
        get() = dispatcher
    override val io: CoroutineDispatcher
        get() = dispatcher
    override val main: CoroutineDispatcher
        get() = dispatcher
    override val mainImmediate: CoroutineDispatcher
        get() = dispatcher
    override val unconfined: CoroutineDispatcher
        get() = dispatcher
}
