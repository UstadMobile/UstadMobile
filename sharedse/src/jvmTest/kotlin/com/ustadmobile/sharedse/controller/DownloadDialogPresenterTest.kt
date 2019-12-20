package com.ustadmobile.sharedse.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter.Companion.STACKED_BUTTON_CANCEL
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.sharedse.network.*
import com.ustadmobile.util.test.checkJndiSetup
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

class DownloadDialogPresenterTest {

    private lateinit var mockedDialogView: DownloadDialogView

    private lateinit var presenter: DownloadDialogPresenter

    private lateinit var umAppDatabase: UmAppDatabase

    private lateinit var umAppDatabaseRepo: UmAppDatabase

    private val context = mock<DoorLifecycleOwner>() {
        on { currentState }.thenReturn(DoorLifecycleObserver.STARTED)
    } as Any

    private lateinit var downloadJob: DownloadJob

    private lateinit var contentEntrySet: RecursiveContentEntrySet

    private lateinit var containerDownloadManager: ContainerDownloadManager

    @Before
    @Throws(IOException::class)
    fun setUp() {
        checkJndiSetup()
        mockedDialogView = mock {
            on { runOnUiThread(any()) } doAnswer {
                Thread(it.getArgument(0) as Runnable).start()
            }
        }

        umAppDatabase = UmAppDatabase.getInstance(context)
        umAppDatabase.clearAllTables()

        umAppDatabaseRepo = umAppDatabase.asRepository(context,"http://localhost/dummy/", "",
                defaultHttpClient(), null)

        val httpd = EmbeddedHTTPD(0, context)
        httpd.start()

        containerDownloadManager = mock {}

        contentEntrySet = insertTestContentEntries(umAppDatabase, System.currentTimeMillis())
    }

    @Test
    fun givenNoExistingDownloadJob_whenViewCreated_shouldRequestTotalSizeFromServer() {
        val contentEntryDaoSpy = spy(umAppDatabase.contentEntryDao) {
            onBlocking {getRecursiveDownloadTotals(eq(contentEntrySet.rootEntry.contentEntryUid)) } doReturn DownloadJobSizeInfo(2, 1000)
        }
        umAppDatabaseRepo = spy(umAppDatabaseRepo) {
            on { contentEntryDao } doReturn contentEntryDaoSpy
        }
        runBlocking {
            val downloadJobItemLiveData = DoorMutableLiveData<DownloadJobItem?>(null)
            val downloadJobLiveData = DoorMutableLiveData<DownloadJob?>(null)
            whenever(containerDownloadManager.getDownloadJobItemByContentEntryUid(any()))
                    .thenReturn(downloadJobItemLiveData)
            whenever(containerDownloadManager.getDownloadJob(any())).thenReturn(downloadJobLiveData)

            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()),
                    mockedDialogView, umAppDatabase, umAppDatabaseRepo, containerDownloadManager)

            presenter.onCreate(mapOf())
            presenter.onStart()

            verifyBlocking(contentEntryDaoSpy, timeout(5000 * 1000)) { getRecursiveDownloadTotals(contentEntrySet.rootEntry.contentEntryUid) }

            verify(mockedDialogView, timeout(5000)).setStatusText(any(),
                    eq(2), eq(UMFileUtil.formatFileSize(1000)))

