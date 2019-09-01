package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Location
import kotlin.js.JsName

interface SelectMultipleLocationTreeDialogView : UstadView {

    @JsName("populateTopLocation")
    fun populateTopLocation(locations: List<Location>)

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

        const val VIEW_NAME = "SelectMultipleTreeDialog"

        const val ARG_LOCATIONS_SET = "locationsSelected"
    }

}