package com.ustadmobile.port.android

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import io.ktor.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.kodein.di.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class DownloadJobConnectivityChangeTriggerEventsTest {


    private lateinit var umAppDatabase: UmAppDatabase

    private lateinit var nodeIdAndAuth: NodeIdAndAuth

    private lateinit var repo: UmAppDatabase

    private lateinit var context: Context


    private lateinit var entry: ContentEntry

    private lateinit var downloadJob: ContentJob

    private val MAX_WAIT_TIME = 3000L

    companion object{
        private val NETWORK_SSID = "NetworkSSID"
        private val mOnChangeCalledCounter: AtomicInteger =  AtomicInteger(0)
    }

    private lateinit var connectivityStatus: ConnectivityStatus


    class ConnectivityStatusChangeObserver: DoorObserver<ConnectivityStatus?>{
        override fun onChanged(t: ConnectivityStatus?) {
            println("$NETWORK_SSID: onChange called with value " +
                    "= ${mOnChangeCalledCounter.getAndIncrement()}")
        }
    }

    @Before
    fun setUp(){

        context = InstrumentationRegistry.getInstrumentation().context
        val di: DI = (context.applicationContext as DIAware).di
        val httpClient: HttpClient = di.direct.instance()
        val okHttpClient: OkHttpClient = di.direct.instance()
        val accountManager: UstadAccountManager = di.direct.instance()

        nodeIdAndAuth = NodeIdAndAuth(Random.nextInt(0, Int.MAX_VALUE),
            randomUuid().toString())

        umAppDatabase = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)


        repo = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_REPO)

        entry = ContentEntry("title 2", "title 2", leaf = true, publik = true)
        entry.contentEntryUid = repo.contentEntryDao.insert(entry)

        downloadJob = ContentJob()

        runBlocking {
            downloadJob.cjUid = umAppDatabase.contentJobDao.insertAsync(downloadJob)
            val contentJobItem = ContentJobItem().apply {
                cjiContentEntryUid = entry.contentEntryUid
                cjiJobUid = downloadJob.cjUid
            }
            contentJobItem.cjiUid = umAppDatabase.contentJobItemDao.insertJobItem(contentJobItem)
        }

        connectivityStatus = ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, NETWORK_SSID)

    }


    @Test
    fun givenActiveDownloadJob_whenConnectivityStatusChanges_shouldTriggerUpdates() = runBlocking{


        umAppDatabase.connectivityStatusDao.insertAsync(connectivityStatus)

        withContext(Dispatchers.Main) {
            umAppDatabase.connectivityStatusDao.statusLive().observeForever(ConnectivityStatusChangeObserver())
        }


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