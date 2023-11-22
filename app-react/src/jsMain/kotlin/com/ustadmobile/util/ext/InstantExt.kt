package com.ustadmobile.util.ext

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Given an Instant that represents a specific time e.g. 10:00am 1/Dec/2022 in Dubai, generate a new
 * instant that represents the same date and hour of the day in the "to" timezone e.g.
 * 10:00am 1/Dec/2022 in Europe/Berlin.
 *
 * The MUI/React time pickers work with the system default timezone. We therefor need to offset the
 * value we give them by the difference between the timezone we want to use as the base (e.g. the
 * time zone for a course, school, UTC, etc) and the local timezone.
 *
 * @receiver an Instant
 * @param fromTimeZone the from TimeZone
 * @param toTimeZone the to TimeZone
 * @return an Instant that represents the same date and hour/min/seconds of the day in the toTimeZone
 */
fun Instant.toSameDateTimeInOtherTimeZone(
    fromTimeZone: TimeZone,
    toTimeZone: TimeZone
): Instant {
    if(fromTimeZone == toTimeZone)
        return this

    val fromLocalDateTime = toLocalDateTime(fromTimeZone)
    return fromLocalDateTime.toInstant(toTimeZone)
}
