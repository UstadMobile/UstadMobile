package com.ustadmobile.sharedse.controller


import com.github.aakira.napier.Napier
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter.Companion.STACKED_BUTTON_CANCEL
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.sharedse.network.*
import com.ustadmobile.sharedse.util.UstadTestRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DownloadDialogPresenterTest {

    private lateinit var mockedDialogView: DownloadDialogView

    private lateinit var presenter: DownloadDialogPresenter

    private val context = Any()

    private val mockLifecycle = mock<DoorLifecycleOwner>() {
        on { currentState }.thenReturn(DoorLifecycleObserver.STARTED)
    }

    private lateinit var downloadJob: DownloadJob

    private lateinit var contentEntrySet: RecursiveContentEntrySet

    private lateinit var containerDownloadManager: ContainerDownloadManager

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var storageDirs: List<UMStorageDir>

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    @Before
    @Throws(IOException::class)
    fun setUp() {
        storageDirs = listOf(UMStorageDir("/", name="Phone", isAvailable = true,
                isUserSpecific = false, removableMedia = false,
                usableSpace = 10 * 1024 * 1024 * 1024L))
        systemImpl = mock {
            on { getString(any(), any())}.thenAnswer {
                "${it.arguments[0]}"
            }

            onBlocking { getStorageDirsAsync(any()) }.thenAnswer {
                storageDirs
            }
        }

        containerDownloadManager = mock<ContainerDownloadManager> {}

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadMobileSystemImpl>(overrides = true) with singleton { systemImpl }
            bind<ContainerDownloadManager>() with scoped(ustadTestRule.endpointScope!!).singleton {
                containerDownloadManager
            }
        }

        mockedDialogView = mock {
            on { runOnUiThread(any()) } doAnswer {
                Thread(it.getArgument(0) as Runnable).start()
            }
        }

        val accountManager: UstadAccountManager by di.instance()
        db =  di.on(accountManager.activeAccount).direct.instance(tag = TAG_DB)
        repo = di.on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)
        contentEntrySet = insertTestContentEntries(db, System.currentTimeMillis())
    }

    @Test
    fun givenNoExistingDownloadJob_whenViewCreated_shouldRequestTotalSizeFromServer() {
        val contentEntrySpy = spy(repo.contentEntryDao) { }

        runBlocking {
            //Use this formulation to avoid the mock setup calling the real method and throwing an exception
            doReturn(DownloadJobSizeInfo(2, 1000)).whenever(contentEntrySpy)
                    .getRecursiveDownloadTotals(eq(contentEntrySet.rootEntry.contentEntryUid))
        }


        whenever(repo.contentEntryDao).thenReturn(contentEntrySpy)

        runBlocking {
            val downloadJobItemLiveData = DoorMutableLiveData<DownloadJobItem?>(null)
            val downloadJobLiveData = DoorMutableLiveData<DownloadJob?>(null)
            whenever(containerDownloadManager.getDownloadJobItemByContentEntryUid(any()))
                    .thenReturn(downloadJobItemLiveData)
            whenever(containerDownloadManager.getDownloadJob(any())).thenReturn(downloadJobLiveData)

            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()),
                    mockedDialogView, di, mockLifecycle)

            presenter.onCreate(mapOf())
            presenter.onStart()

            verifyBlocking(contentEntrySpy, timeout(5000 * 50000)) { getRecursiveDownloadTotals(contentEntrySet.rootEntry.contentEntryUid) }

            verify(mockedDialogView, timeout(5000)).setStatusText(any(),
                    eq(2), eq(UMFileUtil.formatFileSize(1000)))
            verify(mockedDialogView, timeout(5000).atLeastOnce()).setWarningTextVisible(false)

            assertNull("No download job should be created if the user does not select to download",
                    db.downloadJobDao.findDownloadJobByRootContentEntryUid(
                            contentEntrySet.rootEntry.contentEntryUid))
        }


    }

    @Test
    fun givenNoExistingDownload_whenDownloadSizeExceedsAvaiableSpace_shouldShowWarningMessageAndDisableButton() {
        val contentEntrySpy = spy(repo.contentEntryDao) { }

        runBlocking {
            //Use this formulation to avoid the mock setup calling the real method and throwing an exception
            doReturn(DownloadJobSizeInfo(2, 1000)).whenever(contentEntrySpy)
                    .getRecursiveDownloadTotals(eq(contentEntrySet.rootEntry.contentEntryUid))
        }

        whenever(repo.contentEntryDao).thenReturn(contentEntrySpy)

        runBlocking {
            val downloadJobItemLiveData = DoorMutableLiveData<DownloadJobItem?>(null)
            val downloadJobLiveData = DoorMutableLiveData<DownloadJob?>(null)
            whenever(containerDownloadManager.getDownloadJobItemByContentEntryUid(any()))
                    .thenReturn(downloadJobItemLiveData)
            whenever(containerDownloadManager.getDownloadJob(any())).thenReturn(downloadJobLiveData)

            storageDirs = listOf(UMStorageDir("/", name="Phone", isAvailable = true,
                    isUserSpecific = false, removableMedia = false,
                    usableSpace = 10L))

            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()),
                    mockedDialogView, di, mockLifecycle)

            presenter.onCreate(mapOf())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000).atLeastOnce()).setWarningTextVisible(true)
            verify(mockedDialogView, timeout(5000).atLeastOnce()).setWarningText(
                    MessageID.insufficient_space.toString())
            verify(mockedDialogView, timeout(5000).atLeastOnce()).setBottomPositiveButtonEnabled(false)
        }
    }
