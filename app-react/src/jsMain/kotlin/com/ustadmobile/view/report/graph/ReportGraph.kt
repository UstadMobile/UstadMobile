package com.ustadmobile.view.report.graph


import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.impl.locale.StringProvider
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.style
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useEffect
import react.useRef
import space.kscience.plotly.bar
import space.kscience.plotly.layout
import space.kscience.plotly.models.ScatterMode
import space.kscience.plotly.models.TraceType
import space.kscience.plotly.plotDiv
import space.kscience.plotly.scatter

external interface ReportGraphProps : Props {
    var graphSeriesList: List<GraphSeries>
    var reportOptions: ReportOptions2
    var strings: StringProvider
}

/**
 * This is based on the Plotly.kt JS demo found here:
 *
 * https://github.com/SciProgCentre/plotly.kt/blob/master/examples/js-demo/src/main/kotlin/space/kscience/plotly/jsdemo/main.kt
 */

val ReportGraph = FC<ReportGraphProps> { props ->
    val containerRef = useRef<web.html.HTMLElement>()

    useEffect(props.graphSeriesList, props.reportOptions) {
        val container = containerRef.current ?: return@useEffect

        (container as HTMLElement).clear()
        (container as HTMLElement).append {
            div {
                style = "height:50%; width=100%;"
                plotDiv {
                    props.graphSeriesList.forEach { series ->
                        val groupedData = series.data.groupBy { it.subgroup }

                        groupedData.forEach { (subgroup, data) ->
                            val transformedYValues = transformYAxisValues(
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
                                    mode = ScatterMode.lines
                                    type = TraceType.scatter
                                }
                            }
                        }
                    }

                    layout {
                        xaxis {
                            title {
                                text = props.reportOptions.xAxis?.name
                                    ?: props.strings[MR.strings.x_axis]
                                font { size = 16 }
                            }
                        }
                        yaxis {
                            title {
                                text = getYAxisTitle(props.reportOptions, props.strings)
                                font { size = 16 }
                            }
                        }
                        showlegend = true
                    }
                }
            }
        }
    }

    ReactHTML.div {
        ref = containerRef
    }
}

private fun transformYAxisValues(
    data: List<ReportResultQueryRow>,
    reportOptions: ReportOptions2,
): List<Double> {
    val isDuration = reportOptions.series.any {
        it.reportSeriesYAxis?.type == YAxisTypes.DURATION
    }

    return data.map { row ->
        if (isDuration) row.yAxis / (1000 * 60 * 60)
        else row.yAxis
    }
}

private fun getYAxisTitle(
    reportOptions: ReportOptions2,
    strings: StringProvider
): String {
    val isDuration = reportOptions.series.any {
        it.reportSeriesYAxis?.type == YAxisTypes.DURATION
    }
    return if (isDuration) strings[MR.strings.duration_hours]
    else strings[MR.strings.count]
}