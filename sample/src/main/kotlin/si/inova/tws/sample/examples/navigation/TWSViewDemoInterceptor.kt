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

package si.inova.tws.sample.examples.navigation

import android.net.Uri
import si.inova.tws.core.data.TWSViewInterceptor
import si.inova.tws.sample.Screen.TWSViewCustomTabsExample
import si.inova.tws.sample.Screen.TWSViewInjectionExample
import si.inova.tws.sample.Screen.TWSViewMustacheExample
import si.inova.tws.sample.Screen.TWSViewPermissionsExample

/**
 * An implementation of [TWSViewInterceptor] used for custom URL redirects.
 *
 * @param navigate A function called with the corresponding route, depending on the received URL.
 */
class TWSViewDemoInterceptor(private val navigate: (String) -> Unit) : TWSViewInterceptor {
    override fun handleUrl(url: Uri): Boolean {
        val urlString = url.toString()
        val route = when {
            urlString.contains("/customTabsExample") -> TWSViewCustomTabsExample.route
            urlString.contains("/mustacheExample") -> TWSViewMustacheExample.route
            urlString.contains("/injectionExample") -> TWSViewInjectionExample.route
            urlString.contains("/permissionsExample") -> TWSViewPermissionsExample.route
            else -> null
        }

        route?.let { navigate(it) }
        return route != null
    }
}
