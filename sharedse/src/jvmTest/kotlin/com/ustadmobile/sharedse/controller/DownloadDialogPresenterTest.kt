package com.ustadmobile.sharedse.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter.Companion.STACKED_BUTTON_CANCEL
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.networkmanager.DownloadJobPreparer
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.sharedse.network.DeleteJobTaskRunner
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
import java.util.concurrent.atomic.AtomicReference

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

    private val MAX_LATCH_WAITING_TIME = 15000L

    private val MAX_THREAD_SLEEP_TIME = 2

    private lateinit var mockedNetworkManager: NetworkManagerBleCommon

    private lateinit var mockedDeleteTaskRunner: DeleteJobTaskRunner

    private var TEST_ROOT_CONTENT_ENTRY_UID: Long = 0

    private var downloadJobPreparer: DownloadJobPreparer? = null

    @Before
    @Throws(IOException::class)
    fun setUp() {
        checkJndiSetup()
        mockedDialogView = mock {
            on { runOnUiThread(any()) } doAnswer {
                Thread(it.getArgument(0) as Runnable).start()
            }
        }
        mockedDeleteTaskRunner = spy {}


        umAppDatabase = UmAppDatabase.getInstance(context)
        umAppDatabase.clearAllTables()

        umAppDatabaseRepo = umAppDatabase

        val httpd = EmbeddedHTTPD(0, context)
        httpd.start()
        mockedNetworkManager = spy {
            on { makeDeleteJobTask(any(), any()) }.doReturn(mockedDeleteTaskRunner)
        }
        mockedNetworkManager.onCreate()


        rootEntry = ContentEntry("Lorem ipsum title",
                "Lorem ipsum description", leaf = false, publik = true)
        rootEntry.contentEntryUid = umAppDatabase.contentEntryDao.insert(rootEntry)
        println("Insert root entry uid = ${rootEntry.contentEntryUid}")
        TEST_ROOT_CONTENT_ENTRY_UID = rootEntry.contentEntryUid


        container = Container()
        container.containerContentEntryUid = rootEntry.contentEntryUid
        container.lastModified = System.currentTimeMillis()
        container.fileSize = 0
        container.containerUid = umAppDatabase.containerDao.insert(container)

        val entry2 = ContentEntry("title 2", "title 2", leaf = true, publik = true)
        val entry3 = ContentEntry("title 2", "title 2", leaf = false, publik = true)
        val entry4 = ContentEntry("title 4", "title 4", leaf = true, publik = false)

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

        umAppDatabase.contentEntryParentChildJoinDao.insertList(
                listOf(ContentEntryParentChildJoin(rootEntry, entry2, 0),
                        ContentEntryParentChildJoin(rootEntry, entry3, 0),
                        ContentEntryParentChildJoin(entry3, entry4, 0)))

    }

    private fun insertDownloadJobAndJobItems(meteredNetworkAllowed: Boolean = false, status: Int) {
        Assert.assertNotEquals(0, rootEntry.contentEntryUid)
        runBlocking {
            println("DownloadDialogPresenterTest " +
                    "root entry uid = " + rootEntry.contentEntryUid)
            downloadJob = DownloadJob(rootEntry.contentEntryUid,
                    System.currentTimeMillis())
            println("DownloadJob contentEntryUid = ${downloadJob.djRootContentEntryUid}")
            downloadJob.meteredNetworkAllowed = meteredNetworkAllowed
            downloadJob.djStatus = status
            val itemManager = mockedNetworkManager
                    .createNewDownloadJobItemManager(downloadJob)
            println("Item manager content entry uid = ${itemManager.rootContentEntryUid}")
            runBlocking {
                downloadJobPreparer = DownloadJobPreparer(itemManager, umAppDatabase, umAppDatabaseRepo)
                downloadJobPreparer!!.run()
            }
            println("job prepared")
        }
    }

    @Test
    fun givenNoExistingDownloadJob_whenContinueButtonIsPressed_shouldCreateDownloadJobAndJobItems() = runBlocking {
        val viewReadyLatch = CountDownLatch(1)
        doAnswer {
            viewReadyLatch.countDown()
            null
        }.`when`(mockedDialogView).setWifiOnlyOptionVisible(true)

        val args = mapOf(
                ARG_CONTENT_ENTRY_UID to rootEntry.contentEntryUid.toString()
        )

        presenter = DownloadDialogPresenter(context, mockedNetworkManager, args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo)
        presenter.onCreate(mapOf())
        presenter.onStart()

        presenter.handleClickPositive()

        waitForLiveData(umAppDatabase.downloadJobDao.lastJobLive(), 6000) {
            dj -> dj != null }

        val downloadJobUid = umAppDatabase.downloadJobDao
                .findDownloadJobUidByRootContentEntryUid(rootEntry.contentEntryUid)
        assertTrue("Download job was with root content entry uid was created dby presenter",
                downloadJobUid > 0)

        waitForLiveData(umAppDatabase.downloadJobItemDao.findByContentEntryUidLive(
                rootEntry.contentEntryUid), 5000) {
            dji -> dji != null && dji.downloadLength == totalBytesToDownload
                && umAppDatabase.downloadJobItemDao.findAll().size == 4
        }


        assertEquals("4 DownloadJobItem were created ",
                4, umAppDatabase.downloadJobItemDao.findAll().size)

        assertEquals("Total bytes to be downloaded was updated",
                totalBytesToDownload,
                umAppDatabase.downloadJobItemDao
                        .findByContentEntryUid2(rootEntry.contentEntryUid)!!.downloadLength)
        Unit
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenDownloadJobCreated_whenHandleClickCalled_shouldSetStatusToQueued() {
        runBlocking {
            val viewReadyLatch = CountDownLatch(1)
            doAnswer {
                viewReadyLatch.countDown()
                Unit
            }.`when`(mockedDialogView).setWifiOnlyOptionVisible(true)

            val args = HashMap<String, String>()

            args[ARG_CONTENT_ENTRY_UID] = rootEntry.contentEntryUid.toString()
            presenter = DownloadDialogPresenter(context, mockedNetworkManager, args, mockedDialogView,
                    umAppDatabase, umAppDatabaseRepo)
            presenter.onCreate(HashMap<String, String>())
            presenter.onStart()

            presenter.handleClickPositive()

            waitForLiveData(umAppDatabase.downloadJobDao.lastJobLive(), 6000) {
                dj -> dj != null }

            val lastJobRef = AtomicReference<DownloadJob?>(null)
            waitForLiveData(umAppDatabase.downloadJobDao.getJobLive(presenter.currentJobId),
                    MAX_LATCH_WAITING_TIME) {
                job -> job != null && job.djStatus == JobStatus.QUEUED
            }

            val queuedJob = umAppDatabase.downloadJobDao.findByUid(presenter.currentJobId)
            assertEquals("Job status was changed to Queued after clicking continue",
                    JobStatus.QUEUED, queuedJob!!.djStatus)
        }

    }


    @Test
    fun givenDownloadRunning_whenCreated_shouldShowStackedOptions() {
        insertDownloadJobAndJobItems(status = JobStatus.RUNNING)
        val downloadJobUid = umAppDatabase.downloadJobDao.getLatestDownloadJobUidForContentEntryUid(
                rootEntry.contentEntryUid)

        Assert.assertNotEquals(0, downloadJobUid)

        val args = mapOf(ARG_CONTENT_ENTRY_UID to rootEntry.contentEntryUid.toString())
        presenter = DownloadDialogPresenter(context, mockedNetworkManager, args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo)
        presenter.onCreate(HashMap<String, String>())
        presenter.onStart()

        verify(mockedDialogView, timeout(5000)).setStackedOptions(any(), any())
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenDownloadRunning_whenClickPause_shouldSetStatusToPaused() {
        runBlocking {
            insertDownloadJobAndJobItems(status = JobStatus.RUNNING)

            val viewReadyLatch = CountDownLatch(1)
            doAnswer {
                viewReadyLatch.countDown()
                Unit
            }.`when`(mockedDialogView).setWifiOnlyOptionVisible(true)

            val args = HashMap<String, String>()

            args[ARG_CONTENT_ENTRY_UID] = rootEntry.contentEntryUid.toString()
            presenter = DownloadDialogPresenter(context, mockedNetworkManager, args, mockedDialogView,
                    umAppDatabase, umAppDatabaseRepo)
            presenter.onCreate(HashMap<String, String>())
            presenter.onStart()

            viewReadyLatch.await(MAX_LATCH_WAITING_TIME, TimeUnit.MILLISECONDS)

            presenter.handleClickStackedButton(DownloadDialogPresenter.STACKED_BUTTON_PAUSE)

            waitForLiveData(umAppDatabase.downloadJobDao.getJobLive(presenter.currentJobId),
                    MAX_LATCH_WAITING_TIME) {
                it != null && it.djStatus == JobStatus.PAUSED
            }

            val finishedJob = umAppDatabase.downloadJobDao.findByUid(downloadJob.djUid)
            assertEquals("Job status was changed to paused after clicking pause button",
                    JobStatus.PAUSED, finishedJob!!.djStatus)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenDownloadRunning_whenClickCancel_shouldSetStatusToCancelling() {
        runBlocking {
            insertDownloadJobAndJobItems(status = JobStatus.RUNNING)

            val viewReadyLatch = CountDownLatch(1)
            doAnswer {
                viewReadyLatch.countDown()
                null
            }.`when`(mockedDialogView).setWifiOnlyOptionVisible(true)

            val args = HashMap<String, String>()

            args[ARG_CONTENT_ENTRY_UID] = rootEntry.contentEntryUid.toString()
            presenter = DownloadDialogPresenter(context, mockedNetworkManager, args, mockedDialogView,
                    umAppDatabase, umAppDatabaseRepo)
            presenter.onCreate(HashMap<String, String>())
            presenter.onStart()

            viewReadyLatch.await(MAX_LATCH_WAITING_TIME, TimeUnit.MILLISECONDS)

            presenter.handleClickStackedButton(STACKED_BUTTON_CANCEL)

            waitForLiveData(umAppDatabase.downloadJobDao.getJobLive(presenter.currentJobId),
                    MAX_LATCH_WAITING_TIME) {
                it != null && it.djStatus == JobStatus.CANCELLING
            }

            assertEquals("Job status was changed to cancelling after clicking cancel download",
                    JobStatus.CANCELED.toLong(),
                    umAppDatabase.downloadJobDao.findByUid(downloadJob.djUid)!!
                            .djStatus.toLong())
        }


    }

}