package com.ustadmobile.core.domain.contententry.getmetadatafromuri

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.door.DoorUri

data class ContentEntryGetMetadataStatus(
    val indeterminate: Boolean = true,
    val progress: Int = 0,
)

interface IContentEntryGetMetaDataFromUriUseCase {

    suspend operator fun invoke(
        contentUri: DoorUri,
        endpoint: Endpoint,
        onProgress: (ContentEntryGetMetadataStatus) -> Unit,
    ): MetadataResult

}

