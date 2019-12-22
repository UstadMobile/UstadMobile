package com.ustadmobile.core.util

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.checkJndiSetup
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class TestGoToContentEntryUtil {

    private lateinit var umAppDatabase: UmAppDatabase

    private lateinit var umAppRepository: UmAppDatabase

    private lateinit var contentEntry: ContentEntry

    private lateinit var impl: UstadMobileSystemImpl

    private val context = mock<DoorLifecycleOwner>() {
        on { currentState }.thenReturn(DoorLifecycleObserver.STARTED)
    } as Any

    @Before
    fun setUp() {
        checkJndiSetup()
        umAppDatabase = UmAppDatabase.getInstance(context)
        umAppRepository = umAppDatabase //for this test there is no difference
        impl = spy()
        UstadMobileSystemImpl.instance = impl

        contentEntry = ContentEntry()
        contentEntry.contentEntryUid = umAppDatabase.contentEntryDao.insert(contentEntry)

        var container = Container()
        container.containerContentEntryUid = contentEntry.contentEntryUid
        container.fileSize = 10
        container.mimeType = "video/mp4"
        container.cntLastModified = System.currentTimeMillis()
        container.containerUid = umAppDatabase.containerDao.insert(container)

        var dj = DownloadJobItem()
        dj.djiContainerUid = container.containerUid
        dj.djiContentEntryUid = contentEntry.contentEntryUid
        dj.djiStatus = JobStatus.COMPLETE
        umAppDatabase.downloadJobItemDao.insert(dj)
    }

    @Test
    fun givenDownloadRequired_whenEntryDownloaded_thenOpenContent() {
        runBlocking {

            goToContentEntry(contentEntry.contentEntryUid, umAppDatabase, context, impl,
                    true, false, false)
            verify(impl).go(eq(VideoPlayerView.VIEW_NAME), any(), any())
        }
    }

    @Test
    fun givenDownloadedRequired_whenEntryNotDownloaded_thenGotToDetailScreen(){
        runBlocking {

            var contentEntry = ContentEntry()
            contentEntry.contentEntryUid = umAppDatabase.contentEntryDao.insert(contentEntry)

            var container = Container()
            container.containerContentEntryUid = contentEntry.contentEntryUid
            container.fileSize = 10
            container.mimeType = "video/mp4"
            container.cntLastModified = System.currentTimeMillis()
            container.containerUid = umAppDatabase.containerDao.insert(container)

            var dj = DownloadJobItem()
            dj.djiContainerUid = container.containerUid
            dj.djiContentEntryUid = contentEntry.contentEntryUid
            dj.djiStatus = JobStatus.STARTING
            umAppDatabase.downloadJobItemDao.insert(dj)

            goToContentEntry(contentEntry.contentEntryUid, umAppDatabase, context, impl,
                    true, true, false)
            verify(impl).go(eq(ContentEntryDetailView.VIEW_NAME), any(), eq(context))
        }
    }


    @Test
    fun givenDownloadNotRequired_whenEntryDownloaded_thenOpenContent(){
        runBlocking {
            goToContentEntry(contentEntry.contentEntryUid, umAppDatabase, context, impl,
                    false, false, false)
            verify(impl).go(eq(VideoPlayerView.VIEW_NAME), any(), any())
        }
    }

    @Test
    fun givenDownloadNotRequired_whenEntryNotDownloaded_thenOpenContent(){
        runBlocking {
            goToContentEntry(contentEntry.contentEntryUid, umAppDatabase, context, impl,
                    false, false, false)
            verify(impl).go(eq(VideoPlayerView.VIEW_NAME), any(), any())
        }
    }

    @Test
    fun givenDownloadNotRequired_whenEntryDownloadedAndMimeTypeDoesNotMatch_openInDefaultViewer(){
        runBlocking {

            var contentEntry = ContentEntry()
            contentEntry.contentEntryUid = umAppDatabase.contentEntryDao.insert(contentEntry)

            var container = Container()
            container.containerContentEntryUid = contentEntry.contentEntryUid
            container.fileSize = 10
            container.mimeType = "video/wav"
            container.cntLastModified = System.currentTimeMillis()
            container.containerUid = umAppDatabase.containerDao.insert(container)

            var containerEntryFile = ContainerEntryFile()
            containerEntryFile.cefPath = "hello"
            containerEntryFile.cefUid = umAppDatabase.containerEntryFileDao.insert(containerEntryFile)

            var containerEntry = ContainerEntry()
            containerEntry.ceContainerUid = container.containerUid
            containerEntry.ceCefUid = containerEntryFile.cefUid
            containerEntry.ceUid = umAppDatabase.containerEntryDao.insert(containerEntry)

            var dj = DownloadJobItem()
            dj.djiContainerUid = container.containerUid
            dj.djiContentEntryUid = contentEntry.contentEntryUid
            dj.djiStatus = JobStatus.COMPLETE
            umAppDatabase.downloadJobItemDao.insert(dj)

            goToContentEntry(contentEntry.contentEntryUid, umAppDatabase, context, impl,
                    false, true, false)
            verify(impl).openFileInDefaultViewer(eq(context), any(), eq("video/wav"))

        }

    }






}