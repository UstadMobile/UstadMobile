package com.ustadmobile.view.report.detail

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
import react.dom.html.ReactHTML
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
fun TagConsumer<HTMLElement>.plot() {
    div {
        style = "height:50%; width=100%;"
        h1 { +"Histogram demo" }
        plotDiv {
            val rnd = Random(222)
            histogram {
                name = "Random data"
                x.numbers = List(500) { rnd.nextDouble() }
            }

            layout {
                bargap = 0.1
                title {
                    text = "Basic Histogram"
                    font {
                        size = 20
                        color("black")
                    }
                }
                xaxis {
                    title {
                        text = "Value"
                        font {
                            size = 16
                        }
                    }
                }
                yaxis {
                    title {
                        text = "Count"
                        font {
                            size = 16
                        }
                    }
                }
            }
        }
    }

    div {
        style = "height:50%; width=100%;"
        h1 { +"Dynamic trace demo" }
        plotDiv {
            scatter {
                x(1, 2, 3, 4)
                y(10, 15, 13, 17)
                mode = ScatterMode.markers
                type = TraceType.scatter
            }
            scatter {
                x(2, 3, 4, 5)
                y(10, 15, 13, 17)
                mode = ScatterMode.lines
                type = TraceType.scatter

                GlobalScope.launch {
                    while (isActive) {
                        delay(500)
                        marker {
                            if (Random.nextBoolean()) {
                                color("magenta")
                            } else {
                                color("blue")
                            }
                        }
                    }
                }
            }
            scatter {
                x(1, 2, 3, 4)
                y(12, 5, 2, 12)
                mode = ScatterMode.`lines+markers`
                type = TraceType.scatter
                marker {
                    color("red")
                }
            }
            layout {
                title = "Line and Scatter Plot"
            }
        }
    }
    div {
        style = "height:50%; width=100%;"
        h1 { +"Deserialization" }
        val plot = Plotly.plot {
            scatter {
                x(1, 2, 3, 4)
                y(10, 15, 13, 17)
                mode = ScatterMode.markers
                type = TraceType.scatter
            }
        }
        val serialized = plot.toJsonString()
        console.log(serialized)
        val deserialized = Plot(Json.decodeFromString(MetaSerializer, serialized))
        plotDiv(plot = deserialized).on(PlotlyEventListenerType.CLICK){
            console.info(it.toString())
        }
    }
}

val ReportDetailComponent2 = FC<ReportDetailProps> { props ->
    val canvasRef = useRef<web.html.HTMLElement>()

    //Use a reference to get the HTMLElement (DOM object) when it is added by React.
    val canvasRefVal = canvasRef.current

    //useEffect function arguments should include the canvas reference itself
    useEffect(canvasRefVal) {
        if(canvasRefVal == null)
            return@useEffect

        console.log("Plotting time")
        (canvasRefVal as HTMLElement).clear()
        (canvasRefVal as HTMLElement).append {
            plot()
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
