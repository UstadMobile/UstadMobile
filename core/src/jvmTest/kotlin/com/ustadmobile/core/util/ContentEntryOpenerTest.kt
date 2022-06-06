package com.ustadmobile.core.util

import org.mockito.kotlin.*
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.VideoContentView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class
ContentEntryOpenerTest {

    private lateinit var umAppDatabase: UmAppDatabase

    private lateinit var umAppRepository: UmAppDatabase

    private lateinit var contentEntry: ContentEntry

    private lateinit var impl: UstadMobileSystemImpl

    private val context = mock<DoorLifecycleOwner>() {
        on { currentState }.thenReturn(DoorLifecycleObserver.STARTED)
    } as Any

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    lateinit var di: DI

    private lateinit var accountManager: UstadAccountManager

    private lateinit var endpoint: Endpoint

    @Before
    fun setUp() {
        di = DI {
            import(ustadTestRule.diModule)
        }
        accountManager = di.direct.instance()
        endpoint = Endpoint(accountManager.activeAccount.endpointUrl)
        impl = di.direct.instance()

        umAppDatabase = di.on(endpoint).direct.instance(tag = TAG_DB)
        umAppRepository = di.on(endpoint).direct.instance(tag = TAG_REPO)


        contentEntry = ContentEntry()
        contentEntry.contentEntryUid = umAppRepository.contentEntryDao.insert(contentEntry)

        var container = Container()
        container.containerContentEntryUid = contentEntry.contentEntryUid
        container.fileSize = 10
        container.mimeType = "video/mp4"
        container.cntLastModified = System.currentTimeMillis()
        container.containerUid = umAppRepository.containerDao.insert(container)

        val containerEntryFile = ContainerEntryFile().apply {
            this.cefPath = "/home/"
            this.cefUid = umAppDatabase.containerEntryFileDao.insert(this)
        }

        ContainerEntry().apply {
            this.ceContainerUid = container.containerUid
            this.cePath = "example.mp4"
            this.ceCefUid = containerEntryFile.cefUid
            this.ceUid = umAppDatabase.containerEntryDao.insert(this)
        }

    }

    @Test
    fun givenDownloadRequired_whenEntryDownloaded_thenOpenContent() {
        runBlocking {
            ContentEntryOpener(di, endpoint).openEntry(context, contentEntry.contentEntryUid,
                true, false, false)
            verify(impl).go(eq(VideoContentView.VIEW_NAME), any(), any(), any())
        }
    }

    @Test
    fun givenDownloadedRequired_whenEntryNotDownloaded_thenGotToDetailScreen(){
        runBlocking {

            var contentEntry = ContentEntry()
            contentEntry.contentEntryUid = umAppRepository.contentEntryDao.insert(contentEntry)

            var container = Container()
            container.containerContentEntryUid = contentEntry.contentEntryUid
            container.fileSize = 10
            container.mimeType = "video/mp4"
            container.cntLastModified = System.currentTimeMillis()
            container.containerUid = umAppRepository.containerDao.insert(container)

            var dj = ContentJobItem()
            dj.cjiContainerUid = container.containerUid
            dj.cjiContentEntryUid = contentEntry.contentEntryUid
            dj.cjiRecursiveStatus = JobStatus.QUEUED
            umAppDatabase.contentJobItemDao.insertJobItem(dj)

            ContentEntryOpener(di, endpoint).openEntry(context, contentEntry.contentEntryUid,
                true, true, false)
            verify(impl).go(eq(ContentEntryDetailView.VIEW_NAME), any(), eq(context), any())
        }
    }


    @Test
    fun givenDownloadNotRequired_whenEntryDownloaded_thenOpenContent(){
        runBlocking {
            ContentEntryOpener(di, endpoint).openEntry(context, contentEntry.contentEntryUid,
                    false, true, false)
            verify(impl).go(eq(VideoContentView.VIEW_NAME), any(), any(), any())
        }
    }

    @Test
    fun givenDownloadNotRequired_whenEntryNotDownloaded_thenOpenContent(){
        runBlocking {
            ContentEntryOpener(di, endpoint).openEntry(context, contentEntry.contentEntryUid,
                    false, true, false)
            verify(impl).go(eq(VideoContentView.VIEW_NAME), any(), any(), any())
        }
    }


    @Test
    fun givenDownloadNotRequired_whenEntryDownloadedAndMimeTypeDoesNotMatch_openInDefaultViewer(){
        runBlocking {

            var contentEntry = ContentEntry()
            contentEntry.contentEntryUid = umAppRepository.contentEntryDao.insert(contentEntry)

            var container = Container()
            container.containerContentEntryUid = contentEntry.contentEntryUid
            container.fileSize = 10
            container.mimeType = "video/wav"
            container.cntLastModified = System.currentTimeMillis()
            container.containerUid = umAppRepository.containerDao.insert(container)

            var containerEntryFile = ContainerEntryFile()
            containerEntryFile.cefPath = "hello"
            containerEntryFile.cefUid = umAppDatabase.containerEntryFileDao.insert(containerEntryFile)

            var containerEntry = ContainerEntry()
            containerEntry.ceContainerUid = container.containerUid
            containerEntry.ceCefUid = containerEntryFile.cefUid
            containerEntry.ceUid = umAppDatabase.containerEntryDao.insert(containerEntry)

            var dj = ContentJobItem()
            dj.cjiContainerUid = container.containerUid
            dj.cjiContentEntryUid = contentEntry.contentEntryUid
            dj.cjiRecursiveStatus = JobStatus.COMPLETE
            umAppDatabase.contentJobItemDao.insertJobItem(dj)

            ContentEntryOpener(di, endpoint).openEntry(context, contentEntry.contentEntryUid,
                    false, true, false)
            verify(impl).openFileInDefaultViewer(eq(context), any(), eq("video/wav"),
                anyOrNull())
        }

    }

}