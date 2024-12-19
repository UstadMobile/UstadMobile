package com.ustadmobile.core.domain.contententry.getmetadatafromuri

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.contentformats.ContentImportersManager
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.door.DoorUri

class ContentEntryGetMetaDataFromUriUseCaseCommonJvm(
    private val importersManager: ContentImportersManager
): ContentEntryGetMetaDataFromUriUseCase {

    override suspend fun invoke(
        contentUri: DoorUri,
        fileName: String?,
        learningSpace: LearningSpace,
        onProgress: (ContentEntryGetMetadataStatus) -> Unit
    ): MetadataResult {
        return importersManager.extractMetadata(
            uri = contentUri,
            originalFilename = fileName
        ) ?: throw UnsupportedContentException(importersManager.supportedFormatNames().joinToString())
    }
}