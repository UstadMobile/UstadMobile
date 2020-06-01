package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzLog

fun ClazzLog.attendancePercentage() = clazzLogNumPresent.toFloat() / (clazzLogNumPresent + clazzLogNumAbsent + clazzLogNumPartial)
