package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCase

/**
 * Enqueue a blob upload. Running it is done by WorkManager on Android and by Quartz on JVM,
 * which will use BlobUploadClientUseCase
 */
interface EnqueueBlobUploadClientUseCase{

    suspend operator fun invoke(
        blobUrls: List<String>,
        batchUuid: String,
        chunkSize: Int = ChunkedUploadClientUseCase.DEFAULT_CHUNK_SIZE,
    )

}