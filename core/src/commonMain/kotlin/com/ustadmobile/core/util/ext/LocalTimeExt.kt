package com.ustadmobile.core.util.ext

import kotlinx.datetime.LocalTime

fun LocalTime.chopOffSeconds(): LocalTime {
    if(second > 0 || nanosecond > 0)
        return LocalTime(hour, minute)
    else
        return this
}
