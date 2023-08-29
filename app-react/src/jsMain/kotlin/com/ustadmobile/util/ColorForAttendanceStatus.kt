package com.ustadmobile.util

import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import mui.material.SvgIconColor

fun colorForAttendanceStatus(attendance: Float): SvgIconColor {
    return if ((attendance / 100) >=
        ClazzLogAttendanceRecord.ATTENDANCE_THRESHOLD_GOOD
    )
        SvgIconColor.success
    else if ((attendance / 100)
        >= ClazzLogAttendanceRecord.ATTENDANCE_THRESHOLD_WARNING
    )
        SvgIconColor.warning
    else
        SvgIconColor.error
}