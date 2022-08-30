
package com.ustadmobile.core.controller

import com.ustadmobile.core.contentjob.ContentPluginIds
import com.ustadmobile.core.db.JobStatus
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.ContentEntryDetailOverviewView
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*
import java.lang.Thread.sleep

class ContentEntryDetailOverviewPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ContentEntryDetailOverviewView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoContentEntrySpyDao: ContentEntryDao

    private var createdEntry: ContentEntry? = null

    private lateinit var entryContainer: Container

    private val defaultTimeout = 3000L

    private var presenterArgs: Map<String, String>? = null

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()



        di = DI {
            import(ustadTestRule.diModule)
            bind<Boolean>(tag = UstadMobileSystemCommon.TAG_DOWNLOAD_ENABLED) with singleton {
                true
            }
        }

        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        repoContentEntrySpyDao = spy(repo.contentEntryDao)
        whenever(repo.contentEntryDao).thenReturn(repoContentEntrySpyDao)
        createdEntry = ContentEntry().apply {
            title = "Dummy Entry"
            leaf = true
            contentEntryUid = repo.contentEntryDao.insert(this)
        }

        entryContainer = Container().apply {
            containerContentEntryUid = createdEntry?.contentEntryUid ?: 0L
            fileSize = 1000
            cntLastModified = getSystemTimeInMillis()
            containerUid = repo.containerDao.insert(this)
        }

        presenterArgs = mapOf(ARG_ENTITY_UID to createdEntry?.contentEntryUid.toString())
    }

    @Test
    fun givenContentEntryExists_whenLaunched_thenShouldShowContentEntry(){
        val presenter = ContentEntryDetailOverviewPresenter(context,
                presenterArgs!!, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        nullableArgumentCaptor<ContentEntryWithMostRecentContainer>().apply {
            verify(mockView, timeout(defaultTimeout).atLeastOnce()).entity = capture()
            Assert.assertEquals("Expected entry was set on view",
                    createdEntry?.contentEntryUid, lastValue!!.contentEntryUid)
        }

        presenter.onStart()

    }


    @Test
    fun givenContentEntryExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled(){
        val presenter = ContentEntryDetailOverviewPresenter(context,
                presenterArgs!!, mockView, di, mockLifecycleOwner)
        val systemImpl: UstadMobileSystemImpl by di.instance()

        presenter.onCreate(null)

        sleep(defaultTimeout)

        presenter.handleClickEdit()

        val testNavController: UstadNavController = di.direct.instance()

        verify(testNavController).navigate(eq(ContentEntryEdit2View.VIEW_NAME),
            eq(mapOf(ARG_ENTITY_UID to createdEntry?.contentEntryUid.toString(),
            UstadView.ARG_LEAF to true.toString())), any())

    }

    @Test
    fun givenContentEntryExists_whenContentJobItemInProgress_thenShouldSetProgressBarView() {
        val db: UmAppDatabase by di.activeDbInstance()

        val contentJob = ContentJob().apply {
            cjUid = 1
        }
        val contentJobItem = ContentJobItem().apply {
            cjiUid = 1
            cjiJobUid = 1
            cjiStatus = JobStatus.RUNNING
            cjiRecursiveStatus = JobStatus.RUNNING
            cjiItemProgress = 50
            cjiItemTotal = 100
            cjiContentEntryUid = createdEntry!!.contentEntryUid
            cjiContainerUid = entryContainer.containerUid
        }
        runBlocking {
            db.contentJobDao.insertAsync(contentJob)
            db.contentJobItemDao.insertJobItem(contentJobItem)
        }


        val presenter = ContentEntryDetailOverviewPresenter(context,
                presenterArgs!!, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        sleep(defaultTimeout)

        argumentCaptor<List<ContentJobItemProgress>> {
            verify(mockView).activeContentJobItems = capture()
            val contentJobItemCaptured = this.firstValue[0]
            Assert.assertEquals("progress match",
                    contentJobItem.cjiItemProgress.toInt(), contentJobItemCaptured.progress)
            Assert.assertEquals("total match",
                    contentJobItem.cjiItemTotal.toInt(), contentJobItemCaptured.total)
        }

    }

    @Test
    fun givenEntryNotYetDownloaded_whenOnCreatedCalled_thenShouldShowDownloadButtonNotOthers() {
        val presenter = ContentEntryDetailOverviewPresenter(context,
            presenterArgs!!, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)
        nullableArgumentCaptor<ContentEntryButtonModel> {
            verify(mockView, timeout(5000).atLeastOnce()).contentEntryButtons = capture()
            Assert.assertTrue(lastValue!!.showDownloadButton)
            Assert.assertFalse(lastValue!!.showOpenButton)
            Assert.assertFalse(lastValue!!.showUpdateButton)
            Assert.assertFalse(lastValue!!.showDeleteButton)
            Assert.assertFalse(lastValue!!.showManageDownloadButton)
        }
    }

    @Test
    fun givenEntryDownloadInProgress_whenOnCreatedCalled_thenShouldShowManageDownloadButtonNotOthers() {
        val db: UmAppDatabase by di.activeDbInstance()

        runBlocking {
            db.contentJobItemDao.insertJobItem(ContentJobItem().apply {
                cjiContentEntryUid = createdEntry?.contentEntryUid ?: 0
                cjiPluginId = ContentPluginIds.CONTAINER_DOWNLOAD_PLUGIN
                cjiItemProgress = 50
                cjiItemTotal = 100
                cjiRecursiveStatus = JobStatus.RUNNING
                cjiStatus = JobStatus.RUNNING
            })
        }

        val presenter = ContentEntryDetailOverviewPresenter(context,
            presenterArgs!!, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        nullableArgumentCaptor<ContentEntryButtonModel> {
            verify(mockView, timeout(5000).atLeastOnce()).contentEntryButtons = capture()
            Assert.assertFalse(lastValue!!.showDownloadButton)
            Assert.assertFalse(lastValue!!.showOpenButton)
            Assert.assertFalse(lastValue!!.showUpdateButton)
            Assert.assertFalse(lastValue!!.showDeleteButton)
            Assert.assertTrue(lastValue!!.showManageDownloadButton)
        }
    }

    @Test
    fun givenEntryDownloadedNoUpdateAvailable_whenOnCreatedCalled_thenShouldShowOpenAndDeleteButton() {
        val db: UmAppDatabase by di.activeDbInstance()

        runBlocking {
            val cefUid = db.containerEntryFileDao.insert(ContainerEntryFile().apply {
                cefPath = "/dummy"
            })

            db.containerEntryDao.insert(ContainerEntry().apply {
                ceCefUid = cefUid
                ceContainerUid = entryContainer.containerUid
            })
        }

        val presenter = ContentEntryDetailOverviewPresenter(context,
            presenterArgs!!, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        nullableArgumentCaptor<ContentEntryButtonModel> {
            verify(mockView, timeout(5000).atLeastOnce()).contentEntryButtons = capture()
            Assert.assertFalse(lastValue!!.showDownloadButton)
            Assert.assertTrue(lastValue!!.showOpenButton)
            Assert.assertFalse(lastValue!!.showUpdateButton)
            Assert.assertTrue(lastValue!!.showDeleteButton)
            Assert.assertFalse(lastValue!!.showManageDownloadButton)
        }
    }

    @Test
    fun givenEntryDownloadedWithUpdateAvailable_whenOnCreateCalled_thenShouldShowOpenUpdateAndDeleteButton() {
        val db: UmAppDatabase by di.activeDbInstance()

        runBlocking {
            val cefUid = db.containerEntryFileDao.insert(ContainerEntryFile().apply {
                cefPath = "/dummy"
            })

            db.containerEntryDao.insert(ContainerEntry().apply {
                ceCefUid = cefUid
                ceContainerUid = entryContainer.containerUid
            })

            db.containerDao.insert(Container().apply {
                fileSize = 800
                containerContentEntryUid = createdEntry?.contentEntryUid ?: 0
                cntLastModified = systemTimeInMillis()
            })
        }

        val presenter = ContentEntryDetailOverviewPresenter(context,
            presenterArgs!!, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        nullableArgumentCaptor<ContentEntryButtonModel> {
            verify(mockView, timeout(5000).atLeastOnce()).contentEntryButtons = capture()
            Assert.assertFalse(lastValue!!.showDownloadButton)
            Assert.assertTrue(lastValue!!.showOpenButton)
            Assert.assertTrue(lastValue!!.showUpdateButton)
            Assert.assertTrue(lastValue!!.showDeleteButton)
            Assert.assertFalse(lastValue!!.showManageDownloadButton)
        }
    }

    @Test
    fun givenEntryNotDownloadedContainerNotReady_whenOnCreateCalled_thenNoButtonsAvailable() {
        val db: UmAppDatabase by di.activeDbInstance()

        runBlocking {
            entryContainer.fileSize = 0
            db.containerDao.update(entryContainer)
        }

        val presenter = ContentEntryDetailOverviewPresenter(context,
            presenterArgs!!, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        nullableArgumentCaptor<ContentEntryButtonModel> {
            verify(mockView, timeout(5000).atLeastOnce()).contentEntryButtons = capture()
            Assert.assertFalse(lastValue!!.showDownloadButton)
            Assert.assertFalse(lastValue!!.showOpenButton)
            Assert.assertFalse(lastValue!!.showUpdateButton)
            Assert.assertFalse(lastValue!!.showDeleteButton)
            Assert.assertFalse(lastValue!!.showManageDownloadButton)
        }

    }

    @Test
    fun givenEntryDownloadedWithUpdateNotReady_whenOnCreateCalled_thenShouldShowOpenAndDeleteButton() {
        val db: UmAppDatabase by di.activeDbInstance()

        runBlocking {
            val cefUid = db.containerEntryFileDao.insert(ContainerEntryFile().apply {
                cefPath = "/dummy"
            })

            db.containerEntryDao.insert(ContainerEntry().apply {
                ceCefUid = cefUid
                ceContainerUid = entryContainer.containerUid
            })

            db.containerDao.insert(Container().apply {
                fileSize = 0
                containerContentEntryUid = createdEntry?.contentEntryUid ?: 0
                cntLastModified = systemTimeInMillis()
            })
        }

        val presenter = ContentEntryDetailOverviewPresenter(context,
            presenterArgs!!, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        nullableArgumentCaptor<ContentEntryButtonModel> {
            verify(mockView, timeout(5000).atLeastOnce()).contentEntryButtons = capture()
            Assert.assertFalse(lastValue!!.showDownloadButton)
            Assert.assertTrue(lastValue!!.showOpenButton)
            Assert.assertFalse(lastValue!!.showUpdateButton)
            Assert.assertTrue(lastValue!!.showDeleteButton)
            Assert.assertFalse(lastValue!!.showManageDownloadButton)
        }
    }



}
