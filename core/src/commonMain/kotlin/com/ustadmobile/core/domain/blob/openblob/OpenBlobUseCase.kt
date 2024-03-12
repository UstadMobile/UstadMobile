package com.ustadmobile.core.domain.blob.openblob

/**
 * Open a Blob as per platform defaults.
 * On Web: trigger a file download
 * On Android: Download file, use FileProvider and intent to open
 * On Desktop: Open in default program
 */
interface OpenBlobUseCase {

    suspend operator fun invoke(
        item: OpenBlobItem,
        onProgress: (bytesTransferred: Long, totalBytes: Long) ->  Unit = { _, _ -> },
    )

}