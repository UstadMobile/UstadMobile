package com.ustadmobile.core.domain.getapiurl

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.util.ext.requirePostfix

/**
 * Implementation of GetApiUrlUseCase to be used when there is no embedded server intermediary
 * e.g. app-ktor-server and web version.
 */
class GetApiUrlUseCaseDirect(private val learningSpace: LearningSpace): GetApiUrlUseCase {

    override fun invoke(path: String): String {
        return "${learningSpace.url.requirePostfix("/")}${path.removePrefix("/")}"
    }
}