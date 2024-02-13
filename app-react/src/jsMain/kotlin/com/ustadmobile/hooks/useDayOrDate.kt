package com.ustadmobile.hooks

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.wrappers.intl.Intl
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import react.useMemo
import kotlin.js.Date
import com.ustadmobile.core.MR

/**
 * Returns a short "when" for the given timestamp as follows:
 *
 * If timestamp is from the same day
 *   If showTimeIfToday=false return "today" localized string
 *   If showTimeIfToday=true return formatted time
 * If timestamp is from yesterday, return "yesterday"
 * If timestamp is within the last week, return the day of the week
 * Otherwise, return formatted date
 *
 * @param enabled because it is not allowed to run a hook conditionally in React, this can be set
 *        to false so the hook won't do any work when not needed.
 */
fun useDayOrDate(
    enabled: Boolean,
    localDateTimeNow: LocalDateTime,
    timestamp: Long,
    timeZone: TimeZone,
    showTimeIfToday: Boolean,
    timeFormatter: Intl.Companion.DateTimeFormat,
    dateFormatter: Intl.Companion.DateTimeFormat,
    dayOfWeekStringMap: Map<DayOfWeek, String>,
): String? {
    val stringProvider = useStringProvider()

    return useMemo(arrayOf(enabled, timestamp, localDateTimeNow)) {
        if(enabled) {
            val timestampInstant = Instant.fromEpochMilliseconds(timestamp)
            val timestampLocalDateTime = timestampInstant.toLocalDateTime(timeZone)
            val epochDaysPassed = localDateTimeNow.date.toEpochDays() - timestampLocalDateTime.date.toEpochDays()

            when {
                epochDaysPassed == 0 -> if(showTimeIfToday) {
                    timeFormatter.format(Date(timestamp))
                }else {
                    stringProvider[MR.strings.today]
                }
                epochDaysPassed == 1 -> stringProvider[MR.strings.yesterday]
                epochDaysPassed <= 7 -> dayOfWeekStringMap[timestampLocalDateTime.dayOfWeek]
                else -> dateFormatter.format(Date(timestamp))
            }
        }else {
            null
        }
    }
}