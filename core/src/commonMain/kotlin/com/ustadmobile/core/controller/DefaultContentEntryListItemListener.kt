package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.ContentEntry
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class DefaultContentEntryListItemListener(var view: ContentEntryList2View? = null,
                                          var presenter: ContentEntryList2Presenter? = null,
                                          var mListMode: ListViewMode = ListViewMode.BROWSER,
                                          val context: Any,
                                          override val di: DI) : ContentEntryListItemListener, DIAware {

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onClickContentEntry(entry: ContentEntry) {
        when{
            mListMode == ListViewMode.PICKER && !entry.leaf -> {
                presenter?.openContentEntryBranchPicker(entry)
            }

            mListMode == ListViewMode.PICKER && entry.leaf -> {
                view?.finishWithResult(listOf(entry))
            }

            mListMode == ListViewMode.BROWSER -> {
                val args = if(entry.leaf) mapOf(UstadView.ARG_ENTITY_UID to entry.contentEntryUid.toString())
                else mapOf( UstadView.ARG_PARENT_ENTRY_UID to entry.contentEntryUid.toString(),
                        ContentEntryList2View.ARG_CONTENT_FILTER to ContentEntryList2View.ARG_LIBRARIES_CONTENT, UstadView.ARG_PARENT_ENTRY_TITLE to entry.title)
                systemImpl.go(if(entry.leaf) ContentEntry2DetailView.VIEW_NAME
                else ContentEntryList2View.VIEW_NAME, args,context)
            }
        }
    }

    override fun onClickSelectContentEntry(entry: ContentEntry) {
        when(entry.leaf){
            true -> onClickContentEntry(entry)
            false -> view?.finishWithResult(listOf(entry))
        }
    }

    override fun onClickDownloadContentEntry(entry: ContentEntry) {
        view?.showDownloadDialog(mapOf(ARG_PARENT_ENTRY_UID to entry.toString()))
    }
}