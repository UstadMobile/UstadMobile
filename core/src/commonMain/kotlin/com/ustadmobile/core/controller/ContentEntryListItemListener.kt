package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.ContentEntry

interface ContentEntryListItemListener {

    fun onClickContentEntry(entry: ContentEntry)

    /**
     * Called when the user clicks the "select" button which is shown when a ContentEntryList
     * is in picker mode (next to each entry)
     */
    fun onClickSelectContentEntry(entry: ContentEntry)

    fun onClickDownloadContentEntry(entry: ContentEntry)

}