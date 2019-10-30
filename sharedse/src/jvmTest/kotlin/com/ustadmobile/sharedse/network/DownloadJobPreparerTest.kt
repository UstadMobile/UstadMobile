package com.ustadmobile.sharedse.network

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.rest.umRestApplication
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

class DownloadJobPreparerTest {

    private lateinit var mockedNetworkManager: NetworkManagerBleCommon


    @Before
    fun setup(){
        serverDb.clearAllTables()
        clientDb.clearAllTables()

        mockedNetworkManager = spy { }
        mockedNetworkManager.umAppDatabase = clientDb
        mockedNetworkManager.umAppDatabaseRepo = clientRepo

        mockedNetworkManager.onCreate()
    }


    @Test
    fun givenContentEntriesExistOnServerNotClient_whenPrepareCalled_thenDownloadJobtemsAreCreated() = runBlocking{
        val contentEntrySet = insertTestContentEntries(serverDb, System.currentTimeMillis())
        val downloadJob = DownloadJob(contentEntrySet.rootEntry.contentEntryUid,
                System.currentTimeMillis())
        val itemManager = mockedNetworkManager.createNewDownloadJobItemManager(downloadJob)
        itemManager.awaitLoaded()
        val downloadJobPreparer = DownloadJobPreparer()
        downloadJobPreparer.prepare(itemManager, clientDb, clientRepo)


        assertEquals("Total bytes to be downloaded was updated",
                contentEntrySet.totalBytesToDownload,
                clientDb.downloadJobItemDao.findByContentEntryUid2(
                        contentEntrySet.rootEntry.contentEntryUid)!!.downloadLength)
        val downloadJobInDb = clientDb.downloadJobDao.findByUid(itemManager.downloadJobUid)
        assertNotNull("Download job in db is not null", downloadJobInDb)
        assertEquals("DownloadJob status is QUEUED after preparation is finished",
                JobStatus.QUEUED, downloadJobInDb!!.djStatus)
        val downloadJobItems = clientDb.downloadJobItemDao.findByDownloadJobUid(downloadJobInDb.djUid)
        assertTrue("All items status are now QUEUED:",
                downloadJobItems.all { it.djiStatus == JobStatus.QUEUED })

        Unit
    }

    //@Test
    fun givenServerUnavailable_whenPrepareCalled_thenShouldThrowException() {
        //server.stop(0, 2000, TimeUnit.MILLISECONDS)
    }

    //TODO: Don't require the server to be connected if ContentEntry table is recently sync'd

    companion object {

        @JvmStatic
        private lateinit var server: ApplicationEngine

        @JvmStatic
        private lateinit var clientDb: UmAppDatabase

        @JvmStatic
        private lateinit var serverDb: UmAppDatabase

        private lateinit var clientRepo: UmAppDatabase


        @BeforeClass
        @JvmStatic
        fun setupClass() {
            serverDb = UmAppDatabase.getInstance(Any())
            clientDb = UmAppDatabase.getInstance(Any(), "clientdb")
            clientRepo = clientDb.asRepository(Any(),"http://localhost:8087", "",
                    defaultHttpClient()) as UmAppDatabase
            server = embeddedServer(Netty, 8087) {
                umRestApplication(devMode = false, db = serverDb)
            }
            server.start()
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            server.stop(0, 2000, TimeUnit.MILLISECONDS)
        }
    }

}