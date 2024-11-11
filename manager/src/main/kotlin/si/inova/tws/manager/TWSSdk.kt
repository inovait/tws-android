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

package si.inova.tws.manager

import android.content.Context
import android.content.pm.PackageManager
import jakarta.inject.Singleton

@Singleton
object TWSSdk {
    private var globalManager: TWSManager? = null

    fun initialize(context: Context, apiKey: String) {
        if (globalManager != null) {
            return
        }

        val organizationId = getMetaData(context, ORGANIZATION_ID_METADATA)
        val projectId = getMetaData(context, PROJECT_ID_METADATA)

        globalManager = TWSFactory.get(context, TWSConfiguration.Basic(organizationId, projectId, apiKey))
    }

    fun get(): TWSManager {
        return globalManager ?: error("TWS Sdk has not been initialized yet!")
    }

    private fun getMetaData(context: Context, key: String): String {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        return appInfo.metaData?.getString(key)
            ?: error("Missing metadata in Android Manifest. Please check if $key is provided.")
    }
}

private const val ORGANIZATION_ID_METADATA = "si.inova.tws.ORGANIZATION_ID"
private const val PROJECT_ID_METADATA = "si.inova.tws.PROJECT_ID"
