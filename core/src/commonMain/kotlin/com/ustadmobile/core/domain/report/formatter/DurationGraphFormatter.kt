package com.ustadmobile.core.domain.report.formatter

import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import kotlin.time.DurationUnit

class DurationGraphFormatter(
    private val result: RunReportUseCase.RunReportResult,
): GraphFormatter<Double> {

    private val unit: DurationUnit by lazy {
        val maxVal = result.results.maxOfOrNull { list ->
            list.maxOfOrNull { it.yAxis } ?: 0.0
        } ?: 0.0

        if(maxVal > MS_PER_HOUR) {
            DurationUnit.HOURS
        }else {
            DurationUnit.MINUTES
        }
    }

    override fun adjust(value: Double): Double {
        return when(unit) {
            DurationUnit.MINUTES -> value / MS_PER_MIN
            DurationUnit.HOURS -> value / MS_PER_HOUR
            else -> throw IllegalStateException()
        }
    }

    override fun format(value: Double): String {
        return value.toString() //TODO: round this to 2 decimal places
    }

}