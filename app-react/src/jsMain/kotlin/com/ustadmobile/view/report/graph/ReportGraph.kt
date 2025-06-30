package com.ustadmobile.view.report.graph

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.domain.report.query.RunReportUseCase
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
        val isDuration = props.reportResult.yAxisType == YAxisTypes.DURATION

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
                    props.reportResult.resultSeries.forEach { series ->
                        series.data.groupBy { it.subgroup }.forEach { (subgroup, statementRow) ->

                            when (series.reportSeriesOptions.reportSeriesVisualType) {
                                ReportSeriesVisualType.LINE_GRAPH -> scatter {
                                    name = "${series.reportSeriesOptions.reportSeriesTitle} - $subgroup"
                                    x.strings = statementRow.map { it.xAxis}
                                    y.numbers = statementRow.map { it.yAxis }
                                    mode = ScatterMode.`lines+markers`
                                    type = TraceType.scatter
                                }
                                else -> bar {
                                    name = "${series.reportSeriesOptions.reportSeriesTitle} - $subgroup"
                                    x.strings = statementRow.map { it.xAxis }
                                    y.numbers = statementRow.map { it.yAxis }
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
                                text = props.strings[props.reportResult.request.reportOptions.xAxis.label]
                                font { size = if (isCompact) 6 else 16 }
                            }
                            tickmode = TickMode.auto
                            type = AxisType.category
                        }
                        yaxis {
                            automargin = true
                            title {
                                text = if (isDuration) props.strings[MR.strings.duration]
                                else props.strings[MR.strings.count]
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