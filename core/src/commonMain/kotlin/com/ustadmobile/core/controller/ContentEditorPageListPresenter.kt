package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmObserver
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEditorPageListView
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.core.view.ContentEntryEditView
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class ContentEditorPageListPresenter(context: Any, arguments: Map<String, String>,
                                     view: ContentEditorPageListView,
                                     private val pageDelegate: ContentEditorPageDelegate)
    : UstadBaseController<ContentEditorPageListView>(context, arguments, view) {

    private val appDb: UmAppDatabase = UmAppDatabase.getInstance(context)

    private val entryUuid = arguments.getValue(ContentEditorView.CONTENT_ENTRY_UID).toLong()

    private var selectedPage: String? = null

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val entryLiveData: UmLiveData<ContentEntry> = appDb.contentEntryDao.getLiveContentEntry(entryUuid)
        entryLiveData.observe(this, object : UmObserver<ContentEntry> {
            override fun onChanged(t: ContentEntry?) {
                if(t != null){
                    view.runOnUiThread(Runnable { view.setDocumentTitle(t.title!!)})
                    setUpOrUpdateDocumentPages()
                }
            }
        })
    }

    private fun setUpOrUpdateDocumentPages(){
        view.runOnUiThread(Runnable {
            view.updatePageList(pageDelegate.getCurrentDocument()
                    .toc?.getChildren() as MutableList<EpubNavItem>, selectedPage)
        })
    }

    fun handleAddPage(title: String){
        GlobalScope.launch {
            val result = pageDelegate.addPage(title)
            if(result){
                setUpOrUpdateDocumentPages()
            }
        }
    }

    fun handlePageOrderChanged(navItems:List<EpubNavItem>){
        GlobalScope.launch {
            pageDelegate.changePageOrder(navItems as MutableList<EpubNavItem>)
        }
    }


    fun handlePageSelected(href: String) {
        pageDelegate.loadPage(href)
        view.dismissDialog()
    }

    fun handlePageOptionsClicked(page: EpubNavItem?){
        view.showAddOrUpdatePageDialog(page,page != null)
    }

    fun handlePageUpdate(page: EpubNavItem){
        GlobalScope.launch {
            val result = pageDelegate.updatePage(page)
            if(result){
                setUpOrUpdateDocumentPages()
            }
        }
    }

    fun handleRemovePage(href: String){
        GlobalScope.launch {
            val result = pageDelegate.removePage(href)
            if(selectedPage == href){
                pageDelegate.loadPage(result)
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