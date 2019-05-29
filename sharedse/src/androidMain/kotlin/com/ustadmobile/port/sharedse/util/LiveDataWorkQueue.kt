package com.ustadmobile.port.sharedse.util

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock

/**
 * LiveDataWorkQueue can be used to run a queue of work items using a simple live data query
 * and an adapter.
 *
 * @param <T> The type of item to be run from the queue.
</T> */
class LiveDataWorkQueue<T>
/**
 * Create a new live data work queue
 *
 * @param maxThreads maximum number of concurrent threads
 */
(private val maxThreads: Int) {

    private val activeItems = Hashtable<Long, Runnable>()

    private val lock = ReentrantLock()

    private val executor: ExecutorService

    private var workSource: DoorLiveData<List<T>>? = null

    private var workObserver: DoorObserver<List<T>>? = null

    var adapter: WorkQueueItemAdapter<T>? = null

    private val currentQueue: AtomicReference<List<T>>

    private val completedItems: MutableSet<Long>

    private val queueEmptyListener: OnQueueEmptyListener? = null

    /**
     * The adapter must convert the item type into a WorkQueueItemHolder
     *
     * @param <T> The type of item being run from the queue
    </T> */
    interface WorkQueueItemAdapter<T> {

        fun makeRunnable(item: T): Runnable

        fun getUid(item: T): Long

    }

    interface OnQueueEmptyListener {

        fun onQueueEmpty()

    }


    private inner class RunWrapper<T>(private val item: T, var adapter: WorkQueueItemAdapter<T>) : Runnable {

        override fun run() {
            adapter.makeRunnable(item).run()
            this@LiveDataWorkQueue.handleItemFinished(adapter.getUid(item))
        }
    }

    init {
        executor = Executors.newFixedThreadPool(maxThreads)
        currentQueue = AtomicReference()
        completedItems = HashSet()
    }


    /**
     * Start observing the livedata source for items to execute
     *
     * @param workSource UmLiveData that will provide items to be adapted and executed
     */
    fun start(workSource: DoorLiveData<List<T>>) {
        this.workSource = workSource
        workObserver = DoorObserver { t -> handleWorkSourceChanged(t) }
        workSource.observeForever(workObserver!!)
    }

    /**
     * Shutdown the executor and stop observing
     */
    fun shutdown() {
        executor.shutdown()
        workSource!!.removeObserver(workObserver!!)
    }

    private fun handleWorkSourceChanged(sourceData: List<T>?) {
        try {
            lock.lock()
            currentQueue.set(sourceData)
            checkQueue()
        } finally {
            lock.unlock()
        }

    }

    private fun checkQueue() {
        val itemsToCheck = currentQueue.get() ?: return

        try {
            lock.lock()

            for (sourceItem in itemsToCheck) {
                val uid = adapter!!.getUid(sourceItem)
                if (activeItems.size < maxThreads && !activeItems.containsKey(uid)
                        && !completedItems.contains(uid)) {
                    val wrapper = RunWrapper(sourceItem, adapter!!)
                    activeItems[uid] = wrapper
                    executor.submit(wrapper)
                }
            }

            if (activeItems.isEmpty && queueEmptyListener != null) {
                queueEmptyListener.onQueueEmpty()
            }
        } finally {
            lock.unlock()
        }
    }


    private fun handleItemFinished(itemUid: Long) {
        try {
            lock.lock()
            activeItems.remove(itemUid)
            completedItems.add(itemUid)
            checkQueue()
        } finally {
            lock.unlock()
        }
    }

}
