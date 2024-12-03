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

package com.thewebsnippet.manager.fakes.function

import com.thewebsnippet.manager.data.ProjectDto
import com.thewebsnippet.manager.data.SharedSnippetDto
import com.thewebsnippet.manager.function.TWSSnippetFunction
import com.thewebsnippet.manager.utils.FakeService
import com.thewebsnippet.manager.utils.ServiceTestingHelper
import retrofit2.Response

internal class FakeTWSSnippetFunction(
    private val helper: ServiceTestingHelper = ServiceTestingHelper()
) : TWSSnippetFunction, FakeService by helper {
    var returnedProject: Response<ProjectDto>? = null
    var returnedSharedSnippet: SharedSnippetDto? = null

    override suspend fun getWebSnippets(organizationId: String, projectId: String): Response<ProjectDto> {
        helper.intercept()

        return returnedProject ?: error("Returned project not faked!")
    }

    override suspend fun getSharedSnippetData(shareId: String): SharedSnippetDto {
        helper.intercept()

        return returnedSharedSnippet ?: error("Returned shared snippet not faked!")
    }
}
