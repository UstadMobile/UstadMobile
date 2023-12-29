package com.ustadmobile.core.domain.upload

import com.ustadmobile.core.domain.blob.TransferJobItemStatus
import com.ustadmobile.core.util.stringvalues.IStringValues
import com.ustadmobile.door.DoorUri

/**
 * Chunked upload client use case where data is read from a local URI. This can be a file or other
 * supported Uri on JVM/Android, or a blob/file URI on JS.
 */
interface ChunkedUploadClientLocalUriUseCase {

    /**
     * Response from the last chunk that was uploaded (e.g. when the upload was completed)
     */
    data class LastChunkResponse(
        val body: String?,
        val statusCode: Int,
        val headers: IStringValues,
    )

    suspend operator fun invoke(
        uploadUuid: String,
        localUri: DoorUri,
        remoteUrl: String,
        fromByte: Long = 0,
        chunkSize: Int = DEFAULT_CHUNK_SIZE,
        onProgress: (Long) -> Unit = { },
        onStatusChange: (TransferJobItemStatus) -> Unit = { },
    ): LastChunkResponse



}