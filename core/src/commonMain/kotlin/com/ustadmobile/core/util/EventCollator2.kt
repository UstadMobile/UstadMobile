package com.ustadmobile.core.util

import com.ustadmobile.door.ext.mutableLinkedListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * This will be migrated back to door.
 */
class EventCollator2<T>(
    private val maxWaitTime: Long,
    private val coroutineScope: CoroutineScope,
    capacity: Int,
    onBufferOverflow: BufferOverflow,
    private val onCollate: suspend (List<T>) -> Unit,
) {

    private fun Channel<T>.tryReceiveAll(): List<T> {
        val resultList = mutableLinkedListOf<T>()
        while(true) {
            val result = tryReceive()
            if(result.isSuccess)
                resultList += result.getOrThrow()
            else
                return resultList
        }
    }

    private val channel = Channel<T>(capacity = capacity, onBufferOverflow = onBufferOverflow)

    private var dispatchJob: Job? = null

    fun receiveEvent(event: T) {
        channel.trySend(event)

       if(dispatchJob == null){
           dispatchJob = coroutineScope.launch {
               delay(maxWaitTime)
               dispatchJob = null
               onCollate(channel.tryReceiveAll())
           }
       }
   }

}