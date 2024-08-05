package com.ustadmobile.core.domain.getapiurl

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.ext.requirePostfix

/**
 * Implementation of GetApiUrlUseCase to be used when there is no embedded server intermediary
 * e.g. app-ktor-server and web version.
 */
class GetApiUrlUseCaseDirect(private val endpoint: Endpoint): GetApiUrlUseCase {

    override fun invoke(path: String): String {
        return "${endpoint.url.requirePostfix("/")}${path.removePrefix("/")}"
    }
}