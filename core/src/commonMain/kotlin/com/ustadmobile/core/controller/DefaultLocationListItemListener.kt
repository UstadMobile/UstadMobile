package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.LocationEditView
import com.ustadmobile.core.view.LocationListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Location


class DefaultLocationListItemListener(var view: LocationListView?,
                                   var listViewMode: ListViewMode,
                                   val systemImpl: UstadMobileSystemImpl,
                                   val context: Any): LocationListItemListener {

    override fun onClickLocation(location: Location) {
        if(listViewMode == ListViewMode.BROWSER) {
            systemImpl.go(LocationEditView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to location.locationUid.toString()), context)
        }else {
            view?.finishWithResult(listOf(location))
        }
    }
}
