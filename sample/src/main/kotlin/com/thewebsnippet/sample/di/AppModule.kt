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
package com.thewebsnippet.sample.di

import com.thewebsnippet.manager.TWSManager
import com.thewebsnippet.manager.TWSSdk
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal class AppModule {
    /**
     * Provides a global instance of TWSManager configured in Manifest.
     *
     * @return Global instance of [TWSManager].
     */
    @Provides
    fun provideTWSManager(): TWSManager = TWSSdk.getInstance()
}