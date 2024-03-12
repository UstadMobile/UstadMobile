package com.ustadmobile.core.domain.blob.openblob

/**
 * Open a Blob as per platform defaults.
 * On Web: trigger a file download
 * On Android: Download file, use FileProvider and intent to open
 * On Desktop: Open in default program
 */
interface OpenBlobUseCase {

    /**
     * On Desktop/JS, only VIEW is available
     */
    enum class OpenBlobIntent {
        VIEW, SEND
    }

    suspend operator fun invoke(
        item: OpenBlobItem,
        onProgress: (bytesTransferred: Long, totalBytes: Long) ->  Unit = { _, _ -> },
        intent: OpenBlobIntent = OpenBlobIntent.VIEW,
    )

}