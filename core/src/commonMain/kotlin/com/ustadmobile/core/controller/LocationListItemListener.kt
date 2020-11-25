package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Location


interface LocationListItemListener {

    fun onClickLocation(location: Location)

}