package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.upload.ChunkedUploadUseCase

class UploadBlobUseCaseJvm(
    private val chunkedUploadUseCase: ChunkedUploadUseCase,
): UploadBlobUseCase {

    override suspend fun invoke(
        blobUrls: List<String>,
        endpoint: Endpoint,
        onProgress: (Int) -> Unit
    ) {
        TODO("Not yet implemented")
    }

}