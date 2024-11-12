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

package si.inova.tws.manager.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import si.inova.tws.data.TWSAttachment
import si.inova.tws.data.TWSEngine
import si.inova.tws.data.TWSSnippet

@JsonClass(generateAdapter = true)
@Keep
@Parcelize
internal data class WebSnippetDto(
    val id: String,
    val target: String,
    val organizationId: String,
    val projectId: String,
    val visibility: VisibilityDto? = null,
    val headers: Map<String, String>? = emptyMap(),
    val dynamicResources: List<TWSAttachment> = emptyList(),
    val props: Map<String, @RawValue Any> = emptyMap(),
    val engine: TWSEngine = TWSEngine.NONE,
    val loadIteration: Int = 0
) : Parcelable

internal fun WebSnippetDto.toTWSSnippet(localProps: Map<String, Any>) = TWSSnippet(
    id = this.id,
    target = this.target,
    headers = this.headers.orEmpty(),
    dynamicResources = this.dynamicResources,
    props = this.props + localProps,
    engine = this.engine,
    loadIteration = this.loadIteration
)
