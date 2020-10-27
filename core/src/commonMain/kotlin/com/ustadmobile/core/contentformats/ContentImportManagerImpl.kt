package com.ustadmobile.core.contentformats

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.lib.db.entities.ContainerUploadJob

//TODO: make this DIAware and use a context (it is tied to a specific database instance)
open class ContentImportManagerImpl(val contentPlugins: List<ContentTypePlugin>) : ContentImportManager {

    //TODO: most implementation can go here as platform specific stuff is handled by the ContentTypePlugin itself

    //This can also container the LiveDataWorkQueue and host ImportJobRunner here in core.
    //You can replace the dependency on NetworkManagerBle with a dependency network status livedata

    override suspend fun extractMetadata(filePath: String): ImportedContentEntryMetaData? {
        TODO("Not yet implemented")
    }

    override suspend fun queueImportContentFromFile(filePath: String, metadata: ImportedContentEntryMetaData): ContainerUploadJob {
        TODO("Not yet implemented")
    }

    override suspend fun importFileToContainer(filePath: String, metadata: ImportedContentEntryMetaData, progressListener: (Int) -> Unit) {
        TODO("Not yet implemented")
    }
}