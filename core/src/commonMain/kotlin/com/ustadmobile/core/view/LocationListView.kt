package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Location


interface LocationListView: UstadListView<Location, Location> {

    companion object {
        const val VIEW_NAME = "LocationListView"
    }

}