package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.domain.blob.BlobTransferJobItem
import com.ustadmobile.core.domain.blob.BlobTransferProgressUpdate
import com.ustadmobile.core.domain.blob.BlobTransferStatusUpdate

interface BlobDownloadClientUseCase {



    suspend operator fun invoke(
        items: List<BlobTransferJobItem>,
        onProgress: (BlobTransferProgressUpdate) -> Unit = { },
        onStatusUpdate: (BlobTransferStatusUpdate) -> Unit = { },
    )



}