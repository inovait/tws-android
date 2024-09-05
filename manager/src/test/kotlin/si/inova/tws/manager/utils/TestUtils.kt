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

import si.inova.tws.manager.data.ActionBody
import si.inova.tws.manager.data.ProjectDto
import si.inova.tws.manager.data.SharedSnippetDto
import si.inova.tws.manager.data.WebSnippetDto

val FAKE_SNIPPET_ONE = WebSnippetDto(
    id = "0",
    target = "www.google.com",
    organizationId = "organization",
    projectId = "project",
    html = "<html></html>"
)

val FAKE_SNIPPET_TWO = WebSnippetDto(
    id = "1",
    target = "www.blink.com",
    organizationId = "organization",
    projectId = "project"
)

val FAKE_SNIPPET_THREE = WebSnippetDto(
    id = "3",
    target = "www.example.com",
    organizationId = "organization",
    projectId = "project"
)

val FAKE_PROJECT_DTO = ProjectDto(
    snippets = listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO),
    listenOn = "wss:someUrl.com"
)

val FAKE_SHARED_PROJECT = SharedSnippetDto(snippet = FAKE_SNIPPET_ONE)

fun WebSnippetDto.toActionBody() = ActionBody(
    id = id,
    target = target,
    html = html,
    projectId = projectId,
    organizationId = organizationId,
    headers = headers
)
