package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.domain.blob.BlobTransferJobItem
import com.ustadmobile.core.domain.blob.BlobTransferProgressUpdate
import com.ustadmobile.core.domain.blob.BlobTransferStatusUpdate

/**
 * UseCase to batch download a list of blobs, which are stored in the cache.
 */
interface BlobDownloadClientUseCase {

    suspend operator fun invoke(
        items: List<BlobTransferJobItem>,
        onProgress: (BlobTransferProgressUpdate) -> Unit = { },
        onStatusUpdate: (BlobTransferStatusUpdate) -> Unit = { },
    )


    suspend operator fun invoke(
        transferJobUid: Int,
    )

    companion object {

        const val DEFAULT_MAX_ATTEMPTS = 5

    }

}