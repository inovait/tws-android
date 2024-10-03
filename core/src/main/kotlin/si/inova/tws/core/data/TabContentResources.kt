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

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

/**
 * The TabContentResources class is used to represent the visual and textual information associated with a tab in a UI component.
 * This class holds a name and an icon, allowing developers to define both text and image resources that will represent a
 * tab in a TabsWebSnippetComponent.
 *
 */
@Parcelize
data class TabContentResources(
    val name: String? = null,
    val icon: TabIcon? = null
) : Parcelable

interface TabIcon : Parcelable {
    /**
     * Represents an icon with an external URL. This is useful when the icon is hosted remotely and needs to be
     * loaded dynamically via the internet.
     *
     * @property value The URL pointing to the icon image.
     * @property contentDescription An optional content description for accessibility purposes, which provides a textual
     * description of the icon.
     */
    @Parcelize
    data class Url(
        val value: String,
        val contentDescription: String? = null
    ) : TabIcon

    /**
     * Represents an icon that uses a drawable resource from the Android app's resources.
     * This is ideal when the icon is stored locally within the app.
     *
     * @property res The resource ID of the drawable image.
     * @property contentDescription An optional content description for accessibility purposes, which provides a textual
     * description of the icon.
     */
    @Parcelize
    data class Drawable(
        @DrawableRes
        val res: Int,
        val contentDescription: String? = null
    ) : TabIcon

    /**
     * Represents an icon by using a universal index. This is useful when icons are referenced by a standardized index
     * across different contexts, making the icon selection independent of specific resources or URLs. Allows developers
     * to define any custom behavior.
     *
     * @property index The index of the item in the TabWebSnippetComponent tab bar.
     */
    @Parcelize
    data class Universal(val index: Int) : TabIcon
}
