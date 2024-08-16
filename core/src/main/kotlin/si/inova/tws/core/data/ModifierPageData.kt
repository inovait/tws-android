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

package si.inova.tws.core.data

interface ModifierPageData {
   val inject: String?
}

data class ContentInjectData(val content: String, val type: EModifierInject) : ModifierPageData {
   override val inject = when (type) {
      EModifierInject.CSS -> injectContentCss(content)

      EModifierInject.JAVASCRIPT -> content.trimIndent()
   }

   private fun injectContentCss(value: String): String {
      return """
             (function() {
                 var style = document.createElement('style');
                 style.type = 'text/css';
                 style.innerHTML = `$value`;
                 document.head.appendChild(style);
             })();
         """.trimIndent()
   }
}

data class UrlInjectData(val url: String) : ModifierPageData {
   override val inject = when (checkFileType(url)) {
      EModifierInject.CSS -> injectUrlCss(url)

      EModifierInject.JAVASCRIPT -> injectUrlJs(url)
      null -> null
   }

   private fun injectUrlCss(url: String): String {
      return """
            var link = document.createElement('link');
            link.href = '$url';
            link.rel = 'stylesheet';
            document.head.appendChild(link);
         """.trimIndent()
   }

   private fun injectUrlJs(url: String): String {
      return """
            var script = document.createElement('script');
            script.src = '$url';
            script.type = 'text/javascript';
            document.head.appendChild(script);
         """.trimIndent()
   }
}

enum class EModifierInject {
   CSS,
   JAVASCRIPT
}

fun checkFileType(urlString: String): EModifierInject? {
   return when {
      urlString.endsWith(FILE_CSS, ignoreCase = true) -> EModifierInject.CSS
      urlString.endsWith(FILE_JS, ignoreCase = true) -> EModifierInject.JAVASCRIPT
      else -> null
   }
}

private const val FILE_CSS = ".css"
private const val FILE_JS = ".js"
