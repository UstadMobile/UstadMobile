package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.Clazz

fun Clazz.isAttendanceEnabledAndRecorded(): Boolean =
        ((clazzFeatures and Clazz.CLAZZ_FEATURE_ATTENDANCE) == Clazz.CLAZZ_FEATURE_ATTENDANCE) && attendanceAverage >= 0f

/**
 * Shorthand to see if the class has any defined start or end time. This can be used to determine
 * if the start and end date should be displayed
 */
fun Clazz?.isStartOrEndTimeSet(): Boolean =
        this != null && (clazzStartTime != 0L || (clazzEndTime != 0L && clazzEndTime != Long.MAX_VALUE))
