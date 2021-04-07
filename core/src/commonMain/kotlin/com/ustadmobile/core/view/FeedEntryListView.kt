package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.lib.db.entities.FeedSummary


interface FeedEntryListView: UstadListView<FeedEntry, FeedEntry> {

    var summaryStats: FeedSummary?

    companion object {
        const val VIEW_NAME = "FeedEntryListView"
    }

}