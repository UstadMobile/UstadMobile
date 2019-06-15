package com.ustadmobile.sharedse.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.WaitForLiveData
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter
import com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.networkmanager.DownloadJobPreparer
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.sharedse.network.DeleteJobTaskRunner
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon
import com.ustadmobile.util.test.checkJndiSetup
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DownloadDialogPresenterTest {

    private lateinit var mockedDialogView: DownloadDialogView

    private lateinit var presenter: DownloadDialogPresenter

    private lateinit var umAppDatabase: UmAppDatabase

    private lateinit var umAppDatabaseRepo: UmAppDatabase

    private val context = mock<DoorLifecycleOwner>() {
        on { currentState }.thenReturn(DoorLifecycleObserver.STARTED)
    } as Any

    private lateinit var rootEntry: ContentEntry

    private lateinit var downloadJob: DownloadJob

    private lateinit var container: Container

    private var totalBytesToDownload = 0L

    private val MAX_LATCH_WAITING_TIME = 15

    private val MAX_THREAD_SLEEP_TIME = 2

    private var mockedNetworkManager: NetworkManagerBleCommon? = null

    private lateinit var mockedDeleteTaskRunner: DeleteJobTaskRunner

    var TEST_ROOT_CONTENT_ENTRY_UID: Long = 0

    @Before
    @Throws(IOException::class)
    fun setUp() {
        checkJndiSetup()
        mockedDialogView = mock<DownloadDialogView> {
            on { runOnUiThread(any()) } doAnswer {
                Thread(it.getArgument(0) as Runnable).start()
            }
        }
        mockedDeleteTaskRunner = spy<DeleteJobTaskRunner> (){

        }


        umAppDatabase = UmAppDatabase.getInstance(context)
        umAppDatabase.clearAllTables()

        umAppDatabaseRepo = umAppDatabase

        val httpd = EmbeddedHTTPD(0, context)
        httpd.start()
        mockedNetworkManager = spy<NetworkManagerBleCommon> {
            on { makeDeleteJobTask(any(), any()) }.doReturn(mockedDeleteTaskRunner)
        }
        mockedNetworkManager!!.onCreate()


        rootEntry = ContentEntry("Lorem ipsum title",
                "Lorem ipsum description", false, true)
        rootEntry.contentEntryUid = umAppDatabase!!.contentEntryDao.insert(rootEntry)
        TEST_ROOT_CONTENT_ENTRY_UID = rootEntry.contentEntryUid


        container = Container()
        container.containerContentEntryUid = rootEntry.contentEntryUid
        container.lastModified = System.currentTimeMillis()
        container.fileSize = 0
        container.containerUid = umAppDatabase.containerDao.insert(container)

        val entry2 = ContentEntry("title 2", "title 2", true, true)
        val entry3 = ContentEntry("title 2", "title 2", false, true)
        val entry4 = ContentEntry("title 4", "title 4", true, false)

        entry2.contentEntryUid = umAppDatabase.contentEntryDao.insert(entry2)
        entry3.contentEntryUid = umAppDatabase.contentEntryDao.insert(entry3)
        entry4.contentEntryUid = umAppDatabase.contentEntryDao.insert(entry4)

        val cEntry2 = Container()
        cEntry2.containerContentEntryUid = entry2.contentEntryUid
        cEntry2.lastModified = System.currentTimeMillis()
        cEntry2.fileSize = 500
        cEntry2.containerUid = umAppDatabase.containerDao.insert(cEntry2)

        val cEntry4 = Container()
        cEntry4.containerContentEntryUid = entry4.contentEntryUid
        cEntry4.lastModified = System.currentTimeMillis()
        cEntry4.fileSize = 500
        cEntry4.containerUid = umAppDatabase.containerDao.insert(cEntry4)

        totalBytesToDownload = cEntry2.fileSize + cEntry4.fileSize

        umAppDatabase.contentEntryParentChildJoinDao.insertList(Arrays.asList(
                ContentEntryParentChildJoin(rootEntry, entry2, 0),
                ContentEntryParentChildJoin(rootEntry, entry3, 0),
                ContentEntryParentChildJoin(entry3, entry4, 0)
        ))

    }

    private fun insertDownloadJobAndJobItems(meteredNetworkAllowed: Boolean = false, status: Int) {
        Assert.assertNotEquals(0, rootEntry.contentEntryUid)
        UMLog.l(UMLog.DEBUG, 420, "DownloadDialogPresenterTest " +
                "root entry uid = " + rootEntry.contentEntryUid)
        downloadJob = DownloadJob(rootEntry.contentEntryUid,
                System.currentTimeMillis())
        downloadJob.meteredNetworkAllowed = meteredNetworkAllowed
        downloadJob.djStatus = status
        val itemManager = mockedNetworkManager!!
                .createNewDownloadJobItemManager(downloadJob)
        runBlocking {
            DownloadJobPreparer(itemManager, umAppDatabase, umAppDatabaseRepo).run()
        }
        println("job prepared")
    }

    @Test
    fun givenNoExistingDownloadJob_whenOnCreateCalled_shouldCreateDownloadJobAndJobItems() = runBlocking {
        val viewReadyLatch = CountDownLatch(1)
        doAnswer { invocation ->
            viewReadyLatch.countDown()
            null
        }.`when`<DownloadDialogView>(mockedDialogView).setWifiOnlyOptionVisible(true)

        val args = mapOf(
                ARG_CONTENT_ENTRY_UID to rootEntry.contentEntryUid.toString()
        )

        presenter = DownloadDialogPresenter(context, mockedNetworkManager, args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo)
        presenter.onCreate(mapOf())
        presenter.onStart()

        viewReadyLatch.await(MAX_LATCH_WAITING_TIME.toLong(), TimeUnit.SECONDS)

        val downloadJobUid = umAppDatabase.downloadJobDao
                .findDownloadJobUidByRootContentEntryUid(rootEntry.contentEntryUid)
        assertTrue("Download job was with root content entry uid was created dby presenter",
                downloadJobUid > 0)

        waitForLiveData(umAppDatabase.downloadJobItemDao.findByContentEntryUidLive(
                rootEntry.contentEntryUid), 5000) {
            dji -> dji != null && dji.downloadLength == totalBytesToDownload
        }

        val numDownloadItems = umAppDatabase.downloadJobItemDao.findAll().size

        assertEquals("4 DownloadJobItem were created ",
                4, umAppDatabase.downloadJobItemDao.findAll().size)

        assertEquals("Total bytes to be downloaded was updated",
                totalBytesToDownload,
                umAppDatabase.downloadJobItemDao
                        .findByContentEntryUid2(rootEntry.contentEntryUid)!!.downloadLength)
        Unit
    }

}