//
//
    private data class MockDownloadJob(var mockDownloadJob: DownloadJob, var mockDownloadJobItem: DownloadJobItem,
                                       var existingDownloadSizeInfo: DownloadJobSizeInfo) {
    }

    private fun setupMockDownloadJob(djStatus: Int): MockDownloadJob {
        return runBlocking {
            val existingDownloadJob = DownloadJob(1L, System.currentTimeMillis()).also {
                it.djUid = 1
                it.djStatus = djStatus
            }

            val existingDownloadJobItem = DownloadJobItem(existingDownloadJob, 1L,
                    1L, 1000L).also {
                it.djiStatus = djStatus
            }
            val downloadJobLiveData = DoorMutableLiveData<DownloadJob?>(existingDownloadJob)
            val downloadJobItemLiveData = DoorMutableLiveData<DownloadJobItem?>(
                    existingDownloadJobItem)

            val existingDownloadSizeInfo = DownloadJobSizeInfo(4, 1000L)
            val downloadJobDaoSpy = spy(db.downloadJobDao) {
                onBlocking { getDownloadSizeInfo(existingDownloadJob.djUid)}.doReturn(existingDownloadSizeInfo)
            }

            whenever(db.downloadJobDao).thenReturn(downloadJobDaoSpy)

            whenever(containerDownloadManager.getDownloadJobItemByContentEntryUid(existingDownloadJobItem.djiContentEntryUid))
                    .thenReturn(downloadJobItemLiveData)
            whenever(containerDownloadManager.getDownloadJob(existingDownloadJob.djUid))
                    .thenReturn(downloadJobLiveData)

            MockDownloadJob(existingDownloadJob, existingDownloadJobItem, existingDownloadSizeInfo)
        }
    }

    @Test
    fun givenExistingDownloadJobNotStarted_whenViewCreated_shouldGetSizeFromDatabase() {
        val mockExistingDownloadJob = setupMockDownloadJob(JobStatus.NOT_QUEUED)

        runBlocking {
            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to
                            mockExistingDownloadJob.mockDownloadJob.djRootContentEntryUid.toString()),
                    mockedDialogView, di, mockLifecycle)

            presenter.onCreate(mapOf())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000)).setStatusText(any(),
                    eq(mockExistingDownloadJob.existingDownloadSizeInfo.numEntries),
                    eq(UMFileUtil.formatFileSize(mockExistingDownloadJob.existingDownloadSizeInfo.totalSize)))
        }
    }

    @Test
    fun givenExistingDownloadJobCancelled_whenViewCreated_shouldGetSizeFromDatabase() {
        val mockExistingDownloadJob = setupMockDownloadJob(JobStatus.CANCELED)

        runBlocking {
            val preparerFn =  {downloadJobUid: Int, context: Any  -> Unit}
            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to "1"),
                    mockedDialogView, di, mockLifecycle)

            presenter.onCreate(mapOf())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000)).setStatusText(any(),
                    eq(mockExistingDownloadJob.existingDownloadSizeInfo.numEntries),
                    eq(UMFileUtil.formatFileSize(mockExistingDownloadJob.existingDownloadSizeInfo.totalSize)))
        }
    }

    @Test
    fun givenExistingDownloadJobPaused_whenViewCreated_thenShouldShowStackedOptions() {
        val mockExistingDownloadJob = setupMockDownloadJob(JobStatus.PAUSED)

        runBlocking {
            val preparerFn =  {downloadJobUid: Int, context: Any  -> Unit}
            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to "1"),
                    mockedDialogView, di, mockLifecycle)

            presenter.onCreate(mapOf())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000)).setStackOptionsVisible(true)
            verify(mockedDialogView, timeout(5000)).setBottomButtonsVisible(false)

            argumentCaptor<IntArray>() {
                verify(mockedDialogView, timeout(5000)).setStackedOptions(capture(), any())
                assertArrayEquals("Set expected stacked options", DownloadDialogPresenter.STACKED_OPTIONS,
                        firstValue)
            }
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

        val mockDownloadPrepRequester = mock<DownloadPreparationRequester> {  }
        val extendedDi = DI {
            extend(di)
            bind<DownloadPreparationRequester>() with scoped(ustadTestRule.endpointScope!!).singleton { mockDownloadPrepRequester }
        }

        presenter = DownloadDialogPresenter(context, args, mockedDialogView, extendedDi, mockLifecycle)
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

        verify(mockDownloadPrepRequester, timeout(5000)).requestPreparation(any())

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
            presenter = DownloadDialogPresenter(context, args, mockedDialogView, di, mockLifecycle)
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
                    mockedDialogView, di, mockLifecycle)
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
                    di, mockLifecycle)
            presenter.onCreate(HashMap<String, String>())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000)).setStackOptionsVisible(true)

            presenter.handleClickStackedButton(STACKED_BUTTON_CANCEL)

            verify(containerDownloadManager, timeout(5000)).cancel(existingDownloadJob.djUid)
        }
    }
}