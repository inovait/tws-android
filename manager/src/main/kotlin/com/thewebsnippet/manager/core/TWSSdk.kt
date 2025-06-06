/*
 * Copyright 2025 INOVA IT d.o.o.
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

package com.thewebsnippet.manager.core

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.thewebsnippet.manager.R
import com.thewebsnippet.manager.data.manager.NoOpManager
import com.thewebsnippet.manager.data.notification.NotificationPayloadParserImpl
import com.thewebsnippet.manager.data.notification.NotificationHandlerImpl
import com.thewebsnippet.manager.ui.TWSViewPopupActivity

/**
 * Entry point for initializing and accessing the TWS SDK.
 *
 * This singleton provides a globally accessible [TWSManager] instance, which must be initialized
 * before use via [initialize]. Depending on your use case, you can provide a custom configuration
 * ([TWSConfiguration.Basic] or [TWSConfiguration.Shared]), or let the SDK load it from the
 * `AndroidManifest.xml` metadata.
 *
 * ### Example Usage
 * ```kotlin
 * // Default initialization using manifest metadata
 * TWSSdk.initialize(context)
 * val manager = TWSSdk.getInstance()
 *
 * // OR initialize with a specific configuration
 * val config = TWSConfiguration.Basic("your_project_id")
 * TWSSdk.initialize(context, config)
 * val manager = TWSSdk.getInstance()
 * ```
 *
 * Warning: Must be initialized before calling [getInstance], otherwise a no-op implementation is returned.
 */
object TWSSdk {
    private var manager: TWSManager = NoOpManager()

    /**
     * Retrieves the current [TWSManager] instance.
     *
     * @return The initialized [TWSManager], or a no-op implementation if [initialize] was not called.
     */
    fun getInstance(): TWSManager = manager

    /**
     * Initializes the SDK with an optional [TWSConfiguration].
     *
     * If no configuration is provided, it attempts to use the `projectId` defined in
     * the `AndroidManifest.xml` metadata.
     *
     * @param context The application context.
     * @param configuration Optional configuration for the SDK. If null, uses default metadata-based setup.
     */
    fun initialize(context: Context, configuration: TWSConfiguration? = null) {
        manager = configuration?.let {
            when (configuration) {
                is TWSConfiguration.Basic -> TWSFactory.get(context, configuration)
                is TWSConfiguration.Shared -> TWSFactory.get(context, configuration)
            }
        } ?: TWSFactory.get(context)

        manager.register()
    }

    /**
     * Handles and displays a push notification using the TWS SDK.
     *
     * This method processes notification payloads (typically from FCM or similar services) and displays a notification
     * to the user using the provided details. The notification is only shown if the SDK recognizes the payload as a
     * supported TWS notification (i.e., it includes the necessary fields and a supported type).
     *
     * Typical usage is from your `FirebaseMessagingService.onMessageReceived`, passing in the relevant
     * notification data. You may customize the notification appearance via `contentTitle`, `contentText`, `smallIcon`, etc.
     * Optionally, you can pass a navigation back stack (`historyIntents`) for proper navigation when the notification is opened.
     *
     * @param context The context for displaying the notification (usually the application context).
     * @param contentTitle The title to display in the notification.
     * @param contentText The message/body to display in the notification.
     * @param payload The notification payload as a [Map] (typically from FCM's `remoteMessage.data`),
     *                must include at least `projectId`, `snippetId`, and `type` for SDK to recognize and handle notification.
     * @param smallIcon The resource ID of the small icon to show in the notification. Defaults to TWS SDK's default icon.
     * @param autoCancel Whether to dismiss the notification when tapped. Default is `true`.
     * @param priority The notification priority (see [NotificationCompat]). Default is [NotificationCompat.PRIORITY_HIGH].
     * @param historyIntents An optional list of [Intent]s representing previous navigation history to construct the back stack.
     * @return `true` if the notification was handled and displayed by the SDK, `false` otherwise.
     */
    fun displayNotification(
        context: Context,
        contentTitle: String,
        contentText: String,
        payload: Map<String, String>,
        smallIcon: Int = R.drawable.ic_default_notification,
        autoCancel: Boolean = true,
        priority: Int = NotificationCompat.PRIORITY_HIGH,
        historyIntents: List<Intent> = emptyList()
    ): Boolean {
        return NotificationHandlerImpl(context).handle(
            contentTitle,
            contentText,
            payload,
            smallIcon,
            autoCancel,
            priority,
            historyIntents
        )
    }

    /**
     * Prepares an [Intent] to display a snippet detail screen from a push notification payload.
     *
     * Use this method to create an [Intent] that opens [TWSViewPopupActivity] for the snippet and project
     * specified in the given notification data. This is typically used when you want to launch the SDK's
     * snippet UI in response to a push notification tap.
     *
     * The returned [Intent] can be passed to PendingIntent.getActivity() and used as the `contentIntent`
     * in your notification.
     *
     * @param context The context used to create the intent.
     * @param data The notification payload, typically received from FCM or a similar service.
     *             Must contain `projectId`, `snippetId` and `type` for SDK to recognize and prepare an Intent.
     * @return An [Intent] to open full screen snippet, or `null` if the payload is invalid or missing required data.
     *
     */
    fun prepareNotificationIntent(
        context: Context,
        data: Map<String, String>
    ): Intent? {
        return NotificationPayloadParserImpl().parseMetadata(data)?.let {
            TWSViewPopupActivity.createIntent(context, it.snippetId, it.projectId)
        }
    }
}
