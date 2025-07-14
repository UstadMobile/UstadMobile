package com.ustadmobile.core.domain.report.formatter

import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.utils.getMonthDisplayName
import com.ustadmobile.core.impl.locale.StringUiText
import com.ustadmobile.core.impl.locale.UiText
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

    override fun format(value: String): UiText {
        return try {
            when (xAxisType) {
                ReportXAxis.MONTH -> {
                    val date = value.toLocalDate()
                    val monthName = getMonthDisplayName(date.monthNumber)
                    StringUiText("$monthName - ${date.year}")
                }

                ReportXAxis.YEAR -> StringUiText(value.toLocalDate().year.toString())
                else -> StringUiText(value)
            }
        } catch (e: Exception) {
            StringUiText(value)
        }
    }
}