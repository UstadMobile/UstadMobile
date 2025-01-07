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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GenderType
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportTimeRange
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.report.edit.ReportEditUiState
import com.ustadmobile.core.viewmodel.report.edit.ReportEditViewModel
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
    UstadLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(bottom = 16.dp)
    ) {
        item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth()
                        .defaultScreenPadding()
                    ,
                    value = uiState.reportOptions2.title,
                    label = {Text(stringResource(MR.strings.title)+ "*")},
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
            ExposedDropdownMenu(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultScreenPadding(),
                selectedValue = uiState.reportOptions2.xAxis ?: 0,
                label = {Text(stringResource(MR.strings.x_axis)+ "*")},
                options = ReportXAxis.entries.map { xAxis ->
                    MessageIdOption2(
                        stringResource = xAxis.stringResource,
                        value = xAxis.value
                    )
                },
                onOptionSelected = {
                    val updatedOptions = uiState.reportOptions2.copy(xAxis = it.value)
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
                                    stringResource(MR.strings.series_title)+ "*",
                                )
                            },
                            singleLine = true,
                            onValueChange = { newTitle ->
                                val updatedSeries = seriesItem.copy(reportSeriesTitle = newTitle)
                                onSeriesChanged(updatedSeries)
                            },
                            isError = uiState.seriesTitleError != null,
                            supportingText = {
                                Text(
                                    uiState.seriesTitleError ?: stringResource(MR.strings.required)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultScreenPadding(),
                        label = {Text(stringResource(MR.strings.y_axis)+ "*")},
                        options = ReportSeriesYAxis.entries.map { yAxis ->
                            MessageIdOption2(
                                stringResource = yAxis.stringResource,
                                value = yAxis.value
                            )
                        },
                        selectedValue = seriesItem.reportSeriesYAxis?.value ?: 0,
                        onOptionSelected = { selectedOption ->
                            val selectedYAxis =
                                ReportSeriesYAxis.entries.firstOrNull { it.value == selectedOption.value }
                                    ?: ReportSeriesYAxis.NONE
                            onSeriesChanged(seriesItem.copy(reportSeriesYAxis = selectedYAxis))
                        },
                        isError = uiState.yAxisError != null,
                        supportingText = {
                            Text(
                                uiState.yAxisError ?: stringResource(MR.strings.required)
                            )
                        },
                    )

                    // Subgroup Dropdown
                    ExposedDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultScreenPadding(),
                        label = {Text(stringResource(MR.strings.subgroup_by))},
                        options = ReportXAxis.entries.map { xAxis ->
                            MessageIdOption2(
                                stringResource = xAxis.stringResource,
                                value = xAxis.value
                            )
                        },
                        selectedValue = seriesItem.reportSeriesSubGroup?.value ?: 0,
                        onOptionSelected = { selectedOption ->
                            val selectedXAxis =
                                ReportXAxis.entries.firstOrNull { it.value == selectedOption.value }
                                    ?: ReportXAxis.DAY
                            val updatedSeries =
                                seriesItem.copy(reportSeriesSubGroup = selectedXAxis)
                            onSeriesChanged(updatedSeries)
                        }
                    )


                    // Chart Type Dropdown
                    ExposedDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultScreenPadding(),
                        label = {Text(stringResource(MR.strings.chart_type))},
                        options = ReportSeriesVisualType.entries.map { visualType ->
                            MessageIdOption2(
                                stringResource = visualType.stringResource,
                                value = visualType.value
                            )
                        },
                        selectedValue = seriesItem.reportSeriesVisualType?.value ?: 0,
                        onOptionSelected = { selectedOption ->
                            val selectedVisualType =
                                ReportSeriesVisualType.entries.firstOrNull { it.value == selectedOption.value }
                                    ?: ReportSeriesVisualType.BAR_CHART
                            val updatedSeries =
                                seriesItem.copy(reportSeriesVisualType = selectedVisualType)
                            onSeriesChanged(updatedSeries)
                        }
                    )

                    // Time Range Dropdown
                    ExposedDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultScreenPadding(),
                        label = {Text(stringResource(MR.strings.time_range))},
                        options = ReportTimeRange.entries.map { timeRange ->
                            MessageIdOption2(
                                stringResource = timeRange.stringResource,
                                value = timeRange.value
                            )
                        },
                        selectedValue = seriesItem.reportTimeRange?.value ?: 0,
                        onOptionSelected = { selectedOption ->
                            val selectedTimeRange =
                                ReportTimeRange.entries.firstOrNull { it.value == selectedOption.value }
                                    ?: ReportTimeRange.LAST_WEEK
                            val updatedSeries = seriesItem.copy(reportTimeRange = selectedTimeRange)
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
fun ExposedDropdownMenu(
    options: List<MessageIdOption2>,
    selectedValue: Int,
    onOptionSelected: (MessageIdOption2) -> Unit,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
    ) {
    var isExpanded by remember { mutableStateOf(false) }
    val selectedOption = options.firstOrNull { it.value == selectedValue }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded }
    ) {
        OutlinedTextField(
            value = selectedOption?.stringResource?.let { stringResource(it) } ?: "",
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
                DropdownMenuItem(
                    onClick = {
                        onOptionSelected(option)
                        isExpanded = false
                    },
                    text = {
                        Text(stringResource(option.stringResource))
                    }
                )
            }
        }
    }
}
