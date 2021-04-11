package com.ustadmobile.core.network

import com.github.aakira.napier.Napier
import org.mockito.kotlin.mock
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.networkmanager.DownloadJobPreparer
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.rest.umRestApplication
import com.ustadmobile.sharedse.network.ContainerDownloadManagerImpl
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.sharedse.network.insertTestContentEntries
import com.ustadmobile.util.test.ext.baseDebugIfNotEnabled
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.kodein.di.*
import org.kodein.di.ktor.di

class DownloadJobPreparerTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var accountManager: UstadAccountManager

    private lateinit var server: ApplicationEngine

    private lateinit var clientDb: UmAppDatabase

    private lateinit var serverDb: UmAppDatabase

    private lateinit var clientRepo: UmAppDatabase

    private lateinit var serverRepo: UmAppDatabase


    @Before
    fun setup(){
        Napier.baseDebugIfNotEnabled()
        serverDb = UmAppDatabase.getInstance(Any())
        serverDb.clearAllTables()
        serverRepo = serverDb.asRepository(Any(), "http://localhost/dummy",
            "", defaultHttpClient())

        di = DI {
            import(ustadTestRule.diModule)
        }

        accountManager = di.direct.instance()
        accountManager.activeAccount = UmAccount(0, "guest", "",
            "http://localhost:8089/", "Guest", "User")

        server = embeddedServer(Netty, 8089) {
            umRestApplication(devMode = false)
        }
        server.start()

        clientDb = di.on(accountManager.activeAccount).direct.instance(tag = TAG_DB)
        clientRepo = di.on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)
        (clientRepo as DoorDatabaseRepository).connectivityStatus = DoorDatabaseRepository.STATUS_CONNECTED
    }

    @After
    fun tearDown() {
        server.stop(0, 2000)
    }


    @Test
    fun givenContentEntriesExistOnServerNotClient_whenPrepareCalled_thenDownloadJobtemsAreCreated() = runBlocking{
        val contentEntrySet = insertTestContentEntries(serverRepo, System.currentTimeMillis())
        val downloadJob = DownloadJob(contentEntrySet.rootEntry.contentEntryUid,
                System.currentTimeMillis())

        val downloadManagerImpl = ContainerDownloadManagerImpl(di = di, endpoint =
            Endpoint(accountManager.activeAccount.endpointUrl))
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

}