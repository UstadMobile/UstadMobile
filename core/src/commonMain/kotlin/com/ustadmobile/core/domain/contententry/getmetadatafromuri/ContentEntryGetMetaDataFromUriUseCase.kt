package com.ustadmobile.core.domain.contententry.getmetadatafromuri

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.door.DoorUri

/**
 * @param processedBytes the bytes uploaded (on JS)
 */
data class ContentEntryGetMetadataStatus(
    val indeterminate: Boolean = true,
    val error: String? = null,
    val processedBytes: Long = 0,
    val totalBytes: Long = 0,
) {
    val progress: Int
        get() = if(totalBytes != 0L) {
            ((processedBytes * 100) / totalBytes).toInt()
        }else {
            0
        }
}

interface ContentEntryGetMetaDataFromUriUseCase {

    /**
     * @param contentUri the URI from which meta data should be extracted
     * @param fileName the original filename (this is often not possible to retrieve directly from
     *        the Uri itself e.g. on JS when a blob URL is created or when using Android content uris
     * @param endpoint endpoint
     * @param onProgress
     */
    suspend operator fun invoke(
        contentUri: DoorUri,
        fileName: String?,
        endpoint: Endpoint,
        onProgress: (ContentEntryGetMetadataStatus) -> Unit,
    ): MetadataResult

    companion object {

        const val HEADER_ORIGINAL_FILENAME = "upload-original-filename"
    }

}

