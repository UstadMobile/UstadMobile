package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.ustadmobile.core.MR
import kotlinx.datetime.DayOfWeek
import java.text.DateFormat
import java.util.Date

/**
 * Returns a short "when" for the given timestamp as follows:
 *
 * If timestamp is from the same day
 *   If showTimeIfToday=false return "today" localized string
 *   If showTimeIfToday=true return formatted time
 * If timestamp is from yesterday, return "yesterday"
 * If timestamp is within the last week, return the day of the week
 * Otherwise, return formatted date
 */
@Composable
fun rememberDayOrDate(
    localDateTimeNow: LocalDateTime,
    timestamp: Long,
    timeZone: TimeZone,
    showTimeIfToday: Boolean,
    timeFormatter: DateFormat,
    dateFormatter: DateFormat,
    dayOfWeekStringMap: Map<DayOfWeek, String>,
) : String {
    val todayStr = stringResource(MR.strings.today)
    val yesterdayStr = stringResource(MR.strings.yesterday)

    return remember(timestamp, localDateTimeNow) {
        val timestampInstant = Instant.fromEpochMilliseconds(timestamp)
        val timestampLocalDateTime = timestampInstant.toLocalDateTime(timeZone)
        val epochDaysPassed = localDateTimeNow.date.toEpochDays() - timestampLocalDateTime.date.toEpochDays()

        when {
            epochDaysPassed == 0 -> if(showTimeIfToday) {
                timeFormatter.format(Date(timestamp))
            }else {
                todayStr
            }
            epochDaysPassed == 1 -> yesterdayStr
            epochDaysPassed <= 7 -> dayOfWeekStringMap[timestampLocalDateTime.dayOfWeek] ?: ""
            else -> dateFormatter.format(Date(timestamp))
        }
    }

}