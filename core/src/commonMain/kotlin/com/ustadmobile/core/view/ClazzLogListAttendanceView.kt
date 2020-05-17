package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzLog


interface ClazzLogListAttendanceView: UstadListView<ClazzLog, ClazzLog> {

    companion object {
        const val VIEW_NAME = "ClazzLogListAttendanceView"
    }

}