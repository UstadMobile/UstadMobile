package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.UstadMobileConstants
import kotlinx.datetime.*

/**
 * Convert the given instant to 00:00:00 at the given timezone on the same day. This is useful for
 * course start dates, enrolment start dates etc where we want the time to be the very start of the
 * day and the user does not explicitly select the time
 */
fun Instant.toLocalMidnight(timeZone: TimeZone): Instant {
    return toLocalDateTime(timeZone)
        .toLocalMidnight()
        .toInstant(timeZone)
}

fun Instant.toLocalMidnight(timeZoneId: String): Instant {
    return toLocalMidnight(TimeZone.of(timeZoneId))
}

/**
 * Convert the given instant to 23:59:59 at the given timezone on the same day. This is useful for
 * course end dates, enrolment end dates etc where we want the time to be the very start of the day
 * and the user does nto explicitly select the time
 */
fun Instant.toLocalEndOfDay(timeZone: TimeZone): Instant {
    return toLocalDateTime(timeZone)
        .toLocalEndOfDay()
        .toInstant(timeZone)
}

fun Instant.toLocalEndOfDay(timeZoneId: String) = toLocalEndOfDay(TimeZone.of(timeZoneId))

/**
 * Where this Instant represents an instant in the past (e.g. the date of birth) return the age in
 * years
 */
fun Instant.ageInYears(): Int {
    return periodUntil(Clock.System.now(), TimeZone.UTC).years
}

fun Instant.isDateOfBirthAMinor(): Boolean {
    return ageInYears() < UstadMobileConstants.MINOR_AGE_THRESHOLD
}

