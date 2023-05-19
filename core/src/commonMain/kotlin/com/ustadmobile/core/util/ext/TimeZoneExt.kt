package com.ustadmobile.core.util.ext

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt

fun TimeZone.formattedString(
    instant: Instant = Clock.System.now(),
) : String {
    return offsetAt(instant).gmtOffsetString() + " " + id
}
