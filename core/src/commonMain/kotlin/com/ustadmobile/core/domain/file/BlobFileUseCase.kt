package com.ustadmobile.core.domain.file

interface BlobFileUseCase {

    /**
     * Reads file data from a blob URL and returns bytes, filename, and mime type
     */
    suspend operator fun invoke(
        blobUrl: String,
        filename: String,
        mimeType: String? = null
    ): ByteArray?
}