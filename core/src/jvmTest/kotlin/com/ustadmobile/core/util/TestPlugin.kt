package com.ustadmobile.core.util

import com.ustadmobile.core.contentjob.*
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


    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult? {
        return metadata
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ProcessContext, progress: ContentJobProgressListener): ProcessResult {
        return ProcessResult(200)
    }

    companion object{
        const val PLUGIN_ID = 5
    }
}