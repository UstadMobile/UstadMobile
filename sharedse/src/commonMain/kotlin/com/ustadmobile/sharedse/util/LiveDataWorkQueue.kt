package com.ustadmobile.sharedse.util

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import kotlinx.atomicfu.AtomicArray
import kotlinx.atomicfu.atomicArrayOfNulls
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 *
 *
 * @property liveDataSource A DoorLiveData object that will provide a list of a given type
 * @property sameItemFn This is essentially a diffutil so that we can avoid running the same item twice
 * @property numProcessors The number of coroutine receivers to launch to process data
 * @property mainDispatcher A coroutine dispatcher, that on Android, will dispatch onto the main thread.
 * This is required because observerForever on Android must be called from the main thread.
 * @property itemRunner A suspended function that will be executed for each new object received from the LiveData
 */
class LiveDataWorkQueue<T>(private val liveDataSource: DoorLiveData<List<T>>,
                           private val sameItemFn: (item1: T, item2: T) -> Boolean,
                           private val numProcessors: Int = 1,
                           private val coroutineScope: CoroutineScope = GlobalScope,
                           private val mainDispatcher: CoroutineDispatcher = Dispatchers.Default,
                           private val itemRunner: suspend (T) -> Unit) : DoorObserver<List<T>> {

    private val recentlyRunItems = mutableSetOf<T>()

    private val activeWorkItems = atomicArrayOfNulls<T>(numProcessors)

    private val channel: Channel<T> = Channel<T>(capacity = UNLIMITED)

    private lateinit var coroutineCtx: CoroutineContext

    suspend fun start(){
        this.coroutineCtx = coroutineContext
        coroutineScope.launch {
            repeat(numProcessors) {procNum ->
                launch {
                    while(isActive) {
                        val nextItem = channel.receive()
                        activeWorkItems.get(procNum).value = nextItem
                        itemRunner(nextItem)
                        activeWorkItems.get(procNum).value = null
                    }
                }
            }
        }


        withContext(mainDispatcher) {
            liveDataSource.observeForever(this@LiveDataWorkQueue)
        }
    }

    override fun onChanged(t: List<T>) {
        val runningItems = (0..(numProcessors-1)).toList().map { activeWorkItems.get(it).value }
        t.filter { changedItem -> !runningItems.any { it != null && sameItemFn(it, changedItem) } }.forEach {
            coroutineScope.launch {
                recentlyRunItems.add(it)
                channel.send(it)
            }
        }
    }

    suspend fun stop() {
        withContext(mainDispatcher) {
            liveDataSource.removeObserver(this@LiveDataWorkQueue)
        }
        coroutineCtx.cancel()
    }
}