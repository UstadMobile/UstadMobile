package com.ustadmobile.libuicompose.view.report.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.FixedReportTimeRange
import com.ustadmobile.core.domain.report.model.OptionWithLabelStringResource
import com.ustadmobile.core.domain.report.model.RelativeRangeReportPeriod
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportPeriodOption
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportTimeRangeUnit
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.viewmodel.report.edit.ReportEditUiState
import com.ustadmobile.core.viewmodel.report.edit.ReportEditViewModel
import com.ustadmobile.libuicompose.components.UstadDateField
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun ReportEditScreen(viewModel: ReportEditViewModel) {
    val uiState: ReportEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        ReportEditUiState(), Dispatchers.Main.immediate
    )
    ReportEditScreen(
        uiState = uiState,
        onReportChanged = viewModel::onEntityChanged,
        onAddFilter = viewModel::onAddFilter,
        onSeriesChanged = viewModel::onSeriesChanged,
        onAddSeries = viewModel::onAddSeries,
        onRemoveFilter = viewModel::onRemoveFilter,
        onRemoveSeries = viewModel::onRemoveSeries
    )
}

@Composable
private fun ReportEditScreen(
    uiState: ReportEditUiState = ReportEditUiState(),
    onReportChanged: (ReportOptions2) -> Unit = {},
    onAddFilter: (Int) -> Unit = { },
    onAddSeries: () -> Unit = { },
    onSeriesChanged: (ReportSeries2) -> Unit = {},
    onRemoveFilter: (Int, Int) -> Unit = { _, _ -> },
    onRemoveSeries: (Int) -> Unit = { },
) {
    val requiredYAxisType: YAxisTypes? = uiState.reportOptions2.series
        .mapNotNull { it.reportSeriesYAxis?.type }
        .distinct()
        .singleOrNull()

    UstadLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(bottom = 16.dp)
    ) {
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth()
                    .defaultScreenPadding(),
                value = uiState.reportOptions2.title,
                label = { Text(stringResource(MR.strings.title) + "*") },
                singleLine = true,
                onValueChange = { newTitle ->
                    val updatedOptions = uiState.reportOptions2.copy(title = newTitle)
                    onReportChanged(updatedOptions)
                },
                isError = uiState.reportTitleError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                supportingText = {
                    Text(
                        uiState.reportTitleError ?: stringResource(MR.strings.required)
                    )
                },
            )

        }
        item {
            // Determine selected option based on TIME RANGE TYPE
            val selected = remember(uiState.reportOptions2.period) {
                ReportPeriodOption.entries.find { option ->
                    when (val currentPeriod = uiState.reportOptions2.period) {
                        is RelativeRangeReportPeriod -> {
                            val optionPeriod = option.period as? RelativeRangeReportPeriod
                            optionPeriod?.rangeUnit == currentPeriod.rangeUnit &&
                                    optionPeriod?.rangeQuantity == currentPeriod.rangeQuantity
                        }

                        is FixedReportTimeRange -> {
                            option == ReportPeriodOption.CUSTOM_DATE_RANGE
                        }

                        else -> false
                    }
                } ?: run {
                    when (uiState.reportOptions2.period) {
                        is RelativeRangeReportPeriod -> ReportPeriodOption.CUSTOM_PERIOD
                        is FixedReportTimeRange -> ReportPeriodOption.CUSTOM_DATE_RANGE
                        else -> null
                    }
                }
            }

            // In ReportEditScreen's time range dropdown item
            ExposedDropdownMenu(
                label = { Text(stringResource(MR.strings.time_range) + "*") },
                options = ReportPeriodOption.entries,
                selectedValue = selected,
                onOptionSelected = { selectedOption ->
                    handleTimeRangeSelection(
                        selectedOption,
                        uiState.reportOptions2,
                        onReportChanged
                    )
                },
                isError = uiState.timeRangeError != null && selected == null,
                supportingText = {
                    if (selected == null) {
                        Text(uiState.timeRangeError ?: stringResource(MR.strings.required))
                    } else {
                        Text("")
                    }
                }
            )

            // Show CustomPeriodInputs only if selected is CUSTOM_PERIOD
            if (selected == ReportPeriodOption.CUSTOM_PERIOD) {
                // When selected is CUSTOM_PERIOD
                CustomPeriodInputs(
                    currentRange = uiState.reportOptions2.period as RelativeRangeReportPeriod,
                    onCustomPeriodChanged = { qty, unit ->
                        val newRange = RelativeRangeReportPeriod(unit, qty)
                        val updatedOptions = uiState.reportOptions2.copy(period = newRange)
                        onReportChanged(updatedOptions)
                    },
                    quantityError = uiState.quantityError
                )
            }

            // Show CustomDateRangeInputs only if selected is CUSTOM_DATE_RANGE
            if (selected == ReportPeriodOption.CUSTOM_DATE_RANGE) {
                CustomDateRangeInputs(
                    currentRange = uiState.reportOptions2.period as FixedReportTimeRange,
                    onDateRangeChanged = { from, to ->
                        val newRange = FixedReportTimeRange(from, to)
                        val updatedOptions = uiState.reportOptions2.copy(period = newRange)
                        onReportChanged(updatedOptions)
                    }
                )
            }
        }

        item {
            ExposedDropdownMenu(
                selectedValue = uiState.reportOptions2.xAxis,
                label = { Text(stringResource(MR.strings.x_axis) + "*") },
                options = ReportXAxis.entries,
                onOptionSelected = {
                    val updatedOptions = uiState.reportOptions2.copy(xAxis = it)
                    onReportChanged(updatedOptions)
                },
                isError = uiState.xAxisError != null,
                supportingText = {
                    Text(
                        uiState.xAxisError ?: stringResource(MR.strings.required)
                    )
                },
            )
        }

        // Dynamically iterate over the series
        uiState.reportOptions2.series.forEach { seriesItem ->
            item {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultScreenPadding(), thickness = 1.dp
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Series Title Input
                    Row(
                        modifier = Modifier.fillMaxWidth().defaultScreenPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = seriesItem.reportSeriesTitle,
                            label = {
                                Text(
                                    stringResource(MR.strings.series_title) + "*",
                                )
                            },
                            singleLine = true,
                            onValueChange = { newTitle ->
                                val updatedSeries = seriesItem.copy(reportSeriesTitle = newTitle)
                                onSeriesChanged(updatedSeries)
                            },
                            isError = uiState.seriesTitleErrors[seriesItem.reportSeriesUid] != null,
                            supportingText = {
                                Text(
                                    uiState.seriesTitleErrors[seriesItem.reportSeriesUid]
                                        ?: stringResource(MR.strings.required)
                                )
                            },
                        )
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(MR.strings.remove),
                            modifier = Modifier
                                .clickable {
                                    onRemoveSeries(seriesItem.reportSeriesUid)
                                }
                                .align(Alignment.CenterVertically)
                        )
                    }

                    // Y Axis Dropdown
                    ExposedDropdownMenu(
                        label = { Text(stringResource(MR.strings.y_axis) + "*") },
                        options = ReportSeriesYAxis.entries,
                        selectedValue = seriesItem.reportSeriesYAxis,
                        onOptionSelected = { selectedYAxis ->
                            val updatedSeries = seriesItem.copy(reportSeriesYAxis = selectedYAxis)
                            onSeriesChanged(updatedSeries)
                        },
                        isError = uiState.yAxisErrors[seriesItem.reportSeriesUid] != null,
                        supportingText = {
                            Text(
                                uiState.yAxisErrors[seriesItem.reportSeriesUid]
                                    ?: stringResource(MR.strings.required)
                            )
                        },
                        disabledOptions = requiredYAxisType?.let { requiredType ->
                            ReportSeriesYAxis.entries.filter { it.type != requiredType }
                        } ?: emptyList()
                    )


                    // Subgroup Dropdown
                    ExposedDropdownMenu(
                        label = { Text(stringResource(MR.strings.subgroup_by)) },
                        options = ReportXAxis.entries,
                        selectedValue = seriesItem.reportSeriesSubGroup,
                        onOptionSelected = { selectedXAxis ->
                            val updatedSeries =
                                seriesItem.copy(reportSeriesSubGroup = selectedXAxis)
                            onSeriesChanged(updatedSeries)
                        }
                    )


                    // Chart Type Dropdown
                    ExposedDropdownMenu(
                        label = { Text(stringResource(MR.strings.chart_type)) },
                        options = ReportSeriesVisualType.entries,
                        selectedValue = seriesItem.reportSeriesVisualType,
                        onOptionSelected = { selectedVisualType ->
                            val updatedSeries =
                                seriesItem.copy(reportSeriesVisualType = selectedVisualType)
                            onSeriesChanged(updatedSeries)
                        }
                    )

                }
            }

            // Filters Section
            item {
                if (!seriesItem.reportSeriesFilters.isNullOrEmpty()) {
                    Text(
                        text = stringResource(MR.strings.filters),
                        modifier = Modifier.defaultScreenPadding()
                    )
                }
                seriesItem.reportSeriesFilters?.forEachIndexed { index, reportFilter2 ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultScreenPadding()

                    ) {
                        val fieldName = reportFilter2.reportFilterField?.name?.lowercase()
                            ?.replaceFirstChar { it.uppercase() } ?: ""
                        val comparisonSymbol =
                            reportFilter2.reportFilterCondition?.symbol ?: ""
                        val filterText =
                            "$fieldName $comparisonSymbol ${reportFilter2.reportFilterValue?.lowercase()}"

                        Text(
                            text = filterText,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove filter",
                            modifier = Modifier
                                .clickable {
                                    onRemoveFilter(index, seriesItem.reportSeriesUid)
                                }
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { onAddFilter(seriesItem.reportSeriesUid) },
                    modifier = Modifier.fillMaxWidth().defaultScreenPadding()
                ) {
                    Text(
                        text = stringResource(MR.strings.add_filter),
                    )
                }
            }
        }

        item {
            Button(onClick = { onAddSeries() }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(MR.strings.add_series),
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : OptionWithLabelStringResource> ExposedDropdownMenu(
    options: List<T>,
    selectedValue: T?,
    disabledOptions: List<T> = emptyList(),
    modifier: Modifier = Modifier.fillMaxWidth(),
    isError: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    onOptionSelected: (T) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded }
    ) {
        OutlinedTextField(
            value = selectedValue?.let { stringResource(it.label) } ?: "",
            onValueChange = {},
            readOnly = true,
            modifier = modifier,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            supportingText = supportingText,
            isError = isError,
            label = label,
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            options.forEach { option ->
                val isDisabled = option in disabledOptions
                DropdownMenuItem(
                    onClick = {
                        if (!isDisabled) {
                            onOptionSelected(option)
                            isExpanded = false
                        }
                    },
                    text = {
                        Text(
                            stringResource(option.label),
                            color = if (isDisabled) Color.Gray else LocalContentColor.current
                        )
                    },
                    enabled = !isDisabled
                )
            }
        }
    }
}

