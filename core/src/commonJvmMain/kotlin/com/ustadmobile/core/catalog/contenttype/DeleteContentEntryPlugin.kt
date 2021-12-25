package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.contentjob.ContentPluginIds.DELETE_CONTENT_ENTRY_PLUGIN
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.deleteFilesForContentJob
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import io.ktor.client.*
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class DeleteContentEntryPlugin(
        private var context: Any,
        private val endpoint: Endpoint,
        override val di: DI
): ContentPlugin {

    val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    override val pluginId: Int
        get() = DELETE_CONTENT_ENTRY_PLUGIN
    override val supportedMimeTypes: List<String>
        get() = listOf()
    override val supportedFileExtensions: List<String>
        get() = listOf()

    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        return null
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ContentJobProcessContext, progress: ContentJobProgressListener): ProcessResult {

        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")


        contentJobItem.cjiItemProgress = 25
        progress.onProgress(contentJobItem)

        // delete all containerEntries, containerEntryFiles and torrentFile for this contentEntry
        val numFailures = deleteFilesForContentJob(contentJobItem.cjiJobUid, di, endpoint)

        contentJobItem.cjiItemProgress = 100
        progress.onProgress(contentJobItem)

        return if(numFailures == 0){
            ProcessResult(JobStatus.COMPLETE)
        }else{
            ProcessResult(JobStatus.FAILED)
        }
    }
}