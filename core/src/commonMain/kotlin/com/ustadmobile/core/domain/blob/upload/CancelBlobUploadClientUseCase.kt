package com.ustadmobile.core.domain.blob.upload

interface CancelBlobUploadClientUseCase {

    suspend operator fun invoke(transferJobUid: Int)

}