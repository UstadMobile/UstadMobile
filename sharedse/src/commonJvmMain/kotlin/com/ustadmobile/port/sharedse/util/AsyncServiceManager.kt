package com.ustadmobile.port.sharedse.util

import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * Utility class to help manage a service that starts and stops asynchronously.
 *
 */
abstract class AsyncServiceManager(var initialState: Int, var delayedExecutor: (Runnable, Long) -> Unit) {

    @Volatile
    private var targetState: Int = 0

    @Volatile
    var state: Int = 0
        private set

    private val lock = ReentrantLock()

    private val stateChangeListeners = ArrayList<OnStateChangeListener>()

    interface OnStateChangeListener {
        fun onStateChanged(serviceManager: AsyncServiceManager, newState: Int)
    }

    interface AsyncAwaitChecker {
        fun stopWaiting(newState: Int): Boolean
    }

//    constructor(initialState: Int, delayedExecutor: (Runnable, Long) -> Unit) : this(){
//        state = initialState
//        targetState = initialState
//        this.delayedExecutor = delayedExecutor
//    }

    //Blank constructor required for mocking for tests
    internal constructor(): this(0, {runnable, i ->  })

    fun setEnabled(enabled: Boolean) {
        try {
            lock.lock()
            targetState = if (enabled) STATE_STARTED else STATE_STOPPED
            if (targetState == state
                    || targetState == STATE_STARTED && state == STATE_STARTING
                    || targetState == STATE_STOPPED && state == STATE_STOPPING) {
                //nothing to do
                return
            }

            if (targetState == STATE_STARTED && state == STATE_STOPPED) {
                state = STATE_STARTING
                fireStateChangedEvent(state)
                delayedExecutor.invoke(Runnable { this.start() } ,0)
            } else if (targetState == STATE_STOPPED && state == STATE_STARTED) {
                state = STATE_STOPPING
                fireStateChangedEvent(state)
                delayedExecutor.invoke(Runnable { this.stop() }, 0)
            } else {
                delayedExecutor.invoke(Runnable { this.checkState() }, 1000)
            }
        } finally {
            lock.unlock()
        }
    }

    private fun checkState() {
        setEnabled(targetState == STATE_STARTED)
    }

    @JvmOverloads
    fun notifyStateChanged(state: Int, newTargetState: Int = -1) {
        try {
            lock.lock()
            this.state = state

            if (newTargetState != -1)
                targetState = newTargetState

        } finally {
            lock.unlock()
        }
        fireStateChangedEvent(state)
    }

    protected fun fireStateChangedEvent(newState: Int) {
        val listenerList = ArrayList<OnStateChangeListener>(stateChangeListeners.size)
        listenerList.addAll(stateChangeListeners)
        for (listener in listenerList) {
            listener.onStateChanged(this, newState)
        }
    }

    fun addOnStateChangeListener(listener: OnStateChangeListener) {
        stateChangeListeners.add(listener)
    }

    fun removeOnStateChangeListener(listener: OnStateChangeListener) {
        stateChangeListeners.remove(listener)
    }

    fun await(checker: (Int) -> Boolean, timeout: Long) {
        if (checker.invoke(state))
            return

        val latch = CountDownLatch(1)
        val listener = object : OnStateChangeListener {
            override fun onStateChanged(serviceManager: AsyncServiceManager, newState: Int) {
                if (checker.invoke(newState))
                    latch.countDown()
            }
        }
        addOnStateChangeListener(listener)
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) { /*should not happen*/
        }

        removeOnStateChangeListener(listener)
    }


    abstract fun start()

    abstract fun stop()

    companion object {

        const val STATE_STOPPED = 0

        const val STATE_STARTING = 1

        const val STATE_STARTED = 2

        const val STATE_STOPPING = 3
    }


}
