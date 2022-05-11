package com.ustadmobile.core.controller

import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.door.ext.waitUntilWithTimeout
import com.ustadmobile.door.getFirstValue
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.Language
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*
import org.mockito.kotlin.*


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

    private lateinit var contentEntry: ContentEntryWithBlockAndLanguage

    private val parentUid: Long = 12345678L

    private val timeoutInMill: Long = 5000

    private lateinit var mockEntryDao: ContentEntryDao

    private val errorMessage: String = "Dummy error"

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var di: DI

    private lateinit var contentPluginManager: ContentPluginManager

    private lateinit var contentJobManager: ContentJobManager

    private val metadataResult =  MetadataResult(ContentEntryWithLanguage(), TestPlugin.PLUGIN_ID)

    private val storageDir = createTemporaryDir("container").toFile()


    @Before
    fun setUp() {
        context = Any()
        container = createMockContainer()
        contentEntry = createMockEntryWithLanguage()
        mockLifecycleOwner = mock { }
        contentJobManager = mock { }
        contentPluginManager = mock {
            onBlocking { extractMetadata(any(), any()) }.thenAnswer {
                    metadataResult
            }
        }
        systemImpl = mock {
            on { getString(any(), any()) }.thenAnswer { errorMessage }
        }


        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadMobileSystemImpl>(overrides = true) with singleton { systemImpl }
            bind<ContainerStorageManager>() with scoped(ustadTestRule.endpointScope).singleton {
                ContainerStorageManager(listOf(storageDir))
            }
            bind<ContentPluginManager>() with scoped(ustadTestRule.endpointScope).singleton {
                contentPluginManager
            }
            bind<ContentJobManager>() with singleton {
                contentJobManager
            }
        }

        db = di.directActiveDbInstance()
        repo = di.directActiveRepoInstance()

        val systemImpl: UstadMobileSystemImpl by di.instance()


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
            on { compressionEnabled }.thenAnswer{ true }
            on { videoDimensions }.thenAnswer{ Pair(0,0) }
            on { selectedStorageIndex }.thenAnswer { 0 }
            on { storageOptions }.thenAnswer { di.onActiveAccountDirect().instance<ContainerStorageManager>().storageList }
            on { metadataResult }.thenAnswer {
                if (isUriNull) null else
                    metadataResult
            }
        }
    }

    private fun createMockEntryWithLanguage(): ContentEntryWithBlockAndLanguage {
        val language = Language()
        language.iso_639_2_standard = "en"
        language.langUid = 23
        val content = ContentEntryWithBlockAndLanguage()
        content.title = "Dummy Title"
        content.description = "Dummy description"
        content.licenseName = "Dummy Licence Name"
        content.sourceUrl = "content://dummy"
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
        val presenter = ContentEntryEdit2Presenter(context,
            mapOf(UstadView.ARG_PARENT_ENTRY_UID to parentUid.toString()), mockView,
            mockLifecycleOwner, di)

        presenter.onCreate(null)
        mockView.captureLastEntityValue()

        runBlocking{
            presenter.handleClickSave(contentEntry)

            repo.contentEntryDao.findAllLive().waitUntilWithTimeout(5000) {
                it.size == 1
            }

            val entry = repo.contentEntryDao.findAllLive().getFirstValue().first()
            assertEquals("Got expected content entry title", contentEntry.title, entry.title)

        }


        verifyBlocking(contentJobManager, timeout(timeoutInMill)) {
            enqueueContentJob(any(), any())
        }

    }


    @Test
    fun givenPresenterCreatedAndFolderNotCreated_whenClickSave_shouldCreateAFolder() {
        createMockView()
        contentEntry.leaf = false
        val presenter = ContentEntryEdit2Presenter(context, mapOf(UstadView.ARG_PARENT_ENTRY_UID to parentUid.toString()), mockView, mockLifecycleOwner, di)

        whenever(mockView.metadataResult).thenReturn(null)

        presenter.onCreate(null)
        mockView.captureLastEntityValue()
        presenter.handleClickSave(contentEntry)

        runBlocking {
            repo.contentEntryDao.findAllLive().waitUntilWithTimeout(5000) {
                it.size == 1
            }

            val entry = repo.contentEntryDao.findAllLive().getFirstValue().first()
            assertEquals("Got expected folder title", contentEntry.title, entry.title)

        }

        verifyBlocking(contentJobManager, times(0)) {
            enqueueContentJob(any(), any())
        }
    }


    @Test
    fun givenPresenterCreatedAndEntryCreated_whenClickSave_shouldUpdateAnEntry() {
        createMockView()
        contentEntry.contentEntryUid = repo.contentEntryDao.insert(contentEntry)
        val presenter = ContentEntryEdit2Presenter(context,
                mapOf(ARG_ENTITY_UID to contentEntry.contentEntryUid.toString(),
                        UstadView.ARG_PARENT_ENTRY_UID to parentUid.toString()), mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)
        val entrySetOnView = mockView.captureLastEntityValue()
        runBlocking{
            entrySetOnView!!.title = "Updated Title"
            presenter.handleClickSave(entrySetOnView)


            repo.contentEntryDao.findAllLive().waitUntilWithTimeout(5000) {
                it.size == 1
            }

            val entry = repo.contentEntryDao.findAllLive().getFirstValue().first()
            assertEquals("Got expected content entry title", "Updated Title", entry.title)

        }

        verifyBlocking(contentJobManager, timeout(timeoutInMill)) {
            enqueueContentJob(any(), any())
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