fun handleTimeRangeSelection(
    selectedOption: ReportPeriodOption,
    currentOptions: ReportOptions2,
    onReportChanged: (ReportOptions2) -> Unit
) {
    val newOptions = currentOptions.copy(
        period = selectedOption.period
    )

    onReportChanged(newOptions)
}

@Composable
fun CustomPeriodInputs(
    currentRange: RelativeRangeReportPeriod,
    onCustomPeriodChanged: (Int, ReportTimeRangeUnit) -> Unit,
    quantityError: String?
) {
    var quantity by remember { mutableStateOf(currentRange.rangeQuantity.toString()) }
    var selectedUnit by remember { mutableStateOf(currentRange.rangeUnit) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1.2f),
            value = quantity,
            onValueChange = {
                quantity = it
                it.toIntOrNull()?.let { qty ->
                    onCustomPeriodChanged(qty, selectedUnit)
                }
            },
            label = { Text(stringResource(MR.strings.quantity)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = quantityError != null,
            supportingText = { quantityError?.let { Text(it) } },
        )


        ExposedDropdownMenu(
            modifier = Modifier.weight(0.8f),
            label = { Text(stringResource(MR.strings.unit)) },
            options = ReportTimeRangeUnit.entries,
            selectedValue = selectedUnit,
            onOptionSelected = { unit ->
                selectedUnit = unit
                quantity.toIntOrNull()?.let { qty ->
                    onCustomPeriodChanged(qty, unit)
                }
            }
        )
    }
}

@Composable
fun CustomDateRangeInputs(
    currentRange: FixedReportTimeRange,
    onDateRangeChanged: (Long, Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DatePickerButton(
            label = stringResource(MR.strings.from),
            timestamp = currentRange.fromDateMillis,
            onDateSelected = { newFrom ->
                onDateRangeChanged(newFrom, currentRange.toDateMillis)
            },
            modifier = Modifier.weight(1f)
        )

        DatePickerButton(
            label = stringResource(MR.strings.to_),
            timestamp = currentRange.toDateMillis,
            onDateSelected = { newTo ->
                onDateRangeChanged(currentRange.fromDateMillis, newTo)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DatePickerButton(
    label: String,
    timestamp: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        UstadDateField(
            modifier = Modifier.fillMaxWidth(),
            value = timestamp,
            label = { Text(label) },
            timeZoneId = UstadMobileConstants.UTC,
            onValueChange = {
                onDateSelected(it)
            },
            supportingText = {}
        )
    }
}
