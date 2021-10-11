/*
package com.ustadmobile.sharedse.controller


import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UstadMobileSystemImpl
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

    private lateinit var contentEntrySet: RecursiveContentEntrySet

    private lateinit var contentJobManager: ContentJobManager

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
                removableMedia = false,
                usableSpace = 10 * 1024 * 1024 * 1024L))
        systemImpl = mock {
            on { getString(any(), any())}.thenAnswer {
                "${it.arguments[0]}"
            }

            onBlocking { getStorageDirsAsync(any()) }.thenAnswer {
                storageDirs
            }
        }

        contentJobManager = mock<ContentJobManager> {}

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadMobileSystemImpl>(overrides = true) with singleton { systemImpl }
            bind<ContentJobManager>() with singleton {
                contentJobManager
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
        contentEntrySet = insertTestContentEntries(repo, System.currentTimeMillis())
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
          */
/*  val downloadJobItemLiveData = DoorMutableLiveData<ContentJobItem?>(null)
            val downloadJobLiveData = DoorMutableLiveData<ContentJob?>(null)
            whenever(contentJobManager.getDownloadJobItemByContentEntryUid(any()))
                    .thenReturn(downloadJobItemLiveData)
            whenever(contentJobManager.getDownloadJob(any())).thenReturn(downloadJobLiveData)*//*


            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()),
                    mockedDialogView, di, mockLifecycle)

            presenter.onCreate(mapOf())
            presenter.onStart()
            presenter.handleStorageOptionSelection(storageDirs[0])

            verifyBlocking(contentEntrySpy, timeout(5000)) { getRecursiveDownloadTotals(contentEntrySet.rootEntry.contentEntryUid) }

            verify(mockedDialogView, timeout(5000)).setStatusText(any(),
                    eq(2), eq(UMFileUtil.formatFileSize(1000)))
            verify(mockedDialogView, timeout(5000).atLeastOnce()).setWarningTextVisible(false)

            assertNull("No download job should be created if the user does not select to download",
                    db.contentJobItemDao.findLiveDataByContentEntryUid(
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
           */
/* val downloadJobItemLiveData = DoorMutableLiveData<DownloadJobItem?>(null)
            val downloadJobLiveData = DoorMutableLiveData<DownloadJob?>(null)
            whenever(contentJobManager.getDownloadJobItemByContentEntryUid(any()))
                    .thenReturn(downloadJobItemLiveData)
            whenever(contentJobManager.getDownloadJob(any())).thenReturn(downloadJobLiveData)*//*


            storageDirs = listOf(UMStorageDir("/", name="Phone", isAvailable = true,
                    removableMedia = false,
                    usableSpace = 10L))

            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()),
                    mockedDialogView, di, mockLifecycle)

            presenter.onCreate(mapOf())
            presenter.onStart()
            presenter.handleStorageOptionSelection(storageDirs[0])

            verify(mockedDialogView, timeout(5000 * 5000).atLeastOnce()).setWarningTextVisible(true)
            verify(mockedDialogView, timeout(5000).atLeastOnce()).setWarningText(
                    MessageID.insufficient_space.toString())
            verify(mockedDialogView, timeout(5000).atLeastOnce()).setBottomPositiveButtonEnabled(false)
        }
    }
//
//
    private data class MockDownloadJob(var mockContentJob: ContentJob, var mockContentJobItem: ContentJobItem,
                                       var existingDownloadSizeInfo: DownloadJobSizeInfo) {
    }

    private fun setupMockDownloadJob(djStatus: Int): MockDownloadJob {
        return runBlocking {
            val contentJob = ContentJob(1)
            val contentJobItem = ContentJobItem().apply {
                cjiParentCjiUid = contentJob.cjUid
                cjiItemTotal = 1000L
                cjiContentEntryUid = 1L
                cjiContainerUid = 1L
                cjiStatus = djStatus
            }

            val contentJobLiveData = DoorMutableLiveData<ContentJobItem?>(contentJobItem)

            val existingDownloadSizeInfo = DownloadJobSizeInfo(4, 1000L)
            val downloadJobDaoSpy = spy(db.contentJobItemDao) {
                onBlocking { getDownloadSizeInfo(contentJobItem.cjiJobUid)}.doReturn(existingDownloadSizeInfo)
                onBlocking { findLiveDataByContentEntryUid(contentJobItem.cjiContentEntryUid) }.doReturn(contentJobLiveData)
            }

            whenever(db.contentJobItemDao).thenReturn(downloadJobDaoSpy)

            MockDownloadJob(contentJob, contentJobItem, existingDownloadSizeInfo)
        }
    }

    @Test
    fun givenExistingDownloadJobNotStarted_whenViewCreated_shouldGetSizeFromDatabase() {
        val mockExistingDownloadJob = setupMockDownloadJob(JobStatus.NOT_QUEUED)

        runBlocking {
            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to
                            mockExistingDownloadJob.mockContentJobItem.cjiContentEntryUid.toString()),
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
          */
