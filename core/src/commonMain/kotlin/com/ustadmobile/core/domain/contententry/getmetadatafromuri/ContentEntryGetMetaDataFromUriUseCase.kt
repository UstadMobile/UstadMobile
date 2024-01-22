package com.ustadmobile.core.domain.contententry.getmetadatafromuri

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.door.DoorUri

data class ContentEntryGetMetadataStatus(
    val indeterminate: Boolean = true,
    val progress: Int = 0,
    val error: String? = null,
)

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

