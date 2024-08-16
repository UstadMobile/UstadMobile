package com.ustadmobile.core.domain.contententry.getlocalurlforcontent

import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase

class GetLocalUrlForContentUseCaseCommonJvm(
    private val getApiUrlUseCase: GetApiUrlUseCase,
): GetLocalUrlForContentUseCase {

    override fun invoke(contentEntryVersionUid: Long, pathInContentEntry: String): String {
        return getApiUrlUseCase("api/content/$contentEntryVersionUid/$pathInContentEntry")
    }
}
