package com.ustadmobile.view.report.graph


import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.impl.locale.StringProvider
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

    useEffect(props.graphSeriesList, props.reportOptions) {
        val container = containerRef.current ?: return@useEffect
        var maxUnitSuffix = "ms"

        val allYValues = props.graphSeriesList.flatMap { series ->
            series.data.map { it.yAxis }
        }
        val maxY = allYValues.maxOrNull() ?: 0.0
        val isDuration = props.reportOptions.series.any {
            it.reportSeriesYAxis?.type == YAxisTypes.DURATION
        }
        val (_, calculatedUnit) = calculateConversionFactor(isDuration, maxY)
        maxUnitSuffix = calculatedUnit

        (container as HTMLElement).clear()
        (container as HTMLElement).append {
            div {
                plotDiv(
                    plotlyConfig = PlotlyConfig {
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
                            )

                            when (series.type) {
                                SeriesType.BAR -> bar {
                                    name = "${series.name} - $subgroup"
                                    x.strings = data.map { it.xAxis }
                                    y.numbers = transformedYValues
                                }

                                SeriesType.LINE -> scatter {
                                    name = "${series.name} - $subgroup"
                                    x.strings = data.map { it.xAxis }
                                    y.numbers = transformedYValues
                                    mode = ScatterMode.`lines+markers`
                                    type = TraceType.scatter
                                }
                            }
                        }
                    }
                    layout {
                        if (isCompact) {
                            width = 250
                            height = 250
                            margin {
                                l = 40
                                r = 20
                                t = 30
                                b = 40
                                pad = 0
                            }
                        }
                        xaxis {
                            automargin = true
                            title {
                                text = props.reportOptions.xAxis?.name
                                    ?: props.strings[MR.strings.x_axis]
                                font { size = if (isCompact) 6 else 16 }
                            }
                            tickmode = TickMode.auto
                            type = AxisType.category
                        }
                        yaxis {
                            automargin = true  // Let Plotly handle margins
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
            overflow = Overflow.clip
        }
    }
}

private fun calculateConversionFactor(isDuration: Boolean, maxY: Double): Pair<Double, String> {
    return when {
        isDuration -> when {
            maxY >= 3_600_000 -> Pair(1.0 / 3_600_000, "hr")
            maxY >= 60_000 -> Pair(1.0 / 60_000, "min")
            maxY >= 1_000 -> Pair(1.0 / 1_000, "sec")
            else -> Pair(1.0, "ms")  // Handle milliseconds case
        }

        else -> Pair(1.0, "")
    }
}

private fun transformYAxisValues(
    data: List<ReportResultQueryRow>,
    reportOptions: ReportOptions2,
): Pair<List<Double>, String> {
    val isDuration = reportOptions.series.any {
        it.reportSeriesYAxis?.type == YAxisTypes.DURATION
    }
    val maxY = data.maxOfOrNull { it.yAxis } ?: 0.0
    val (conversionFactor, unitSuffix) = calculateConversionFactor(isDuration, maxY)

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
        it.reportSeriesYAxis?.type == YAxisTypes.DURATION
    }
    return if (isDuration) "${strings[MR.strings.duration]} ($unitSuffix)"
    else strings[MR.strings.count]
}