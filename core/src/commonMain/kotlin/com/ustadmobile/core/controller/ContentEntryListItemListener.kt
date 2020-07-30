package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer

interface ContentEntryListItemListener {

    fun onClickContentEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)

    /**
     * Called when the user clicks the "select" button which is shown when a ContentEntryList
     * is in picker mode (next to each entry)
     */
    fun onClickSelectContentEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)

    fun onClickDownloadContentEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)

}