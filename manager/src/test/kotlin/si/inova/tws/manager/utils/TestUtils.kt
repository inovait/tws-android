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

package si.inova.tws.manager.utils

import si.inova.tws.data.TWSAttachment
import si.inova.tws.data.TWSAttachmentType
import si.inova.tws.data.TWSSnippet
import si.inova.tws.manager.data.ActionBody
import si.inova.tws.manager.data.ActionType
import si.inova.tws.manager.data.ProjectDto
import si.inova.tws.manager.data.SharedSnippetDto
import si.inova.tws.manager.data.SnippetUpdateAction
import si.inova.tws.manager.data.TWSSnippetDto
import si.inova.tws.manager.data.VisibilityDto
import java.time.Instant

internal val FAKE_SNIPPET_ONE = TWSSnippetDto(
    id = "0",
    target = "www.google.com",
    organizationId = "organization",
    projectId = "project",
    props = mapOf(
        Pair("tabName", "test1"),
        Pair("tabIcon", "icon1")
    )
)

internal val FAKE_SNIPPET_ONE_ACTION_BODY_HTML = ActionBody(
    id = "0",
    target = "www.google.com",
    organizationId = "organization",
    projectId = "project",
    props = mapOf(
        Pair("tabName", "test1"),
        Pair("tabIcon", "icon1")
    )
)

internal val FAKE_EXPOSED_SNIPPET_ONE = TWSSnippet(
    id = "0",
    target = "www.google.com",
    props = mapOf(
        Pair("tabName", "test1"),
        Pair("tabIcon", "icon1")
    )
)

internal val FAKE_SNIPPET_TWO = TWSSnippetDto(
    id = "1",
    target = "www.blink.com",
    organizationId = "organization",
    projectId = "project",
    props = mapOf(
        Pair("tabName", "test2"),
        Pair("tabIcon", "icon2")
    )
)

internal val FAKE_EXPOSED_SNIPPET_TWO = TWSSnippet(
    id = "1",
    target = "www.blink.com",
    props = mapOf(
        Pair("tabName", "test2"),
        Pair("tabIcon", "icon2")
    )
)

internal val FAKE_SNIPPET_THREE = TWSSnippetDto(
    id = "3",
    target = "www.example.com",
    organizationId = "organization",
    projectId = "project",
    props = mapOf(
        Pair("tabName", "test3"),
        Pair("tabIcon", "icon3")
    )
)

internal val FAKE_EXPOSED_SNIPPET_THREE = TWSSnippet(
    id = "3",
    target = "www.example.com",
    props = mapOf(
        Pair("tabName", "test3"),
        Pair("tabIcon", "icon3")
    )
)

internal val FAKE_SNIPPET_FOUR = TWSSnippetDto(
    id = "4",
    target = "www.popup1.com",
    organizationId = "organization",
    projectId = "project"
)

internal val FAKE_EXPOSED_SNIPPET_FOUR = TWSSnippet(
    id = "4",
    target = "www.popup1.com"
)

internal val FAKE_SNIPPET_FIVE = TWSSnippetDto(
    id = "5",
    target = "www.popup2.com",
    organizationId = "organization",
    projectId = "project"
)

internal val FAKE_EXPOSED_SNIPPET_FIVE = TWSSnippet(
    id = "5",
    target = "www.popup2.com"
)

internal val FAKE_PROJECT_DTO = ProjectDto(
    snippets = listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO, FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE),
    listenOn = "wss:someUrl.com"
)

internal val FAKE_PROJECT_DTO_2 = ProjectDto(
    snippets = listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO),
    listenOn = "wss:someUrl2.com"
)

internal val FAKE_SHARED_PROJECT = SharedSnippetDto(snippet = FAKE_SNIPPET_ONE)

internal fun TWSSnippetDto.toActionBody() = ActionBody(
    id = id,
    target = target,
    headers = headers,
    dynamicResources = dynamicResources,
    props = props
)

internal fun TWSSnippetDto.setVisibility(ts: Long) = copy(
    visibility = VisibilityDto(untilUtc = Instant.ofEpochMilli(ts))
)

internal const val CREATE_SNIPPET = """
{
    "type": "snippetCreated",
    "data": {
        "id": "test"
    }
}
"""

internal const val UPDATE_SNIPPET_DYNAMIC_RESOURCES = """
{
    "type": "snippetUpdated",
    "data": {
        "id": "test",
        "dynamicResources":[{"contentType":"text/css","url":"https://www.test.css"}]
    }
}
"""

internal const val UPDATE_SNIPPET_PROPS = """
{
    "type": "snippetUpdated",
    "data": {
        "id": "test",
        "props":{"tabName":"Name of tab"}
    }
}
"""

internal const val UPDATE_SNIPPET_URL = """
{
    "type": "snippetUpdated",
    "data": {
        "id": "test",
        "target": "www.newtarget.url"
    }
}
"""

internal const val UPDATE_SNIPPET_HTML = """
{
    "type": "snippetUpdated",
    "data": {
        "id": "test"
    }
}
"""

internal const val DELETE_SNIPPET = """
{
    "type": "snippetDeleted",
    "data": {
        "id": "test"
    }
}
"""

internal val ADD_FAKE_SNIPPET_SOCKET = SnippetUpdateAction(
    type = ActionType.CREATED,
    data = ActionBody(id = "test")
)

internal val UPDATED_FAKE_SNIPPET_SOCKET = SnippetUpdateAction(
    type = ActionType.UPDATED,
    data = ActionBody(
        id = "test",
        dynamicResources = listOf(TWSAttachment(url = "https://www.test.css", contentType = TWSAttachmentType.CSS))
    )
)

internal val UPDATED_FAKE_SNIPPET_SOCKET_PROPS = SnippetUpdateAction(
    type = ActionType.UPDATED,
    data = ActionBody(id = "test", props = mapOf("tabName" to "Name of tab"))
)

internal val UPDATED_FAKE_SNIPPET_SOCKET_URL = SnippetUpdateAction(
    type = ActionType.UPDATED,
    data = ActionBody(id = "test", target = "www.newtarget.url")
)

internal val UPDATED_FAKE_SNIPPET_SOCKET_HTML = SnippetUpdateAction(
    type = ActionType.UPDATED,
    data = ActionBody(id = "test")
)

internal val DELETE_FAKE_SNIPPET_SOCKET = SnippetUpdateAction(
    type = ActionType.DELETED,
    data = ActionBody(id = "test")
)

internal const val MILLISECONDS_DATE = 952_077_600_000 // 3.3.2000 10:00
internal const val MILLISECONDS_DATE_FUTURE_1 = 952_077_660_000 // 3.3.2000 10:01
internal const val MILLISECONDS_DATE_FUTURE_5 = 952_077_900_000 // 3.3.2000 10:11
internal const val MILLISECONDS_DATE_FUTURE_11 = 952_078_260_000 // 3.3.2000 10:11
internal const val MILLISECONDS_DATE_PAST = 952_077_540_000 // 3.3.2020 9:59
