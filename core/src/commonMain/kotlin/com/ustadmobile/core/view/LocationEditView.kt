package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Location


interface LocationEditView: UstadEditView<Location> {

    companion object {

        const val VIEW_NAME = "LocationEditEditView"

    }

}