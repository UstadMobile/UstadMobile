package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.DownloadJobItem
import org.kodein.di.DIAware

interface ContentPlugin : DIAware {

    val pluginId: Int

    val supportedMimeTypes: List<String>

    val supportedFileExtensions: List<String>

    suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult?

    suspend fun processJob(
            jobItem: ContentJobItem,
            process: ProcessContext,
            progress: ContentJobProgressListener
    ) : ProcessResult

}