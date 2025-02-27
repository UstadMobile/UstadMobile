package com.ustadmobile.view.report.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.FixedReportTimeRange
import com.ustadmobile.core.domain.report.model.RelativeReportTimeRange
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportTimeRangeOption
import com.ustadmobile.core.domain.report.model.ReportTimeRangeUnit
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.viewmodel.report.edit.ReportEditUiState
import com.ustadmobile.core.viewmodel.report.edit.ReportEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadDateField
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.util.ext.onTextChange
import kotlinx.coroutines.Dispatchers
import mui.icons.material.Close
import mui.material.Box
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
import react.dom.onChange
import react.useRequiredContext
import web.cssom.AlignItems
import web.cssom.Color
import web.cssom.JustifyContent
import web.cssom.pct
import web.cssom.px
import web.html.HTMLInputElement

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
                id = "title"
                value = props.uiState.reportOptions2.title ?: ""
                label = ReactNode(strings[MR.strings.title] + "*")
                onTextChange = { newValue ->
                    props.onEntityChanged(props.uiState.reportOptions2.copy(title = newValue))
                }
                helperText =
                    ReactNode(props.uiState.reportTitleError ?: strings[MR.strings.required])
                error = props.uiState.reportTitleError != null

            }

            // Time Range Dropdown
            val selected =
                ReportTimeRangeOption.entries.find {
                    it.timeRange == props.uiState.reportOptions2.timeRange
                } ?: run {
                    when (props.uiState.reportOptions2.timeRange) {
                        is RelativeReportTimeRange -> ReportTimeRangeOption.CUSTOM_PERIOD
                        is FixedReportTimeRange -> ReportTimeRangeOption.CUSTOM_DATE_RANGE
                        else -> null
                    }
                }
            FormControl {
                fullWidth = true
                error = props.uiState.timeRangeError != null
                InputLabel {
                    id = "time_range_label"
                    shrink = true
                    sx {
                        backgroundColor = Color(theme.palette.background.default)
                    }
                    +ReactNode(strings[MR.strings.time_range] + "*")
                }
                Select {
                    value = selected
                    id = "time_range"
                    labelId = "time_range_label"
                    fullWidth = true
                    onChange = { event, _ ->
                        val selectedOption = ReportTimeRangeOption.entries.find { it.name == event.target.value }
                        if (selectedOption != null) {
                            props.onEntityChanged(props.uiState.reportOptions2.copy(timeRange = selectedOption.timeRange))
                        }
                    }

                    ReportTimeRangeOption.entries.forEach { option ->
                        MenuItem {
                            value = option.name
                            +ReactNode(strings[option.label])
                        }
                    }
                }
            }

            // Show CustomPeriodInputs only if CUSTOM_PERIOD is selected
            if (selected == ReportTimeRangeOption.CUSTOM_PERIOD) {
                Stack {
                    direction = responsive(StackDirection.row)
                    spacing = responsive(8.px)
                    sx {
                        justifyContent = JustifyContent.spaceBetween
                        alignItems = AlignItems.center
                    }
                    TextField {
                        label = ReactNode(strings[MR.strings.quantity])
                        fullWidth = true
                        value = (props.uiState.reportOptions2.timeRange as RelativeReportTimeRange).reportUnitQuantity.toString()
                        onChange = { event ->
                            val target = event.target as? HTMLInputElement
                            val quantity = target?.value?.toIntOrNull() ?: 0
                            val newRange = RelativeReportTimeRange(
                                (props.uiState.reportOptions2.timeRange as RelativeReportTimeRange).reportUnit,
                                quantity
                            )
                            props.onEntityChanged(props.uiState.reportOptions2.copy(timeRange = newRange))
                        }
                        error = props.uiState.quantityError != null
                    }

                    Select {
                        fullWidth = true
                        value = (props.uiState.reportOptions2.timeRange as RelativeReportTimeRange).reportUnit.name
                        onChange = { event, _ ->
                            val newUnit = ReportTimeRangeUnit.valueOf(event.target.value)
                            val newRange = RelativeReportTimeRange(newUnit, (props.uiState.reportOptions2.timeRange as RelativeReportTimeRange).reportUnitQuantity)
                            props.onEntityChanged(props.uiState.reportOptions2.copy(timeRange = newRange))
                        }

                        ReportTimeRangeUnit.entries.forEach { unit ->
                            MenuItem {
                                value = unit.name
                                +ReactNode(unit.name)
                            }
                        }
                    }
                }
            }

            // Show CustomDateRangeInputs only if CUSTOM_DATE_RANGE is selected
            if (selected == ReportTimeRangeOption.CUSTOM_DATE_RANGE) {
                // Custom Date Range Inputs
                Stack {
                    direction = responsive(StackDirection.row)
                    spacing = responsive(8.px)
                    sx {
                        justifyContent = JustifyContent.spaceBetween
                        alignItems = AlignItems.center
                    }
                    UstadDateField {
                        fullWidth = true
                        id = "from_date"
                        timeInMillis =
                            (props.uiState.reportOptions2.timeRange as FixedReportTimeRange).from
                        label = ReactNode(strings[MR.strings.from])
                        timeZoneId = UstadMobileConstants.UTC
                        onChange = { newDate ->
                            val newRange = FixedReportTimeRange(
                                newDate,
                                (props.uiState.reportOptions2.timeRange as FixedReportTimeRange).to
                            )
                            props.onEntityChanged(props.uiState.reportOptions2.copy(timeRange = newRange))
                        }
                    }

                    UstadDateField {
                        fullWidth = true
                        id = "to_date"
                        timeInMillis =
                            (props.uiState.reportOptions2.timeRange as FixedReportTimeRange).to
                        label = ReactNode(strings[MR.strings.to_])
                        timeZoneId = UstadMobileConstants.UTC
                        onChange = { newDate ->
                            val newRange = FixedReportTimeRange(
                                (props.uiState.reportOptions2.timeRange as FixedReportTimeRange).from,
                                newDate
                            )
                            props.onEntityChanged(props.uiState.reportOptions2.copy(timeRange = newRange))
                        }
                    }
                }
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
                        id = "series_title"
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
                            id = "y_axis_label"
                            shrink = true
                            sx {
                                backgroundColor = Color(theme.palette.background.default)
                            }
                            +ReactNode(strings[MR.strings.y_axis] + "*")
                        }

                        Select {
                            value = series.reportSeriesYAxis?.name ?: ""
                            id = "y_axis"
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
                            id = "sub_group_label"
                            shrink = true
                            sx {
                                backgroundColor = Color(theme.palette.background.default)
                            }
                            +ReactNode(strings[MR.strings.subgroup_by] + "*")
                        }

                        Select {
                            value = series.reportSeriesSubGroup?.name ?: ""
                            id = "subgroup_by"
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
                        id = "chart_type_label"
                        shrink = true
                        sx {
                            backgroundColor = Color(theme.palette.background.default)
                        }
                        +ReactNode(strings[MR.strings.chart_type] + "*")
                    }

                    Select {
                        value = series.reportSeriesVisualType?.name ?: ""
                        id = "chart_type"
                        labelId = "chart_type_label"
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
                    id = "filter_add_button"
                    fullWidth = true
                    onClick = { props.onAddFilter(series.reportSeriesUid) }
                    variant = ButtonVariant.outlined
                    +strings[MR.strings.add_filter]
                }
            }
            Button {
                id = "series_add_button"
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