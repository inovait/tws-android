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

package si.inova.tws.manager.network

import jakarta.inject.Singleton
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import si.inova.tws.manager.data.ProjectDto
import si.inova.tws.manager.data.SharedSnippetDto

@Singleton
interface WebSnippetFunction {
    @GET("organizations/{organizationId}/projects/{projectId}/register")
    suspend fun getWebSnippets(
        @Path("organizationId")
        organizationId: String,
        @Path("projectId")
        projectId: String,
        @Query("apiKey")
        apiKey: String? = null
    ): ProjectDto

    @Headers("Accept: application/json")
    @GET("shared/{shareId}")
    suspend fun getSharedSnippetData(
        @Path("shareId")
        shareId: String
    ): SharedSnippetDto
}