/*  whenever(contentJobManager.getDownloadJobItemByContentEntryUid(any()))
                    .thenReturn(DoorMutableLiveData(null))
            whenever(contentJobManager.getDownloadJobItemByJobItemUid(any()))
                    .thenReturn(DoorMutableLiveData(null))
            whenever(contentJobManager.getDownloadJob(any()))
                    .thenReturn(DoorMutableLiveData(null))*//*

        }

        whenever(mockedDialogView.setWifiOnlyOptionVisible(true)).doAnswer {
            viewReadyLatch.countDown()
        }

        val args = mapOf(
                ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()
        )

        presenter = DownloadDialogPresenter(context, args, mockedDialogView, di, mockLifecycle)
        presenter.onCreate(mapOf())
        presenter.onStart()
        viewReadyLatch.await(5, TimeUnit.SECONDS)

        presenter.handleClickWiFiOnlyOption(meteredNetworkAllowed)

        presenter.handleClickPositive()

        argumentCaptor<Long>().apply {
            verifyBlocking(contentJobManager, timeout(5000)) {
                enqueueContentJob(any(), capture())
            }
            val contentJob = db.contentJobItemDao.findByJobId(this.firstValue)!!
            assertEquals("Download Job created with status = NEEDS_PREPARED",
                    JobStatus.QUEUED, contentJob.cjiStatus)
            assertEquals("Download job root content entry uid is the same as presenter arg",
                    contentEntrySet.rootEntry.contentEntryUid, contentJob.cjiContentEntryUid)
            if(meteredNetworkAllowed){
                assertEquals("Metered data allowed is Metered", ContentJobItem.ACCEPT_METERED,
                        contentJob.cjiConnectivityAcceptable)
            }else{
                assertEquals("Metered data allowed is Metered", ContentJobItem.ACCEPT_UNMETERED,
                        contentJob.cjiConnectivityAcceptable)
            }

        }

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

          */
/*  whenever(contentJobManager.getDownloadJob(existingDownloadJob.djUid))
                    .thenReturn(existingDownloadJobLiveData)
            whenever(contentJobManager.getDownloadJobItemByContentEntryUid(existingDownloadJobItem.djiContentEntryUid))
                    .thenReturn(existingDownloadJobItemLiveData)*//*


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

         */
/*   whenever(contentJobManager.getDownloadJob(existingDownloadJob.djUid))
                    .thenReturn(existingDownloadJobLiveData)
            whenever(contentJobManager.getDownloadJobItemByContentEntryUid(existingDownloadJobItem.djiContentEntryUid))
                    .thenReturn(existingDownloadJobItemLiveData)*//*


            presenter = DownloadDialogPresenter(context, mapOf(ARG_CONTENT_ENTRY_UID to "1"),
                    mockedDialogView, di, mockLifecycle)
            presenter.onCreate(HashMap<String, String>())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000)).setStackOptionsVisible(true)

            presenter.handleClickStackedButton(DownloadDialogPresenter.STACKED_BUTTON_PAUSE)
*/
/*
            verify(contentJobManager, timeout(5000)).pause(existingDownloadJob.djUid)*//*

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

         */
/*   whenever(contentJobManager.getDownloadJob(existingDownloadJob.djUid))
                    .thenReturn(existingDownloadJobLiveData)
            whenever(contentJobManager.getDownloadJobItemByContentEntryUid(existingDownloadJobItem.djiContentEntryUid))
                    .thenReturn(existingDownloadJobItemLiveData)*//*


            presenter = DownloadDialogPresenter(context, mapOf(ARG_CONTENT_ENTRY_UID to "1"), mockedDialogView,
                    di, mockLifecycle)
            presenter.onCreate(HashMap<String, String>())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000)).setStackOptionsVisible(true)

            presenter.handleClickStackedButton(STACKED_BUTTON_CANCEL)
*/
/*
            verify(contentJobManager, timeout(5000)).cancel(existingDownloadJob.djUid)*//*

        }
    }
}*/
