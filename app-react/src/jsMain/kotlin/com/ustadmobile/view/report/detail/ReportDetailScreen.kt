package com.ustadmobile.view.report.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
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
import react.useMemo
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

val ReportDetailComponent2 = FC<ReportDetailProps> { props ->
    val string = useStringProvider()

    val graphSeriesList = useMemo(listOf(props.uiState.reportResults, props.uiState.reportOptions2.series)) {
        props.uiState.reportOptions2.series.mapIndexed { index, reportSeries ->
            GraphSeries(
                type = when (reportSeries.reportSeriesVisualType) {
                    ReportSeriesVisualType.LINE_GRAPH -> SeriesType.LINE
                    else -> SeriesType.BAR
                },
                data = props.uiState.reportResults.getOrNull(index)?.map { statementRow ->
                    ReportResultQueryRow(
                        xAxis = statementRow.xAxis,
                        yAxis = statementRow.yAxis,
                        subgroup = statementRow.subgroup
                    )
                } ?: emptyList(),
                name = reportSeries.reportSeriesTitle
            )
        }
    }
    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(8.px)

            ReportGraph {
                this.graphSeriesList = graphSeriesList
                this.reportOptions = props.uiState.reportOptions2
                this.strings = string
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

    val data = useMemo(props.uiState) {
        props.uiState.reportOptions2.series.mapIndexed { index, reportSeries ->
            GraphSeries(
                type = SeriesType.BAR,
                data = props.uiState.reportResults.getOrNull(index)?.map {
                    ReportResultQueryRow(
                        xAxis = it.xAxis,
                        yAxis = it.yAxis,
                        subgroup = it.subgroup
                    )
                } ?: emptyList(),
                name = reportSeries.reportSeriesTitle
            )
        }
    }
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