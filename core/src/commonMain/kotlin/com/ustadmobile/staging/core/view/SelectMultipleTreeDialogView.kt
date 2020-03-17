package com.ustadmobile.core.view


import com.ustadmobile.lib.db.entities.Location

/**
 * SelectMultipleTreeDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SelectMultipleTreeDialogView : UstadView {

    fun populateTopLocation(locations: List<Location>)

    /**
     * For Android: closes the activity.
     */
    fun finish()

    companion object {

        val VIEW_NAME = "SelectMultipleTreeDialog"
    }

}
