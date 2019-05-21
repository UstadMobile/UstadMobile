package com.ustadmobile.port.sharedse.util

import java.util.Vector
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Simple queue manager class that can use an abstract source of Runnables (e.g. one tied to a
 * database query) and then submit them to an executor service.
 */
class WorkQueue(private val source: WorkQueueSource, private val maxThreads: Int) {

    private var executor: ExecutorService? = null

    private val activeItems: MutableList<Runnable>

    private val emptyWorkQueueListeners: MutableList<EmptyWorkQueueListener>

    interface WorkQueueSource {

        fun nextItem(): Runnable

    }

    interface EmptyWorkQueueListener {

        fun onQueueEmpty(queue: WorkQueue)

    }

    init {
        activeItems = Vector(maxThreads)
        emptyWorkQueueListeners = Vector()
    }

    fun start() {
        executor = Executors.newFixedThreadPool(maxThreads)
        checkQueue()
    }

    fun shutdown() {
        executor!!.shutdown()
    }

    fun checkQueue() {
        var nextItem: Runnable
        synchronized(activeItems) {
            while (activeItems.size < maxThreads && (nextItem = source.nextItem()) != null) {
                val runWrapper = {
                    nextItem.run()
                    activeItems.remove(nextItem)
                    checkQueue()
                }
                activeItems.add(nextItem)
                executor!!.submit(runWrapper)
            }

            if (activeItems.isEmpty()) {
                fireWorkQueueEmptyEvent()
            }
        }

    }

    protected fun fireWorkQueueEmptyEvent() {
        for (listener in emptyWorkQueueListeners) {
            listener.onQueueEmpty(this)
        }
    }

    fun addEmptyWorkQueueListener(listener: EmptyWorkQueueListener) {
        emptyWorkQueueListeners.add(listener)
    }

    fun removeEmptyWorkQueueListener(listener: EmptyWorkQueueListener) {
        emptyWorkQueueListeners.remove(listener)
    }


}
