package com.ustadmobile.core.controller

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.core.view.ContentEntryDetailView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.ContentEntryEditView
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_FOLDER
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_FILE
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentEntryStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class ContentEntryListPresenter (context: Any, arguments: Map<String, String>?, view: ContentEntryListView)
    : UstadBaseController<ContentEntryListView>(context, arguments!!, view)  {

    private var contentEntryUid = 0L

    val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        view.runOnUiThread(Runnable{
            view.showCreateContentOption(true)
        })
        contentEntryUid = arguments.getValue(ARG_CONTENT_ENTRY_UID)!!.toLong()
    }


    fun handleBackNavigation(){
        view.runOnUiThread(Runnable{
            view.navigateBack()
        })
    }


    fun handleImportedFile(contentEntry: ContentEntry?, fileSize: Long){
        GlobalScope.launch {
            if(contentEntry != null){
                val appDatabase = UmAppDatabase.getInstance(context)
                val contentJoin =  ContentEntryParentChildJoin()
                contentJoin.cepcjParentContentEntryUid = contentEntryUid
                contentJoin.cepcjChildContentEntryUid = contentEntry.contentEntryUid
                appDatabase.contentEntryParentChildJoinDao.insert(contentJoin)

                val status =  ContentEntryStatus(contentEntry.contentEntryUid,
                        true, fileSize)
                status.downloadStatus = JobStatus.COMPLETE
                status.cesLeaf = true
                status.cesUid = appDatabase.contentEntryStatusDao.insert(status)

            }
            val message = impl.getString(if(contentEntry == null)
                MessageID.content_import_failure_message else
                MessageID.content_import_success_message,context)
            view.runOnUiThread(Runnable { view.showMessage(message) })
        }
    }

    fun handleContentCreation(contentType: Int, newContent: Boolean){
        val args = HashMap<String,String?>()
        args.putAll(arguments)
        args[ContentEditorView.CONTENT_ENTRY_UID] = (if(newContent) 0
        else contentEntryUid).toString()
        args[ContentEntryEditView.CONTENT_ENTRY_LEAF] = true.toString()
        args[ContentEntryEditView.CONTENT_TYPE] = contentType.toString()

        when(contentType){
            CONTENT_CREATE_FOLDER -> {
                args[ContentEntryEditView.CONTENT_ENTRY_LEAF] = false.toString()
                view.runOnUiThread(Runnable{view.createNewFolder(args)})
            }

            CONTENT_IMPORT_FILE -> {
                view.runOnUiThread(Runnable{view.startFileBrowser(args)})
            }

            CONTENT_CREATE_CONTENT -> {
                view.runOnUiThread(Runnable{view.createNewContent(args)})
            }
        }
    }
}