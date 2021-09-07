package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.EventCollator
import com.ustadmobile.core.util.createTemporaryDir
import com.ustadmobile.core.io.ext.emptyRecursively
import com.ustadmobile.core.networkmanager.ConnectivityLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.jvm.Volatile
import kotlin.math.min

/**
 * Runs a given ContentJob.
 */
class ContentJobRunner(
    val jobId: Long,
    endpoint: Endpoint,
    override val di: DI,
    val numProcessors: Int = DEFAULT_NUM_PROCESSORS,
    val maxItemAttempts: Int = DEFAULT_NUM_RETRIES
) : DIAware, ContentJobProgressListener, DoorObserver<ConnectivityStatus?>{

    data class ContentJobResult(val status: Int)

    /**
     * Sending anything on this channel will result in one queue check. If there is an available
     * processor, one new item will be started.
     */
    private val checkQueueSignalChannel = Channel<Boolean>(Channel.UNLIMITED)

    private val activeJobItemIds = concurrentSafeListOf<Long>()

    private val db: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val repo: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val contentPluginManager: ContentPluginManager by on(endpoint).instance()

    private val eventCollator = EventCollator(1000, this::commitProgressUpdates)

    private val connectivityLiveData: ConnectivityLiveData by on(endpoint).instance()

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.produceJobs() = produce<ContentJobItemAndContentJob> {
        var done : Boolean
        try {
            withContext(doorMainDispatcher()) {
                connectivityLiveData.liveData.observeForever(this@ContentJobRunner)
            }

            do {
                checkQueueSignalChannel.receive()
                val numProcessorsAvailable = numProcessors - activeJobItemIds.size
                if(numProcessorsAvailable > 0) {
                    //Check queue and filter out any duplicates that are being actively processed
                    val queueItems = db.contentJobItemDao.findNextItemsInQueue(jobId, numProcessors * 2).filter {
                        (it.contentJobItem?.cjiUid ?: 0) !in activeJobItemIds
                    }

                    val numJobsToAdd = min(numProcessorsAvailable, queueItems.size)

                    for(i in 0 until numJobsToAdd) {
                        val contentJobItemUid = queueItems[i].contentJobItem?.cjiUid ?: 0L
                        activeJobItemIds +=  contentJobItemUid
                        db.contentJobItemDao.updateItemStatus(contentJobItemUid, JobStatus.RUNNING)
                        send(queueItems[i])
                    }
                }


                done = db.contentJobItemDao.isJobDone(jobId)
            }while(!done)
        }finally {
            withContext(NonCancellable + doorMainDispatcher()) {
                connectivityLiveData.liveData.removeObserver(this@ContentJobRunner)
            }
            close()
        }
    }

    private fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<ContentJobItemAndContentJob>) = launch {
        val tmpDir = createTemporaryDir("job-$id")

        for(item in channel) {
            val processContext = ProcessContext(tmpDir, null, mutableMapOf())
            println("Proessor #$id processing job #${item.contentJobItem?.cjiUid} attempt #${item.contentJobItem?.cjiAttemptCount}")
            try {
                val sourceUri = item.contentJobItem?.sourceUri?.let { DoorUri.parse(it) }
                    ?: throw IllegalArgumentException("ContentJobItem #${item.contentJobItem?.cjiUid} has no source uri!")

                if(item.contentJobItem?.cjiContentEntryUid == 0L) {
                    val metadataResult = contentPluginManager.extractMetadata(sourceUri)
                        ?: throw FatalContentJobException("ContentJobItem #${item.contentJobItem?.cjiUid}: cannot extract metadata")
                    val contentEntryUid = repo.contentEntryDao.insertAsync(metadataResult.entry)
                    item.contentJobItem?.cjiContentEntryUid = contentEntryUid
                    db.contentJobItemDao.updateContentEntryUid(item.contentJobItem?.cjiUid ?: 0,
                        contentEntryUid)
                }


                val plugin = if(item.contentJobItem?.cjiPluginId != 0) {
                    contentPluginManager.getPluginById(item.contentJobItem?.cjiPluginId ?: 0)
                }else {
                    TODO("this")
                }

                plugin.processJob(item, processContext, this@ContentJobRunner)
                println("Processor #$id completed job #${item.contentJobItem?.cjiUid}")
            }catch(e: Exception) {
                if(e is CancellationException)
                    throw e

                //something went wrong
                val finalStatus = if(e is FatalContentJobException ||
                        (item.contentJobItem?.cjiAttemptCount ?: maxItemAttempts) >= maxItemAttempts) {
                    JobStatus.FAILED
                }else {
                    JobStatus.QUEUED //requeue for another try
                }

                db.contentJobItemDao.updateJobItemAttemptCountAndStatus(
                    item.contentJobItem?.cjiUid ?: 0,
                    (item.contentJobItem?.cjiAttemptCount ?: 0) + 1, finalStatus)

                e.printStackTrace()
            }finally {
                activeJobItemIds -= (item.contentJobItem?.cjiUid ?: 0)
                tmpDir.emptyRecursively()
                println("Processor #$id sending check queue signal after finishing with #${item.contentJobItem?.cjiUid}")
                checkQueueSignalChannel.send(true)
            }
        }
    }

    @ExperimentalCoroutinesApi
    @Volatile
    private var jobItemProducer : ReceiveChannel<ContentJobItemAndContentJob>? = null

    override fun onProgress(contentJobItem: ContentJobItem) {
        GlobalScope.launch {
            eventCollator.send(contentJobItem.toProgressUpdate())
        }
    }

    private suspend fun commitProgressUpdates(updates: List<ContentJobItemProgressUpdate>) {
        db.contentJobItemDao.commitProgressUpdates(updates)
    }


    //This can be called using Worker (Android) or Quartz (JVM)
    suspend fun runJob(): ContentJobResult {
        withContext(Dispatchers.Default) {
            val producerVal = produceJobs().also {
                jobItemProducer = it
            }

            repeat(numProcessors) {
                launchProcessor(it, producerVal)
            }

            checkQueueSignalChannel.send(true)
        }

        //TODO: get the final status by doing a query
        return ContentJobResult(JobStatus.COMPLETE)
    }

    override fun onChanged(t: ConnectivityStatus?) {
        GlobalScope.launch {
            if(t != null){
                checkQueueSignalChannel.send(true)
            }
        }
    }

    companion object {

        const val DEFAULT_NUM_PROCESSORS = 10

        const val DEFAULT_NUM_RETRIES = 5

    }
}