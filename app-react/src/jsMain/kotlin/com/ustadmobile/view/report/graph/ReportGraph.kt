package com.ustadmobile.view.report.graph


import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.domain.report.utils.DefaultXAxisLabelFormatter
import com.ustadmobile.core.domain.report.utils.getMaxYValue
import com.ustadmobile.core.impl.locale.StringProvider
import dev.icerock.moko.resources.StringResource
import js.objects.jso
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useEffect
import react.useRef
import space.kscience.dataforge.meta.configure
import space.kscience.plotly.PlotlyConfig
import space.kscience.plotly.bar
import space.kscience.plotly.layout
import space.kscience.plotly.models.AxisType
import space.kscience.plotly.models.ScatterMode
import space.kscience.plotly.models.TickMode
import space.kscience.plotly.models.TraceType
import space.kscience.plotly.plotDiv
import space.kscience.plotly.scatter
import web.cssom.Overflow
import web.cssom.px

external interface ReportGraphProps : Props {
    var graphSeriesList: List<GraphSeries>
    var reportOptions: ReportOptions2
    var strings: StringProvider
    var compact: Boolean?
}

/**
 * This is based on the Plotly.kt JS demo found here:
 *
 * https://github.com/SciProgCentre/plotly.kt/blob/master/examples/js-demo/src/main/kotlin/space/kscience/plotly/jsdemo/main.kt
 */

val ReportGraph = FC<ReportGraphProps> { props ->
    val containerRef = useRef<web.html.HTMLElement>()
    val isCompact = props.compact ?: false
    val xAxisType = props.reportOptions.xAxis
    val formatter = DefaultXAxisLabelFormatter()

    useEffect(props.graphSeriesList, props.reportOptions) {
        val container = containerRef.current ?: return@useEffect

        val maxY = props.graphSeriesList.getMaxYValue()

        val isDuration = props.reportOptions.series.any {
            it.reportSeriesYAxis.type == YAxisTypes.DURATION
        }
        val (_, calculatedUnit) = calculateConversionFactor(isDuration, maxY, props.strings)
        val maxUnitSuffix: String = calculatedUnit

        (container as HTMLElement).clear()
        (container as HTMLElement).append {
            div {
                plotDiv(
                    plotlyConfig = PlotlyConfig {
                        responsive = true
                        configure {
                            "displayModeBar" put !isCompact
                            "staticPlot" put isCompact
                        }
                    }
                ) {
                    props.graphSeriesList.forEach { series ->
                        val groupedData = series.data.groupBy { it.subgroup }

                        groupedData.forEach { (subgroup, data) ->
                            val (transformedYValues, _) = transformYAxisValues(
                                data,
                                props.reportOptions,
                                props.strings
                            )
                            // Format x-axis values based on report type
                            val formattedXValues = data.map { row ->
                                when (val formatted = xAxisType.let {
                                    formatter.formatLabel(row.xAxis, it)
                                }) {
                                    is StringResource -> props.strings[formatted]
                                    else -> formatted.toString() ?: ""
                                }
                            }

                            when (series.type) {
                                SeriesType.BAR -> bar {
                                    name = "${series.name} - $subgroup"
                                    x.strings = formattedXValues
                                    y.numbers = transformedYValues
                                }

                                SeriesType.LINE -> scatter {
                                    name = "${series.name} - $subgroup"
                                    x.strings = formattedXValues
                                    y.numbers = transformedYValues
                                    mode = ScatterMode.`lines+markers`
                                    type = TraceType.scatter
                                }
                            }
                        }
                    }

                    layout {
                        if (isCompact) {
                            width = 320
                            height = 250
                            margin {
                                l = 30
                                r = 30
                                t = 20
                                b = 40
                                pad = 0
                            }
                        }
                        autosize = true
                        xaxis {
                            automargin = true
                            title {
                                text = props.reportOptions.xAxis.name
                                font { size = if (isCompact) 6 else 16 }
                            }
                            tickmode = TickMode.auto
                            type = AxisType.category
                        }
                        yaxis {
                            automargin = true
                            title {
                                text =
                                    getYAxisTitle(props.reportOptions, props.strings, maxUnitSuffix)
                                font { size = if (isCompact) 6 else 16 }
                            }
                        }
                        showlegend = !isCompact
                    }
                }
            }
        }
    }

    ReactHTML.div {
        ref = containerRef
        style = jso {
            padding = 4.px
            overflow = Overflow.clip
        }
    }
}

private fun calculateConversionFactor(
    isDuration: Boolean, maxY: Double,
    strings: StringProvider,
): Pair<Double, String> {
    return when {
        isDuration -> when {
            maxY >= 3_600_000 -> Pair(1.0 / 3_600_000, strings[MR.strings.hour_unit])
            maxY >= 60_000 -> Pair(1.0 / 60_000, strings[MR.strings.minute_unit])
            else -> Pair(1.0 / 1_000, strings[MR.strings.second_unit])
        }

        else -> Pair(1.0, "")
    }
}

private fun transformYAxisValues(
    data: List<ReportResultQueryRow>,
    reportOptions: ReportOptions2,
    strings: StringProvider,
): Pair<List<Double>, String> {
    val isDuration = reportOptions.series.any {
        it.reportSeriesYAxis.type == YAxisTypes.DURATION
    }
    val maxY = data.maxOfOrNull { it.yAxis } ?: 0.0
    val (conversionFactor, unitSuffix) = calculateConversionFactor(isDuration, maxY, strings)

    val transformedValues = data.map { row ->
        (row.yAxis * conversionFactor).let {
            if (isDuration) it else it.toInt().toDouble()
        }
    }

    return Pair(transformedValues, unitSuffix)
}

private fun getYAxisTitle(
    reportOptions: ReportOptions2,
    strings: StringProvider,
    unitSuffix: String
): String {
    val isDuration = reportOptions.series.any {
        it.reportSeriesYAxis.type == YAxisTypes.DURATION
    }
    return if (isDuration) "${strings[MR.strings.duration]} ($unitSuffix)"
    else strings[MR.strings.count]
}