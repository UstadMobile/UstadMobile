package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.TimeZoneEntity

interface TimeZoneEntityListView: UstadListView<TimeZoneEntity, TimeZoneEntity> {

    companion object {
        const val VIEW_NAME = "TimeZoneEntityListView"
    }

}