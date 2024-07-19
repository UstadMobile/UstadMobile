package com.ustadmobile.core.domain.getapiurl

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer

/**
 * Implements GetApiUrlUseCase for platforms that use the embedded server (Android, Desktop)
 */
class GetApiUrlUseCaseEmbeddedServer(
    private val embeddedServer: EmbeddedHttpServer,
    private val endpoint: Endpoint,
): GetApiUrlUseCase {

    override fun invoke(path: String): String {
        return embeddedServer.endpointUrl(endpoint, path)
    }
}