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

package si.inova.tws.core.util

import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import si.inova.tws.core.BuildConfig
import si.inova.tws.data.DynamicResourceDto
import si.inova.tws.data.EngineType

class HtmlModifierHelperTest {

    private lateinit var helper: HtmlModifierHelper

    @Before
    fun setUp() {
        helper = HtmlModifierHelper()
    }

    @Test
    fun `should inject CSS into HTML with head tag`() {
        val html = "<html><head></head><body>Hello World</body></html>"
        val cssResource = DynamicResourceDto("https://example.com/style.css", "text/css")

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = listOf(cssResource),
            mustacheProps = emptyMap(),
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<head>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<link rel="stylesheet" href="https://example.com/style.css">""" +
            """</head>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should inject JavaScript into HTML with head tag`() {
        val html = "<html><head></head><body>Hello World</body></html>"
        val jsResource = DynamicResourceDto("https://example.com/script.js", "text/javascript")

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = listOf(jsResource),
            mustacheProps = emptyMap(),
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<head>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<script src="https://example.com/script.js" type="text/javascript"></script>""" +
            """</head>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should inject both CSS and JavaScript into HTML with head tag`() {
        val html = "<html><head></head><body>Hello World</body></html>"
        val cssResource = DynamicResourceDto("https://example.com/style.css", "text/css")
        val jsResource = DynamicResourceDto("https://example.com/script.js", "text/javascript")

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = listOf(cssResource, jsResource),
            mustacheProps = emptyMap(),
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<head>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<script src="https://example.com/script.js" type="text/javascript"></script>""" +
            """<link rel="stylesheet" href="https://example.com/style.css">""" +
            """</head>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should inject Mustache content`() {
        val html = "<html><body>{{greeting}} World</body></html>"
        val mustacheProps = mapOf("greeting" to "Hello")

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = emptyList(),
            mustacheProps = mustacheProps,
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should inject CSS, JavaScript, and Mustache content`() {
        val html = "<html><head></head><body>{{greeting}} World</body></html>"
        val cssResource = DynamicResourceDto("https://example.com/style.css", "text/css")
        val jsResource = DynamicResourceDto("https://example.com/script.js", "text/javascript")
        val mustacheProps = mapOf("greeting" to "Hello")

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = listOf(cssResource, jsResource),
            mustacheProps = mustacheProps,
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<head>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<script src="https://example.com/script.js" type="text/javascript"></script>""" +
            """<link rel="stylesheet" href="https://example.com/style.css">""" +
            """</head>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should inject CSS and JavaScript into HTML without head tag`() {
        val html = "<html><body>Hello World</body></html>"
        val cssResource = DynamicResourceDto("https://example.com/style.css", "text/css")
        val jsResource = DynamicResourceDto("https://example.com/script.js", "text/javascript")

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = listOf(cssResource, jsResource),
            mustacheProps = emptyMap(),
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<script src="https://example.com/script.js" type="text/javascript"></script>""" +
            """<link rel="stylesheet" href="https://example.com/style.css">""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should inject multiple CSS resources`() {
        val html = "<html><head></head><body>Hello World</body></html>"
        val cssResources = listOf(
            DynamicResourceDto("https://example.com/style1.css", "text/css"),
            DynamicResourceDto("https://example.com/style2.css", "text/css")
        )

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = cssResources,
            mustacheProps = emptyMap(),
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<head>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<link rel="stylesheet" href="https://example.com/style1.css">""" +
            """<link rel="stylesheet" href="https://example.com/style2.css">""" +
            """</head>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should inject multiple JavaScript resources`() {
        val html = "<html><head></head><body>Hello World</body></html>"
        val jsResources = listOf(
            DynamicResourceDto("https://example.com/script1.js", "text/javascript"),
            DynamicResourceDto("https://example.com/script2.js", "text/javascript")
        )

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = jsResources,
            mustacheProps = emptyMap(),
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<head>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<script src="https://example.com/script1.js" type="text/javascript"></script>""" +
            """<script src="https://example.com/script2.js" type="text/javascript"></script>""" +
            """</head>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should inject multiple CSS and JavaScript resources`() {
        val html = "<html><head></head><body>Hello World</body></html>"
        val resources = listOf(
            DynamicResourceDto("https://example.com/style1.css", "text/css"),
            DynamicResourceDto("https://example.com/style2.css", "text/css"),
            DynamicResourceDto("https://example.com/script1.js", "text/javascript"),
            DynamicResourceDto("https://example.com/script2.js", "text/javascript")
        )

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = resources,
            mustacheProps = emptyMap(),
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<head>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<script src="https://example.com/script1.js" type="text/javascript"></script>""" +
            """<script src="https://example.com/script2.js" type="text/javascript"></script>""" +
            """<link rel="stylesheet" href="https://example.com/style1.css">""" +
            """<link rel="stylesheet" href="https://example.com/style2.css">""" +
            """</head>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should not modify HTML if no modifiers are provided`() {
        val html = "<html><head></head><body>Hello World</body></html>"
        val result = helper.modifyContent(html, emptyList(), emptyMap())

        val expected = """<html>""" +
            """<head>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """</head>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should inject CSS into HTML without head tag`() {
        val html = "<html><body>Hello World</body></html>"
        val cssResource = DynamicResourceDto("https://example.com/style.css", "text/css")

        val result = helper.modifyContent(html, listOf(cssResource), emptyMap())

        val expected = """<html>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<link rel="stylesheet" href="https://example.com/style.css">""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should inject JavaScript into HTML without head tag`() {
        val html = "<html><body>Hello World</body></html>"
        val jsResource = DynamicResourceDto("https://example.com/script.js", "text/javascript")

        val result = helper.modifyContent(html, listOf(jsResource), emptyMap())

        val expected = """<html>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<script src="https://example.com/script.js" type="text/javascript"></script>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should inject head with mustache and then inject CSS and JavaScript`() {
        val html = "<html>{{injectHead}}<body>{{greeting}} World</body></html>"
        val cssResource = DynamicResourceDto("https://example.com/style.css", "text/css")
        val jsResource = DynamicResourceDto("https://example.com/script.js", "text/javascript")
        val mustacheProps = mapOf("injectHead" to "<head></head>", "greeting" to "Hello")

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = listOf(cssResource, jsResource),
            mustacheProps = mustacheProps,
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<head>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<script src="https://example.com/script.js" type="text/javascript"></script>""" +
            """<link rel="stylesheet" href="https://example.com/style.css">""" +
            """</head>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should handle partial mustache properties and missing defaulting to blank`() {
        val html = "<html><body>{{greeting}} World, {{name}}</body></html>"
        val mustacheProps = mapOf("greeting" to "Hello")

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = emptyList(),
            mustacheProps = mustacheProps,
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<body>Hello World, </body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `should ignore unused mustache properties`() {
        val html = "<html><body>{{greeting}} World</body></html>"
        val mustacheProps = mapOf("greeting" to "Hello", "extraProp" to "This should not be used")
        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = emptyList(),
            mustacheProps = mustacheProps,
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<body>Hello World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `Default system variables should not be overridden in Mustache`() {
        val html = "<html>" +
            "<body>" +
            "Hello {{name}}\n" +
            "This is {{version}} comparing to {{ device.vendor }}, {{os.version}}" +
            "</body>" +
            "</html>"

        val mustacheProps = mapOf("name" to "World", "version" to "1.0.0-override")

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = emptyList(),
            mustacheProps = mustacheProps,
            engineType = EngineType.MUSTACHE
        )

        val expected = "<html>" +
            "<script type=\"text/javascript\">var tws_injected = true;</script>" +
            "<body>" +
            "Hello World\n" +
            "This is ${BuildConfig.TWS_VERSION} comparing to ${Build.MANUFACTURER ?: ""}, ${Build.VERSION.RELEASE ?: ""}" +
            "</body>" +
            "</html>"

        assertEquals(expected, result)
    }

    @Test
    fun `Default system variables for Mustache and CSS & JS injection`() {
        val html = "<html>" +
            "{{injectHead}}" +
            "<body>" +
            "Hello {{name}}\n" +
            "This is {{version}} comparing to {{ device.vendor }}, {{os.version}}" +
            "</body>" +
            "</html>"

        val mustacheProps = mapOf("injectHead" to "<head></head>", "name" to "World", "version" to "1.0.0-override")

        val cssResource = DynamicResourceDto("https://example.com/style.css", "text/css")
        val jsResource = DynamicResourceDto("https://example.com/script.js", "text/javascript")

        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = listOf(cssResource, jsResource),
            mustacheProps = mustacheProps,
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<head>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<script src="https://example.com/script.js" type="text/javascript"></script>""" +
            """<link rel="stylesheet" href="https://example.com/style.css">""" +
            """</head>""" +
            "<body>" +
            "Hello World\n" +
            "This is ${BuildConfig.TWS_VERSION} comparing to ${Build.MANUFACTURER ?: ""}, ${Build.VERSION.RELEASE ?: ""}" +
            "</body>" +
            "</html>"

        assertEquals(expected, result)
    }

    @Test
    fun `should ignore mustache due to different engine type`() {
        val html = "<html><body>{{greeting}} World</body></html>"
        val mustacheProps = mapOf("greeting" to "Hello")
        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = emptyList(),
            mustacheProps = mustacheProps,
            engineType = null
        )

        val expected = """<html>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<body>{{greeting}} World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }

    @Test
    fun `do not ignore mustache engine if mustache props are empty`() {
        val html = "<html><body>{{greeting}} World</body></html>"
        val result = helper.modifyContent(
            htmlContent = html,
            dynamicModifiers = emptyList(),
            mustacheProps = emptyMap(),
            engineType = EngineType.MUSTACHE
        )

        val expected = """<html>""" +
            """<script type="text/javascript">var tws_injected = true;</script>""" +
            """<body> World</body>""" +
            """</html>"""

        assertEquals(expected, result)
    }
}
