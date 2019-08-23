package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEditorPageListView
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.core.view.ContentEntryEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class ContentEditorPageListPresenter(context: Any, arguments: Map<String, String>,
                                     view: ContentEditorPageListView, val contentEntryDao: ContentEntryDao,
                                     private val pageActionDelegate: ContentEditorPageActionDelegate)
    : UstadBaseController<ContentEditorPageListView>(context, arguments, view) {

    private val entryUuid = arguments.getValue(ContentEditorView.CONTENT_ENTRY_UID).toLong()

    private var entryLiveData: DoorLiveData<ContentEntry?>? = null

    private var selectedPage: String? = null

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        entryLiveData  = contentEntryDao.findLiveContentEntry(entryUuid)
        entryLiveData!!.observe(this, this::onEntryValueChanged)
    }


    private fun onEntryValueChanged(entry: ContentEntry?){
        if(entry != null){
            view.runOnUiThread(Runnable { view.setDocumentTitle(entry.title!!)})
            setUpOrUpdateDocumentPages()
        }
    }

    private fun setUpOrUpdateDocumentPages(){
        view.runOnUiThread(Runnable {
            view.updatePageList(pageActionDelegate.getCurrentDocument()
                    .toc?.getChildren() as MutableList<EpubNavItem>, selectedPage)
        })
    }

    fun handleAddPage(title: String){
        GlobalScope.launch {
            val result = pageActionDelegate.addPage(title)
            if(result){
                setUpOrUpdateDocumentPages()
            }
        }
    }

    fun handlePageOrderChanged(navItems:List<EpubNavItem>){
        GlobalScope.launch {
            pageActionDelegate.changePageOrder(navItems as MutableList<EpubNavItem>)
        }
    }


    fun handlePageSelected(href: String) {
        pageActionDelegate.loadPage(href)
        view.dismissDialog()
    }

    fun handlePageOptionsClicked(page: EpubNavItem?){
        view.showAddOrUpdatePageDialog(page,page != null)
    }

    fun handlePageUpdate(page: EpubNavItem){
        GlobalScope.launch {
            val result = pageActionDelegate.updatePage(page)
            if(result){
                setUpOrUpdateDocumentPages()
            }
        }
    }

    fun handleRemovePage(href: String){
        GlobalScope.launch {
            val result = pageActionDelegate.removePage(href)
            if(selectedPage == href){
                pageActionDelegate.loadPage(result!!)
            }
            setUpOrUpdateDocumentPages()
        }
    }

    fun handleEditDocument(){
        UstadMobileSystemImpl.instance.go(ContentEntryEditView.VIEW_NAME,
                arguments, view.viewContext)
    }

    fun handleDismissDialog() {
        view.runOnUiThread(Runnable{ view.dismissDialog()})
    }
}