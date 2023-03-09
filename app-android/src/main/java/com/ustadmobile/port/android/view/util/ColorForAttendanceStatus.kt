package com.ustadmobile.port.android.view.util

import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord

fun colorForAttendanceStatus(
    attendance: Float
) = if ((attendance/100) >= ClazzLogAttendanceRecord.ATTENDANCE_THRESHOLD_GOOD)
        R.color.successColor
    else if ((attendance/100) >= ClazzLogAttendanceRecord.ATTENDANCE_THRESHOLD_WARNING)
        R.color.secondaryColor
    else
        R.color.errorColor