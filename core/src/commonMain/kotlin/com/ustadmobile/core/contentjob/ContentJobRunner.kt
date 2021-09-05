package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.EventCollator
import com.ustadmobile.core.util.createTemporaryDir
import com.ustadmobile.core.util.ext.emptyRecursively
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.lib.db.entities.ContentJobItemProgressUpdate
import com.ustadmobile.lib.db.entities.toProgressUpdate
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
    val numProcessors: Int
) : DIAware, ContentJobProgressListener{

    data class ContentJobResult(val status: Int)

    private val checkQueueSignalChannel = Channel<Boolean>(numProcessors + 1)

    private val activeJobItemIds = concurrentSafeListOf<Long>()

    private val db: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val contentPluginManager: ContentPluginManager by on(endpoint).instance()

    private val eventCollator = EventCollator(1000, this::commitProgressUpdates)

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.produceJobs() = produce<ContentJobItemAndContentJob> {
        var done : Boolean
        do {
            checkQueueSignalChannel.receive()

            //Check queue and filter out any duplicates that are being actively processed
            val queueItems = db.contentJobItemDao.findNextItemsInQueue(jobId, numProcessors * 2).filter {
                (it.contentJobItem?.cjiUid ?: 0) !in activeJobItemIds
            }

            val numJobsToAdd = min(numProcessors - activeJobItemIds.size, queueItems.size)

            for(i in 0 until numJobsToAdd) {
                val contentJobItemUid = queueItems[i].contentJobItem?.cjiUid ?: 0L
                activeJobItemIds +=  contentJobItemUid
                db.contentJobItemDao.updateItemStatus(contentJobItemUid, JobStatus.RUNNING)
                send(queueItems[i])
            }

            done = db.contentJobItemDao.isJobDone(jobId)
        }while(!done)

        close()
    }

    private fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<ContentJobItemAndContentJob>) = launch {
        val tmpDir = createTemporaryDir("job-$id")

        for(item in channel) {
            val processContext = ProcessContext(tmpDir, null, mutableMapOf())
            println("Proessor #$id processing job #${item.contentJobItem?.cjiUid}")
            try {
                val plugin = if(item.contentJobItem?.cjiPluginId != 0) {
                    contentPluginManager.getPluginById(item.contentJobItem?.cjiPluginId ?: 0)
                }else {
                    TODO("lookup using URI")
                }

                //TODO
                if(item.contentJobItem?.cjiContentEntryUid == 0L) {
                    //Must extract metadata
                }

                plugin.processJob(item, processContext, this@ContentJobRunner)
                println("Processor #$id completed job #${item.contentJobItem?.cjiUid}")
            }catch(e: Exception) {
                if(e is CancellationException)
                    throw e

                e.printStackTrace()
            }finally {
                activeJobItemIds -= (item.contentJobItem?.cjiUid ?: 0)
                tmpDir.emptyRecursively()
                println("Processor #$id sending check queue signal #${item.contentJobItem?.cjiUid}")
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
        return ContentJobResult(200)
    }


}