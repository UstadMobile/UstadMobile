package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.commitProgressUpdates
import com.ustadmobile.core.util.EventCollator
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.*
import com.ustadmobile.door.util.TransactionMode
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.math.min

/**
 * Runs a given ContentJob.
 */
class ContentImportJobRunner(
    val jobId: Long,
    val endpoint: Endpoint,
    override val di: DI,
    val numProcessors: Int = DEFAULT_NUM_PROCESSORS,
    val maxItemAttempts: Int = DEFAULT_NUM_RETRIES
) : DIAware, ContentJobProgressListener, ContentJobItemTransactionRunner {

    data class ContentJobResult(val status: Int)

    /**
     * Sending anything on this channel will result in one queue check. If there is an available
     * processor, one new item will be started.
     */
    private val checkQueueSignalChannel = Channel<Boolean>(Channel.UNLIMITED)

    private val activeJobItemIds = concurrentSafeListOf<Long>()

    private val db: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val contentPluginManager: ContentImportersManager by on(endpoint).instance()

    private val eventCollator = EventCollator(500, this::commitProgressUpdates)

    private val logPrefix: String
        get() = "ContentJobRunner Job#${jobId} :"

    private val contentJobItemUpdateMutex = Mutex()

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.produceJobs() = produce<ContentJobItemAndContentJob> {
        var done = false
        try {
            do {
                Napier.d("$logPrefix waiting for signal to check queue")
                checkQueueSignalChannel.receive()
                val numProcessorsAvailable = numProcessors - activeJobItemIds.size
                Napier.d("$logPrefix num process available :$numProcessorsAvailable")
                val queueItemsToSend = mutableListOf<ContentJobItemAndContentJob>()

                val jobItemsToSend = withContentJobItemTransaction { txDb ->
                    if (numProcessorsAvailable > 0) {
                        //Check queue and filter out any duplicates that are being actively processed
                        val queueItems = txDb.contentJobItemDao.findNextItemsInQueue(jobId, numProcessors * 2).filter {
                            (it.contentJobItem?.cjiUid ?: 0) !in activeJobItemIds
                        }

                        val numJobsToAdd = min(numProcessorsAvailable, queueItems.size)
                        Napier.d("$logPrefix num of Jobs to add :$numJobsToAdd")

                        for (i in 0 until numJobsToAdd) {
                            val contentJobItemUid = queueItems[i].contentJobItem?.cjiUid ?: 0L
                            activeJobItemIds += contentJobItemUid
                            txDb.contentJobItemDao.updateItemStatus(contentJobItemUid,
                                JobStatus.RUNNING)
                            queueItemsToSend += queueItems[i]
                        }
                    }

                    done = txDb.contentJobItemDao.isJobDone(jobId)
                    Napier.d("$logPrefix is job Done :$done")
                    queueItemsToSend
                }

                jobItemsToSend.forEach {
                    send(it)
                }
            } while (!done)
        }catch(e: Exception) {
            Napier.d(e.stackTraceToString(), e)
        }finally {
            Napier.d("$logPrefix close produce job")
            close()
        }
    }

    private fun CoroutineScope.launchProcessor(
        id: Int,
        channel: ReceiveChannel<ContentJobItemAndContentJob>
    ) = launch {
        Napier.d("$logPrefix created tempDir job-$id")

        for(item in channel) {
            Napier.d("$logPrefix : " +
                "Proessor #$id processing job #${item.contentJobItem?.cjiUid} " +
                "attempt #${item.contentJobItem?.cjiAttemptCount}")

            var processResult: ProcessResult? = null
            var processException: Throwable? = null

            try {
                val cjiUid = item.contentJobItem?.cjiUid
                    ?: throw IllegalArgumentException("no content job item uid")
                contentJobItemUpdateMutex.withLock {
                    db.contentJobItemDao.updateStartTimeForJob(cjiUid, getSystemTimeInMillis())
                }

                val sourceUri = item.contentJobItem?.sourceUri?.let { DoorUri.parse(it) }
                    ?: throw IllegalArgumentException("ContentJobItem #${item.contentJobItem?.cjiUid} has no source uri!")

                var metadataResult: MetadataResult? = null
                if(item.contentJobItem?.cjiContentEntryUid == 0L) {
                    metadataResult = contentPluginManager.extractMetadata(sourceUri)
                        ?: throw FatalContentJobException("ContentImportJobRunner: $sourceUri " +
                                "has no ContentEntry and cannot extract metadata")
                    withContentJobItemTransaction { txDb ->
                        val contentEntryUid = txDb.contentEntryDao.insertAsync(
                            metadataResult.entry)
                        item.contentJobItem?.cjiContentEntryUid = contentEntryUid
                        txDb.contentJobItemDao.updateContentEntryUid(
                            cjiUid = item.contentJobItem?.cjiUid ?: 0,
                            contentEntryUid = contentEntryUid,
                            makeContentInactiveOnCancel = true,
                        )

                        if(item.contentJobItem?.cjiParentContentEntryUid != 0L){
                            txDb.contentEntryParentChildJoinDao.insertAsync(
                                ContentEntryParentChildJoin().apply {
                                    cepcjParentContentEntryUid =
                                        item.contentJobItem?.cjiParentContentEntryUid ?: 0L
                                    cepcjChildContentEntryUid =
                                        item.contentJobItem?.cjiContentEntryUid ?: 0L
                                })
                        }
                    }
                }

                val pluginId = if(item.contentJobItem?.cjiPluginId == 0) {
                    metadataResult?.importerId ?: contentPluginManager.extractMetadata(sourceUri)
                        ?.importerId ?: -1
                }else {
                    item.contentJobItem?.cjiPluginId ?: 0
                }

                val plugin = contentPluginManager.requireImporterById(pluginId)

                val jobResult = async {
                    try {
                        plugin.processJob(
                            jobItem = item,
                            progressListener = this@ContentImportJobRunner,
                            transactionRunner = this@ContentImportJobRunner
                        ).also {
                            Napier.i("ContentJobRunner: completed process")
                        }
                    }catch(e: Exception) {
                        Napier.e("ContentJobRunner: jobResult: caught exception", e)
                        processException = e
                        ProcessResult(JobStatus.FAILED)
                    }
                }


                processResult = jobResult.await()

                withContentJobItemTransaction { txDb ->
                    txDb.contentJobItemDao.updateItemStatus(item.contentJobItem?.cjiUid ?: 0,
                        processResult.status)
                    txDb.contentJobItemDao.updateFinishTimeForJob(item.contentJobItem?.cjiUid ?: 0,
                        systemTimeInMillis())
                }

                Napier.d {
                    "$logPrefix Processor #$id completed job #${item.contentJobItem?.cjiUid} " +
                    "status=${JobStatus.statusToString(processResult.status)}"
                }
            }catch(e: Exception) {
                //something went wrong
                processException = e
                e.printStackTrace()
                delay(1000)
            }finally {
                withContext(NonCancellable) {
                    val finalStatus: Int = when {
                        processException == null && processResult != null -> {
                            // if status can is queued (e.g. due to needing to wait for connectivity
                            // to continue the job), don't count that as an attempt
                            if(processResult.status != JobStatus.QUEUED){
                                item.contentJobItem?.cjiAttemptCount = (item.contentJobItem?.cjiAttemptCount ?: 0) + 1
                            }

                            processResult.status
                        }
                        processException is FatalContentJobException -> {
                            item.contentJobItem?.cjiAttemptCount = (item.contentJobItem?.cjiAttemptCount ?: 0) + 1
                            JobStatus.FAILED
                        }
                        processException is ContentTypeNotSupportedException -> JobStatus.COMPLETE
                        processException is CancellationException -> JobStatus.CANCELED
                        (item.contentJobItem?.cjiAttemptCount ?: maxItemAttempts) >= maxItemAttempts -> JobStatus.FAILED
                        else -> {
                            item.contentJobItem?.cjiAttemptCount = (item.contentJobItem?.cjiAttemptCount ?: 0) + 1
                            JobStatus.QUEUED
                        }
                    }

                    withContentJobItemTransaction { txDb ->
                        txDb.contentJobItemDao.updateJobItemAttemptCountAndStatus(
                            item.contentJobItem?.cjiUid ?: 0,
                            item.contentJobItem?.cjiAttemptCount?: 0, finalStatus)
                        txDb.contentJobItemDao.updateFinishTimeForJob(
                            item.contentJobItem?.cjiUid ?: 0, systemTimeInMillis())
                    }

                    activeJobItemIds -= (item.contentJobItem?.cjiUid ?: 0)
                    Napier.d {
                        "$logPrefix Processor #$id sending check queue signal after " +
                        "finishing with #${item.contentJobItem?.cjiUid} " +
                        "status=${JobStatus.statusToString(finalStatus)} " +
                        "attempts=${item.contentJobItem?.cjiAttemptCount}"
                    }
                }

                if(processException is CancellationException &&
                    processException !is ConnectivityCancellationException)
                    throw processException as CancellationException

                checkQueueSignalChannel.send(true)
            }
        }
    }

    /**
     * Concurrent updates to ContentJobItem can cause a transaction deadlock on postgres. Therefor
     * all updates to ContentJobItem need to be done in a Mutex (e.g. progress, etc).
     */
    override suspend fun <R> withContentJobItemTransaction(block: suspend (UmAppDatabase) -> R): R {
        return contentJobItemUpdateMutex.withLock {
            db.withDoorTransactionAsync(TransactionMode.READ_WRITE, block)
        }
    }

    override fun onProgress(contentJobItem: ContentJobItem) {
        GlobalScope.launch {
            eventCollator.send(contentJobItem.toProgressUpdate())
        }
    }

    private suspend fun commitProgressUpdates(updates: List<ContentJobItemProgressUpdate>) {
        withContentJobItemTransaction { txDb ->
            txDb.contentJobItemDao.commitProgressUpdates(updates)
        }
    }


    //This can be called using Worker (Android) or Quartz (JVM)
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun runJob(): ContentJobResult {
        withContext(Dispatchers.Default) {
            val producerVal = produceJobs()

            try {
                /* this is used so we can catch the exception for launchProcessor as seen in key point 5 in below article.
                   https://www.lukaslechner.com/why-exception-handling-with-kotlin-coroutines-is-so-hard-and-how-to-successfully-master-it*/
                coroutineScope {
                    repeat(numProcessors) {
                        Napier.d("$logPrefix launch processor $it")
                        launchProcessor(it, producerVal)
                    }
                    Napier.d("$logPrefix run Job, send queue signal")
                    checkQueueSignalChannel.send(true)
                }

            }catch(e: CancellationException) {
                withContext(NonCancellable) {
                    withContentJobItemTransaction { txDb ->
                        txDb.contentEntryDao.updateContentEntryActiveByContentJobUid(jobId,
                            true, systemTimeInMillis())
                        txDb.contentJobItemDao.updateAllStatusesByJobUid(jobId, JobStatus.CANCELED)
                    }

                    throw e
                }
            }

            Napier.d("$logPrefix run Job, send queue signal")
            checkQueueSignalChannel.send(true)
        }


        return ContentJobResult(JobStatus.COMPLETE)
    }

    companion object {

        const val DEFAULT_NUM_PROCESSORS = 10

        const val DEFAULT_NUM_RETRIES = 5

    }
}