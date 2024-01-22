package com.ustadmobile.core.domain.tmpfiles

class IsTempFileCheckerUseCaseJs : IsTempFileCheckerUseCase{

    override fun invoke(uri: String): Boolean {
        return uri.startsWith("blob:")
    }
}