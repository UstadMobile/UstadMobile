package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DIAware

interface ContentPlugin : DIAware {

    val pluginId: Int

    val supportedMimeTypes: List<String>

    val supportedFileExtensions: List<String>

    suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult?

    suspend fun processJob(
            jobItem: ContentJobItemAndContentJob,
            process: ProcessContext,
            progress: ContentJobProgressListener
    ) : ProcessResult

}