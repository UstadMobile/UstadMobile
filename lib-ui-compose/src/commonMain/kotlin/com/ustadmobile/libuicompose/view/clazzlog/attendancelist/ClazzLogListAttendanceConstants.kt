package com.ustadmobile.libuicompose.view.clazzlog.attendancelist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel

object ClazzLogListAttendanceConstants {

    val RECORD_ATTENDANCE_OPTIONS_ICON = mapOf(
        ClazzLogListAttendanceViewModel.RecordAttendanceOption.RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE
                to Icons.Default.CalendarToday,
        ClazzLogListAttendanceViewModel.RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE
                to Icons.Default.Add
    )

}