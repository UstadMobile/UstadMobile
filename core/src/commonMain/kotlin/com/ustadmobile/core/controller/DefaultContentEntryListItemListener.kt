package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class DefaultContentEntryListItemListener(var view: UstadView? = null,
                                          var presenter: ContentEntryList2Presenter? = null,
                                          var mListMode: ListViewMode = ListViewMode.BROWSER,
                                          var clazzUid: Long,
                                          val context: Any,
                                          override val di: DI) : ContentEntryListItemListener, DIAware {

    val systemImpl: UstadMobileSystemImpl by instance()

    var serializationStrategy: KSerializer<List<ContentEntry>> =
        ListSerializer(ContentEntry.serializer())

    override fun onClickContentEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        when{
            mListMode == ListViewMode.PICKER && !entry.leaf -> {
                presenter?.openContentEntryBranchPicker(entry)
            }

            mListMode == ListViewMode.PICKER && entry.leaf -> {
                presenter?.handleEntrySelectedFromPicker(entry)
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

        presenter?.handleEntrySelectedFromPicker(entry)
    }

    override fun onClickDownloadContentEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        systemImpl.go("DownloadDialog",
                mapOf(UstadView.ARG_CONTENT_ENTRY_UID to entry.contentEntryUid.toString()),
                context)
    }
}