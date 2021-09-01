package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.createTemporaryDir
import com.ustadmobile.core.util.ext.emptyRecursively
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.sync.Mutex
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.coroutines.CoroutineContext
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

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.produceJobs() = produce<ContentJobItem> {
        var done : Boolean
        do {
            checkQueueSignalChannel.receive()

            //Check queue and filter out any duplicates that are being actively processed
            val queueItems = db.contentJobItemDao.findNextItemsInQueue(jobId, numProcessors * 2).filter {
                it.cjiUid !in activeJobItemIds
            }

            val numJobsToAdd = min(numProcessors - activeJobItemIds.size, queueItems.size)

            for(i in 0 until numJobsToAdd) {
                activeJobItemIds += queueItems[i].cjiUid
                db.contentJobItemDao.updateItemStatus(queueItems[i].cjiUid, JobStatus.RUNNING)
                send(queueItems[i])
            }

            done = db.contentJobItemDao.isJobDone(jobId)
        }while(!done)

        close()
    }

    private fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<ContentJobItem>) = launch {
        val tmpDir = createTemporaryDir("job-$id")

        for(item in channel) {
            val processContext = ProcessContext(tmpDir, null, mutableMapOf())
            println("Proessor #$id processing job #${item.cjiUid}")
            try {
                val plugin = if(item.cjiPluginId != 0) {
                    contentPluginManager.getPluginById(item.cjiPluginId)
                }else {
                    TODO("lookup using URI")
                }

                plugin.processJob(item, processContext, this@ContentJobRunner)
                println("Processor #$id completed job #${item.cjiUid}")
            }catch(e: Exception) {
                e.printStackTrace()
            }finally {
                activeJobItemIds -= item.cjiUid
                tmpDir.emptyRecursively()
                println("Processor #$id sending check queue signal #${item.cjiUid}")
                checkQueueSignalChannel.send(true)
            }
        }
    }

    @ExperimentalCoroutinesApi
    @Volatile
    private var jobItemProducer : ReceiveChannel<ContentJobItem>? = null

    override fun onProgress(contentJobItem: ContentJobItem) {

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