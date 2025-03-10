package com.ustadmobile.view.report.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadQuickActionButton
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.report.graph.ReportGraph
import mui.icons.material.ImportExport
import mui.icons.material.Share
import mui.material.Box
import mui.material.Card
import mui.material.Dialog
import mui.material.Divider
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.material.Orientation
import mui.material.Typography
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
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

// In ReportDetailScreen.kt
val ReportDetailComponent2 = FC<ReportDetailProps> { props ->
    val string = useStringProvider()

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(8.px)

            ReportGraph {
                graphSeriesList = sharedGraphSeriesList
                reportOptions = props.uiState.reportOptions2
                strings = string
            }
            moreOption {
                uiState = props.uiState
                onShowDialog = props.onShowDialog
                onDismissDialog = props.onDismissDialog
            }
        }
    }
}
private val moreOption = FC<ReportDetailProps> { props ->
    val strings = useStringProvider()
    val header = listOf(
        strings[MR.strings.x_axis],
        strings[MR.strings.y_axis],
        strings[MR.strings.subgroup_by]
    )
    // Example data
    // Use the shared data
    val data = sharedGraphSeriesList

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(8.px)
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
                        row.data.forEach {
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
)
