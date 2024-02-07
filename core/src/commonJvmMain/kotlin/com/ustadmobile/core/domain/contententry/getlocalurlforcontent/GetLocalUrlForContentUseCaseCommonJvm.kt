package com.ustadmobile.core.domain.contententry.getlocalurlforcontent

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer

class GetLocalUrlForContentUseCaseCommonJvm(
    private val endpoint: Endpoint,
    private val embeddedHttpServer: EmbeddedHttpServer,
): GetLocalUrlForContentUseCase {

    override fun invoke(contentEntryVersionUid: Long, pathInContentEntry: String): String {
        return embeddedHttpServer.endpointUrl(endpoint, "api/content/$contentEntryVersionUid/$pathInContentEntry")
    }
}
