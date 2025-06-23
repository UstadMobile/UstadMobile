package com.ustadmobile.core.domain.report.utils

import com.ustadmobile.core.domain.report.model.ReportXAxis
import kotlinx.datetime.toLocalDate


object ReportFormatter {

    fun formatDateForReport(dateStr: String, xAxis: ReportXAxis): String {
        return try {
            val date = dateStr.toLocalDate()

            when (xAxis) {
                ReportXAxis.MONTH -> {
                    val monthName = getMonthDisplayName(date.monthNumber)
                    "$monthName - ${date.year}"
                }

                ReportXAxis.YEAR -> date.year.toString()
                else -> dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }
}

expect fun getMonthDisplayName(month: Int): String