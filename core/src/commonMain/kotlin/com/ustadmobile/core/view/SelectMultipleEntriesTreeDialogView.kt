package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry

interface SelectMultipleEntriesTreeDialogView : UstadView {

    fun populateTopEntries(locations: List<ContentEntry>)

    /**
     * Sets the title of the fragment
     * @param title
     */
    fun setTitle(title: String)

    /**
     * For Android: closes the activity.
     */
    fun finish()

    companion object {

        const val VIEW_NAME = "SelectMultipleEntriesTreeDialog"

        const val ARG_CONTENT_ENTRY_SET = "EntriesSelected"
    }

}