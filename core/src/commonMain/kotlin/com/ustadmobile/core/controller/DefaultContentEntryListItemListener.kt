package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class DefaultContentEntryListItemListener(var view: ContentEntryList2View? = null,
                                          var presenter: ContentEntryList2Presenter? = null,
                                          var mListMode: ListViewMode = ListViewMode.BROWSER,
                                          var clazzUid: Long,
                                          val context: Any,
                                          override val di: DI) : ContentEntryListItemListener, DIAware {

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onClickContentEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        when{
            mListMode == ListViewMode.PICKER && !entry.leaf -> {
                presenter?.openContentEntryBranchPicker(entry)
            }

            mListMode == ListViewMode.PICKER && entry.leaf -> {
                view?.finishWithResult(listOf(entry))
            }

            mListMode == ListViewMode.BROWSER -> {
                val args = if(entry.leaf) {
                    mapOf(UstadView.ARG_ENTITY_UID to entry.contentEntryUid.toString(),
                            UstadView.ARG_CLAZZUID to clazzUid.toString(),
                            UstadView.ARG_PARENT_ENTRY_TITLE to entry.title)
                } else {
                    mapOf( UstadView.ARG_PARENT_ENTRY_UID to entry.contentEntryUid.toString(),
                            UstadView.ARG_CLAZZUID to clazzUid.toString(),
                            ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_OPTION to ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_PARENT)
                }
                systemImpl.go(if(entry.leaf) ContentEntryDetailView.VIEW_NAME
                else ContentEntryList2View.VIEW_NAME, args,context)
            }
        }
    }

    override fun onClickSelectContentEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {

        presenter?.handleMoveWithSelectedEntry(entry)

        view?.finishWithResult(listOf(entry))
    }

    override fun onClickDownloadContentEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        systemImpl.go("DownloadDialog",
                mapOf(UstadView.ARG_CONTENT_ENTRY_UID to entry.contentEntryUid.toString()),
                context)
    }
}