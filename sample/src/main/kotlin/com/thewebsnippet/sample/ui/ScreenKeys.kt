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
package com.thewebsnippet.sample.ui

/**
 * A sealed class used for navigating between screens.
 * @param route The name of the screen.
 */
internal sealed class Screen(val route: String) {
    data object TWSViewCustomInterceptorExampleKey : Screen("customInterceptorExample")
    data object TWSViewCustomTabsExampleKey : Screen("customTabsExample")
    data object TWSViewMustacheExampleKey : Screen("mustacheExample")
    data object TWSViewInjectionExampleKey : Screen("injectionExample")
    data object TWSViewPermissionsExampleKey : Screen("permissionsExample")
    data object NativeViewUserEngagementExampleKey : Screen("userEngagementExample")
}
