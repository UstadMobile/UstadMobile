package com.ustadmobile.view.report.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportTimeRange
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.report.edit.ReportEditUiState
import com.ustadmobile.core.viewmodel.report.edit.ReportEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.util.ext.onTextChange
import kotlinx.coroutines.Dispatchers
import mui.icons.material.Close
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Divider
import mui.material.FormControl
import mui.material.FormHelperText
import mui.material.Icon
import mui.material.IconButton
import mui.material.InputLabel
import mui.material.MenuItem
import mui.material.Orientation
import mui.material.Select
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
import react.useRequiredContext
import web.cssom.AlignItems
import web.cssom.Color
import web.cssom.JustifyContent
import web.cssom.pct
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
    val theme by useRequiredContext(ThemeContext)
    val requiredYAxisType: YAxisTypes? =props.uiState.reportOptions2.series
        .mapNotNull { it.reportSeriesYAxis?.type }
        .distinct()
        .singleOrNull()

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
                helperText =
                    ReactNode(props.uiState.reportTitleError ?: strings[MR.strings.required])
                error = props.uiState.reportTitleError != null

            }

            // X Axis Selection
            FormControl {
                fullWidth = true
                error = props.uiState.xAxisError != null

                InputLabel {
                    id = "x_axis_label"
                    shrink = true
                    sx {
                        backgroundColor = Color(theme.palette.background.default)
                    }
                    +ReactNode(strings[MR.strings.x_axis] + "*")
                }

                Select {
                    value = props.uiState.reportOptions2.xAxis?.name
                        ?: ""
                    id = "x_axis"
                    labelId = "x_axis_label"
                    fullWidth = true

                    onChange = { event, _ ->
                        val selectedValue = ReportXAxis.entries.firstOrNull {
                            it.name == event.target.value
                        } ?: ReportXAxis.NONE

                        props.onEntityChanged(props.uiState.reportOptions2.copy(xAxis = selectedValue))
                    }

                    ReportXAxis.entries.forEach { option ->
                        MenuItem {
                            value = option.name
                            +ReactNode(strings[option.label])
                        }
                    }
                }


                FormHelperText {
                    +ReactNode(props.uiState.xAxisError ?: strings[MR.strings.required])
                }
            }

            Divider { orientation = Orientation.horizontal }

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
                        helperText = ReactNode(
                            props.uiState.seriesTitleError ?: strings[MR.strings.required]
                        )
                        error = props.uiState.seriesTitleError != null
                    }

                    // Y Axis Dropdown
                    FormControl {
                        fullWidth = true
                        error = props.uiState.yAxisError != null

                        InputLabel {
                            id = "Y Axis"
                            shrink = true
                            sx {
                                backgroundColor = Color(theme.palette.background.default)
                            }
                            +ReactNode(strings[MR.strings.y_axis] + "*")
                        }

                        Select {
                            value = series.reportSeriesYAxis?.name ?: ""
                            id = "Y Axis"
                            labelId = "y_axis_label"
                            fullWidth = true
                            onChange = { event, _ ->
                                val selectedValue =
                                    ReportSeriesYAxis.entries.firstOrNull { it.name == event.target.value }
                                        ?: ReportSeriesYAxis.NONE
                                props.onSeriesChanged(series.copy(reportSeriesYAxis = selectedValue))
                            }

                            ReportSeriesYAxis.entries.forEach { option ->
                                val isDisabled = requiredYAxisType != null && option.type != requiredYAxisType
                                MenuItem {
                                    value = option.name
                                    disabled = isDisabled  // Disable if it doesn’t match requiredYAxisType
                                    +ReactNode(strings[option.label])
                                }
                            }
                        }

                        FormHelperText {
                            +ReactNode(props.uiState.yAxisError ?: strings[MR.strings.required])
                        }
                    }


                    // Subgroup by Dropdown
                    FormControl {
                        fullWidth = true

                        InputLabel {
                            id = "Subgroup by"
                            shrink = true
                            sx {
                                backgroundColor = Color(theme.palette.background.default)
                            }
                            +ReactNode(strings[MR.strings.subgroup_by] + "*")
                        }

                        Select {
                            value = series.reportSeriesSubGroup?.name ?: ""
                            id = "Subgroup by"
                            labelId = "sub_group_label"
                            fullWidth = true
                            onChange = { event, _ ->
                                val selectedValue =
                                    ReportXAxis.entries.firstOrNull { it.name == event.target.value }
                                        ?: ReportXAxis.NONE
                                props.onSeriesChanged(series.copy(reportSeriesSubGroup = selectedValue))
                            }

                            ReportXAxis.entries.forEach { option ->
                                MenuItem {
                                    value = option.name
                                    +ReactNode(strings[option.label])
                                }
                            }
                        }
                    }
                }

                // Chart Type Dropdown
                FormControl {
                    fullWidth = true
                    InputLabel {
                        id = "Chart Type"
                        shrink = true
                        sx {
                            backgroundColor = Color(theme.palette.background.default)
                        }
                        +ReactNode(strings[MR.strings.chart_type] + "*")
                    }

                    Select {
                        value = series.reportSeriesVisualType?.name ?: ""
                        id = "Chart Type"
                        labelId = "y_axis_label"
                        fullWidth = true
                        onChange = { event, _ ->
                            val selectedValue =
                                ReportSeriesVisualType.entries.firstOrNull { it.name == event.target.value }
                                    ?: ReportSeriesVisualType.BAR_CHART
                            props.onSeriesChanged(series.copy(reportSeriesVisualType = selectedValue))
                        }

                        ReportSeriesVisualType.entries.forEach { option ->
                            MenuItem {
                                value = option.name
                                +ReactNode(strings[option.label])
                            }
                        }
                    }
                }

                // Time Range Dropdown
                FormControl {
                    fullWidth = true
                    InputLabel {
                        id = "Time Range"
                        shrink = true
                        sx {
                            backgroundColor = Color(theme.palette.background.default)
                        }
                        +ReactNode(strings[MR.strings.time_range] + "*")
                    }

                    Select {
                        value = series.reportTimeRange?.name ?: ""
                        id = "Time Range"
                        labelId = "time_range_label"
                        fullWidth = true
                        onChange = { event, _ ->
                            val selectedValue =
                                ReportTimeRange.entries.firstOrNull { it.name == event.target.value }
                                    ?: ReportTimeRange.LAST_WEEK
                            props.onSeriesChanged(series.copy(reportTimeRange = selectedValue))
                        }

                        ReportTimeRange.entries.forEach { option ->
                            MenuItem {
                                value = option.name
                                +ReactNode(strings[option.label])
                            }
                        }
                    }
                }

                if (series.reportSeriesFilters?.isNotEmpty() == true) {
                    Typography {
                        variant = TypographyVariant.h6
                        +strings[MR.strings.filters]
                    }
                }

                series.reportSeriesFilters?.forEachIndexed { index, reportFilter2 ->
                    Stack {
                        direction = responsive(StackDirection.row)
                        spacing = responsive(8.px)
                        sx {
                            width = 100.pct
                            justifyContent = JustifyContent.spaceBetween
                            alignItems = AlignItems.center
                        }
                        val fieldName = reportFilter2.reportFilterField?.name?.lowercase()
                            ?.replaceFirstChar { it.uppercase() } ?: ""
                        val comparisonSymbol = reportFilter2.reportFilterCondition?.symbol ?: ""
                        val filterText =
                            "$fieldName $comparisonSymbol ${reportFilter2.reportFilterValue}"

                        Typography {
                            variant = TypographyVariant.h6
                            +ReactNode(filterText)
                        }
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