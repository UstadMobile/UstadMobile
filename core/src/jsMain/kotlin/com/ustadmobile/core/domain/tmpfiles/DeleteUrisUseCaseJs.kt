package com.ustadmobile.core.domain.tmpfiles

import web.url.URL

class DeleteUrisUseCaseJs(
    private val isTempFileCheckerUseCase: IsTempFileCheckerUseCase,
): DeleteUrisUseCase {
    override suspend fun invoke(uris: List<String>, onlyIfTemp: Boolean) {
        uris.forEach { uri ->
            if(!onlyIfTemp || isTempFileCheckerUseCase(uri)) {
                URL.revokeObjectURL(uri)
            }
        }
    }

}