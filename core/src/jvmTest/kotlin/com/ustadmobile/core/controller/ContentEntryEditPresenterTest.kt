package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.core.view.ContentEntryEditView
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_FILE
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.util.test.checkJndiSetup
import junit.framework.Assert.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Test

class ContentEntryEditPresenterTest {

    private val context = Any()

    private lateinit var mockView: ContentEntryEditView

    private lateinit var umAppDatabase: UmAppDatabase

    private val umAccount: UmAccount = UmAccount(0,"","","")

    private lateinit var presenter: ContentEntryEditPresenter

    private val leafContentEntry = ContentEntry("Dummy Title","Dummy Description", leaf = true, publik = true)

    private val dummyFilePath = "dummyPath/dummyFile.file"

    private lateinit var impl :UstadMobileSystemImpl

    private val entryUid = (1..10).random().toLong()



    private val arguments = mutableMapOf(
            ContentEntryEditView.CONTENT_ENTRY_LEAF to "true",
            ARG_CONTENT_ENTRY_UID to entryUid.toString(),
            ContentEntryEditView.CONTENT_TYPE  to CONTENT_IMPORT_FILE.toString())

    @Before
    fun setUp() {
        checkJndiSetup()
        umAppDatabase = UmAppDatabase.getInstance(context)
        umAppDatabase.clearAllTables()
        leafContentEntry.contentEntryUid = entryUid
        val list = mutableListOf(UMStorageDir("","", removableMedia = false, isAvailable = false, isUserSpecific = false))

        impl = mock {
            on {getStorageDirs(any(), any())}.thenAnswer {
                    (it.getArgument(1) as UmResultCallback<List<UMStorageDir>>).onDone(list) }

            on {getAppPref(any(), any())}.thenAnswer {""}

            on {getString(any(), any())}.thenAnswer {""}

            }

        mockView = mock{
            on {runOnUiThread(any())}.thenAnswer {Thread(it.getArgument(0) as Runnable).run()}
        }
    }


    @Test
    fun givenFileImport_WhenFileIsSelected_thenShouldUpdateUi(){
        presenter = ContentEntryEditPresenter(context,arguments ,mockView,umAppDatabase.contentEntryDao,
                umAppDatabase.contentEntryParentChildJoinDao, umAppDatabase.contentEntryStatusDao,umAccount, impl){
            dir: String,mimetype: String, entry: ContentEntry -> return@ContentEntryEditPresenter leafContentEntry
        }

        presenter.onCreate(null)

        presenter.handleSelectedFile(dummyFilePath,0L,"*", leafContentEntry)

        argumentCaptor<ContentEntry>().apply {
            verify(mockView).setContentEntry(capture())
            assertEquals(leafContentEntry.title, firstValue.title)
        }
    }


    @Test
    fun givenValidFile_whenSelected_shouldCreateEntryAndSetEntryProperties(){
        presenter = ContentEntryEditPresenter(context, arguments,
                mockView,umAppDatabase.contentEntryDao,umAppDatabase.contentEntryParentChildJoinDao,
                umAppDatabase.contentEntryStatusDao,umAccount, impl){
            dir: String,mimetype: String, entry: ContentEntry -> return@ContentEntryEditPresenter leafContentEntry
        }

        presenter.onCreate(null)

        presenter.handleSelectedFile(dummyFilePath,0L,"", leafContentEntry)

        verify(mockView).setContentEntry(any())
    }


    @Test
    fun givenValidFileSelected_whenDoneButtonClicked_shouldShowProgressDialogAndDismissItWhenDoneSaving(){
        presenter = ContentEntryEditPresenter(context, arguments,
                mockView,umAppDatabase.contentEntryDao,umAppDatabase.contentEntryParentChildJoinDao,
                umAppDatabase.contentEntryStatusDao,umAccount, impl){
            dir: String,mimetype: String, entry: ContentEntry -> return@ContentEntryEditPresenter leafContentEntry
        }

        presenter.onCreate(null)

        presenter.handleSelectedFile(dummyFilePath,0L,"", leafContentEntry)

        presenter.handleSaveUpdateEntry(leafContentEntry.title as String,leafContentEntry.description as String,dummyFilePath,
                1, leafContentEntry.ceInactive, leafContentEntry.publik)

        verify(mockView, timeout(5000)).showProgressDialog()

        verify(mockView, timeout(5000)).showMessageAndDismissDialog(any(), any())
    }

