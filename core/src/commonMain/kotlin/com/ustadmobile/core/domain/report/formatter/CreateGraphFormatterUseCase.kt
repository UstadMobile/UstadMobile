package com.ustadmobile.core.domain.report.formatter

import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import kotlin.reflect.KClass


class CreateGraphFormatterUseCase() {

    data class FormatterOptions<T: Any>(
        val paramType: KClass<T>,
        val axis: Axis
    ) {
        enum class Axis {
            Y_AXIS_VALUES, X_AXIS_VALUES
        }
    }

    /**
     * Create a GraphFormatter for a given report result and axis (or other options)
     *
     * e.g. To get the y axis values:
     *
     * val yAxisValuesFormatter = invoke(
     *    reportResult = reportResult,
     *    optiosn = FormatterOptions(paramType = Double::class, axis = FormatterOptions.Axis.Y_AXIS_VALUES)
     * )
     *
     * If necessary this can use different implementations on different platforms
     */
    @Suppress("UNCHECKED_CAST") // Mike to check
    operator fun <T: Any> invoke(
        reportResult: RunReportUseCase.RunReportResult,
        options: FormatterOptions<T>
    ): GraphFormatter<T> {
        return when {
            options.axis == FormatterOptions.Axis.Y_AXIS_VALUES && options.paramType == Double::class -> {
                when(reportResult.request.reportOptions.series.first().reportSeriesYAxis.type) {
                    YAxisTypes.DURATION -> DurationGraphFormatter(reportResult)
                    else -> throw IllegalArgumentException("Unsupported type")
                }
            }

            else -> {
                throw IllegalArgumentException("TODO")
            }
        } as GraphFormatter<T>
    }

}