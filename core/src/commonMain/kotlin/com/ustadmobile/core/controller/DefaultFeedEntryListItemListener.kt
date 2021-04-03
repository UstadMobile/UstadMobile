package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.FeedEntryListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.lib.db.entities.FeedEntry
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance


class DefaultFeedEntryListItemListener(var view: FeedEntryListView?,
                                   var listViewMode: ListViewMode,
                                   val context: Any,
                                   override val di: DI): FeedEntryListItemListener, DIAware {

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onClickFeedEntry(feedEntry: FeedEntry) {
        if(listViewMode == ListViewMode.BROWSER) {
            val dest = feedEntry.feViewDest ?: return
            systemImpl.go(dest, context)
        }else {
            view?.finishWithResult(listOf(feedEntry))
        }
    }
}
