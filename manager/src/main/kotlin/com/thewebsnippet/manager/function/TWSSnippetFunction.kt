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
package com.thewebsnippet.manager.function

import com.thewebsnippet.manager.data.ProjectDto
import com.thewebsnippet.manager.data.SharedSnippetDto
import jakarta.inject.Singleton
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

@Singleton
internal interface TWSSnippetFunction {
    @GET("organizations/{organizationId}/projects/{projectId}/v2/register")
    suspend fun getWebSnippets(
        @Path("organizationId")
        organizationId: String,
        @Path("projectId")
        projectId: String
    ): Response<ProjectDto>

    @Headers("Accept: application/json")
    @GET("shared/{shareId}")
    suspend fun getSharedSnippetToken(
        @Path("shareId")
        shareId: String
    ): SharedSnippetDto

    @GET("register-shared")
    suspend fun getSharedSnippetData(
        @Query("shareToken")
        shareToken: String
    ): Response<ProjectDto>
}
