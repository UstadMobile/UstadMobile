package com.ustadmobile.view.report.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadQuickActionButton
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadFab
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.dom.clear
import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import kotlinx.html.h1
import kotlinx.html.js.div
import kotlinx.html.style
import mui.icons.material.GroupAdd
import mui.icons.material.ImportExport
import mui.icons.material.PersonAdd
import mui.icons.material.Share
import mui.material.Box
import mui.material.Card
import mui.material.Dialog
import mui.material.Divider
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Orientation
import mui.material.Typography
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML
import react.useEffect
import react.useRef
import space.kscience.plotly.bar
import space.kscience.plotly.layout
import space.kscience.plotly.models.ScatterMode
import space.kscience.plotly.models.TraceType
import space.kscience.plotly.plotDiv
import space.kscience.plotly.scatter
import web.cssom.px

external interface ReportDetailProps : Props {
    var uiState: ReportDetailUiState
    var onDismissDialog: () -> Unit
    var onShowDialog: () -> Unit
}

val ReportDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ReportDetailViewModel(di, savedStateHandle)
    }
    val uiState by viewModel.uiState.collectAsState(ReportDetailUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())
    val strings = useStringProvider()

    UstadFab { fabState = appState.fabState }
    Dialog {
        open = uiState.dialogVisible

        onClose = { _, _ ->
            viewModel.onDismissDialog()
        }

        mui.material.List {
            ListItem {
                ListItemButton {
                    id = "share"
                    onClick = {
                    }

                    ListItemIcon {
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.share])
                    }
                }
            }

            ListItem {
                ListItemButton {
                    id = "export data"

                    onClick = {
                    }

                    ListItemIcon {
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.export_data])
                    }
                }
            }
        }
    }
    ReportDetailComponent2 {
        this.uiState = uiState
        onShowDialog = viewModel::onShowDialog
        onDismissDialog = viewModel::onDismissDialog
    }
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
        // Use the shared data
        val graphSeriesList = sharedGraphSeriesList

        // Determine Y-axis type
        val isDuration = uiState.reportOptions2.series.any { it.reportSeriesYAxis?.type == YAxisTypes.DURATION }
        val yAxisTitle = if (isDuration) "Duration (hours)" else "Count"

        // Function to transform Y-axis values based on type
        fun transformYAxisValues(data: List<ReportResultQueryRow>): List<Double> {
            return data.map { row ->
                if (isDuration) {
                    // Convert milliseconds to hours
                    row.yAxis / (1000 * 60 * 60)
                } else {
                    // Keep as is for count
                    row.yAxis
                }
            }
        }

        // Function to generate distinct colors for subgroups
        fun generateColors(numColors: Int): List<String> {
            val colors = mutableListOf<String>()
            val hueStep = 360.0 / numColors
            for (i in 0 until numColors) {
                val hue = (i * hueStep) % 360
                colors.add("hsl($hue, 70%, 50%)") // HSL format for distinct colors
            }
            return colors
        }

        // Get all unique subgroups
        val allSubgroups = graphSeriesList.flatMap { it.data.map { row -> row.subgroup } }.toSet()

        // Generate colors for each subgroup
        val subgroupColors = generateColors(allSubgroups.size).zip(allSubgroups.toList()).toMap()

        plotDiv {
            // Iterate through the graphSeriesList and render each series
            graphSeriesList.forEach { series ->
                // Group data by subgroup
                val groupedData = series.data.groupBy { it.subgroup }

                groupedData.forEach { (subgroup, data) ->
                    val transformedYValues = transformYAxisValues(data)
                    when (series.type) {
                        SeriesType.BAR -> {
                            bar {
                                name = "${series.name} - $subgroup"
                                x.strings = data.map { it.xAxis }
                                y.numbers = transformedYValues

                            }
                        }
                        SeriesType.LINE -> {
                            scatter {
                                name = "${series.name} - $subgroup"
                                x.strings = data.map { it.xAxis }
                                y.numbers = transformedYValues
                                mode = ScatterMode.lines
                                type = TraceType.scatter
                            }
                        }
                    }
                }
            }

            layout {
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
                        text = yAxisTitle
                        font {
                            size = 16
                        }
                    }
                }
                // Add a legend to distinguish subgroups
                showlegend = true
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
            moreOption{
                uiState = props.uiState
                onShowDialog = props.onShowDialog
                onDismissDialog = props.onDismissDialog
            }
        }
    }
}
private  val moreOption = FC<ReportDetailProps> { props ->
    val strings = useStringProvider()
    val header = listOf(strings[MR.strings.x_axis], strings[MR.strings.y_axis], strings[MR.strings.sub_group]) // Replace with string resources if available
    // Example data
    // Use the shared data
    val data = sharedGraphSeriesList
    UstadStandardContainer {

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(8.px)
            //divider
            Divider { orientation = Orientation.horizontal }

            Stack {
                direction = responsive(StackDirection.row)
                UstadQuickActionButton {
                    icon = Share.create()
                    text = strings[MR.strings.share]
                    onClick = {
                    }
                }
                UstadQuickActionButton {
                    icon = ImportExport.create()
                    text = strings[MR.strings.export_data]
                    onClick = {
                        props.onShowDialog()
                    }
                }

            }
            Divider { orientation = Orientation.horizontal }

            // data table
            Card {
                Box {
                    sx {
                        padding = 8.px
                    }

                    // Header Row
                    Stack {
                        direction = responsive(StackDirection.row)
                        spacing = responsive(16.px)

                        header.forEach { title ->
                            Typography {
                                +title
                            }
                        }
                    }
                    // Data Rows
                    data.forEach { row ->
                        row.data.forEach { it ->
                            Stack {
                                direction = responsive(StackDirection.row)
                                spacing = responsive(8.px)

                                Typography {
                                    +it.xAxis
                                }

                                Divider { orientation = Orientation.vertical }

                                Typography {
                                    +it.yAxis.toString()
                                }

                                Divider { orientation = Orientation.vertical }

                                Typography {
                                    +it.subgroup ?: "-"
                                }
                            }
                        }
                    }
                }
            }

        }
    }

}
// Shared data for both graph and table
val sharedBarSeries1 = listOf(
    ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 5000000.0, subgroup = "Category A"),
    ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 4000000.0, subgroup = "Category B"),
    ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 1000000.0, subgroup = "Category A"),
    ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 5000000.0, subgroup = "Category B"),
    ReportResultQueryRow(xAxis = "03/01/2024", yAxis = 4000000.0, subgroup = "Category B")
)

val sharedLineSeries = listOf(
    ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 2000000.0, subgroup = "Category M"),
    ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 9000000.0, subgroup = "Category N"),
    ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 7000000.0, subgroup = "Category M"),
    ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 1000000.0, subgroup = "Category N"),
)

val sharedLineSeries1 = listOf(
    ReportResultQueryRow(xAxis = "female", yAxis = 20.0, subgroup = "Category A"),
    ReportResultQueryRow(xAxis = "male", yAxis = 90.0, subgroup = "Category A"),
    ReportResultQueryRow(xAxis = "female", yAxis = 80.0, subgroup = "Category B"),
    ReportResultQueryRow(xAxis = "male", yAxis = 10.0, subgroup = "Category B"),
)

// Convert the shared data into GraphSeries
val sharedGraphSeriesList = listOf(
    GraphSeries(type = SeriesType.BAR, data = sharedBarSeries1, name = "Bar Series 1"),
    GraphSeries(type = SeriesType.LINE, data = sharedLineSeries, name = "Line Series"),
//     GraphSeries(type = SeriesType.LINE, data = sharedLineSeries1, name = "Line Series 1")
)
