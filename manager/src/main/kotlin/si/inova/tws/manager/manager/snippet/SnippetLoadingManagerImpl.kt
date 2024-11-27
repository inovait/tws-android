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

package si.inova.tws.manager.manager.snippet

import android.content.Context
import si.inova.tws.manager.TWSConfiguration
import si.inova.tws.manager.factory.BaseServiceFactory
import si.inova.tws.manager.factory.create
import si.inova.tws.manager.function.TWSSnippetFunction
import si.inova.tws.manager.manager.auth.Auth
import si.inova.tws.manager.manager.auth.AuthLoginManagerImpl
import si.inova.tws.manager.preference.AuthPreference
import si.inova.tws.manager.preference.AuthPreferenceImpl
import java.time.Instant

internal class SnippetLoadingManagerImpl(
    context: Context,
    private val configuration: TWSConfiguration,
    authPreference: AuthPreference = AuthPreferenceImpl(context),
    snippetLoginAuth: Auth = AuthLoginManagerImpl(authPreference),
    private val functions: TWSSnippetFunction = BaseServiceFactory(snippetLoginAuth).create()
) : SnippetLoadingManager {
    private var orgId: String? = null
    private var projId: String? = null

    override suspend fun load(): ProjectResponse {
        return if (configuration is TWSConfiguration.Basic) {
            loadProjectAndSetupWss(organizationId = configuration.organizationId, projectId = configuration.projectId)
        } else if (configuration is TWSConfiguration.Shared) {
            val cachedOrgId = orgId
            val cachedProjId = projId

            if (cachedProjId != null && cachedOrgId != null) {
                loadProjectAndSetupWss(organizationId = cachedOrgId, projectId = cachedProjId)
            } else {
                loadSharedSnippetData(sharedId = configuration.sharedId)
            }
        } else {
            error("Unknown configuration")
        }
    }

    private suspend fun loadSharedSnippetData(sharedId: String): ProjectResponse {
        val sharedSnippet = functions.getSharedSnippetData(sharedId).snippet
        return loadProjectAndSetupWss(sharedSnippet.organizationId, sharedSnippet.projectId, sharedSnippet.id)
    }

    private suspend fun loadProjectAndSetupWss(
        organizationId: String,
        projectId: String,
        sharedId: String? = null
    ): ProjectResponse {
        orgId = organizationId
        projId = projectId

        val twsProjectResponse = functions.getWebSnippets(organizationId, projectId)
        return ProjectResponse(
            twsProjectResponse.body() ?: error("Body not available"),
            twsProjectResponse.headers().getDate(HEADER_DATE)?.toInstant() ?: Instant.now(),
            sharedId
        )
    }

    companion object {
        private const val HEADER_DATE = "date"
    }
}
