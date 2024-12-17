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
package com.thewebsnippet.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Represents a dynamic resource that can be injected into a WebView.
 *
 * @param url The URL of the resource to inject.
 * @param contentType The type of resource, such as CSS or JavaScript.
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class TWSAttachment(
    val url: String,
    val contentType: TWSAttachmentType
) : Parcelable {
    @IgnoredOnParcel
    val inject = when (contentType) {
        TWSAttachmentType.CSS -> injectUrlCss()
        TWSAttachmentType.JAVASCRIPT -> injectUrlJs()
        TWSAttachmentType.OTHER -> null
    }
}

private fun TWSAttachment.injectUrlCss(): String {
    return """<link rel="stylesheet" href="$url">""".trimIndent()
}

private fun TWSAttachment.injectUrlJs(): String {
    return """<script src="$url" type="text/javascript"></script>"""
}
