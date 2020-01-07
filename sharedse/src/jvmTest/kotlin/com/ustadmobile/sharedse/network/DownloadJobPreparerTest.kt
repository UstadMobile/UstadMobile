package com.ustadmobile.sharedse.network

import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.rest.umRestApplication
import com.ustadmobile.sharedse.test.util.bindDbForActiveContext
import com.ustadmobile.util.test.ext.bindJndiForActiveEndpoint
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
        Napier.base(DebugAntilog())
        serverDb.clearAllTables()
        clientDb.clearAllTables()

        mockedNetworkManager = spy {

        }
        mockedNetworkManager.umAppDatabase = clientDb

        mockedNetworkManager.onCreate()
    }


    @Test
    fun givenContentEntriesExistOnServerNotClient_whenPrepareCalled_thenDownloadJobtemsAreCreated() = runBlocking{
        val contentEntrySet = insertTestContentEntries(serverDb, System.currentTimeMillis())
        val downloadJob = DownloadJob(contentEntrySet.rootEntry.contentEntryUid,
                System.currentTimeMillis())

        val downloadManagerImpl = ContainerDownloadManagerImpl(appDb = clientDb) { job, manager -> mock() }
        downloadManagerImpl.createDownloadJob(downloadJob)

        val downloadJobPreparer = DownloadJobPreparer(downloadJobUid = downloadJob.djUid)
        downloadJobPreparer.prepare(downloadManagerImpl, clientDb, clientRepo, {})


        assertEquals("Total bytes to be downloaded was updated",
                contentEntrySet.totalBytesToDownload,
                clientDb.downloadJobItemDao.findByContentEntryUid2(
                        contentEntrySet.rootEntry.contentEntryUid)!!.downloadLength)
        val downloadJobInDb = clientDb.downloadJobDao.findByUid(downloadJob.djUid)
        assertNotNull("Download job in db is not null", downloadJobInDb)
        assertEquals("4 Download jobs were created in the download",
                4, clientDb.downloadJobItemDao.findByDownloadJobUid(downloadJob.djUid).size)

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
            UmAccountManager.bindDbForActiveContext(Any())
            serverDb = UmAppDatabase.getInstance(Any())
            clientDb = UmAccountManager.getActiveDatabase(Any())
            clientRepo = clientDb.asRepository(Any(),"http://localhost:8087", "",
                    defaultHttpClient(), null) as UmAppDatabase
            (clientRepo as DoorDatabaseRepository).connectivityStatus = DoorDatabaseRepository.STATUS_CONNECTED

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