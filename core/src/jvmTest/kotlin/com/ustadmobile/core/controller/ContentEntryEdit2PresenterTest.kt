package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.UmAccount
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton


class ContentEntryEdit2PresenterTest  {

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: ContentEntryEdit2View

    private lateinit var activeAccount: DoorMutableLiveData<UmAccount?>

    private lateinit var context: Any

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var container: Container

    private lateinit var contentEntry: ContentEntryWithLanguage

    private lateinit var containerManager: ContainerDownloadManager

    private val parentUid: Long = 12345678L

    private val entryUid: Long = 100L

    private val timeoutInMill: Long = 500

    private lateinit var mockEntryDao:ContentEntryDao

    private val errorMessage: String = "Dummy error"

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var di: DI


    @Before
    fun setUp() {
        context = Any()
        container = createMockContainer()
        contentEntry = createMockEntryWithLanguage()
        mockLifecycleOwner = mock { }
        activeAccount = DoorMutableLiveData(UmAccount(42, "bobjones", "",
                "http://localhost"))
        val realDb = UmAppDatabase.getInstance(context)
        db =  spy(realDb) { }
        db.clearAllTables()

        mockEntryDao = spy{
            onBlocking { insertAsync(any()) }.thenReturn(entryUid)
        }

        repo = spy(realDb) {
            on{contentEntryDao}.thenAnswer{mockEntryDao}
        }

        containerManager = spy{}

        systemImpl = mock{
            on { getStorageDirs(any(), any()) }.thenAnswer {
                (it.getArgument(1) as UmResultCallback<List<UMStorageDir>>).onDone(
                        mutableListOf(UMStorageDir("", "", removableMedia = false,
                        isAvailable = false, isUserSpecific = false)))
            }
            on { getString(any(), any()) }.thenAnswer{errorMessage}
        }

        val mockAccountManager: UstadAccountManager = mock {

        }

        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { systemImpl }
            bind<UmAppDatabase>(tag = TAG_DB) with singleton { db }
            bind<UmAppDatabase>(tag = TAG_REPO) with singleton { repo }
            bind<UstadAccountManager>() with singleton { mockAccountManager }
        }

    }

    @After
    fun tearDown() {}
    private fun createMockView(isUriNull: Boolean = false){
        mockView = mock{
            onBlocking {
                saveContainerOnExit(entryUid, "", db, repo)}.thenAnswer{container}
            on {selectedStorageIndex}.thenAnswer {0}
            on{selectedFileUri}.thenAnswer{if(isUriNull) null else "Dummy Uri"}
        }
    }

    private fun createMockEntryWithLanguage(): ContentEntryWithLanguage{
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
                ,mockView,mockLifecycleOwner, di)

        presenter.onCreate(null)

        val initialEntry = mockView.captureLastEntityValue()

        presenter.handleClickSave(contentEntry)

        argumentCaptor<ContentEntryWithLanguage>().apply {
            verifyBlocking(mockEntryDao, timeout(5000)){
                insertAsync(capture())
            }
            assertEquals("Got expected content entry title",contentEntry.title, firstValue.title)
        }

        argumentCaptor<Long>().apply {
            verifyBlocking(mockView, timeout(timeoutInMill)){
                mockView.saveContainerOnExit(capture(), any(), eq(db), eq(repo))
            }
            assertEquals("Got expected content entry uid",entryUid, firstValue)
        }

    }


    @Test
    fun givenPresenterCreatedAndFolderNotCreated_whenClickSave_shouldCreateAFolder() {
        createMockView()
        contentEntry.leaf = false
        val presenter = ContentEntryEdit2Presenter(context, mapOf(UstadView.ARG_PARENT_ENTRY_UID to parentUid.toString())
                ,mockView,mockLifecycleOwner,di)

        presenter.onCreate(null)
        mockView.captureLastEntityValue()
        presenter.handleClickSave(contentEntry)

        argumentCaptor<ContentEntryWithLanguage>().apply {
            verifyBlocking(mockEntryDao, timeout(5000)){
                insertAsync(capture())
            }
            assertEquals("Got expected folder title",contentEntry.title, firstValue.title)
        }

        //verify that container was not created
        verifyBlocking(mockView, times(0)){
            mockView.saveContainerOnExit(any(), any(), eq(db), eq(repo))
        }
    }


    @Test
    fun givenPresenterCreatedAndEntryCreated_whenClickSave_shouldUpdateAnEntry() {
        createMockView()
        contentEntry.contentEntryUid = entryUid
        val presenter = ContentEntryEdit2Presenter(context, mapOf(UstadView.ARG_PARENT_ENTRY_UID to parentUid.toString())
                ,mockView,mockLifecycleOwner, di)

        presenter.onCreate(null)
        mockView.captureLastEntityValue()
        presenter.handleClickSave(contentEntry)

        argumentCaptor<ContentEntryWithLanguage>().apply {
            verifyBlocking(mockEntryDao, timeout(5000)){
                updateAsync(capture())
            }
            assertEquals("Got expected content entry title",contentEntry.title, firstValue.title)
        }

        argumentCaptor<Long>().apply {
            verifyBlocking(mockView){
                mockView.saveContainerOnExit(capture(), any(), eq(db), eq(repo))
            }
            assertEquals("Got expected content entry uid",entryUid, firstValue)
        }


    }

    @Test
    fun givenPresenterCreatedAndEntryTitleIsNotFilled_whenClickSave_shouldShowErrorMessage() {
        createMockView()
        contentEntry.title = null
        val presenter = ContentEntryEdit2Presenter(context, mapOf()
                ,mockView,mockLifecycleOwner, di)

        presenter.onCreate(null)
        mockView.captureLastEntityValue()
        presenter.handleClickSave(contentEntry)

        argumentCaptor<Boolean>().apply {
            verify(mockView, after(timeoutInMill).times(2)).titleErrorEnabled = capture()
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