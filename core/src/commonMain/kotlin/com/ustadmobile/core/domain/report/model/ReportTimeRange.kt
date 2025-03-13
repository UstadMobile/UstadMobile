//package com.ustadmobile.core.domain.report.model
//
//import dev.icerock.moko.resources.StringResource
//import kotlinx.datetime.Clock
//import com.ustadmobile.core.MR
//import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
//import kotlinx.datetime.DateTimeUnit
//import kotlinx.datetime.LocalDateTime
//import kotlinx.datetime.TimeZone
//import kotlinx.datetime.minus
//import kotlinx.datetime.toInstant
//import kotlinx.datetime.toLocalDateTime
//import kotlinx.serialization.Serializable
//
///**
// * Enum of the relative units that can be selected for a report e.g. last x days, last x months, etc.
// */
//enum class ReportTimeRangeUnit(
//    override val label: StringResource,
//    val unit: DateTimeUnit.DateBased
//) : OptionWithLabelStringResource {
//    DAY(MR.strings.days, DateTimeUnit.DAY),
//    WEEK(MR.strings.weeks, DateTimeUnit.WEEK),
//    MONTH(MR.strings.months, DateTimeUnit.MONTH),
//    YEAR(MR.strings.year, DateTimeUnit.YEAR)
//}
//
//
///**
// *
// */
//enum class ReportTimeRangeOption(
//    val timeRange: ReportTimeRange,
//    override val label: StringResource
//): OptionWithLabelStringResource {
//
//    LAST_WEEK(RelativeReportTimeRange(ReportTimeRangeUnit.WEEK, 1), MR.strings.last_week),
//
//    LAST_MONTH(RelativeReportTimeRange(ReportTimeRangeUnit.MONTH, 1), MR.strings.last_month),
//
//    CUSTOM_PERIOD(RelativeReportTimeRange(ReportTimeRangeUnit.DAY, 1), MR.strings.custom_period),
//
//    CUSTOM_DATE_RANGE(FixedReportTimeRange(0L, UNSET_DISTANT_FUTURE), MR.strings.custom_date_range),
//}
//
///**
// * Sealed class that represents a report time range as selected by the user.
// */
//@Serializable
//sealed class ReportTimeRange {
//
//    /**
//     * The start time of the range (in ms since epoch)
//     */
//    abstract val from: Long
//
//    /**
//     * The end time of the range (in ms since epoch)
//     */
//    abstract val to: Long
//
//}
//
///**
// * A relative report time range is relative to the current time - e.g. last x days, last y months, etc.
// *
// * @param reportUnit The unit of time to subtract from the current time
// * @param reportUnitQuantity The quantity of the unit to subtract from the current time
// */
//@Serializable
//class RelativeReportTimeRange(
//    val reportUnit: ReportTimeRangeUnit,
//    val reportUnitQuantity: Int,
//): ReportTimeRange() {
//    override val to: Long
//        get() = Clock.System.now().toEpochMilliseconds()
//
//    override val from: Long
//        get() {
//            val timeZone = TimeZone.currentSystemDefault()
//            val nowDateTime = Clock.System.now().toLocalDateTime(timeZone)
//            return LocalDateTime(
//                date = nowDateTime.date.minus(reportUnitQuantity, reportUnit.unit),
//                time = nowDateTime.time
//            ).toInstant(timeZone).toEpochMilliseconds()
//        }
//}
//
///**
// * A fixed report date range as specified by the user.
// */
//@Serializable
//class FixedReportTimeRange(
//    override val from: Long,
//    override val to: Long,
//): ReportTimeRange()
