package com.ustadmobile.core.domain.report.formatter

import com.ustadmobile.core.domain.report.query.RunReportUseCase

/**
 * Base formatter for count values (simple numeric display)
 */
class CountGraphFormatter(
    private val result: RunReportUseCase.RunReportResult,
    ) : GraphFormatter<Double> {
    override fun adjust(value: Double): Double = value

    override fun format(value: Double): String = value.toInt().toString()
}