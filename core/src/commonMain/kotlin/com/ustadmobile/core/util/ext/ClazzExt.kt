package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.Clazz

fun Clazz.isAttendanceEnabledAndRecorded(): Boolean =
        ((clazzFeatures and Clazz.CLAZZ_FEATURE_ATTENDANCE) == Clazz.CLAZZ_FEATURE_ATTENDANCE) && attendanceAverage >= 0f