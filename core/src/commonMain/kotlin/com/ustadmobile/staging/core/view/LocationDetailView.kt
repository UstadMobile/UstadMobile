package com.ustadmobile.core.view


import com.ustadmobile.lib.db.entities.Location

/**
 * Core View. Screen is for LocationDetail's View
 */
interface LocationDetailView : UstadView {

    fun populateTopLocation(locations: List<Location>)

    fun updateLocationOnView(location: Location)

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "LocationDetail"

        //Any argument keys:
        val LOCATION_UID = "LocationUid"
        val LOCATIONS_SET = "LocationDetailLocationSet"
    }


}

