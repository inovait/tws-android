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

package si.inova.tws.core.ui.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableSharedFlow

internal class ScreenResetNotifier {
   private val _notifyFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

   fun requestScreenReset() {
      _notifyFlow.tryEmit(Unit)
   }

   @Composable
   fun Listen(callback: suspend () -> Unit) {
      LaunchedEffect(Unit) {
         _notifyFlow.collect {
            callback()
         }
      }
   }
}

@Composable
internal fun DoOnScreenReset(callback: () -> Unit) {
   LocalScreenResetNotifier.current.Listen(callback)
}

internal val LocalScreenResetNotifier = staticCompositionLocalOf<ScreenResetNotifier> {
   error("Screen reset notifier should be provided")
}
