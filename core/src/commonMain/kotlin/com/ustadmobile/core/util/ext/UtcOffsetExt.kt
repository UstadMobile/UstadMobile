package com.ustadmobile.core.util.ext

import kotlinx.datetime.UtcOffset
import kotlin.math.absoluteValue


fun UtcOffset.gmtOffsetString(): String {
    val hours = totalSeconds / (3600) //1hr = 60seconds per min x 60 mins per hour
    val minutes = totalSeconds.mod(3600)

    return buildString {
        append("GMT")
        if(hours >= 0)
            append("+")
        else
            append("-")
        append(hours.absoluteValue.toString().padStart(2, '0'))
        append(":")
        append(minutes.absoluteValue.toString().padStart(2, '0'))
    }
}
