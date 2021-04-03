package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.FeedEntry


interface FeedEntryListView: UstadListView<FeedEntry, FeedEntry> {

    companion object {
        const val VIEW_NAME = "FeedEntryListView"
    }

}