package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryEditView
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_FILE
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.Test

class ContentEntryEditPresenterTest {

    private val context = Any()

    private var mockView: ContentEntryEditView = mock()

    private lateinit var umAppDatabase: UmAppDatabase

    private val umAccount: UmAccount = UmAccount(0,"","","")

    private lateinit var presenter: ContentEntryEditPresenter

    private lateinit var contentEntry: ContentEntry

    private val dummyFilePath = "dummyPath/dummyFile.file"

    private val impl :UstadMobileSystemImpl = mock ()

    private val arguments =mapOf(
            ContentEntryEditView.CONTENT_ENTRY_LEAF to "true",
            ContentEntryDetailPresenter.ARG_CONTENT_ENTRY_UID to 1.toString(),
            ContentEntryEditView.CONTENT_TYPE  to CONTENT_IMPORT_FILE.toString())

    @Before
    fun setUp() {
        checkJndiSetup()
        umAppDatabase = UmAppDatabase.getInstance(context)
        contentEntry = ContentEntry("","", leaf = true, publik = true)
        contentEntry.contentEntryUid = 1L
        val list = mutableListOf(UMStorageDir("","", removableMedia = false, isAvailable = false, isUserSpecific = false))

        doAnswer {
            (it.getArgument(1) as UmCallback<List<UMStorageDir>>).onSuccess(list)
        }.`when`(impl).getStorageDirs(any(), any())

        doAnswer {
            "Dummy"
        }.`when`(impl).getAppPref(any(), any())

        doAnswer {
            "Dummy"
        }.`when`(impl).getString(any(), any())

        doAnswer {
            Thread(it.arguments[0] as Runnable).run()
            return@doAnswer // or you can type return@doAnswer null ​
        }.`when`(mockView).runOnUiThread(any())
    }


    @Test
    fun givenFileImport_WhenFileIsSelected_thenShouldUpdateUi(){
        presenter = ContentEntryEditPresenter(context,arguments ,mockView,umAppDatabase.contentEntryDao,
                umAppDatabase.contentEntryParentChildJoinDao, umAppDatabase.contentEntryStatusDao,umAccount, impl){
            dir: String,mimetype: String, entry: ContentEntry -> return@ContentEntryEditPresenter contentEntry
        }

        presenter.onCreate(null)

        presenter.handleSelectedFile(dummyFilePath,0L,"", contentEntry)

        verify(mockView).setContentEntry(any())
    }

    @Test
    fun givenValidFile_whenSelected_shouldSetEntryProperties(){
        presenter = ContentEntryEditPresenter(context, arguments,
                mockView,umAppDatabase.contentEntryDao,umAppDatabase.contentEntryParentChildJoinDao,
                umAppDatabase.contentEntryStatusDao,umAccount, impl){
            dir: String,mimetype: String, entry: ContentEntry -> return@ContentEntryEditPresenter contentEntry
        }

        presenter.onCreate(null)

        presenter.handleSelectedFile(dummyFilePath,0L,"", ContentEntry("","", leaf = true, publik = true))

        verify(mockView).setContentEntry(any())
    }


    @Test
    fun givenValidFileSelected_whenDoneClicked_shouldShowProgressDialogAndDismissItWhenDoneSaving(){
        presenter = ContentEntryEditPresenter(context, arguments,
                mockView,umAppDatabase.contentEntryDao,umAppDatabase.contentEntryParentChildJoinDao,
                umAppDatabase.contentEntryStatusDao,umAccount, impl){
            dir: String,mimetype: String, entry: ContentEntry -> return@ContentEntryEditPresenter contentEntry
        }

        presenter.onCreate(null)

        presenter.handleSelectedFile(dummyFilePath,0L,"", ContentEntry("","", leaf = true, publik = true))

        presenter.handleSaveUpdateEntry("Dummy","Dummy",dummyFilePath,1)

        verify(mockView).showProgressDialog()

        verify(mockView).showMessageAndDismissDialog(any(), any())
    }


}