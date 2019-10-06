package com.ustadmobile.sharedse.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter.Companion.STACKED_BUTTON_CANCEL
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.sharedse.network.*
import com.ustadmobile.util.test.checkJndiSetup
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class DownloadDialogPresenterTest {

    private lateinit var mockedDialogView: DownloadDialogView

    private lateinit var presenter: DownloadDialogPresenter

    private lateinit var umAppDatabase: UmAppDatabase

    private lateinit var umAppDatabaseRepo: UmAppDatabase

    private val context = mock<DoorLifecycleOwner>() {
        on { currentState }.thenReturn(DoorLifecycleObserver.STARTED)
    } as Any

    private lateinit var downloadJob: DownloadJob

    private val MAX_LATCH_WAITING_TIME = 15000L

    private val MAX_THREAD_SLEEP_TIME = 2

    private lateinit var mockedNetworkManager: NetworkManagerBleCommon

    private lateinit var mockedDeleteTaskRunner: DeleteJobTaskRunner

    private var TEST_ROOT_CONTENT_ENTRY_UID: Long = 0

    private lateinit var contentEntrySet: RecursiveContentEntrySet


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

        umAppDatabaseRepo = umAppDatabase.asRepository("http://localhost/dummy/", "",
                defaultHttpClient())

        val httpd = EmbeddedHTTPD(0, context)
        httpd.start()
        mockedNetworkManager = spy {
            on { makeDeleteJobTask(any(), any()) }.doReturn(mockedDeleteTaskRunner)
        }
        mockedNetworkManager.onCreate()

        contentEntrySet = insertTestContentEntries(umAppDatabase, System.currentTimeMillis())
    }

    private fun insertDownloadJobAndJobItems(meteredNetworkAllowed: Boolean = false, status: Int) {
        Assert.assertNotEquals(0, contentEntrySet.rootEntry.contentEntryUid)
        runBlocking {
            println("DownloadDialogPresenterTest " +
                    "root entry uid = " + contentEntrySet.rootEntry.contentEntryUid)
            downloadJob = DownloadJob(contentEntrySet.rootEntry.contentEntryUid,
                    System.currentTimeMillis())
            println("DownloadJob contentEntryUid = ${downloadJob.djRootContentEntryUid}")
            downloadJob.meteredNetworkAllowed = meteredNetworkAllowed
            downloadJob.djStatus = status
            val itemManager = mockedNetworkManager
                    .createNewDownloadJobItemManager(downloadJob)
            println("Item manager content entry uid = ${itemManager.rootContentEntryUid}")
            runBlocking {
                val downloadJobPreparer = DownloadJobPreparer(statusAfterPreparation = status)//TODO: fix this to use the actual server instead
                downloadJobPreparer.prepare(itemManager, umAppDatabase, umAppDatabaseRepo)
            }
            println("job prepared")
        }
    }

    @Test
    fun givenNoExistingDownloadJob_whenViewCreated_shouldRequestTotalSizeFromServer() {
        val contentEntryDaoSpy = spy(umAppDatabase.contentEntryDao) {
            onBlocking {getRecursiveDownloadTotals(eq(contentEntrySet.rootEntry.contentEntryUid)) } doReturn DownloadJobSizeInfo(2, 1000)
        }
        umAppDatabaseRepo = spy(umAppDatabaseRepo) {
            on { contentEntryDao } doReturn contentEntryDaoSpy
        }
        presenter = DownloadDialogPresenter(context, mockedNetworkManager,
                mapOf(ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()),
                mockedDialogView, umAppDatabase, umAppDatabaseRepo)

        presenter.onCreate(mapOf())
        presenter.onStart()

        verifyBlocking(contentEntryDaoSpy, timeout(5000)) { getRecursiveDownloadTotals(contentEntrySet.rootEntry.contentEntryUid) }

        verify(mockedDialogView, timeout(5000)).setStatusText(any(),
                eq(2), eq(UMFileUtil.formatFileSize(1000)))

        assertNull("No download job should be created if the user does not select to download",
                umAppDatabase.downloadJobDao.findDownloadJobByRootContentEntryUid(
                        contentEntrySet.rootEntry.contentEntryUid))
    }

    @Test
    fun givenExistingDownloadJobNotStarted_whenViewCreated_shouldGetSizeFromDatabase() {
        insertDownloadJobAndJobItems(status = JobStatus.NOT_QUEUED)

        val preparerFn =  {downloadJobUid: Int, context: Any  -> Unit}
        presenter = DownloadDialogPresenter(context, mockedNetworkManager,
                mapOf(ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()),
                mockedDialogView, umAppDatabase, umAppDatabaseRepo, preparerFn)

        presenter.onCreate(mapOf())
        presenter.onStart()

        verify(mockedDialogView, timeout(5000)).setStatusText(any(),
                eq(4), eq(UMFileUtil.formatFileSize(contentEntrySet.totalBytesToDownload)))
    }

    private fun givenNoExistingDownloadJob_whenContinueIsPressed_shouldCreateDownloadJobAndInvokePreparerAndSetStatusToNeedsPrepared(meteredNetworkAllowed: Boolean) = runBlocking{
        val viewReadyLatch = CountDownLatch(1)
        doAnswer {
            viewReadyLatch.countDown()
            null
        }.`when`(mockedDialogView).setWifiOnlyOptionVisible(true)

        val args = mapOf(
                ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()
        )

        var preparerCountdownLatch = CountDownLatch(1)
        var preparationRequested = AtomicBoolean(false)
        val downloadJobPreparerRequester = {downloadJobUid: Int, context: Any ->
            preparationRequested.set(true)
            preparerCountdownLatch.countDown()
        }

        presenter = DownloadDialogPresenter(context, mockedNetworkManager, args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo, downloadJobPreparerRequester)
        presenter.onCreate(mapOf())
        presenter.onStart()
        presenter.handleClickWiFiOnlyOption(!meteredNetworkAllowed)

        presenter.handleClickPositive()

        waitForLiveData(umAppDatabase.downloadJobDao.lastJobLive(), 6000) {
            dj -> dj != null }

        val downloadJobUid = umAppDatabase.downloadJobDao
                .findDownloadJobUidByRootContentEntryUid(contentEntrySet.rootEntry.contentEntryUid)
        assertTrue("Download job was with root content entry uid was created dby presenter",
                downloadJobUid > 0)
        waitForLiveData(umAppDatabase.downloadJobDao.getJobLive(downloadJobUid.toInt()), 5000) {
            downloadJob -> downloadJob != null && downloadJob.djStatus == JobStatus.NEEDS_PREPARED
        }

        preparerCountdownLatch.await(5000, TimeUnit.MILLISECONDS)
        assertTrue("Preparer requester was invoked", preparationRequested.get())

        val downloadJobCreated = umAppDatabase.downloadJobDao.findDownloadJobByRootContentEntryUid(
                contentEntrySet.rootEntry.contentEntryUid)
        assertNotNull("DownloadJob was created", downloadJobCreated)
        assertEquals("Download status is set to needs prepared", JobStatus.NEEDS_PREPARED,
                downloadJobCreated!!.djStatus)
        assertEquals("Metered data allowed is $meteredNetworkAllowed", meteredNetworkAllowed,
                downloadJobCreated.meteredNetworkAllowed)
        Unit
    }

    @Test
    fun givenNoExistingDownloadJobNoMeteredDataAllowed_whenContinueButtonIsPressed_shouldCreateDownlaodJobInvokePreparerRequesterAndSetStatusToNeedsPrepared() = runBlocking {
        givenNoExistingDownloadJob_whenContinueIsPressed_shouldCreateDownloadJobAndInvokePreparerAndSetStatusToNeedsPrepared(false)
    }

    @Test
    fun givenNoExistingDownloadJobMeteredDataAllowed_whenContinueButtonIsPressed_shouldCreateDownlaodJobInvokePreparerRequesterAndSetStatusToNeedsPrepared() = runBlocking {
        givenNoExistingDownloadJob_whenContinueIsPressed_shouldCreateDownloadJobAndInvokePreparerAndSetStatusToNeedsPrepared(true)
    }

    @Test
    fun givenDownloadJobCreated_whenHandleClickCalled_shouldSetStatusToQueued() {
        runBlocking {
            insertDownloadJobAndJobItems(true, JobStatus.NOT_QUEUED)
            val viewReadyLatch = CountDownLatch(1)
            doAnswer {
                viewReadyLatch.countDown()
                Unit
            }.`when`(mockedDialogView).setWifiOnlyOptionVisible(true)

            val args = HashMap<String, String>()

            args[ARG_CONTENT_ENTRY_UID] = contentEntrySet.rootEntry.contentEntryUid.toString()
            presenter = DownloadDialogPresenter(context, mockedNetworkManager, args, mockedDialogView,
                    umAppDatabase, umAppDatabaseRepo, {Int, Any -> Unit})
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
                contentEntrySet.rootEntry.contentEntryUid)

        Assert.assertNotEquals(0, downloadJobUid)

        val args = mapOf(ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString())
        presenter = DownloadDialogPresenter(context, mockedNetworkManager, args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo, {Int, Any -> Unit})
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

            args[ARG_CONTENT_ENTRY_UID] = contentEntrySet.rootEntry.contentEntryUid.toString()
            presenter = DownloadDialogPresenter(context, mockedNetworkManager, args, mockedDialogView,
                    umAppDatabase, umAppDatabaseRepo, {Int, Any -> Unit})
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
            val startTime = System.currentTimeMillis()
            insertDownloadJobAndJobItems(status = JobStatus.RUNNING)

            val viewReadyLatch = CountDownLatch(1)
            doAnswer {
                viewReadyLatch.countDown()
                null
            }.`when`(mockedDialogView).setWifiOnlyOptionVisible(true)

            val args = HashMap<String, String>()

            args[ARG_CONTENT_ENTRY_UID] = contentEntrySet.rootEntry.contentEntryUid.toString()
            presenter = DownloadDialogPresenter(context, mockedNetworkManager, args, mockedDialogView,
                    umAppDatabase, umAppDatabaseRepo, {Int, Any -> Unit})
            presenter.onCreate(HashMap<String, String>())
            presenter.onStart()

            viewReadyLatch.await(MAX_LATCH_WAITING_TIME, TimeUnit.MILLISECONDS)
            println("View ready after ${System.currentTimeMillis() - startTime}ms")

            presenter.handleClickStackedButton(STACKED_BUTTON_CANCEL)

            //TODO: should this be canceling instead of canceled for the DownloadJob itself?
            waitForLiveData(umAppDatabase.downloadJobDao.getJobLive(presenter.currentJobId),
                    MAX_LATCH_WAITING_TIME) {
                it != null && it.djStatus == JobStatus.CANCELED
            }
            println("Livedata ready after ${System.currentTimeMillis() - startTime}ms")

            assertEquals("Job status was changed to cancelling after clicking cancel download",
                    JobStatus.CANCELED.toLong(),
                    umAppDatabase.downloadJobDao.findByUid(downloadJob.djUid)!!
                            .djStatus.toLong())
        }


    }

}