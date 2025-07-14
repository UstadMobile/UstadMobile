package com.ustadmobile.core.domain.report.formatter

import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.core.impl.locale.StringUiText
import com.ustadmobile.core.impl.locale.UiText
import kotlin.reflect.KClass


class CreateGraphFormatterUseCase() {

    data class FormatterOptions<T : Any>(
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
    operator fun <T : Any> invoke(
        reportResult: RunReportUseCase.RunReportResult,
        options: FormatterOptions<T>
    ): GraphFormatter<T> {
        return when {
            options.axis == FormatterOptions.Axis.Y_AXIS_VALUES && options.paramType == Double::class -> {
                when (reportResult.request.reportOptions.series.first().reportSeriesYAxis.type) {
                    YAxisTypes.DURATION -> DurationGraphFormatter(reportResult)
                    YAxisTypes.COUNT -> CountGraphFormatter()
                    else -> throw IllegalArgumentException("Unsupported Y-axis type")
                }
            }

            options.axis == FormatterOptions.Axis.X_AXIS_VALUES && options.paramType == String::class -> {
                when (reportResult.request.reportOptions.xAxis) {
                    ReportXAxis.DAY,
                    ReportXAxis.WEEK,
                    ReportXAxis.MONTH,
                    ReportXAxis.YEAR -> DateGraphFormatter(reportResult.request.reportOptions.xAxis)

                    ReportXAxis.GENDER -> GenderGraphFormatter()

                    ReportXAxis.CLASS -> NoOpGraphFormatter() // No formatting for class names

                    else -> throw IllegalArgumentException("Unsupported X-axis type")
                }
            }

            else -> {
                throw IllegalArgumentException("Unsupported formatter options combination")
            }
        } as GraphFormatter<T>
    }
}

/**
 * Formatter that returns values as-is without any transformation,
 * wrapping them in UiText for consistent localization handling.
 */
class NoOpGraphFormatter : GraphFormatter<String> {
    override fun adjust(value: String): String = value

    override fun format(value: String): UiText = StringUiText(value)
}