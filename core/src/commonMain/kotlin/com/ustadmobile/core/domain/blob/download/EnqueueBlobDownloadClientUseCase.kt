package com.ustadmobile.core.domain.blob.download

interface EnqueueBlobDownloadClientUseCase {

    /**
     * @param expectedSize if not null, then the expected size of the download. If this is already
     *        known, it should be provided to avoid the need to make an HTTP head request to get the
     *        total size.
     */
    data class EnqueueBlobDownloadItem(
        val url: String,
        val expectedSize: Long? = null,
        val entityUid: Long = 0,
        val tableId: Int = 0,
    )

    suspend operator fun invoke(
        items: List<EnqueueBlobDownloadItem>,
        existingTransferJobId: Int = 0,
    )

}