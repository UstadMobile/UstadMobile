package com.ustadmobile.core.domain.blob.download

interface CancelDownloadUseCase {

    suspend operator fun invoke(
        transferJobId: Int,
        offlineItemUid: Long,
    )

}