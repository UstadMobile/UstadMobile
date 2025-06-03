package com.ustadmobile.core.domain.getapiurl

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer

/**
 * Implements GetApiUrlUseCase for platforms that use the embedded server (Android, Desktop)
 */
class GetApiUrlUseCaseEmbeddedServer(
    private val embeddedServer: EmbeddedHttpServer,
    private val learningSpace: LearningSpace,
): GetApiUrlUseCase {

    override fun invoke(path: String): String {
        return embeddedServer.learningSpaceUrl(learningSpace, path)
    }
}