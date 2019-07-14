package com.ustadmobile.core.view


import com.ustadmobile.lib.db.entities.Location

/**
 * SelectMultipleTreeDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SelectMultipleLocationTreeDialogView : UstadView {

    /**
     * Populate the Top level location list to the view. Called when we start the tree fragment.
     * @param locations The list of top locations
     */
    fun populateTopLocation(locations: List<Location>)

    /**
     * Sets the title of the fragment
     * @param title
     */
    fun setTitle(title: String)

    /**
     * Sends back selection to parent activity and closes the fragment
     */
    fun finish()

    companion object {

        val VIEW_NAME = "SelectMultipleTreeDialog"

        //This argument is used to store the selection uids so that when this fragment is opened,
        // we can pre-tick the locations already selected.
        val ARG_LOCATIONS_SET = "LocationsSelected"
    }

}
