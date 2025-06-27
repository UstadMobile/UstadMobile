package com.ustadmobile.view.report.graph

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.lib.db.composites.StatementReportRow
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
    var reportResult: RunReportUseCase.RunReportResult
    var strings: StringProvider
    var compact: Boolean?
}

val ReportGraph = FC<ReportGraphProps> { props ->
    val containerRef = useRef<web.html.HTMLElement>()
    val isCompact = props.compact ?: false

    useEffect(props.reportResult) {
        val container = containerRef.current ?: return@useEffect

        val seriesList = props.reportResult.resultSeries
        val maxY = seriesList.flatMap { it.data.map { row -> row.yAxis } }.maxOrNull() ?: 0.0

        val isDuration = seriesList.any { series ->
            series.reportSeriesOptions.reportSeriesYAxis.type == YAxisTypes.DURATION
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
                    seriesList.forEach { series ->
                        val groupedData = series.data.groupBy { it.subgroup }

                        groupedData.forEach { (subgroup, data) ->
                            val (transformedYValues, _) = transformYAxisValues(
                                data,
                                series.reportSeriesOptions.reportSeriesYAxis.type == YAxisTypes.DURATION,
                                props.strings
                            )
                            // Format x-axis values
                            val formattedXValues = data.map { it.xAxis.toString() }

                            when (series.reportSeriesOptions.reportSeriesVisualType) {
                                ReportSeriesVisualType.LINE_GRAPH -> scatter {
                                    name = "${series.reportSeriesOptions.reportSeriesTitle} - $subgroup"
                                    x.strings = formattedXValues
                                    y.numbers = transformedYValues
                                    mode = ScatterMode.`lines+markers`
                                    type = TraceType.scatter
                                }
                                else -> bar {
                                    name = "${series.reportSeriesOptions.reportSeriesTitle} - $subgroup"
                                    x.strings = formattedXValues
                                    y.numbers = transformedYValues
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
                                text = props.reportResult.request.reportOptions.xAxis.name
                                font { size = if (isCompact) 6 else 16 }
                            }
                            tickmode = TickMode.auto
                            type = AxisType.category
                        }
                        yaxis {
                            automargin = true
                            title {
                                text = getYAxisTitle(
                                    isDuration = seriesList.any {
                                        it.reportSeriesOptions.reportSeriesYAxis.type == YAxisTypes.DURATION
                                    },
                                    strings = props.strings,
                                    unitSuffix = maxUnitSuffix
                                )
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

// The helper functions remain unchanged
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
    data: List<StatementReportRow>,
    isDuration: Boolean,
    strings: StringProvider,
): Pair<List<Double>, String> {
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
    isDuration: Boolean,
    strings: StringProvider,
    unitSuffix: String
): String {
    return if (isDuration) "${strings[MR.strings.duration]} ($unitSuffix)"
    else strings[MR.strings.count]
}