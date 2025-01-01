package com.ustadmobile.libuicompose.view.report.reportedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportTimeRange
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.model.getComparisonSymbol
import com.ustadmobile.core.impl.locale.entityconstants.ReportSeriesVisualTypeConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportSeriesYAxisConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportTimeRangeConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportXAxisConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.report.ReportEditUiState
import com.ustadmobile.core.viewmodel.report.ReportEditViewModel
import com.ustadmobile.libuicompose.components.UstadExposedDropDownMenuField
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(MR.strings.title), fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.reportOptions2.title,
                    label = null,
                    singleLine = true,
                    onValueChange = { newTitle ->
                        val updatedOptions = uiState.reportOptions2.copy(title = newTitle)
                        onReportChanged(updatedOptions)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    supportingText = {
                    }
                )
            }
        }
        item {
            EditReportDropdown(
                value = uiState.reportOptions2.xAxis ?: 0,
                label = stringResource(MR.strings.x_axis),
                options = ReportXAxisConstants.X_AXIS_OPTIONS,
                onOptionSelected = {
                    val updatedOptions = uiState.reportOptions2.copy(xAxis = it.value)
                    onReportChanged(updatedOptions)
                },
            )
        }

        // Dynamically iterate over the series
        uiState.reportOptions2.series.forEach { seriesItem ->
            item {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultScreenPadding()
                        .height(2.dp)
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Series Title Input
                    Text(
                        stringResource(MR.strings.series_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = seriesItem.reportSeriesTitle,
                            label = null,
                            singleLine = true,
                            onValueChange = { newTitle ->
                                val updatedSeries = seriesItem.copy(reportSeriesTitle = newTitle)
                                onSeriesChanged(updatedSeries)
                            },
                            supportingText = {}
                        )
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove filter",
                            modifier = Modifier
                                .clickable {
                                    onRemoveSeries(seriesItem.reportSeriesUid)
                                }
                                .defaultItemPadding(start = 16.dp, bottom = 12.dp)
                        )
                    }

                    // Y Axis Dropdown
                    EditReportDropdown(
                        label = stringResource(MR.strings.y_axis),
                        options = ReportSeriesYAxis.entries.map { yAxis ->
                            MessageIdOption2(
                                stringResource = ReportSeriesYAxis.getStringResourceForYAxis(yAxis),
                                value = yAxis.value
                            )
                        },
                        value = seriesItem.reportSeriesYAxis?.value ?: 0,
                        onOptionSelected = { selectedOption ->
                            val selectedYAxis =
                                ReportSeriesYAxis.entries.firstOrNull { it.value == selectedOption.value }
                                    ?: ReportSeriesYAxis.NONE
                            onSeriesChanged(seriesItem.copy(reportSeriesYAxis = selectedYAxis))
                        }
                    )

                    // Subgroup Dropdown
                    EditReportDropdown(
                        label = stringResource(MR.strings.subgroup_by),
                        options = ReportXAxis.entries.map { xAxis ->
                            MessageIdOption2(
                                stringResource = ReportXAxis.getStringResourceForXAxis(xAxis),
                                value = xAxis.value
                            )
                        },
                        value = seriesItem.reportSeriesSubGroup?.value ?: 0,
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
                    EditReportDropdown(
                        label = stringResource(MR.strings.chart_type),
                        options = ReportSeriesVisualType.entries.map { visualType ->
                            MessageIdOption2(
                                stringResource = ReportSeriesVisualType.getStringResourceForVisualType(
                                    visualType
                                ),
                                value = visualType.value
                            )
                        },
                        value = seriesItem.reportSeriesVisualType?.value ?: 0,
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
                    EditReportDropdown(
                        label = stringResource(MR.strings.time_range),
                        options = ReportTimeRange.entries.map { timeRange ->
                            MessageIdOption2(
                                stringResource = ReportTimeRange.getStringResourceForTimeRange(
                                    timeRange
                                ),
                                value = timeRange.value
                            )
                        },
                        value = seriesItem.reportTimeRange?.value ?: 0,
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
                            reportFilter2.reportFilterCondition?.let { getComparisonSymbol(it) }
                        val filterText =
                            "$fieldName $comparisonSymbol ${reportFilter2.reportFilterValue}"

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
                    modifier = Modifier.fillMaxWidth()
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

@Composable
fun EditReportDropdown(
    value: Int,
    label: String,
    options: List<MessageIdOption2>,
    onOptionSelected: (MessageIdOption2) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    supportingText: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier) {
        Text(label, fontWeight = FontWeight.SemiBold)
        UstadExposedDropDownMenuField(
            value = options.firstOrNull { it.value == value },
            label = "",
            options = options,
            onOptionSelected = onOptionSelected,
            itemText = { stringResource(resource = it.stringResource) },
            modifier = modifier,
            isError = isError,
            enabled = enabled,
            supportingText = supportingText,
        )
    }
}