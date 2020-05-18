package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzLog


interface ClazzLogListAttendanceView: UstadListView<ClazzLog, ClazzLog> {

    //Used to offset times so that they are displayed as per the local time applicable to the class
    var clazzTimeZone: String?

    companion object {
        const val VIEW_NAME = "ClazzLogListAttendanceView"
    }

}