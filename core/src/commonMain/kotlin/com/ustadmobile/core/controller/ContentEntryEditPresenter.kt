package com.ustadmobile.core.controller

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.db.dao.ContentEntryStatusDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.core.view.ContentEditorView.Companion.CONTENT_STORAGE_OPTION
import com.ustadmobile.core.view.ContentEntryEditView
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.ContentEntryImportLinkView.Companion.CONTENT_ENTRY_PARENT_UID
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_FOLDER
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_FILE
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.FLAG_CONTENT_EDITOR
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.FLAG_IMPORTED
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_OTHER
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentEntryStatus
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class ContentEntryEditPresenter(context: Any, arguments: Map<String, String?>, view: ContentEntryEditView,
                                private val contentEntryDao: ContentEntryDao,
                                private val contentEntryParentChildJoinDao: ContentEntryParentChildJoinDao,
                                private val contentEntryStatusDao: ContentEntryStatusDao,
                                private val account: UmAccount, val impl: UstadMobileSystemImpl,
                                val importFileFn: suspend (baseDir: String, mimeType: String, entry: ContentEntry) -> ContentEntry)
    : UstadBaseController<ContentEntryEditView>(context, arguments, view) {


    private var contentEntry: ContentEntry = ContentEntry()

    private var args = HashMap(arguments)

    private var author = ""

    private var selectedFilePath  = ""

    private var selectedFileMimeType  = ""

    private var selectedFileSize: Long = 0

    private var selectedStorageOption = ""

    private var isNewContent = true

    private var importedEntry = false

    private val licenceTypes: List<String> = mutableListOf("CC BY","CC BY SA","CC BY SA NC",
            "CC BY NC","CC BY NC SA","Public Domain","All Rights Reserved","Other")

    private val licenceIds: List<Int> = mutableListOf(ContentEntry.LICENSE_TYPE_CC_BY, ContentEntry.LICENSE_TYPE_CC_BY_SA,
            ContentEntry.LICENSE_TYPE_CC_BY_SA_NC, ContentEntry.LICENSE_TYPE_CC_BY_NC, ContentEntry.ALL_RIGHTS_RESERVED,
            ContentEntry.LICENSE_TYPE_CC_BY_NC_SA, ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN,LICENSE_TYPE_OTHER)

    private val contentType = arguments.getValue(ContentEntryEditView.CONTENT_TYPE)?.toInt()

    private var isImportedContent: Boolean = contentType == CONTENT_IMPORT_FILE

    override fun onCreate(savedState: Map<String, String?> ?) {
        super.onCreate(savedState)

        GlobalScope.launch {
            val entry = contentEntryDao.findByEntryId(arguments.getValue(ARG_CONTENT_ENTRY_UID)?.toLong()?: 0)
            contentEntry = entry ?: ContentEntry()
            importedEntry = (contentEntry.contentFlags and FLAG_IMPORTED) == FLAG_IMPORTED || contentType == CONTENT_IMPORT_FILE

            impl.getStorageDirs(context, object : UmResultCallback<List<UMStorageDir>> {
                override fun onDone(result: List<UMStorageDir>?) {
                    view.runOnUiThread(Runnable {
                        selectedStorageOption = result!![0].dirURI.toString()
                        view.setUpStorageOption(result)
                        setUpUi()
                    })
                }
            })
        }

    }


    private fun setUpUi(){
        view.runOnUiThread(Runnable {
            view.setUpLicence(licenceTypes,if(contentEntry.contentEntryUid == 0L) 0
            else licenceIds.indexOf(contentEntry.licenseType))
            view.showFileSelector(importedEntry)
            view.updateFileBtnLabel(impl.getString(MessageID.content_entry_label_select_file, context))
            view.showStorageOptions(contentType != CONTENT_CREATE_FOLDER)
            if(contentEntry.contentEntryUid != 0L){
                author = contentEntry.author?: ""
                isNewContent = false
                updateUi(contentEntry)
            }
        })
    }
    private fun updateUi(contentEntry: ContentEntry){
        author = contentEntry.author?: ""
        view.runOnUiThread(Runnable {
            view.setContentEntry(contentEntry)
            view.showErrorMessage("",false)
            view.updateFileBtnLabel(impl.getString(MessageID.content_entry_label_update_content, context))
        })
    }

    fun handleStorageOptionChange(storageOption: String){
        selectedStorageOption = storageOption
    }

    fun handleSaveUpdateEntry(title: String, description: String, thumbnailUrl: String, licence: Int, isActive: Boolean, isPublic: Boolean){
        if(title.isNotEmpty() and description.isNotEmpty()){

            if((contentType == CONTENT_IMPORT_FILE) and selectedFilePath.isEmpty()
                    and isNewContent){
                view.showErrorMessage("*" + impl.getString(
                        MessageID.content_creation_file_required_error, context), true)
                return
            }

            val isLeaf = arguments.getValue(ContentEntryEditView.CONTENT_ENTRY_LEAF)!!.toBoolean()
            contentEntry.title = title
            contentEntry.author = author
            contentEntry.publik = isPublic
            contentEntry.ceInactive = !isActive
            contentEntry.description = description
            contentEntry.licenseType = licenceIds[licence]
            contentEntry.thumbnailUrl = thumbnailUrl

            if(isNewContent){
                contentEntry.contentFlags = if(isImportedContent) FLAG_IMPORTED else FLAG_CONTENT_EDITOR
                contentEntry.leaf = isLeaf
                contentEntry.author = if(author.isEmpty()) account.username else author
                contentEntry.contentEntryUid = contentEntryDao.insert(contentEntry)

                val contentEntryJoin = ContentEntryParentChildJoin()
                contentEntryJoin.cepcjChildContentEntryUid = contentEntry.contentEntryUid
                contentEntryJoin.cepcjParentContentEntryUid =
                        arguments[CONTENT_ENTRY_PARENT_UID]?.toLong()!!
                contentEntryJoin.cepcjUid = contentEntryParentChildJoinDao.insert(contentEntryJoin)
            }else{
                contentEntry.contentEntryUid = contentEntry.contentEntryUid
                contentEntryDao.update(contentEntry)
            }

            when(contentType){
                CONTENT_IMPORT_FILE -> view.runOnUiThread(Runnable {
                    if(isNewContent){

                        view.runOnUiThread(Runnable {
                            view.showProgressDialog()
                        })

                        GlobalScope.launch {

                            val contentEntry =  importFileFn(selectedStorageOption, selectedFileMimeType, contentEntry)
                            handleImportedFile(contentEntry)
                        }
                    } else
                        view.showMessageAndDismissDialog(impl.getString(
                                MessageID.content_update_massage,context), true)
                })

                CONTENT_CREATE_FOLDER -> view.runOnUiThread(Runnable {
                    view.showMessageAndDismissDialog(impl.getString(
                            if(isNewContent) MessageID.content_creation_folder_new_message
                            else MessageID.content_creation_folder_update_message, context),
                            false)})
                CONTENT_CREATE_CONTENT -> {
                    view.runOnUiThread(Runnable { view.dismissDialog() })
                    args[ARG_CONTENT_ENTRY_UID] =
                            contentEntry.contentEntryUid.toString()
                    args[CONTENT_STORAGE_OPTION] = selectedStorageOption
                    if(isNewContent)
                        impl.go(ContentEditorView.VIEW_NAME, args, context)
                    else
                        view.runOnUiThread(Runnable {
                            view.showMessageAndDismissDialog(null,true) })

                }
            }
        }else{
            view.runOnUiThread(Runnable {view.showErrorMessage("*" +
                    impl.getString(MessageID.content_creation_empty_field_error, context),true)})
        }
    }


    fun handleSelectedFile(filePath: String, fileSize: Long,fileMimeType: String?, contentEntry: ContentEntry?){
        selectedFilePath = filePath; selectedFileSize = fileSize

        if(contentEntry == null){
            view.showErrorMessage("*" + impl.getString(MessageID.content_import_failure_message, context), true)
        }else{
            if(fileMimeType != null){
                selectedFileMimeType = fileMimeType
            }
            updateUi(contentEntry)
        }

    }

    private fun handleImportedFile(newContentEntry: ContentEntry?){
        val message = impl.getString(MessageID.content_import_success_message,context)

        if(newContentEntry != null){
            val contentJoin =  ContentEntryParentChildJoin()
            contentJoin.cepcjParentContentEntryUid = contentEntry.contentEntryUid
            contentJoin.cepcjChildContentEntryUid = newContentEntry.contentEntryUid

            if(isNewContent)
                contentEntryParentChildJoinDao.insert(contentJoin)
            else
                contentEntryParentChildJoinDao.update(contentJoin)
        }

        val status =  ContentEntryStatus(newContentEntry?.contentEntryUid!!,
                true, selectedFileSize)
        status.downloadStatus = JobStatus.COMPLETE
        status.cesLeaf = true
        contentEntryStatusDao.update(status)

        view.runOnUiThread(Runnable {
            view.showMessageAndDismissDialog(message, false)})
    }


    fun handleAddThumbnail(){
        view.showAddThumbnailMessage()
    }


    fun handleUpdateLink() {
        val args = HashMap<String, String?>()
        args[ARG_CONTENT_ENTRY_UID]  =  contentEntry.contentEntryUid.toString()
        args[CONTENT_ENTRY_PARENT_UID] = contentEntryParentChildJoinDao.
                findParentByChildUuids(contentEntry.contentEntryUid)!!.cepcjParentContentEntryUid.toString()
        impl.go(ContentEntryImportLinkView.VIEW_NAME, args, context)

    }

    fun handleContentButton() {
        if(isNewContent){
            view.startBrowseFiles()
        }else{
            view.showUpdateContentDialog(impl.getString(MessageID.content_entry_label_update_content, context),
                            listOf(impl.getString(MessageID.content_from_file, context), impl.getString(MessageID.content_from_link, context)))
        }
    }
}
