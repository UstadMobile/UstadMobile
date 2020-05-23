package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer

interface ContentEntryList2View: UstadListView<ContentEntry, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> {

    enum class ContentEntryListViewMode(val viewMode: Int) {
        NORMAL(1),
        PICKER(2)
    }

    fun checkAndGetContentType(type: String?): String

    fun checkAndGetContentDrawable(type: String?): Int

    companion object {

        const val ARG_DOWNLOADED_CONTENT = "downloaded"

        const val ARG_CONTENT_FILTER = "filter"

        const val ARG_LIBRARIES_CONTENT = "libraries"

        const val ARG_RECYCLED_CONTENT = "recycled"

        const val VIEW_NAME = "ContentEntryListView"
    }

}