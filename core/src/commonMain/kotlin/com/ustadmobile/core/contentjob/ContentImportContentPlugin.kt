package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.core.network.NetworkProgressListenerAdapter
import com.ustadmobile.core.util.ext.updateTotalFromContainerSize
import com.ustadmobile.core.util.ext.updateTotalFromLocalUriIfNeeded
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

/**
 * Base class for a ContentPlugin that handle import of content from some source (e.g. URL, file, etc)
 * to create a Container, which may if needed be uploaded to the upstream server.
 */
abstract class ContentImportContentPlugin(
    protected val endpoint: Endpoint,
    protected val context: Any,
    protected val uploader: ContentPluginUploader,
) : ContentPlugin {

    /**
     * This function should be implemented to make the container when required. This will only run
     * once per job.
     *
     * @param jobItem ContentJobItem
     * @param process ContentJobProcessContext
     * @param progressListener
     */
    abstract suspend fun makeContainer(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progressListener: ContentJobProgressListener,
        containerStorageUri: DoorUri,
    ): Container

    /**
     * Can be used to override whether or not content is uploaded to the upstream endpoint server.
     * By default this returns null, in which case the content will be uploaded if it is not a remote
     * url.
     */
    open suspend fun shouldUpload(): Boolean? {
        return null
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progress: ContentJobProgressListener
    ): ProcessResult {
        val contentJobItem = jobItem.contentJobItem
            ?: throw IllegalArgumentException("missing job item")
        val db: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
        val repo: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)

        val localUri = process.getLocalOrCachedUri()
        contentJobItem.updateTotalFromLocalUriIfNeeded(localUri, localUri.isRemote(),
            progress, context, di)

        val containerStorageManager: ContainerStorageManager = on(endpoint).direct.instance()

        val sourceUri = contentJobItem.sourceUri?.let { DoorUri.parse(it) }

        val shouldUpload = shouldUpload() ?: (sourceUri?.isRemote() == false)

        if(!contentJobItem.cjiContainerProcessed) {
            val containerStorageUri = DoorUri.parse(jobItem.contentJob?.toUri
                ?: containerStorageManager.storageList.first().dirUri)

            val container = makeContainer(jobItem, process, progress, containerStorageUri)

            contentJobItem.cjiContainerUid = container.containerUid

            process.withContentJobItemTransactionMutex { txDb ->
                txDb.contentJobItemDao.updateContentJobItemContainer(contentJobItem.cjiUid,
                    container.containerUid)
                txDb.contentJobItemDao.updateContainerProcessed(contentJobItem.cjiUid, true)
                contentJobItem.updateTotalFromContainerSize(shouldUpload, txDb,
                    progress)
            }
            contentJobItem.cjiContainerProcessed = true
        }

        if(shouldUpload) {
            val haveConnectivityToContinueJob = db.contentJobDao
                .isConnectivityAcceptableForJob(jobItem.contentJob?.cjUid ?: 0)

            if (!haveConnectivityToContinueJob) {
                return ProcessResult(JobStatus.WAITING_FOR_CONNECTION)
            }

            process.withContentJobItemTransactionMutex { txDb ->
                txDb.contentJobItemDao.updateConnectivityNeeded(contentJobItem.cjiUid, true)
            }

            val progressListenerAdapter = NetworkProgressListenerAdapter(progress,
                contentJobItem)
            return ProcessResult(uploader.upload(
                contentJobItem, progressListenerAdapter, di.direct.instance(), endpoint,
                process))
        }

        return ProcessResult(JobStatus.COMPLETE)
    }
}