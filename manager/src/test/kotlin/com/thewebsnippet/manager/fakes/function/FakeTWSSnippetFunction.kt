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
package com.thewebsnippet.manager.fakes.function

import com.thewebsnippet.manager.domain.model.ProjectDto
import com.thewebsnippet.manager.domain.model.SharedSnippetDto
import com.thewebsnippet.manager.data.function.TWSSnippetFunction
import com.thewebsnippet.manager.utils.FakeService
import com.thewebsnippet.manager.utils.ServiceTestingHelper
import retrofit2.Response

internal class FakeTWSSnippetFunction(
    private val helper: ServiceTestingHelper = ServiceTestingHelper()
) : TWSSnippetFunction, FakeService by helper {
    var returnedProject: Response<ProjectDto>? = null
    var returnedSharedSnippet: SharedSnippetDto? = null

    override suspend fun getWebSnippets(projectId: String): Response<ProjectDto> {
        helper.intercept()

        return returnedProject ?: error("Returned project not faked!")
    }

    override suspend fun getSharedSnippetToken(shareId: String): SharedSnippetDto {
        helper.intercept()

        return returnedSharedSnippet ?: error("Returned shared snippet not faked!")
    }

    override suspend fun getSharedSnippetData(shareToken: String): Response<ProjectDto> {
        helper.intercept()

        return returnedProject ?: error("Returned project not faked!")
    }
}
