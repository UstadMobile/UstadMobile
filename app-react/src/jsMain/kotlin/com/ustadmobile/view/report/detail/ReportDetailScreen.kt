package com.ustadmobile.view.report.detail

import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadFab
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.useEffect
import react.useRef
import web.cssom.px
import kotlinx.html.TagConsumer
import kotlinx.html.js.div
import kotlinx.html.style
import org.w3c.dom.HTMLElement
import kotlinx.html.h1
import kotlinx.serialization.json.Json
import space.kscience.dataforge.meta.MetaSerializer
import space.kscience.plotly.plotDiv
import space.kscience.plotly.*
import space.kscience.plotly.events.PlotlyEventListenerType
import space.kscience.plotly.models.ScatterMode
import space.kscience.plotly.models.TraceType
import kotlin.random.Random
import kotlinx.html.dom.append
import moe.tlaster.precompose.viewmodel.viewModel
import react.dom.html.ReactHTML
import web.console.Console

external interface ReportDetailProps : Props {
    var uiState: ReportDetailUiState
}

val ReportDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ReportDetailViewModel(di, savedStateHandle)
    }
    val uiState by viewModel.uiState.collectAsState(ReportDetailUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab { fabState = appState.fabState }
    ReportDetailComponent2 { this.uiState = uiState }
}

/**
 * This is based on the Plotly.kt JS demo found here:
 *
 * https://github.com/SciProgCentre/plotly.kt/blob/master/examples/js-demo/src/main/kotlin/space/kscience/plotly/jsdemo/main.kt
 */
@OptIn(DelicateCoroutinesApi::class)
fun TagConsumer<HTMLElement>.plot(uiState: ReportDetailUiState) {
    div {
        style = "height:50%; width=100%;"
        h1 { +"Report Graph" }

        // Example data
        val barSeries1 = listOf(
            ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 5000000.0, subgroup = "Category A"),
            ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 4000000.0, subgroup = "Category B"),
            ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 1000000.0, subgroup = "Category A"),
            ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 5000000.0, subgroup = "Category B"),
            ReportResultQueryRow(xAxis = "03/01/2024", yAxis = 4000000.0, subgroup = "Category B")
        )

        val lineSeries = listOf(
            ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 2000000.0, subgroup = "Category M"),
            ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 9000000.0, subgroup = "Category N"),
            ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 7000000.0, subgroup = "Category M"),
            ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 1000000.0, subgroup = "Category N"),
        )

        val lineSeries1 = listOf(
            ReportResultQueryRow(xAxis = "female", yAxis = 20.0, subgroup = "Category A"),
            ReportResultQueryRow(xAxis = "male", yAxis = 90.0, subgroup = "Category A"),
            ReportResultQueryRow(xAxis = "female", yAxis = 20.0, subgroup = "Category B"),
            ReportResultQueryRow(xAxis = "male", yAxis = 900.0, subgroup = "Category B"),
        )
        val yAxis = if (uiState.reportOptions2.series.any { it.reportSeriesYAxis?.type == YAxisTypes.DURATION }) {
            "Duration"
        } else {
            "Count"
        }
        console.log("yAxis key = ${uiState.reportOptions2}")

        plotDiv {
            // Bar Series
            bar {
                name = "Bar Series 1"
                x.strings = barSeries1.map { it.xAxis }
                y.numbers = barSeries1.map { it.yAxis }
            }

            // Line Series
            scatter {
                name = "Line Series"
                x.strings = lineSeries.map { it.xAxis }
                y.numbers = lineSeries.map { it.yAxis }
                mode = ScatterMode.lines
                type = TraceType.scatter
            }

            // Line Series 1
//            scatter {
//                name = "Line Series 1"
//                x.strings = lineSeries1.map { it.xAxis }
//                y.numbers = lineSeries1.map { it.yAxis }
//                mode = ScatterMode.lines
//                type = TraceType.scatter
//            }

            layout {
                title {
                    text = uiState.reportOptions2?.xAxis?.name ?: "X Axis"
                    font {
                        size = 20
                        color("black")
                    }
                }
                xaxis {
                    title {
                        text = uiState.reportOptions2?.xAxis?.name ?: "X Axis"
                        font {
                            size = 16
                        }
                    }
                }
                yaxis {
                    title {
                        text = yAxis
                        font {
                            size = 16
                        }
                    }
                }
            }
        }
    }
}

val ReportDetailComponent2 = FC<ReportDetailProps> { props ->
    val canvasRef = useRef<web.html.HTMLElement>()

    // Use a reference to get the HTMLElement (DOM object) when it is added by React.
    val canvasRefVal = canvasRef.current

    // useEffect function arguments should include the canvas reference itself and props.uiState
    useEffect(canvasRefVal, props.uiState) {
        if (canvasRefVal == null)
            return@useEffect

        console.log("Plotting time")
        (canvasRefVal as HTMLElement).clear()
        (canvasRefVal as HTMLElement).append {
            plot(props.uiState)
        }
    }

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(8.px)

            ReactHTML.div {
                ref = canvasRef
            }
        }
    }
}
