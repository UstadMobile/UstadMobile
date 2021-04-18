package com.ustadmobile.sharedse.controller

import org.mockito.kotlin.mock
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class DownloadJobConnectivityChangeTriggerJDBCEventsTest {


    private lateinit var umAppDatabase: UmAppDatabase

    private val context = mock<DoorLifecycleOwner> {
        on { currentState }.thenReturn(DoorLifecycleObserver.STARTED)
    } as Any


    private lateinit var entry: ContentEntry

    private lateinit var downloadJob: DownloadJob

    private val MAX_WAIT_TIME = 3000L

    companion object{
        private val NETWORK_SSID = "NetworkSSID"
        private val mOnChangeCalledCounter: AtomicInteger =  AtomicInteger(0)
    }

    private lateinit var connectivityStatus: ConnectivityStatus


    class DownloadItemsObserver: DoorObserver<List<DownloadJobItem>>{
        override fun onChanged(t: List<DownloadJobItem>) {
            println("$NETWORK_SSID: onChange called with value " +
                    "= ${mOnChangeCalledCounter.getAndIncrement()}")
        }
    }

    @Before
    fun setUp(){

        umAppDatabase = UmAppDatabase.getInstance(context)
        umAppDatabase.clearAllTables()

        entry = ContentEntry("title 2", "title 2", leaf = true, publik = true)
        entry.contentEntryUid = umAppDatabase.contentEntryDao.insert(entry)

        downloadJob = DownloadJob(entry.contentEntryUid, System.currentTimeMillis())
        downloadJob.djUid = umAppDatabase.downloadJobDao.insert(downloadJob).toInt()

        connectivityStatus = ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, NETWORK_SSID)

    }


    //19/July/19 - temporarily disabled for testing out coverage
    //@Test
    fun givenActiveDownloadJob_whenConnectivityStatusChanges_shouldTriggerUpdates() = runBlocking{


        umAppDatabase.connectivityStatusDao.insertAsync(connectivityStatus)

        umAppDatabase.downloadJobItemDao.findNextDownloadJobItems()
                .observeForever(DownloadItemsObserver())

        println("$NETWORK_SSID: waiting......")
        Thread.sleep(MAX_WAIT_TIME)

        println("$NETWORK_SSID: connection changing to disconnected")
        umAppDatabase.connectivityStatusDao.updateState(ConnectivityStatus.STATE_DISCONNECTED, NETWORK_SSID)

        println("$NETWORK_SSID: waiting......")
        Thread.sleep(MAX_WAIT_TIME)
        println("$NETWORK_SSID: connection changing to connected")
        connectivityStatus.connectivityState = ConnectivityStatus.STATE_UNMETERED
        umAppDatabase.connectivityStatusDao.insertAsync(connectivityStatus)

        println("$NETWORK_SSID: finishing test.......")
        Thread.sleep(MAX_WAIT_TIME)

        assertEquals("OnChange should have been called 3 times", 3, mOnChangeCalledCounter.get())
    }
}