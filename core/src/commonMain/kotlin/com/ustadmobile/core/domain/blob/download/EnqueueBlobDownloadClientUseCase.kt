package com.ustadmobile.core.domain.blob.download

interface EnqueueBlobDownloadClientUseCase {

    data class EnqueueBlobDownloadItem(
        val url: String,
        val totalSize: Long? = null,
        val entityUid: Long = 0,
        val tableId: Int = 0,
    )

    suspend operator fun invoke(
        items: List<EnqueueBlobDownloadItem>
    )

}