/*
 * Copyright 2025 INOVA IT d.o.o.
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

package com.thewebsnippet.manager.fakes.preference

import android.content.Context
import com.thewebsnippet.manager.preference.TWSBuild

object FakeTWSBuild : TWSBuild {
    override fun safeInit(context: Context) {
        // Unused in testing
    }

    override val token: String = "JWT-test"
    override val baseUrl: String = "https://tws.test.dev/"
}
