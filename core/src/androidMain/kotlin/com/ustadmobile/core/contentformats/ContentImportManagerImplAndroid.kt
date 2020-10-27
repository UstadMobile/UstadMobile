package com.ustadmobile.core.contentformats

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.lib.db.entities.ContainerUploadJob

class ContentImportManagerImplAndroid(contentPlugins: List<ContentTypePlugin>) : ContentImportManagerImpl(contentPlugins) {

    override suspend fun queueImportContentFromFile(filePath: String, metadata: ImportedContentEntryMetaData): ContainerUploadJob {
        val uploadJob =  super.queueImportContentFromFile(filePath, metadata)

        //TODO: Start a foreground notification on Android to show progress to the user and prevent it being killed

        return uploadJob
    }
}