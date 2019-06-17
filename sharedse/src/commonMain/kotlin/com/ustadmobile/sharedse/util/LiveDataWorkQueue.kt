package com.ustadmobile.sharedse.util

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class LiveDataWorkQueue<T>(private val liveDataSource: DoorLiveData<List<T>>,
                           private val sameItemFn: (item1: T, item2: T) -> Boolean,
                           private val numProcessors: Int = 1,
                           private val coroutineScope: CoroutineScope = GlobalScope,
                           private val itemRunner: suspend (T) -> Unit) : DoorObserver<List<T>> {

    private val recentlyRunItems = mutableSetOf<T>()

    private val channel: Channel<T> = Channel<T>(capacity = UNLIMITED)

    private lateinit var coroutineCtx: CoroutineContext

    suspend fun start(){
        this.coroutineCtx = coroutineContext
        coroutineScope.launch {
            repeat(numProcessors) {
                launch {
                    while(isActive) {
                        val nextItem = channel.receive()
                        itemRunner(nextItem)
                    }
                }
            }
        }



        liveDataSource.observeForever(this)
    }

    override fun onChanged(t: List<T>) {
        t.filter { changedItem -> !recentlyRunItems.any { sameItemFn(it, changedItem) } }.forEach {
            coroutineScope.launch {
                recentlyRunItems.add(it)
                channel.send(it)
            }
        }
    }

    suspend fun stop() {
        liveDataSource.removeObserver(this)
        coroutineCtx.cancel()
    }
}