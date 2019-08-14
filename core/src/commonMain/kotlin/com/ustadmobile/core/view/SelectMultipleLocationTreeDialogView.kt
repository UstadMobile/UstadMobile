package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Location

interface SelectMultipleLocationTreeDialogView : UstadView {

    fun populateTopLocation(locations: List<Location>)

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

        const val VIEW_NAME = "SelectMultipleTreeDialog"

        const val ARG_LOCATIONS_SET = "LocationsSelected"
    }

}