    @Test
    fun givenOptions_whenCreateFolderOptionIsSelected_shouldHideFileImportViews(){
        arguments[ContentEntryEditView.CONTENT_TYPE] = ContentEntryListView.CONTENT_CREATE_FOLDER.toString()
        presenter = ContentEntryEditPresenter(context, arguments,
                mockView,umAppDatabase.contentEntryDao,umAppDatabase.contentEntryParentChildJoinDao,
                umAppDatabase.contentEntryStatusDao,umAccount, impl){
            dir: String,mimetype: String, entry: ContentEntry -> return@ContentEntryEditPresenter leafContentEntry
        }

        presenter.onCreate(null)

        verify(mockView, timeout(5000)).showFileSelector(equals(false))

        verify(mockView, timeout(5000)).showStorageOptions(equals(false))

    }

    @Test
    fun givenOptions_whenCreateContentOptionIsSelected_shouldHideFileImportViews(){
        arguments[ContentEntryEditView.CONTENT_TYPE] = ContentEntryListView.CONTENT_CREATE_CONTENT.toString()
        presenter = ContentEntryEditPresenter(context, arguments,
                mockView,umAppDatabase.contentEntryDao,umAppDatabase.contentEntryParentChildJoinDao,
                umAppDatabase.contentEntryStatusDao,umAccount, impl){
            dir: String,mimetype: String, entry: ContentEntry -> return@ContentEntryEditPresenter leafContentEntry
        }

        presenter.onCreate(null)

        verify(mockView, timeout(5000)).showFileSelector(equals(false))
    }


    @Test
    fun givenOptions_whenImportFileOptionIsSelected_shouldShowFileImportViews(){
        presenter = ContentEntryEditPresenter(context, arguments,
                mockView,umAppDatabase.contentEntryDao,umAppDatabase.contentEntryParentChildJoinDao,
                umAppDatabase.contentEntryStatusDao,umAccount, impl){
            dir: String,mimetype: String, entry: ContentEntry -> return@ContentEntryEditPresenter leafContentEntry
        }

        presenter.onCreate(null)

        argumentCaptor<Boolean>().apply {
            verify(mockView, timeout(5000)).showFileSelector(capture())

            assertTrue("File selector view was shown", firstValue)
        }

        argumentCaptor<Boolean>().apply {
            verify(mockView, timeout(5000)).showStorageOptions(capture())

            assertTrue("Storage option view was shown", firstValue)
        }
    }



    @Test
    fun givenContentEntryExists_whenHandleFileSelectedAndHandleClickSave_thenShouldCreateNewContainerAndUpdateContentEntry(){
        GlobalScope.launch {
            umAppDatabase.contentEntryDao.insert(leafContentEntry)

            presenter = ContentEntryEditPresenter(context, arguments,
                    mockView,umAppDatabase.contentEntryDao,umAppDatabase.contentEntryParentChildJoinDao,
                    umAppDatabase.contentEntryStatusDao,umAccount, impl){
                dir: String,mimetype: String, entry: ContentEntry -> return@ContentEntryEditPresenter leafContentEntry
            }

            presenter.onCreate(null)

            leafContentEntry.title = "New Dummy title"
            presenter.handleSaveUpdateEntry(leafContentEntry.title as String,leafContentEntry.description as String,dummyFilePath,
                    1, leafContentEntry.ceInactive, leafContentEntry.publik)
            argumentCaptor<String>().apply {
                verify(mockView).showMessageAndDismissDialog(capture(), any())
                assertTrue("New container was created and entry was updated",
                        firstValue.contains("updated successfully"))

                val contentEntry = umAppDatabase.contentEntryDao.findByEntryId(leafContentEntry.contentEntryUid)

                assertNotNull("Entry was created and inserted in the Db",contentEntry)

                assertEquals("Entry was updated", contentEntry?.title, leafContentEntry.title)
            }

        }

    }

    @Test
    fun givenContentEntryNotExisting_whenHandleFileSelectedAndHandleClickSave_thenShouldCreateNewContentEntryAndNewContainer(){
        GlobalScope.launch {

            presenter = ContentEntryEditPresenter(context, arguments,
                    mockView,umAppDatabase.contentEntryDao,umAppDatabase.contentEntryParentChildJoinDao,
                    umAppDatabase.contentEntryStatusDao,umAccount, impl){
                dir: String,mimetype: String, entry: ContentEntry -> return@ContentEntryEditPresenter leafContentEntry
            }

            presenter.onCreate(null)

            presenter.handleSaveUpdateEntry(leafContentEntry.title as String,leafContentEntry.description as String,dummyFilePath,
                    1, leafContentEntry.ceInactive, leafContentEntry.publik)
            argumentCaptor<String>().apply {
                verify(mockView).showMessageAndDismissDialog(capture(), any())
                assertTrue("Container and entry was created",firstValue.contains("successfully imported a file"))

                val contentEntry = umAppDatabase.contentEntryDao.findByEntryId(leafContentEntry.contentEntryUid)

                assertNotNull("Entry was created and inserted in the Db",contentEntry)

                assertEquals("File was imported", contentEntry?.contentTypeFlag, ContentEntry.FLAG_IMPORTED)
            }

        }
    }


}