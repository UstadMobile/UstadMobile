package com.ustadmobile.sharedse.controller


import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.Lifecycle
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter.Companion.STACKED_BUTTON_CANCEL
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.sharedse.network.*
import com.ustadmobile.sharedse.util.UstadTestRule
import kotlinx.coroutines.runBlocking
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

    private val mockLifecycle = mock<Lifecycle> {
        on { realCurrentDoorState }.thenReturn(DoorState.STARTED)
    }

    private val mockLifecycleOwner = mock<LifecycleOwner>() {
        on { getLifecycle() }.thenReturn(mockLifecycle)
    }

    private lateinit var contentEntrySet: RecursiveContentEntrySet

    private lateinit var contentJobManager: ContentJobManager

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var storageDirs: List<ContainerStorageDir>

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    @Before
    @Throws(IOException::class)
    fun setUp() {
        storageDirs = listOf(ContainerStorageDir("/","Phone",
                10 * 1024 * 1024 * 1024L,false))


        systemImpl = mock {
            on { getString(any(), any())}.thenAnswer {
                "${it.arguments[0]}"
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
        db =  di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_DB)
        repo = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)
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
            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()),
                    mockedDialogView, di, mockLifecycleOwner)

            presenter.onCreate(mapOf())
            presenter.onStart()
            presenter.handleStorageOptionSelection(storageDirs[0])

            verifyBlocking(contentEntrySpy, timeout(5000)) { getRecursiveDownloadTotals(contentEntrySet.rootEntry.contentEntryUid) }

            verify(mockedDialogView, timeout(5000)).setStatusText(any(),
                    eq(2), eq(UMFileUtil.formatFileSize(1000)))
            verify(mockedDialogView, timeout(5000).atLeastOnce()).setWarningTextVisible(false)

            val contentJobItem = db.contentJobItemDao.findAll()
            assertEquals("No download job should be created if the user does not select to download", true, contentJobItem.isEmpty())
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
            storageDirs = listOf(ContainerStorageDir("/","Phone",
                    10L,true))

            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()),
                    mockedDialogView, di, mockLifecycleOwner)

            presenter.onCreate(mapOf())
            presenter.onStart()
            presenter.handleStorageOptionSelection(storageDirs[0])

            verify(mockedDialogView, timeout(5000 * 5000).atLeastOnce()).setWarningTextVisible(true)
            verify(mockedDialogView, timeout(5000).atLeastOnce()).setWarningText(
                    MessageID.insufficient_space.toString())
            verify(mockedDialogView, timeout(5000).atLeastOnce()).setBottomPositiveButtonEnabled(false)
        }
    }

    private data class MockDownloadJob(var contentEntryUid: Long, var status: Int,
                                       var existingDownloadSizeInfo: DownloadJobSizeInfo) {
    }

    private fun setupMockDownloadJob(djStatus: Int): MockDownloadJob {
        return runBlocking {
            val existingDownloadSizeInfo = DownloadJobSizeInfo(4, 1000L)
            val entryDaoSpy = spy(db.contentEntryDao) {
                onBlocking { getRecursiveDownloadTotals( any()) }.doReturn(existingDownloadSizeInfo)
                onBlocking { statusForDownloadDialog(any()) }.thenAnswer {
                    djStatus
                }
            }

            whenever(repo.contentEntryDao).thenReturn(entryDaoSpy)
            whenever(db.contentEntryDao).thenReturn(entryDaoSpy)

            MockDownloadJob(1L, djStatus, existingDownloadSizeInfo)
        }
    }

    @Test
    fun givenExistingDownloadJobNotStarted_whenViewCreated_shouldGetSizeFromDatabase() {
        val mockExistingDownloadJob = setupMockDownloadJob(JobStatus.NOT_QUEUED)

        runBlocking {
            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to
                            mockExistingDownloadJob.contentEntryUid.toString()),
                    mockedDialogView, di, mockLifecycleOwner)

            presenter.onCreate(mapOf())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000)).setStatusText(any(),
                    eq(mockExistingDownloadJob.existingDownloadSizeInfo.numEntries),
                    eq(UMFileUtil.formatFileSize(mockExistingDownloadJob.existingDownloadSizeInfo.totalSize)))
        }
    }

    //@Test
    fun givenExistingDownloadJobPaused_whenViewCreated_thenShouldShowStackedOptions() {
        val mockExistingDownloadJob = setupMockDownloadJob(JobStatus.PAUSED)

        runBlocking {
            val preparerFn =  {downloadJobUid: Int, context: Any  -> Unit}
            presenter = DownloadDialogPresenter(context,
                    mapOf(ARG_CONTENT_ENTRY_UID to "1"),
                    mockedDialogView, di, mockLifecycleOwner)

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

        whenever(mockedDialogView.setWifiOnlyOptionVisible(true)).doAnswer {
            viewReadyLatch.countDown()
        }

        val args = mapOf(
                ARG_CONTENT_ENTRY_UID to contentEntrySet.rootEntry.contentEntryUid.toString()
        )

        presenter = DownloadDialogPresenter(context, args, mockedDialogView, di, mockLifecycleOwner)
        presenter.onCreate(mapOf())
        presenter.onStart()
        viewReadyLatch.await(5, TimeUnit.SECONDS)

        presenter.handleClickWiFiOnlyOption(!meteredNetworkAllowed)

        presenter.handleClickPositive()

        argumentCaptor<Long>().apply {
            verifyBlocking(contentJobManager, timeout(5000)) {
                enqueueContentJob(any(), capture())
            }
            val contentJobItem = db.contentJobItemDao.findRootJobItemByJobId(this.firstValue)!!
            assertEquals("Download Job created with status = NEEDS_PREPARED",
                    JobStatus.QUEUED, contentJobItem.cjiRecursiveStatus)
            assertEquals("Download job root content entry uid is the same as presenter arg",
                    contentEntrySet.rootEntry.contentEntryUid, contentJobItem.cjiContentEntryUid)
            val contentJob = db.contentJobDao.findByUid(this.firstValue)!!
            assertEquals("Metered data allowed set correctly", meteredNetworkAllowed,
                        contentJob.cjIsMeteredAllowed)
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
            setupMockDownloadJob(JobStatus.RUNNING)
            val args = mapOf(ARG_CONTENT_ENTRY_UID to "1")
            presenter = DownloadDialogPresenter(context, args, mockedDialogView, di, mockLifecycleOwner)
            presenter.onCreate(mapOf())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000 * 1000)).setStackOptionsVisible(true)
            argumentCaptor<IntArray>() {
                verify(mockedDialogView, timeout(5000)).setStackedOptions(capture(), any())
                assertArrayEquals("Set expected stacked options", DownloadDialogPresenter.STACKED_OPTIONS,
                        firstValue)
            }
        }
    }

    @Test
    fun givenDownloadRunning_whenClickCancel_shouldCancelJob() {
        runBlocking {
            setupMockDownloadJob(JobStatus.RUNNING)

            presenter = DownloadDialogPresenter(context, mapOf(ARG_CONTENT_ENTRY_UID to "1"),
                mockedDialogView, di, mockLifecycleOwner)
            presenter.onCreate(mapOf())
            presenter.onStart()

            verify(mockedDialogView, timeout(5000)).setStackOptionsVisible(true)

            presenter.handleClickStackedButton(STACKED_BUTTON_CANCEL)
            verifyBlocking(contentJobManager, timeout(5000)) {
                cancelContentJob(any(), any())
            }

        }
    }
}
