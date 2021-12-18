package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.contentjob.ContentPluginIds.CONTAINER_DOWNLOAD_PLUGIN
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.util.ext.makeContentEntryDeepLink
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import org.kodein.di.DI
import org.kodein.di.DIAware

/**
 * This plugin will index a content entry folder and create new jobs for any child leaves or branches
 */
class ContentEntryBranchDownloadPlugin(
    context: Any,
    endpoint: Endpoint,
    di: DI,
) : AbstractContentEntryPlugin(context, endpoint, di), DIAware{

    override val pluginId: Int
        get() = CONTENT_ENTRY_BRANCH_DOWNLOAD_PLUGIN_ID

    override val supportedMimeTypes: List<String>
        get() = listOf()

    override val supportedFileExtensions: List<String>
        get() = listOf()

    override suspend fun extractMetadata(
        uri: DoorUri,
        process: ContentJobProcessContext
    ): MetadataResult? {
        return extractMetadata(ContentEntryList2View.VIEW_NAME, uri)
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progress: ContentJobProgressListener
    ): ProcessResult {
        //find all children, then make jobs for them
        val job = jobItem.contentJob ?: throw IllegalArgumentException("no job!")
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("No job item!")

        val contentEntryUid = contentJobItem.cjiContentEntryUid
        val jobUid = job.cjUid

        var offset = 0
        val batchSize = 500
        do {
            val childItemParams = repo.contentEntryDao.getContentJobItemParamsByParentUid(
                contentEntryUid, batchSize, offset)

            val jobItems = childItemParams.map {
                ContentJobItem().apply {
                    cjiItemTotal = it.mostRecentContainerSize
                    cjiJobUid = jobUid
                    cjiContentEntryUid = it.contentEntryUid
                    sourceUri = makeContentEntryDeepLink(it.contentEntryUid, it.leaf, endpoint)
                    cjiPluginId = if(it.leaf) CONTAINER_DOWNLOAD_PLUGIN else CONTENT_ENTRY_BRANCH_DOWNLOAD_PLUGIN_ID
                    cjiStatus = JobStatus.QUEUED
                    cjiIsLeaf = it.leaf
                    cjiConnectivityNeeded = true
                    cjiParentCjiUid = contentJobItem.cjiUid
                    cjiParentContentEntryUid = contentJobItem.cjiContentEntryUid
                }
            }
            db.contentJobItemDao.insertJobItems(jobItems)

            offset += childItemParams.size
        }while(childItemParams.size == batchSize)

        return ProcessResult(JobStatus.COMPLETE)
    }

    companion object {

        const val CONTENT_ENTRY_BRANCH_DOWNLOAD_PLUGIN_ID = 11

    }

}