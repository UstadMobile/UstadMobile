package com.ustadmobile.core.controller

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin.Companion.CONTENT_ENTRY
import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.core.view.ContentEditorView.Companion.CONTENT_ENTRY_UID
import com.ustadmobile.core.view.ContentEditorView.Companion.CONTENT_STORAGE_OPTION
import com.ustadmobile.core.view.ContentEntryEditView
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_FOLDER
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_FILE
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_OTHER
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentEntryStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class ContentEntryEditPresenter(context: Any, arguments: Map<String, String?>, view: ContentEntryEditView)
    : UstadBaseController<ContentEntryEditView>(context, arguments, view) {

    val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    private val appDb : UmAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)

    private var contentEntry: ContentEntry = ContentEntry()

    private var args = HashMap(arguments)

    private var author: String = ""

    private var selectedFilePath: String = ""

    private var selectedStorageOption: String = ""

    private var isNewContent: Boolean = true

    private var importedContent =  HashMap<String, Any?>()

    private val licenceTypes: List<String> = mutableListOf("CC BY","CC BY SA","CC BY SA NC",
            "CC BY NC","CC BY NC SA","Public Domain","All Rights Reserved","Other")

    private val licenceIds: List<Int> = mutableListOf(ContentEntry.LICENSE_TYPE_CC_BY, ContentEntry.LICENSE_TYPE_CC_BY_SA,
            ContentEntry.LICENSE_TYPE_CC_BY_SA_NC, ContentEntry.LICENSE_TYPE_CC_BY_NC, ContentEntry.ALL_RIGHTS_RESERVED,
            ContentEntry.LICESNE_TYPE_CC_BY_NC_SA, ContentEntry.PUBLIC_DOMAIN,LICENSE_TYPE_OTHER)

    val contentType = arguments.getValue(ContentEntryEditView.CONTENT_TYPE)?.toInt()

    private var isImportedContent: Boolean = contentType == CONTENT_IMPORT_FILE

    override fun onCreate(savedState: Map<String, String?> ?) {
        super.onCreate(savedState)

        GlobalScope.launch {
            val entry = appDb.contentEntryDao.findByEntryId(arguments.getValue(
                    ContentEditorView.CONTENT_ENTRY_UID)!!.toLong())
            contentEntry = entry ?: ContentEntry()
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
            view.showFileSelector(contentEntry.imported or (contentType == CONTENT_IMPORT_FILE))
            view.updateFileBtnLabel(impl.getString(MessageID.content_entry_label_select_file, context))
            view.showStorageOptions(contentType != CONTENT_CREATE_FOLDER)
            if(contentEntry.contentEntryUid != 0L){
                author = contentEntry.author!!
                isNewContent = false
                updateUi(contentEntry)
            }
        })
    }
    private fun updateUi(contentEntry: ContentEntry){
        author = contentEntry.author!!
        view.runOnUiThread(Runnable {
            view.setDescription(contentEntry.description ?: "")
            view.setEntryTitle(contentEntry.title ?: "")
            view.setThumbnail(contentEntry.thumbnailUrl)
            view.showErrorMessage("",false)
            view.updateFileBtnLabel(impl.getString(MessageID.content_entry_label_update_file, context))
        })
    }

    fun handleStorageOptionChange(storageOption: String){
        selectedStorageOption = storageOption
    }

    fun handleSaveUpdateEntry(title: String, description: String, thumbnailUrl: String, licence: Int){
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
            contentEntry.description = description
            contentEntry.licenseType = licenceIds[licence]
            contentEntry.thumbnailUrl = thumbnailUrl
            contentEntry.imported = isImportedContent

            if(isNewContent){
                contentEntry.leaf = isLeaf
                contentEntry.contentEntryUid = appDb.contentEntryDao.insert(contentEntry)

                val contentEntryJoin = ContentEntryParentChildJoin()
                contentEntryJoin.cepcjChildContentEntryUid = contentEntry.contentEntryUid
                contentEntryJoin.cepcjParentContentEntryUid =
                        arguments[ARG_CONTENT_ENTRY_UID]?.toLong()!!
                contentEntryJoin.cepcjUid =
                        appDb.contentEntryParentChildJoinDao.insert(contentEntryJoin)

                val status =  ContentEntryStatus(contentEntry.contentEntryUid, true, 0)
                status.downloadStatus = JobStatus.COMPLETE
                status.cesLeaf = isLeaf
                status.cesUid = appDb.contentEntryStatusDao.insert(status)
            }else{
                contentEntry.contentEntryUid = contentEntry.contentEntryUid
                appDb.contentEntryDao.update(contentEntry)
            }

            when(contentType){
                CONTENT_IMPORT_FILE -> view.runOnUiThread(Runnable {
                    if(isNewContent){
                        importedContent[CONTENT_ENTRY] = contentEntry

                        view.runOnUiThread(Runnable {
                            view.showProgressDialog()
                        })

                        view.importContent(importedContent)
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
                    args[CONTENT_ENTRY_UID] =
                            contentEntry.contentEntryUid.toString()
                    args[CONTENT_STORAGE_OPTION] =
                            getSelectedStorageOption()
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


    fun handleImportedFile(newContentEntry: ContentEntry?, fileSize: Long){
        val message = impl.getString(MessageID.content_import_success_message,context)

        if(newContentEntry != null){
            val contentJoin =  ContentEntryParentChildJoin()
            contentJoin.cepcjParentContentEntryUid = contentEntry.contentEntryUid
            contentJoin.cepcjChildContentEntryUid = newContentEntry.contentEntryUid

            if(isNewContent)
                appDb.contentEntryParentChildJoinDao.insert(contentJoin)
            else
                appDb.contentEntryParentChildJoinDao.update(contentJoin)
        }

        val status =  ContentEntryStatus(newContentEntry?.contentEntryUid!!,
                true, fileSize)
        status.downloadStatus = JobStatus.COMPLETE
        status.cesLeaf = true
        appDb.contentEntryStatusDao.update(status)

        view.runOnUiThread(Runnable {
            view.showMessageAndDismissDialog(message, false)})
    }

    fun handleSelectedFileToImport(content: HashMap<String, Any?>){
        val contentEntry = if (content.containsKey(CONTENT_ENTRY))
            content[CONTENT_ENTRY] as ContentEntry? else null
        if(contentEntry == null){
            view.showErrorMessage("*" + impl.getString(
                    MessageID.content_import_failure_message, context), true)
        }else{
            importedContent.putAll(content)
            updateUi(contentEntry)
        }
    }

    fun handleSelectedFilePath(filePath: String){
        selectedFilePath = filePath
    }

    fun handleAddThumbnail(){
        view.showAddThumbnailMessage()
    }


    fun getSelectedStorageOption(): String{
        return selectedStorageOption
    }

}
