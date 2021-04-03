package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.FeedEntry


interface FeedEntryListItemListener {

    fun onClickFeedEntry(feedEntry: FeedEntry)

}