package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzLog

private fun ClazzLog.totalStudents() = (clazzLogNumPresent + clazzLogNumAbsent + clazzLogNumPartial)

fun ClazzLog.attendancePercentage() = (clazzLogNumPresent.toFloat() + clazzLogNumPartial) / totalStudents()

fun ClazzLog.latePercentage() = (clazzLogNumPartial.toFloat()) / totalStudents()

