package com.ustadmobile.core.util

import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import org.kodein.di.DI

class TestPlugin(override val di: DI, val metadata: MetadataResult) : ContentPlugin {
    override val pluginId: Int
        get() = PLUGIN_ID
    override val supportedFileExtensions: List<String>
        get() = listOf()
    override val supportedMimeTypes: List<String>
        get() = listOf()


    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        return metadata
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ContentJobProcessContext, progress: ContentJobProgressListener): ProcessResult {
        return ProcessResult(JobStatus.COMPLETE)
    }

    companion object{
        const val PLUGIN_ID = 5
    }
}