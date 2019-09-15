package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry
import kotlin.js.JsName

interface SelectMultipleEntriesTreeDialogView : UstadView, UstadViewWithProgress {

    @JsName("populateTopEntries")
    fun populateTopEntries(entries: List<ContentEntry>)

    /**
     * Sets the title of the fragment
     * @param title
     */
    @JsName("setTitle")
    fun setTitle(title: String)

    /**
     * For Android: closes the activity.
     */
    @JsName("finish")
    fun finish()

    companion object {

        const val VIEW_NAME = "EntriesTreeDialog"

        const val ARG_CONTENT_ENTRY_SET = "entriesSelected"
    }

}