            assertNull("No download job should be created if the user does not select to download",
                    umAppDatabase.downloadJobDao.findDownloadJobByRootContentEntryUid(
                            contentEntrySet.rootEntry.contentEntryUid))
        }

    }

    @Test
    fun givenExistingDownloadJobNotStarted_whenViewCreated_shouldGetSizeFromDatabase() {
        val existingDownloadJob = DownloadJob(1L, System.currentTimeMillis())
        existingDownloadJob.djUid = 1
        val existingDownloadJobItem = DownloadJobItem(existingDownloadJob, 1L,
                1L, 1000L).also {
            it.djiStatus = JobStatus.NOT_QUEUED
        }
        val downloadJobLiveData = DoorMutableLiveData<DownloadJob?>(existingDownloadJob)
        val downloadJobItemLiveData = DoorMutableLiveData<DownloadJobItem?>(
                existingDownloadJobItem)

        val existingDownloadSizeInfo = DownloadJobSizeInfo(4, 1000L)
        val downloadJobDaoSpy = spy(umAppDatabase.downloadJobDao) {
            onBlocking { getDownloadSizeInfo(existingDownloadJob.djUid)}.doReturn(existingDownloadSizeInfo)
        }
        umAppDatabase = spy(umAppDatabase) {
            on { downloadJobDao }.thenReturn(downloadJobDaoSpy)
        }

        runBlocking {
            val existingSize = umAppDatabase.downloadJobDao.getDownloadSizeInfo(1)
            println(existingSize)
            whenever(containerDownloadManager.getDownloadJobItemByContentEntryUid(existingDownloadJobItem.djiContentEntryUid))
                    .thenReturn(downloadJobItemLiveData)
            whenever(containerDownloadManager.getDownloadJob(existingDownloadJob.djUid))
                    .thenReturn(downloadJobLiveData)

            val preparerFn =  {downloadJobUid: Int, context: Any  -> Unit}
            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to "1"),
                    mockedDialogView, umAppDatabase, umAppDatabaseRepo, containerDownloadManager,
                    preparerFn)

            presenter.onCreate(mapOf())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000 * 1000)).setStatusText(any(),
                    eq(4), eq(UMFileUtil.formatFileSize(contentEntrySet.totalBytesToDownload)))
        }
    }

    private fun givenNoExistingDownloadJob_whenContinueIsPressed_shouldCreateDownloadJobAndInvokePreparerAndSetStatusToNeedsPrepared(meteredNetworkAllowed: Boolean) = runBlocking{
        val viewReadyLatch = CountDownLatch(1)

        runBlocking {
            whenever(containerDownloadManager.getDownloadJobItemByContentEntryUid(any()))
                    .thenReturn(DoorMutableLiveData(null))
            whenever(containerDownloadManager.getDownloadJobItemByJobItemUid(any()))
                    .thenReturn(DoorMutableLiveData(null))
            whenever(containerDownloadManager.getDownloadJob(any()))
                    .thenReturn(DoorMutableLiveData(null))
        }

        whenever(mockedDialogView.setWifiOnlyOptionVisible(true)).doAnswer {
            viewReadyLatch.countDown()
        }

        val args = mapOf(
                ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()
        )

        val preparerCountdownLatch = CountDownLatch(1)
        val preparationRequested = AtomicBoolean(false)
        val downloadJobPreparerRequester = {downloadJobUid: Int, context: Any ->
            preparationRequested.set(true)
            preparerCountdownLatch.countDown()
        }

        presenter = DownloadDialogPresenter(context, args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo, containerDownloadManager,
                downloadJobPreparerRequester)
        presenter.onCreate(mapOf())
        presenter.onStart()
        viewReadyLatch.await(5, TimeUnit.SECONDS)

        presenter.handleClickWiFiOnlyOption(!meteredNetworkAllowed)

        presenter.handleClickPositive()

        argumentCaptor<DownloadJob>().apply {
            verifyBlocking(containerDownloadManager, timeout(5000)) {createDownloadJob(capture())}
            Assert.assertEquals("Download Job created with status = NEEDS_PREPARED",
                    JobStatus.NEEDS_PREPARED, firstValue.djStatus)
            Assert.assertEquals("Download job root content entry uid is the same as presenter arg",
                    contentEntrySet.rootEntry.contentEntryUid, firstValue.djRootContentEntryUid)
            assertEquals("Metered data allowed is $meteredNetworkAllowed", meteredNetworkAllowed,
                    firstValue.meteredNetworkAllowed)
        }

        preparerCountdownLatch.await(5000, TimeUnit.MILLISECONDS)
        assertTrue("Preparer requester was invoked", preparationRequested.get())

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
    fun givenDownloadJobAlreadyCreated_whenHandleClickCalled_shouldSetStatusToQueued() {
        runBlocking {
            val existingDownloadJob = DownloadJob(1L, System.currentTimeMillis())
            val existingDownloadJobLiveData = DoorMutableLiveData<DownloadJob?>(existingDownloadJob)
            val existingDownloadJobItem = DownloadJobItem(existingDownloadJob, 1L, 1L, 1000L)
            val existingDownloadJobItemLiveData = DoorMutableLiveData<DownloadJobItem?>(existingDownloadJobItem)

            whenever(containerDownloadManager.getDownloadJob(existingDownloadJob.djUid))
                    .thenReturn(existingDownloadJobLiveData)
            whenever(containerDownloadManager.getDownloadJobItemByContentEntryUid(existingDownloadJobItem.djiContentEntryUid))
                    .thenReturn(existingDownloadJobItemLiveData)


            presenter = DownloadDialogPresenter(context, mapOf(ARG_CONTENT_ENTRY_UID to "1"), mockedDialogView,
                    umAppDatabase, umAppDatabaseRepo, containerDownloadManager, {Int, Any -> Unit})
            presenter.onCreate(HashMap<String, String>())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000).atLeastOnce()).setWifiOnlyOptionVisible(true)
            verify(mockedDialogView, timeout(5000).atLeastOnce()).setStackOptionsVisible(false)
            verify(mockedDialogView, timeout(5000).atLeastOnce()).setBottomButtonsVisible(true)

            presenter.handleClickPositive()

            verify(containerDownloadManager).enqueue(existingDownloadJob.djUid)
        }

    }
    @Test
    fun givenDownloadRunning_whenCreated_shouldShowStackedOptions() {
        runBlocking{
            val existingDownloadJob = DownloadJob(1L, System.currentTimeMillis()).also {
                it.djStatus = JobStatus.RUNNING
            }
            val existingDownloadJobLiveData = DoorMutableLiveData<DownloadJob?>(existingDownloadJob)
            val existingDownloadJobItem = DownloadJobItem(existingDownloadJob, 1L, 1L,
                    1000L).also {
                it.djiStatus = JobStatus.RUNNING
            }
            val existingDownloadJobItemLiveData = DoorMutableLiveData<DownloadJobItem?>(existingDownloadJobItem)

            whenever(containerDownloadManager.getDownloadJob(existingDownloadJob.djUid))
                    .thenReturn(existingDownloadJobLiveData)
            whenever(containerDownloadManager.getDownloadJobItemByContentEntryUid(existingDownloadJobItem.djiContentEntryUid))
                    .thenReturn(existingDownloadJobItemLiveData)

            val args = mapOf(ARG_CONTENT_ENTRY_UID to "1")
            presenter = DownloadDialogPresenter(context, args, mockedDialogView,
                    umAppDatabase, umAppDatabaseRepo, containerDownloadManager, { Int, Any -> Unit })
            presenter.onCreate(HashMap<String, String>())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000)).setStackOptionsVisible(true)
            argumentCaptor<IntArray>() {
                verify(mockedDialogView, timeout(5000)).setStackedOptions(capture(), any())
                assertArrayEquals("Set expected stacked options", DownloadDialogPresenter.STACKED_OPTIONS,
                        firstValue)
            }
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenDownloadRunning_whenClickPause_shouldCallPause() {
        runBlocking {
            val existingDownloadJob = DownloadJob(1L, System.currentTimeMillis()).also {
                it.djStatus = JobStatus.RUNNING
            }
            val existingDownloadJobLiveData = DoorMutableLiveData<DownloadJob?>(existingDownloadJob)
            val existingDownloadJobItem = DownloadJobItem(existingDownloadJob, 1L, 1L,
                    1000L).also {
                it.djiStatus = JobStatus.RUNNING
            }
            val existingDownloadJobItemLiveData = DoorMutableLiveData<DownloadJobItem?>(existingDownloadJobItem)

            whenever(containerDownloadManager.getDownloadJob(existingDownloadJob.djUid))
                    .thenReturn(existingDownloadJobLiveData)
            whenever(containerDownloadManager.getDownloadJobItemByContentEntryUid(existingDownloadJobItem.djiContentEntryUid))
                    .thenReturn(existingDownloadJobItemLiveData)

            presenter = DownloadDialogPresenter(context, mapOf(ARG_CONTENT_ENTRY_UID to "1"),
                    mockedDialogView, umAppDatabase, umAppDatabaseRepo, containerDownloadManager,
                    {Int, Any -> Unit})
            presenter.onCreate(HashMap<String, String>())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000)).setStackOptionsVisible(true)

            presenter.handleClickStackedButton(DownloadDialogPresenter.STACKED_BUTTON_PAUSE)

            verify(containerDownloadManager, timeout(5000)).pause(existingDownloadJob.djUid)
        }
    }

    @Test
    fun givenDownloadRunning_whenClickCancel_shouldSetStatusToCancelling() {
        runBlocking {
            val existingDownloadJob = DownloadJob(1L, System.currentTimeMillis()).also {
                it.djStatus = JobStatus.RUNNING
            }
            val existingDownloadJobLiveData = DoorMutableLiveData<DownloadJob?>(existingDownloadJob)
            val existingDownloadJobItem = DownloadJobItem(existingDownloadJob, 1L, 1L,
                    1000L).also {
                it.djiStatus = JobStatus.RUNNING
            }
            val existingDownloadJobItemLiveData = DoorMutableLiveData<DownloadJobItem?>(existingDownloadJobItem)

            whenever(containerDownloadManager.getDownloadJob(existingDownloadJob.djUid))
                    .thenReturn(existingDownloadJobLiveData)
            whenever(containerDownloadManager.getDownloadJobItemByContentEntryUid(existingDownloadJobItem.djiContentEntryUid))
                    .thenReturn(existingDownloadJobItemLiveData)

            presenter = DownloadDialogPresenter(context, mapOf(ARG_CONTENT_ENTRY_UID to "1"), mockedDialogView,
                    umAppDatabase, umAppDatabaseRepo, containerDownloadManager, { Int, Any -> Unit })
            presenter.onCreate(HashMap<String, String>())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000)).setStackOptionsVisible(true)

            presenter.handleClickStackedButton(STACKED_BUTTON_CANCEL)

            verify(containerDownloadManager, timeout(5000)).cancel(existingDownloadJob.djUid)
        }
    }
}