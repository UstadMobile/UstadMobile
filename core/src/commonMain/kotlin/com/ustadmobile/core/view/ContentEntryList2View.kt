package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer

interface ContentEntryList2View: UstadListView<ContentEntry, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> {

    fun showContentEntryAddOptions(parentEntryUid: Long)

    /**
     * Show the download dialog button. If required by the OS, show a permission dialog first
     */
    fun showDownloadDialog(args: Map<String, String>)

    var title: String?

    var editOptionVisible: Boolean

    companion object {

        const val ARG_SHOW_ONLY_FOLDER_FILTER = "folder"

        const val ARG_DISPLAY_CONTENT_BY_OPTION = "displayOption"

        const val ARG_DISPLAY_CONTENT_BY_CLAZZ = "displayContentByClazz"

        const val ARG_DISPLAY_CONTENT_BY_PARENT = "displayContentByParent"

        const val ARG_DISPLAY_CONTENT_BY_DOWNLOADED = "displayContentByDownloaded"

        const val ARG_CLAZZWORK_FILTER = "clazzworkFilter"

        const val VIEW_NAME = "ContentEntryListView"

        const val FOLDER_VIEW_NAME = "ContentEntryListFolderView"

        const val ARG_MOVING_CONTENT = "SelectedItems"

        const val ARG_MOVING_COUNT = "moveCount"

    }

}