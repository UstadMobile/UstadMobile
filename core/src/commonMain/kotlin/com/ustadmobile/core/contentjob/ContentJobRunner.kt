package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.kodein.di.DI
import org.kodein.di.DIAware
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.Volatile

/**
 * Runs a given ContentJob.
 */
class ContentJobRunner(
    val jobId: Int,
    private val endpoint: Endpoint,
    override val di: DI,
    val numProcessors: Int
) : DIAware, ContentJobProgressListener{

    data class ContentJobResult(val status: Int)

    private val checkQueueSignalChannel = Channel<Boolean>()

    private val activeJobs = concurrentSafeListOf<ContentJobItem>()

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.produceJobs() = produce<ContentJobItem> {
        while(true) {
            checkQueueSignalChannel.receive()


            //Check queue and send the next job


            //activeJobs += nextJob
            //send(nextJob)

            val done = false
            if(done)
                close()
        }
    }

    private fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<ContentJobItem>) = launch {
        for(item in channel) {
            //Use the correct plugin to actually run the job
            println("$id processing")
            checkQueueSignalChannel.trySend(true)
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
            checkQueueSignalChannel.send(true)
            val producerVal = produceJobs().also {
                jobItemProducer = it
            }

            repeat(numProcessors) {
                launchProcessor(it, producerVal)
            }

        }

        //TODO: get the final status by doing a query
        return ContentJobResult(-1)
    }


}