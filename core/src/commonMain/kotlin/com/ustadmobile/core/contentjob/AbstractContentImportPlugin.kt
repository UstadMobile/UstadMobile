package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.core.network.NetworkProgressListenerAdapter
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.updateTotalFromContainerSize
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

/**
 * Base class for a ContentPlugin that handle import of content from some source (e.g. URL, file, etc)
 * to create a Container, which may if needed be uploaded to the upstream server.
 */
abstract class AbstractContentImportPlugin(
    protected val endpoint: Endpoint,
    protected val uploader: ContentImporterUploader,
    protected val uriHelper: UriHelper,
) : ContentImporter {

    /**
     * This function should cache the given job into the HttpCache for storage.
     *
     * @param jobItem ContentJobItem
     * @param progressListener
     */
    abstract suspend fun addToCache(
        jobItem: ContentJobItemAndContentJob,
        progressListener: ContentJobProgressListener,
    ): ContentEntryVersion

    /**
     * Can be used to override whether or not content is uploaded to the upstream endpoint server.
     * By default this returns null, in which case the content will be uploaded if it is not a remote
     * url.
     */
    open suspend fun shouldUpload(): Boolean? {
        return false
    }

    /**
     * Create the URL prefix for a content item in the form of:
     * https://endpointserer.com/api/content/contentEntryVersionUid/
     */
    protected fun createContentUrlPrefix(contentEntryVersionUid: Long): String {
        return endpoint.url + ContentEntryVersion.PATH_POSTFIX + contentEntryVersionUid + "/"
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        progressListener: ContentJobProgressListener,
        transactionRunner: ContentJobItemTransactionRunner,
    ): ProcessResult {
        val contentJobItem = jobItem.contentJobItem
            ?: throw IllegalArgumentException("missing job item")
        val db: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
        val sourceUri = contentJobItem.sourceUri?.let { DoorUri.parse(it) }
            ?: throw IllegalArgumentException("ContentJobItem has no sourceUri")

        if(contentJobItem.cjiItemTotal == 0L) {
            contentJobItem.cjiItemTotal = uriHelper.getSize(sourceUri)
        }

        val shouldUpload = shouldUpload() ?: !sourceUri.isRemote()

        if(!contentJobItem.cjiContainerProcessed) {
            val contentEntryVersion = addToCache(jobItem, progressListener)
            contentJobItem.cjiContentEntryVersion = contentEntryVersion.cevUid

            transactionRunner.withContentJobItemTransaction {
                db.contentEntryVersionDao.insertAsync(contentEntryVersion)
                db.contentJobItemDao.updateContentJobItemContentEntryVersion(
                    cjiUid = contentJobItem.cjiUid,
                    contentEntryVersion = contentEntryVersion.cevUid
                )
                db.contentJobItemDao.updateContainerProcessed(contentJobItem.cjiUid, true)
                contentJobItem.updateTotalFromContainerSize(shouldUpload, db,
                    progressListener)
            }
            contentJobItem.cjiContainerProcessed = true
        }

        if(shouldUpload) {
            val haveConnectivityToContinueJob = db.contentJobDao
                .isConnectivityAcceptableForJob(jobItem.contentJob?.cjUid ?: 0)

            if (!haveConnectivityToContinueJob) {
                return ProcessResult(JobStatus.WAITING_FOR_CONNECTION)
            }

            transactionRunner.withContentJobItemTransaction { txDb ->
                txDb.contentJobItemDao.updateConnectivityNeeded(contentJobItem.cjiUid, true)
            }

            val progressListenerAdapter = NetworkProgressListenerAdapter(progressListener,
                contentJobItem)
            TODO()
        }

        return ProcessResult(JobStatus.COMPLETE)
    }
}