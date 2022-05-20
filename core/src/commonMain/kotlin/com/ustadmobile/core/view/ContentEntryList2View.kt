package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer

interface ContentEntryList2View: UstadListView<ContentEntry, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> {

    fun showContentEntryAddOptions()

    /**
     * Show the download dialog button. If required by the OS, show a permission dialog first
     */
    fun showDownloadDialog(args: Map<String, String>)

    var title: String?

    var editOptionVisible: Boolean

    companion object {

        const val ARG_SHOW_ONLY_FOLDER_FILTER = "folder"

        const val ARG_DISPLAY_CONTENT_BY_OPTION = "displayOption"

        const val ARG_DISPLAY_CONTENT_BY_PARENT = "displayContentByParent"

        //Used to make the item on the home screen seen as a different screen
        const val VIEW_NAME_HOME = "ContentEntryListHome"

        const val VIEW_NAME = "ContentEntryListView"

        const val FOLDER_VIEW_NAME = "ContentEntryListFolderView"

        const val ARG_MOVING_CONTENT = "SelectedItems"

        const val ARG_MOVING_COUNT = "moveCount"

        const val ARG_SELECT_FOLDER_VISIBLE = "selectFolderVisible"

        /**
         * Show chips for My content, from my courses, and library (e.g. when selecting content that
         * is going to be added to a course).
         */
        const val ARG_USE_CHIPS = "contentEntryListChips"

    }

}