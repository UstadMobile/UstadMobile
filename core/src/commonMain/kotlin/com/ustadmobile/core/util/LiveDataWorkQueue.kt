package com.ustadmobile.core.util

import io.github.aakira.napier.Napier
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.lifecycle.Observer
import com.ustadmobile.lib.util.copyOnWriteListOf
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 *
 *
 * @property liveDataSource A LiveData object that will provide a list of a given type
 * @property sameItemFn This is essentially a diffutil so that we can avoid running the same item twice
 * @property numProcessors The number of coroutine receivers to launch to process data
 * @property mainDispatcher A coroutine dispatcher, that on Android, will dispatch onto the main thread.
 * This is required because observerForever on Android must be called from the main thread.
 * @property itemRunner A suspended function that will be executed for each new object received from the LiveData
 */
class LiveDataWorkQueue<T>(private val liveDataSource: LiveData<List<T>>,
                           private val sameItemFn: (item1: T, item2: T) -> Boolean,
                           private val numProcessors: Int = 1,
                           private val coroutineScope: CoroutineScope = GlobalScope,
                           private val mainDispatcher: CoroutineDispatcher = Dispatchers.Default,
                           private val onItemStarted: (T) -> Unit = {},
                           private val onItemFinished: (T) -> Unit = {},
                           private var onQueueEmpty: (T) -> Unit = {},
                           private val itemRunner: suspend (T) -> Unit) : Observer<List<T>> {

    private val channel: Channel<T> = Channel<T>(capacity = UNLIMITED)

    private val queuedOrActiveItems = copyOnWriteListOf<T>()

    private lateinit var coroutineCtx: CoroutineContext

    suspend fun start(){
        this.coroutineCtx = coroutineContext
        coroutineScope.launch {
            repeat(numProcessors) {procNum ->
                launch {
                    while(isActive) {
                        val nextItem = channel.receive()
                        try {
                            onItemStarted.invoke(nextItem)
                            itemRunner(nextItem)
                        }catch(e: Exception) {
                            Napier.e("LiveDataWorkQueue: exception running item $nextItem",
                                throwable = e)
                        }finally {
                            queuedOrActiveItems.remove(nextItem)
                            onItemFinished(nextItem)
                        }

                        if(queuedOrActiveItems.isEmpty() && channel.isEmpty)
                            onQueueEmpty.invoke(nextItem)
                    }
                }
            }
        }


        withContext(mainDispatcher) {
            liveDataSource.observeForever(this@LiveDataWorkQueue)
        }
    }

    //TODO: Add thread safety on JDBC/JVM. On Android: this will only ever be called on the main thread
    override fun onChanged(t: List<T>) {
        val itemsToQueue = t.filter { changedItem -> !queuedOrActiveItems.any { sameItemFn(it, changedItem) } }
        queuedOrActiveItems.addAll(itemsToQueue)
        coroutineScope.launch {
            itemsToQueue.forEach { channel.send(it) }
        }
    }

    suspend fun stop() {
        withContext(mainDispatcher) {
            liveDataSource.removeObserver(this@LiveDataWorkQueue)
        }
        coroutineCtx.cancel()
    }
}