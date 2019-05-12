package com.ustadmobile.core.util

import org.junit.After
import org.junit.Assert
import org.junit.Test

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.spy
import org.mockito.Mockito.timeout
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class TestAsyncServiceManager {

    private var scheduledExecutor: ScheduledExecutorService? = null

    private val lastTimeStarting = AtomicLong()

    private val lastTimeStarted = AtomicLong()

    private val lastTimeStopped = AtomicLong()

    private val lastTimeStopping = AtomicLong()

    private fun setupAsyncServiceManagerMock(startDelay: Int, stopDelay: Int): AsyncServiceManager {
        lastTimeStopping.set(0L)
        lastTimeStopped.set(0L)
        lastTimeStarted.set(0L)
        lastTimeStopping.set(0L)

        val asyncServiceManager = spy(AsyncServiceManager::class.java)
        doAnswer {
            Thread {
                lastTimeStarting.set(System.currentTimeMillis())
                TimeUnit.MILLISECONDS.sleep(startDelay.toLong())
                lastTimeStarted.set(System.currentTimeMillis())
                asyncServiceManager.notifyStateChanged(AsyncServiceManager.STATE_STARTED)
            }.start()
            null
        }.`when`(asyncServiceManager).start()

        doAnswer {
            Thread {
                lastTimeStopping.set(System.currentTimeMillis())
                TimeUnit.MILLISECONDS.sleep(stopDelay.toLong())
                lastTimeStopped.set(System.currentTimeMillis())
                asyncServiceManager.notifyStateChanged(AsyncServiceManager.STATE_STOPPED)
            }.start()
            null
        }.`when`(asyncServiceManager).stop()

        scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

        asyncServiceManager.setDelayedExecutor(object : AsyncServiceManager.DelayedExecutor{
            override fun runAfterDelay(runnable: Runnable, delay: Long) {
                scheduledExecutor?.schedule(runnable, delay, TimeUnit.MILLISECONDS)
            }
        })

        return asyncServiceManager
    }

    @After
    fun tearDown() {
        if (scheduledExecutor != null)
            scheduledExecutor!!.shutdown()
    }

    @Test
    fun givenServiceStopped_whenSetEnabledTrueCalled_shouldCallStart() {
        val serviceManager = setupAsyncServiceManagerMock(1000, 1000)
        serviceManager.setEnabled(true)
        verify(serviceManager, timeout((2000 * 1000).toLong())).start()
        verify(serviceManager, times(1)).start()
    }

    @Test
    fun givenServiceStopped_whenSetEnabledCalledTwice_shouldCallStartOnce() {
        val serviceManager = setupAsyncServiceManagerMock(1000, 1000)
        serviceManager.setEnabled(true)
        serviceManager.setEnabled(true)
        verify(serviceManager, timeout(5000)).start()
        verify(serviceManager, times(1)).start()
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenServiceStarting_whenStopCalled_shouldStopAfterStarted() {
        val serviceManager = setupAsyncServiceManagerMock(0, 1000)
        serviceManager.setEnabled(true)
        Thread.sleep(1000)
        serviceManager.setEnabled(false)
        verify(serviceManager, timeout(5000)).stop()
    }

    @Test
    fun givenServiceStarting_whenStopCalled_shouldCallStopAfterStarted() {
        val startTime = System.currentTimeMillis()
        val serviceManager = setupAsyncServiceManagerMock(1000, 1000)
        serviceManager.setEnabled(true)
        TimeUnit.MILLISECONDS.sleep(500L)
        val startingTimeBeforeStopRequest = lastTimeStarting.get()

        serviceManager.setEnabled(false)

        verify(serviceManager, timeout(10000)).stop()
        verify(serviceManager, timeout(10000)).start()
        Assert.assertTrue("Service was starting when setEnabled false was called",
                startingTimeBeforeStopRequest >= startTime)
        Assert.assertTrue("Stop requested after service started completed",
                lastTimeStopping.get() > lastTimeStarted.get())
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenServiceStarting_whenAwaitCalled_shouldWaitForStarting() {
        val serviceManager = setupAsyncServiceManagerMock(200, 1000)
        serviceManager.setEnabled(true)
        serviceManager.await(object : AsyncServiceManager.AsyncAwaitChecker{
            override fun stopWaiting(newState: Int): Boolean {
                return  newState == AsyncServiceManager.STATE_STARTED;
            }

        }, 2000,
                TimeUnit.MILLISECONDS)
        verify(serviceManager, times(1)).start()
        Assert.assertEquals(AsyncServiceManager.STATE_STARTED.toLong(), serviceManager.state.toLong())
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenServiceStopped_whenStartingFails_shouldTargetStateStopped() {
        val asyncServiceManager = spy(AsyncServiceManager::class.java)
        doAnswer {
            Thread {
                lastTimeStarted.set(System.currentTimeMillis())
                try {
                    Thread.sleep(200)
                } catch (e: InterruptedException) {
                }

                asyncServiceManager.notifyStateChanged(AsyncServiceManager.STATE_STOPPED,
                        AsyncServiceManager.STATE_STOPPED)
            }.start()
            null
        }.`when`(asyncServiceManager).start()

        scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

        asyncServiceManager.setDelayedExecutor(object : AsyncServiceManager.DelayedExecutor{
            override fun runAfterDelay(runnable: Runnable, delay: Long) {
                scheduledExecutor?.schedule(runnable, delay, TimeUnit.MILLISECONDS)
            }
        })
        asyncServiceManager.setEnabled(true)
        asyncServiceManager.await(object : AsyncServiceManager.AsyncAwaitChecker{
            override fun stopWaiting(newState: Int): Boolean {
                return  newState == AsyncServiceManager.STATE_STOPPED
            }

        },
                5000, TimeUnit.MILLISECONDS)

        Assert.assertEquals("When a service is asked to start, but fails, both the" + "state and targe state can revert to stopped", AsyncServiceManager.STATE_STOPPED.toLong(),
                asyncServiceManager.state.toLong())
    }


}
