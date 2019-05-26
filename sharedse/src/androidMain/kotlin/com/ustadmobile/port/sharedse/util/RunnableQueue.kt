package com.ustadmobile.port.sharedse.util

import java.util.LinkedList
import java.util.Vector
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

class RunnableQueue {

    private val queue = Vector<Runnable>()

    private val ready = AtomicBoolean(false)

    private val lock = ReentrantLock()

    fun runWhenReady(runnable: Runnable) {
        try {
            lock.lock()
            if (ready.get()) {
                runnable.run()
            } else {
                queue.add(runnable)
            }
        } finally {
            lock.unlock()
        }
    }

    fun setReady(ready: Boolean) {
        var itemsToRun: List<Runnable>? = null
        try {
            lock.lock()
            if (ready && !this.ready.get()) {
                this.ready.set(ready)
                itemsToRun = LinkedList(queue)
                queue.clear()
            }
        } finally {
            lock.unlock()
        }

        if (itemsToRun != null) {
            for (r in itemsToRun) {
                r.run()
            }
        }
    }
}
