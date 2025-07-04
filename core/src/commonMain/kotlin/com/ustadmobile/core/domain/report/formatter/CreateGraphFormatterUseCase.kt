package com.ustadmobile.core.domain.report.formatter

import com.ustadmobile.core.domain.report.query.RunReportUseCase

interface CreateGraphFormatterUseCase {

    data class FormatterOptions<T>(
        val axis: Axis
    ) {
        enum class Axis {
            Y_AXIS, X_AXIS
        }
    }

    /**
     * Create a GraphFormatter for a given report result and axis (or other options)
     */
    operator fun <T: Any> invoke(
        reportResult: RunReportUseCase.RunReportResult,
        options: FormatterOptions<T>
    ): GraphFormatter<T>

}