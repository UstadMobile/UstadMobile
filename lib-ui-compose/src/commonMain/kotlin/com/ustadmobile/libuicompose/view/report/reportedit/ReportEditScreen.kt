package com.ustadmobile.libuicompose.view.report.reportedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.ustadmobile.core.impl.locale.entityconstants.ReportSeriesVisualTypeConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportSeriesYAxisConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportTimeRangeConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportXAxisConstants
import com.ustadmobile.core.util.MessageIdOption3
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
        onRemoveFilter = viewModel::onRemoveFilter
    )
}

@Composable
private fun ReportEditScreen(
    uiState: ReportEditUiState = ReportEditUiState(),
    onReportChanged: (ReportOptions2) -> Unit = {},
    onAddFilter: () -> Unit = { },
    onAddSeries: () -> Unit = { },
    onSeriesChanged: (ReportSeries2) -> Unit = {},
    onRemoveFilter: (Int, Int) -> Unit = { _, _ -> }
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
                value = uiState.reportOptions2.xAxis ?: "",
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
                    Text(stringResource(MR.strings.series_title), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = seriesItem.reportSeriesTitle,
                        label = null,
                        singleLine = true,
                        onValueChange = { newTitle ->
                            val updatedSeries = seriesItem.copy(reportSeriesTitle = newTitle)
                            onSeriesChanged(updatedSeries)
                        },
                        supportingText = {}
                    )

                    // Y Axis Dropdown
                    EditReportDropdown(
                        label = stringResource(MR.strings.y_axis),
                        options = ReportSeriesYAxisConstants.Y_AXIS_OPTIONS,
                        value = seriesItem.reportSeriesYAxis?.name ?: "",
                        onOptionSelected = { selectedOption ->
                            val updatedSeries = seriesItem.copy(
                                reportSeriesYAxis = ReportSeriesYAxis.valueOf(selectedOption.value)
                            )
                            onSeriesChanged(updatedSeries)
                        }
                    )

                    // Subgroup Dropdown
                    EditReportDropdown(
                        label = stringResource(MR.strings.subgroup_by),
                        options = ReportXAxisConstants.X_AXIS_OPTIONS,
                        value = seriesItem.reportSeriesSubGroup?.name ?: "",
                        onOptionSelected = { selectedOption ->
                            val updatedSeries = seriesItem.copy(
                                reportSeriesSubGroup = ReportXAxis.valueOf(selectedOption.value)
                            )
                            onSeriesChanged(updatedSeries)
                        }
                    )

                    // Chart Type Dropdown
                    EditReportDropdown(
                        label = stringResource(MR.strings.chart_type),
                        options = ReportSeriesVisualTypeConstants.VISUAL_TYPE_OPTIONS,
                        value = seriesItem.reportSeriesVisualType?.name ?: "",
                        onOptionSelected = { selectedOption ->
                            val updatedSeries = seriesItem.copy(
                                reportSeriesVisualType = ReportSeriesVisualType.valueOf(
                                    selectedOption.value
                                )
                            )
                            onSeriesChanged(updatedSeries)
                        }
                    )

                    // Time Range Dropdown
                    EditReportDropdown(
                        label = stringResource(MR.strings.time_range),
                        options = ReportTimeRangeConstants.TIME_RANGE_OPTIONS,
                        value = seriesItem.reportTimeRange?.name ?: "",
                        onOptionSelected = { selectedOption ->
                            val updatedSeries = seriesItem.copy(
                                reportTimeRange = ReportTimeRange.valueOf(selectedOption.value)
                            )
                            onSeriesChanged(updatedSeries)
                        }
                    )
                }
            }
            // Filters Section
            item {
                if(!seriesItem.reportSeriesFilters.isNullOrEmpty()) {
                    Text(
                        text = stringResource(MR.strings.filters),
                    )
                }
                seriesItem.reportSeriesFilters?.forEachIndexed { index, reportFilter2 ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),

                        ) {
                        Text("filter.description", modifier = Modifier.weight(1f))
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
                Button(onClick = { onAddFilter() }, modifier = Modifier.fillMaxWidth()) {
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
    value: String,
    label: String,
    options: List<MessageIdOption3>,
    onOptionSelected: (MessageIdOption3) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    supportingText: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier) {
        Text(label, fontWeight = FontWeight.SemiBold)
        UstadExposedDropDownMenuField(
            value = options.firstOrNull { it.value.toString() == value },
            label = "",
            options = options,
            onOptionSelected = { selectedOption ->
                onOptionSelected(selectedOption)
            },
            itemText = { stringResource(resource = it.stringResource) },
            modifier = modifier,
            isError = isError,
            enabled = enabled,
            supportingText = supportingText,
        )
    }
}