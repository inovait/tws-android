@file:Suppress("MaxLineLength")

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

package si.inova.tws.examples

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thewebsnippet.core.TWSView
import com.thewebsnippet.data.TWSAttachment
import com.thewebsnippet.data.TWSAttachmentType
import com.thewebsnippet.data.TWSSnippet

/**
 * Demonstrates how to use a custom `TWSViewInterceptor` with `TWSView` to intercept URL loading.
 * By implementing the `interceptUrlCallback`, you can override the default web page loading behavior
 * and open your native application flow (e.g., navigate to a specific screen in your app).
 *
 * This is particularly useful when your web content includes links that should trigger in-app navigation
 * instead of rendering a web page.
 *
 * Working example can be found at [here](https://github.com/inovait/tws-android-sdk/blob/develop/sample/app/src/main/kotlin/si/inova/tws/sample/examples/navigation/TWSViewCustomInterceptorExample.kt).
 * Download the Sample app from our web page to explore this functionality interactively.
 *
 * @sample si.inova.tws.examples.TWSViewCustomInterceptorExample
 */
@Composable
fun TWSViewCustomInterceptorExample(snippet: TWSSnippet) {
    TWSView(
        snippet = snippet,
        interceptUrlCallback = { url ->
            val urlString = url.toString()
            if (urlString.startsWith("www.example.com/details")) {
                // open native details screen
                Log.d("TWS", "Opening native flow.")
                true
            } else {
                false
            }
        }
    )
}

/**
 * Demonstrates how to use `TWSView` with custom properties (`props`) defined in the `TWSSnippet`.
 * The `props` field in the `TWSSnippet` allows you to attach custom metadata to a snippet in JSON format.
 * These properties can be used for various purposes, such as configuring the behavior of your app
 * or providing additional context to your view.
 *
 * In this example, we access the custom `tabName` property (if it exists) and display it using a `Text` composable.
 * The `props` can include values of any type, including classes, allowing you to extend the functionality
 * of the snippet with complex data structures.
 *
 * This flexibility allows you to pass any additional information your app might need to handle the snippet.
 *
 * Working example can be found at [here](https://github.com/inovait/tws-android-sdk/blob/develop/sample/app/src/main/kotlin/si/inova/tws/sample/examples/tabs/TWSViewCustomTabsExample.kt).
 * Download the Sample app from our web page to explore this functionality interactively.
 *
 * @sample si.inova.tws.examples.TWSViewCustomPropsExample
 */
@Composable
fun TWSViewCustomPropsExample(snippet: TWSSnippet, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        snippet.props["tabName"]?.toString()?.let { tabName ->
            Text(text = tabName)
        }

        TWSView(snippet = snippet)
    }
}

/**
 * Demonstrates how to use the `Mustache` engine with `TWSView` in combination with local properties on the snippet.
 *
 * If the `TWSSnippet` has its `engine` set to `TWSEngine.Mustache`, all properties defined in the `props`
 * are used for Mustache template processing. This allows you to dynamically render content based on the provided data.
 *
 * In this example, we showcase how to add additional local properties to the snippet using the `copy` method.
 * A new property named `example` is added to the existing `props`, demonstrating how to extend the snippet's
 * properties dynamically at runtime.
 *
 *
 * ### Working Example
 * You can see a working example at [here](https://github.com/inovait/tws-android-sdk/blob/develop/sample/app/src/main/kotlin/si/inova/tws/sample/examples/mustache/TWSViewMustacheExample.kt).
 * Download the Sample app from our web page to explore this functionality interactively.
 *
 * @sample si.inova.tws.examples.TWSViewMustacheExample
 */
@Composable
fun TWSViewMustacheExample(snippet: TWSSnippet) {
    val modifiedSnippet = snippet.copy(
        props = snippet.props + mapOf("example" to "mustache")
    )

    TWSView(snippet = modifiedSnippet)
}

/**
 * Demonstrates how to dynamically inject external resources into a `TWSView` using the `dynamicResources` property.
 *
 * The `dynamicResources` field in the `TWSSnippet` allows you to inject external resources (CSS, JavaScript) to your
 * web page to enhance the behavior or add styling of the rendered content. This provides a powerful mechanism
 * to customize the appearance or functionality of the content loaded in `TWSView`.
 *
 * In this example, we add a CSS file dynamically by copying the existing snippet and appending a new `TWSAttachment`
 * to the `dynamicResources` list. The attached resource is specified with a URL and a content type of `CSS`. Note that
 * injected files can also be specified and uploaded on our platform.
 *
 * A working example can be found at [here](https://github.com/inovait/tws-android-sdk/blob/develop/sample/app/src/main/kotlin/si/inova/tws/sample/examples/injection/TWSViewInjectionExample.kt).
 * Download the Sample app from our web page to explore this functionality interactively.
 *
 * @sample si.inova.tws.examples.TWSViewInjectionExample
 */
@Composable
fun TWSViewInjectionExample(snippet: TWSSnippet) {
    val modifiedSnippet = snippet.copy(
        dynamicResources = snippet.dynamicResources + listOf(
            TWSAttachment(
                url = "link-to-css/style.css",
                contentType = TWSAttachmentType.CSS
            )
        )
    )

    TWSView(snippet = modifiedSnippet)
}
