package com.ustadmobile.view.report.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportTimeRange
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.ReportXAxisConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.report.edit.ReportEditUiState
import com.ustadmobile.core.viewmodel.report.edit.ReportEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadMessageIdSelectField
import kotlinx.coroutines.Dispatchers
import mui.icons.material.Close
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Divider
import mui.material.Icon
import mui.material.IconButton
import mui.material.Orientation
import mui.material.Stack
import mui.material.StackDirection
import mui.material.TextField
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import web.cssom.px

external interface ReportEditScreenProps : Props {
    var uiState: ReportEditUiState
    var onEntityChanged: (ReportOptions2) -> Unit
    var onSeriesChanged: (ReportSeries2) -> Unit
    var onAddSeries: () -> Unit
    var onAddFilter: (seriesId: Int) -> Unit
    var onRemoveFilter: (index: Int, seriesId: Int) -> Unit
}

private val ReportEditScreenComponent2 = FC<ReportEditScreenProps> { props ->
    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {
            spacing = responsive(2)

            TextField {
                id = "Title"
                value = props.uiState.reportOptions2.title ?: ""
                label = ReactNode(strings[MR.strings.title] + "*")
                onTextChange = { newValue ->
                    props.onEntityChanged(props.uiState.reportOptions2.copy(title = newValue))
                }
            }


            // X Axis Selection
            UstadMessageIdSelectField {
                id = "X Axis"
                value = props.uiState.reportOptions2.xAxis ?: 0
                options = ReportXAxisConstants.X_AXIS_OPTIONS
                label = strings[MR.strings.x_axis]
                onChange = { selectedValue ->
                    props.onEntityChanged(props.uiState.reportOptions2.copy(xAxis = selectedValue.value))
                }
            }

            Divider { orientation = Orientation.horizontal }

            // Iterate over Series
            props.uiState.reportOptions2.series.forEach { series ->
                Stack {
                    spacing = responsive(16.px)

                    // Series Title
                    TextField {
                        id = "Series Title"
                        value = series.reportSeriesTitle
                        label = ReactNode(strings[MR.strings.series_title] + "*")
                        onTextChange = { newValue ->
                            props.onSeriesChanged(series.copy(reportSeriesTitle = newValue))

                        }
                    }

                    // Y Axis Dropdown
                    UstadMessageIdSelectField {
                        id = "Y Axis"
                        value = series.reportSeriesYAxis?.value ?: 0
                        options = ReportSeriesYAxis.entries.map { yAxis ->
                            MessageIdOption2(
                                stringResource = yAxis.stringResource,
                                value = yAxis.value
                            )
                        }
                        label = strings[MR.strings.y_axis]
                        onChange = { selectedValue ->
                            val selectedYAxis =
                                ReportSeriesYAxis.entries.firstOrNull { it.value == selectedValue.value }
                                    ?: ReportSeriesYAxis.NONE
                            props.onSeriesChanged(series.copy(reportSeriesYAxis = selectedYAxis))
                        }
                    }

                    // Subgroup by Dropdown
                    UstadMessageIdSelectField {
                        id = "Subgroup by"
                        value = series.reportSeriesSubGroup?.value ?: 0
                        options = ReportXAxis.entries.map { xAxis ->
                            MessageIdOption2(
                                stringResource = xAxis.stringResource,
                                value = xAxis.value
                            )
                        }
                        label = strings[MR.strings.subgroup_by]
                        onChange = { selectedValue ->
                            val selectedXAxis =
                                ReportXAxis.entries.firstOrNull { it.value == selectedValue.value }
                                    ?: ReportXAxis.DAY
                            val updatedSeries =
                                series.copy(reportSeriesSubGroup = selectedXAxis)
                            props.onSeriesChanged(updatedSeries)
                        }
                    }
                }

                // Chart Type Dropdown
                UstadMessageIdSelectField {
                    id = "Chart Type"
                    value = series.reportSeriesVisualType?.value ?: 0
                    options = ReportSeriesVisualType.entries.map { visualType ->
                        MessageIdOption2(
                            stringResource = visualType.stringResource,
                            value = visualType.value
                        )
                    }
                    label = strings[MR.strings.chart_type]
                    onChange = { selectedValue ->
                        val selectedVisualType =
                            ReportSeriesVisualType.entries.firstOrNull { it.value == selectedValue.value }
                                ?: ReportSeriesVisualType.BAR_CHART
                        val updatedSeries =
                            series.copy(reportSeriesVisualType = selectedVisualType)
                        props.onSeriesChanged(updatedSeries)
                    }
                }

                // Time Range Dropdown
                UstadMessageIdSelectField {
                    id = "Time Range"
                    value = series.reportTimeRange?.value ?: 0
                    options = ReportTimeRange.entries.map { timeRange ->
                        MessageIdOption2(
                            stringResource = timeRange.stringResource,
                            value = timeRange.value
                        )
                    }
                    label = strings[MR.strings.time_range]
                    onChange = { selectedValue ->
                        val selectedTimeRange =
                            ReportTimeRange.entries.firstOrNull { it.value == selectedValue.value }
                                ?: ReportTimeRange.LAST_WEEK
                        val updatedSeries = series.copy(reportTimeRange = selectedTimeRange)
                        props.onSeriesChanged(updatedSeries)
                    }
                }
                // Filters section converted to MUI:
                if (series.reportSeriesFilters?.isNotEmpty() == true) {
                    // Display filters header
                    Typography {
                        variant = TypographyVariant.h6
                        +strings[MR.strings.filters]
                    }
                }
                series.reportSeriesFilters?.forEachIndexed { index, reportFilter2 ->
                    Stack {
                        direction = responsive(StackDirection.row)
                        spacing = responsive(8.px)
                        val fieldName = reportFilter2.reportFilterField?.name?.lowercase()
                            ?.replaceFirstChar { it.uppercase() } ?: ""
                        val comparisonSymbol = reportFilter2.reportFilterCondition?.symbol ?: ""
                        val filterText = "$fieldName $comparisonSymbol ${reportFilter2.reportFilterValue}"

                        Typography {
                            variant = TypographyVariant.h6
                            +ReactNode(filterText)
                        }
                        // Button to remove filter
                        IconButton {
                            onClick = {
                                props.onRemoveFilter(index, series.reportSeriesUid)
                            }
                            Icon {
                                sx {
                                    width = 20.px
                                    height = 20.px
                                }
                                Close {
                                    sx {
                                        width = 20.px
                                        height = 20.px
                                    }
                                }
                            }
                        }
                    }
                }
                Button {
                    id = "Filter add Button"
                    fullWidth = true
                    onClick = { props.onAddFilter(series.reportSeriesUid) }
                    variant = ButtonVariant.outlined
                    +strings[MR.strings.add_filter]
                }
            }
            Button {
                id = "Series add Button"
                fullWidth = true
                onClick = { props.onAddSeries() }
                variant = ButtonVariant.outlined
                +strings[MR.strings.add_series]
            }
        }
    }
}

val ReportEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ReportEditViewModel(di, savedStateHandle)
    }
    val uiStateVar by viewModel.uiState.collectAsState(
        ReportEditUiState(), Dispatchers.Main.immediate
    )
    ReportEditScreenComponent2 {
        this.uiState = uiStateVar
        onEntityChanged = viewModel::onEntityChanged
        onSeriesChanged = viewModel::onSeriesChanged
        onAddSeries = viewModel::onAddSeries
        onAddFilter = viewModel::onAddFilter
        onRemoveFilter = viewModel::onRemoveFilter
    }
}
