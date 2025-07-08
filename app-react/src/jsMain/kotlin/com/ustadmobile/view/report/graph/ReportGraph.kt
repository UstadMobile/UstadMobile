package com.ustadmobile.view.report.graph

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.formatter.GraphFormatter
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

private const val COMPACT_WIDTH = 320
private const val COMPACT_HEIGHT = 250

external interface ReportGraphProps : Props {
    var reportResult: RunReportUseCase.RunReportResult
    var strings: StringProvider
    var compact: Boolean?
    var xAxisFormatter: GraphFormatter<String>?
    var yAxisFormatter: GraphFormatter<Double>?
}

val ReportGraph = FC<ReportGraphProps> { props ->
    val containerRef = useRef<web.html.HTMLElement>()
    val isCompact = props.compact ?: false

    useEffect(props.reportResult.timestamp) {
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
                    props.reportResult.distinctSubgroups.forEach { resultSubgroup ->
                        when (resultSubgroup.series.reportSeriesOptions.reportSeriesVisualType) {
                            ReportSeriesVisualType.LINE_GRAPH -> scatter {
                                name = resultSubgroup.series.reportSeriesOptions.reportSeriesTitle
                                x.strings = resultSubgroup.subgroupData.map {
                                    props.xAxisFormatter?.format(it.xAxis) ?: it.xAxis
                                }
                                y.numbers = resultSubgroup.subgroupData.map {
                                    props.yAxisFormatter?.format(it.yAxis)?.toFloat() ?: it.yAxis.toFloat()
                                }
                                mode = ScatterMode.`lines+markers`
                                type = TraceType.scatter
                            }

                            else -> bar {
                                name = resultSubgroup.series.reportSeriesOptions.reportSeriesTitle
                                x.strings = resultSubgroup.subgroupData.map {
                                    props.xAxisFormatter?.format(it.xAxis) ?: it.xAxis
                                }
                                y.numbers = resultSubgroup.subgroupData.map {
                                    props.yAxisFormatter?.format(it.yAxis)?.toFloat() ?: it.yAxis.toFloat()
                                }
                            }
                        }
                    }
                    layout {
                        if (isCompact) {
                            width = COMPACT_WIDTH
                            height = COMPACT_HEIGHT
                        }
                        autosize = true
                        margin {
                            l = 0
                            r = 0
                            t = 0
                            b = 0
                            pad = 0
                            autoexpand = true
                        }
                        xaxis {
                            automargin = true
                            title {
                                text =
                                    props.strings[props.reportResult.request.reportOptions.xAxis.label]
                            }
                            tickmode = TickMode.auto
                            type = AxisType.category
                        }
                        yaxis {
                            automargin = true
                            title {
                                text = if (isDuration)
                                    props.strings[MR.strings.duration]
                                else
                                    props.strings[MR.strings.count]
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