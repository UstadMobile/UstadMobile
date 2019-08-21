package com.ustadmobile.core.controller

import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.core.view.ContentEntryEditView
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_FOLDER
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_FILE
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_LINK
import kotlinx.coroutines.Runnable

class ContentEntryListPresenter(context: Any, arguments: Map<String, String>?, view: ContentEntryListView)
    : UstadBaseController<ContentEntryListView>(context, arguments!!, view) {

    private var contentEntryUid = 0L

    val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        contentEntryUid = arguments.getValue(ARG_CONTENT_ENTRY_UID)!!.toLong()
    }

    fun handleShowContentEditorOptios(show: Boolean) {
        view.runOnUiThread(Runnable {
            view.showCreateContentOption(show)
        })
    }


    fun handleBackNavigation() {
        view.runOnUiThread(Runnable {
            view.navigateBack()
        })
    }


    fun handleContentCreation(contentType: Int, newContent: Boolean) {
        val args = HashMap<String, String?>()
        args.putAll(arguments)
        args[ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID] = contentEntryUid.toString()
        args[ContentEditorView.CONTENT_ENTRY_UID] = (if (newContent) 0
        else contentEntryUid).toString()
        args[ContentEntryEditView.CONTENT_ENTRY_LEAF] = true.toString()
        args[ContentEntryEditView.CONTENT_TYPE] = contentType.toString()

        view.runOnUiThread(Runnable {
            when (contentType) {
                CONTENT_CREATE_FOLDER -> {
                    args[ContentEntryEditView.CONTENT_ENTRY_LEAF] = false.toString()
                    view.createNewFolder(args)
                }

                CONTENT_IMPORT_FILE -> {
                    view.startFileBrowser(args)
                }

                CONTENT_CREATE_CONTENT -> {
                   view.createNewContent(args)
                }
                CONTENT_IMPORT_LINK -> {
                    view.importContentFromLink(args)
                }
            }
        })

    }
}