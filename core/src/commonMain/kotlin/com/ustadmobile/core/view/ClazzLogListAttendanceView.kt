package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ClazzLogListAttendancePresenter
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzLog


interface ClazzLogListAttendanceView: UstadListView<ClazzLog, ClazzLog> {

    //Used to offset times so that they are displayed as per the local time applicable to the class
    var clazzTimeZone: String?

    /**
     * The data that will be used to draw the chart. This is a list with pairs that represent the
     * x/y coordinates. The x is the timestamp, the y is the attendance (between 0 and 100)
     */
    var graphData: DoorMutableLiveData<ClazzLogListAttendancePresenter.AttendanceGraphData>?

    var recordAttendanceOptions: List<ClazzLogListAttendancePresenter.RecordAttendanceOption>?

    companion object {
        const val VIEW_NAME = "CourseLogListAttendanceView"
    }

}