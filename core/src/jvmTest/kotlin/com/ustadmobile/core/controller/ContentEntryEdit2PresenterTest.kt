package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.directActiveDbInstance
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.Language
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton


class ContentEntryEdit2PresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ContentEntryEdit2View

    private lateinit var context: Any

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var container: Container

    private lateinit var contentEntry: ContentEntryWithLanguage

    private lateinit var containerManager: ContainerDownloadManager

    private val parentUid: Long = 12345678L

    private val timeoutInMill: Long = 5000

    private lateinit var mockEntryDao: ContentEntryDao

    private val errorMessage: String = "Dummy error"

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var di: DI


    @Before
    fun setUp() {
        context = Any()
        container = createMockContainer()
        contentEntry = createMockEntryWithLanguage()
        mockLifecycleOwner = mock { }
        containerManager = spy {}

        systemImpl = mock {
            on { getStorageDirs(any(), any()) }.thenAnswer {
                (it.getArgument(1) as UmResultCallback<List<UMStorageDir>>).onDone(
                        mutableListOf(UMStorageDir("", "", removableMedia = false,
                                isAvailable = false, isUserSpecific = false)))
            }
            on { getString(any(), any()) }.thenAnswer { errorMessage }
        }


        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadMobileSystemImpl>(overrides = true) with singleton { systemImpl }
            bind<ContainerDownloadManager>() with singleton { containerManager }
        }

        db = di.directActiveDbInstance()
        repo = di.directActiveRepoInstance()

        val systemImpl: UstadMobileSystemImpl by di.instance()

        whenever(systemImpl.getStorageDirs(any(), any())).thenAnswer {
            (it.getArgument(1) as UmResultCallback<List<UMStorageDir>>).onDone(
                    mutableListOf(UMStorageDir("", "", removableMedia = false,
                            isAvailable = false, isUserSpecific = false)))
        }

        whenever(systemImpl.getString(any(), any())).thenReturn(errorMessage)

        val repo: UmAppDatabase by di.activeRepoInstance()
        mockEntryDao = spy(repo.contentEntryDao)
        whenever(repo.contentEntryDao).thenReturn(mockEntryDao)
    }

    @After
    fun tearDown() {
    }

    private fun createMockView(isUriNull: Boolean = false) {
        mockView = mock {
            onBlocking { saveContainerOnExit(any(), any(), any(), any()) }.thenAnswer { container }
            on { selectedStorageIndex }.thenAnswer { 0 }
            on { entryMetaData }.thenAnswer { if (isUriNull) null else
                ImportedContentEntryMetaData(ContentEntryWithLanguage(), "application/epub+zip", "file:/Dummy", 1)
            }
        }
    }

    private fun createMockEntryWithLanguage(): ContentEntryWithLanguage {
        val language = Language()
        language.iso_639_2_standard = "en"
        language.langUid = 23
        val content = ContentEntryWithLanguage()
        content.title = "Dummy Title"
        content.description = "Dummy description"
        content.licenseName = "Dummy Licence Name"
        content.licenseType = 1
        content.leaf = true
        content.language = language
        return content
    }

    private fun createMockContainer(): Container {
        val container = Container()
        container.containerUid = 90
        container.fileSize = 23459
        return container
    }


    @Test
    fun givenPresenterCreatedAndEntryNotCreated_whenClickSave_shouldCreateAnEntry() {
        createMockView()
        val presenter = ContentEntryEdit2Presenter(context, mapOf(UstadView.ARG_PARENT_ENTRY_UID to parentUid.toString())
                , mockView, mockLifecycleOwner, di)

        presenter.onCreate(null)

        val initialEntry = mockView.captureLastEntityValue()

        presenter.handleClickSave(contentEntry)

        argumentCaptor<ContentEntryWithLanguage>().apply {
            verifyBlocking(mockEntryDao, timeout(5000)) {
                insertAsync(capture())
            }
            assertEquals("Got expected content entry title", contentEntry.title, firstValue.title)
        }

        argumentCaptor<Long>().apply {
            verifyBlocking(mockView, timeout(timeoutInMill)) {
                mockView.saveContainerOnExit(capture(), any(), eq(db), eq(repo))
            }
            assertEquals("Got expected content entry uid", contentEntry.contentEntryUid, firstValue)
        }

    }


    @Test
    fun givenPresenterCreatedAndFolderNotCreated_whenClickSave_shouldCreateAFolder() {
        createMockView()
        contentEntry.leaf = false
        val presenter = ContentEntryEdit2Presenter(context, mapOf(UstadView.ARG_PARENT_ENTRY_UID to parentUid.toString())
                , mockView, mockLifecycleOwner, di)

        presenter.onCreate(null)
        mockView.captureLastEntityValue()
        presenter.handleClickSave(contentEntry)

        argumentCaptor<ContentEntryWithLanguage>().apply {
            verifyBlocking(mockEntryDao, timeout(5000)) {
                insertAsync(capture())
            }
            assertEquals("Got expected folder title", contentEntry.title, firstValue.title)
        }

        //verify that container was not created
        verifyBlocking(mockView, times(0)) {
            mockView.saveContainerOnExit(any(), any(), eq(db), eq(repo))
        }
    }


    @Test
    fun givenPresenterCreatedAndEntryCreated_whenClickSave_shouldUpdateAnEntry() {
        createMockView()
        contentEntry.contentEntryUid = db.contentEntryDao.insert(contentEntry)
        val presenter = ContentEntryEdit2Presenter(context,
                mapOf(ARG_ENTITY_UID to contentEntry.contentEntryUid.toString(),
                        UstadView.ARG_PARENT_ENTRY_UID to parentUid.toString())
                , mockView, mockLifecycleOwner, di)

        presenter.onCreate(null)
        val entrySetOnView = mockView.captureLastEntityValue()
        entrySetOnView!!.title = "Updated Title"
        presenter.handleClickSave(entrySetOnView)

        argumentCaptor<ContentEntryWithLanguage>().apply {
            verifyBlocking(mockEntryDao, timeout(5000)) {
                updateAsync(capture())
            }
            assertEquals("Got expected content entry title", "Updated Title", firstValue.title)
        }

        argumentCaptor<Long>().apply {
            verifyBlocking(mockView, timeout(5000)) {
                mockView.saveContainerOnExit(capture(), any(), eq(db), eq(repo))
            }

            assertEquals("Got expected content entry uid", contentEntry.contentEntryUid,
                    firstValue)
        }
    }

    @Test
    fun givenPresenterCreatedAndEntryTitleIsNotFilled_whenClickSave_shouldShowErrorMessage() {
        createMockView()
        val presenter = ContentEntryEdit2Presenter(context, mapOf(), mockView, mockLifecycleOwner, di)

        presenter.onCreate(null)
        val entityOnView = mockView.captureLastEntityValue()
        entityOnView!!.title = null
        presenter.handleClickSave(entityOnView)

        argumentCaptor<Boolean>().apply {
            verify(mockView, timeout(timeoutInMill).times(2)).titleErrorEnabled = capture()
            assertTrue("Got expected entry title error flag", secondValue)
        }
    }


    @Test
    fun givenPresenterCreatedAndEntryFileIsNotSelected_whenClickSave_shouldShowErrorMessage() {
        createMockView(true)
        val presenter = ContentEntryEdit2Presenter(context, mapOf(), mockView, mockLifecycleOwner, di)

        presenter.onCreate(null)
        mockView.captureLastEntityValue()
        presenter.handleClickSave(contentEntry)

        argumentCaptor<Boolean>().apply {
            verify(mockView, after(timeoutInMill).times(2)).fileImportErrorVisible = capture()
            assertTrue("Got expected file import error flag", secondValue)
        }
    }

}