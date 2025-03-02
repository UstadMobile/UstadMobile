package com.ustadmobile.core.domain.report.model

import dev.icerock.moko.resources.StringResource
import kotlinx.datetime.Clock
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

/**
 * Enum of the relative units that can be selected for a report e.g. last x days, last x months, etc.
 */
enum class ReportTimeRangeUnit(
    val label: StringResource,
    val unit: DateTimeUnit.DateBased
) {
    DAY(MR.strings.days, DateTimeUnit.DAY),
    WEEK(MR.strings.weeks, DateTimeUnit.WEEK),
    MONTH(MR.strings.months, DateTimeUnit.MONTH),
    YEAR(MR.strings.year, DateTimeUnit.YEAR),
}

/**
 *
 */
enum class ReportTimeRangeOption(
    val timeRange: ReportTimeRange,
    override val label: StringResource
): OptionWithLabelStringResource {

    LAST_WEEK(RelativeReportTimeRange(ReportTimeRangeUnit.WEEK, 1), MR.strings.last_week),

    LAST_MONTH(RelativeReportTimeRange(ReportTimeRangeUnit.MONTH, 1), MR.strings.last_month),

    CUSTOM_PERIOD(RelativeReportTimeRange(ReportTimeRangeUnit.DAY, 1), MR.strings.custom_period),

    CUSTOM_DATE_RANGE(FixedReportTimeRange(0L, UNSET_DISTANT_FUTURE), MR.strings.custom_date_range),
}

/**
 * Sealed class that represents a report time range as selected by the user.
 *
 * When a report is run, the time range will always be from 00:00.00 (midnight) on the first day (
 * as per the timezone) until 23:59.999 (end of day) on the last day of the range inclusive.
 */
@Serializable
sealed class ReportTimeRange {

    /**
     * The start time of the range (in ms since epoch) as selected by the user.
     */
    abstract val from: Long

    /**
     * The end time of the range (in ms since epoch)
     */
    abstract val to: Long

}

/**
 * A relative report time range is relative to the current time - e.g. last x days, last y months, etc.
 *
 * @param reportUnit The unit of time to subtract from the current time
 * @param reportUnitQuantity The quantity of the unit to subtract from the current time
 */
@Serializable
class RelativeReportTimeRange(
    val reportUnit: ReportTimeRangeUnit,
    val reportUnitQuantity: Int,
): ReportTimeRange() {
    override val to: Long
        get() = Clock.System.now().toEpochMilliseconds()

    override val from: Long
        get() {
            val timeZone = TimeZone.UTC
            val nowDateTime = Clock.System.now().toLocalDateTime(timeZone)

            /*
             * Because reports are always run from 00:00.00 to 23:59.999 as per the request timezone,
             * we always need to add one day - e.g. if a report is for the last 1 day, then from=to,
             * however when the report runs from will be set to 00:00.000 and to will be set to
             * 23:59.999 for the current day.
             */
            return LocalDateTime(
                date = nowDateTime.date.minus(reportUnitQuantity, reportUnit.unit)
                    .plus(DatePeriod(days = 1)),
                time = nowDateTime.time
            ).toInstant(timeZone).toEpochMilliseconds()
        }
}

/**
 * A fixed report date range as specified by the user.
 */
@Serializable
class FixedReportTimeRange(
    override val from: Long,
    override val to: Long,
): ReportTimeRange()
