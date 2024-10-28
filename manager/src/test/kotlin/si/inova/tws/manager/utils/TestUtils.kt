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

import si.inova.tws.data.VisibilityDto
import si.inova.tws.data.WebSnippetDto
import si.inova.tws.manager.data.ActionBody
import si.inova.tws.manager.data.ProjectDto
import si.inova.tws.manager.data.SharedSnippetDto
import java.time.Instant

val FAKE_SNIPPET_ONE = WebSnippetDto(
    id = "0",
    target = "www.google.com",
    organizationId = "organization",
    projectId = "project",
    props = mapOf(
        Pair("tabName", "test1"),
        Pair("tabIcon", "icon1")
    )
)

val FAKE_SNIPPET_TWO = WebSnippetDto(
    id = "1",
    target = "www.blink.com",
    organizationId = "organization",
    projectId = "project",
    props = mapOf(
        Pair("tabName", "test2"),
        Pair("tabIcon", "icon2")
    )
)

val FAKE_SNIPPET_THREE = WebSnippetDto(
    id = "3",
    target = "www.example.com",
    organizationId = "organization",
    projectId = "project",
    props = mapOf(
        Pair("tabName", "test3"),
        Pair("tabIcon", "icon3")
    )
)

val FAKE_SNIPPET_FOUR = WebSnippetDto(
    id = "4",
    target = "www.popup1.com",
    organizationId = "organization",
    projectId = "project"
)

val FAKE_SNIPPET_FIVE = WebSnippetDto(
    id = "5",
    target = "www.popup2.com",
    organizationId = "organization",
    projectId = "project"
)

val FAKE_PROJECT_DTO = ProjectDto(
    snippets = listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO, FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE),
    listenOn = "wss:someUrl.com"
)

val FAKE_SHARED_PROJECT = SharedSnippetDto(snippet = FAKE_SNIPPET_ONE)

fun WebSnippetDto.toActionBody() = ActionBody(
    id = id,
    target = target,
    headers = headers,
    dynamicResources = dynamicResources,
    props = props
)

fun WebSnippetDto.setVisibility(ts: Long) = copy(
    visibility = VisibilityDto(untilUtc = Instant.ofEpochMilli(ts))
)

const val CREATE_SNIPPET = """
{
    "type": "SNIPPET_CREATED",
    "data": {
        "id": "test"
    }
}
"""

const val MILLISECONDS_DATE = 952_077_600_000 // 3.3.2000 10:00
const val MILLISECONDS_DATE_FUTURE_1 = 952_077_660_000 // 3.3.2000 10:01
const val MILLISECONDS_DATE_FUTURE_5 = 952_077_900_000 // 3.3.2000 10:11
const val MILLISECONDS_DATE_FUTURE_11 = 952_078_260_000 // 3.3.2000 10:11
const val MILLISECONDS_DATE_PAST = 952_077_540_000 // 3.3.2020 9:59
