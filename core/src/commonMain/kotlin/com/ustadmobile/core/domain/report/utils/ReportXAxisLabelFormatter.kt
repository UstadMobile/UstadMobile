package com.ustadmobile.core.domain.report.utils

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_FEMALE
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_MALE
import dev.icerock.moko.resources.StringResource
import kotlinx.datetime.toLocalDate

interface ReportXAxisLabelFormatter {
    fun formatLabel(value: Any?, xAxis: ReportXAxis): Any
}

class DefaultXAxisLabelFormatter(
) : ReportXAxisLabelFormatter {
    override fun formatLabel(value: Any?, xAxis: ReportXAxis): Any {
        return when (xAxis) {
            ReportXAxis.GENDER -> genderLabelProvider(value?.toString() ?: "")
            ReportXAxis.CLASS -> value?.toString() ?: ""
            else -> value?.let { formatDateForReports(it.toString(), xAxis) } ?: ""
        }
    }
}

fun formatDateForReports(dateStr: String, xAxis: ReportXAxis): String {
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

fun genderLabelProvider(string: String): StringResource {
    return when (string) {
        GENDER_FEMALE.toString() -> MR.strings.female
        GENDER_MALE.toString() -> MR.strings.male
        else -> {
            MR.strings.other
        }
    }
}

expect fun getMonthDisplayName(month: Int): String