package com.ustadmobile.core.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.jvm.Volatile

class EventCollator<T>(
    val maxWait: Long,
    private val onCollate: suspend (List<T>) -> Unit
) {

    private val channel = Channel<T>(Channel.UNLIMITED)

    @Volatile
    private var runJob: Job? = null

    private val mutex = Mutex()

    private suspend fun collateAndFire() {
        val fireList = mutableSetOf<T>()
        var channelResult: ChannelResult<T>
        do {
            channelResult = channel.tryReceive()
            if(channelResult.isSuccess)
                fireList += channelResult.getOrThrow()

        }while(channelResult.isSuccess)

        onCollate(fireList.toList())
    }

    suspend fun send(event: T) {
        channel.send(event)
        mutex.withLock {
            if(runJob == null){
                runJob = GlobalScope.launch {
                    delay(maxWait)
                    runJob = null
                    collateAndFire()
                }
            }
        }
    }



}