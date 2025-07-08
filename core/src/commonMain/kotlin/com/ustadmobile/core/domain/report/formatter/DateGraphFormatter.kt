package com.ustadmobile.core.domain.report.formatter

import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.utils.getMonthDisplayName
import kotlinx.datetime.toLocalDate

/**
 * Formatter for date values (handles different date groupings)
 */
class DateGraphFormatter(
    private val xAxisType: ReportXAxis
) : GraphFormatter<String> {
    override fun adjust(value: String): String {
        return value
    }

    override fun format(value: String): String {
        return try {
            when (xAxisType) {
                ReportXAxis.MONTH -> {
                    val monthName = getMonthDisplayName( value.toLocalDate().monthNumber)
                    "$monthName - ${ value.toLocalDate().year}"
                }

                ReportXAxis.YEAR ->  value.toLocalDate().year.toString()
                else -> value
            }
        } catch (e: Exception) {
            value
        }
    }

}