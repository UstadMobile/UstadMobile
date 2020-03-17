package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class DailyAttendanceNumbers {

    var clazzUid: Long = 0
    var clazzLogUid: Long = 0
    var logDate: Long = 0
    var attendancePercentage: Float = 0.toFloat()
    var absentPercentage: Float = 0.toFloat()
    var partialPercentage: Float = 0.toFloat()

    var femaleAttendance: Float = 0.toFloat()
    var maleAttendance: Float = 0.toFloat()
}
