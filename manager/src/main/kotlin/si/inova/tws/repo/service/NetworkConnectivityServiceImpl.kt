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

package si.inova.tws.repo.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dispatch.core.DispatcherProvider
import dispatch.core.IOCoroutineScope
import dispatch.core.dispatcherProvider
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import si.inova.tws.repo.data.NetworkStatus

@Singleton
internal class NetworkConnectivityServiceImpl(context: Context) : NetworkConnectivityService {

   private val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

   override val networkStatus: Flow<NetworkStatus> = callbackFlow {
      val connectivityCallback = object : ConnectivityManager.NetworkCallback() {
         override fun onAvailable(network: Network) {
            trySend(NetworkStatus.Connected)
         }

         override fun onUnavailable() {
            trySend(NetworkStatus.Disconnected)
         }

         override fun onLost(network: Network) {
            trySend(NetworkStatus.Disconnected)
         }
      }

      val request = NetworkRequest.Builder()
         .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
         .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
         .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
         .build()

      connectivityManager?.registerNetworkCallback(request, connectivityCallback)

      awaitClose {
         connectivityManager?.unregisterNetworkCallback(connectivityCallback)
      }
   }.distinctUntilChanged().flowOn(IOCoroutineScope(object : DispatcherProvider {}).dispatcherProvider)
}
