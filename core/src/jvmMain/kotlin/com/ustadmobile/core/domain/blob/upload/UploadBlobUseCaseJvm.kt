package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.upload.ChunkedUploadUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class UploadBlobUseCaseJvm(
    private val chunkedUploadUseCase: ChunkedUploadUseCase,
): BlobBatchUploadUseCase {

    private val uploadScope = CoroutineScope(Dispatchers.IO + Job())

    override suspend fun invoke(
        blobUrls: List<String>,
        batchUuid: String,
        endpoint: Endpoint,
        onProgress: (Int) -> Unit
    ) {
        withContext(uploadScope.coroutineContext) {

        }


        TODO("Not yet implemented")
    }
}