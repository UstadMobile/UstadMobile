package com.ustadmobile.libuicompose.view.report.reportedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.impl.locale.entityconstants.ReportSeriesVisualTypeConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportSeriesYAxisConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportTimeRangeConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportXAxisConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.report.ReportEditUiState
import com.ustadmobile.core.viewmodel.report.ReportEditViewModel
import com.ustadmobile.libuicompose.components.UstadExposedDropDownMenuField
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
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
        onAddFilter = viewModel::onAddFilter
    )
}

@Composable
private fun ReportEditScreen(
    uiState: ReportEditUiState = ReportEditUiState(),
    onReportChanged: (ReportOptions2?) -> Unit = {},
    onAddFilter: () -> Unit = { },
    onAddSeries: () -> Unit = { },
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding()
    ) {
        item {
            Column {
                Text("Title", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                OutlinedTextField(
                    modifier = Modifier.testTag("first_names").fillMaxWidth(),
                    value = uiState.reportOptions2?.title ?: "",
                    label = { androidx.compose.material3.Text("Report title" + "*") },
                    singleLine = true,
                    onValueChange = { newTitle ->
                        val updatedOptions = uiState.reportOptions2?.copy(title = newTitle)
                            ?: ReportOptions2(title = newTitle)
                        onReportChanged(updatedOptions)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    supportingText = {
                    }
                )
            }
        }
        // X Axis Dropdown
        item {
            EditReportDropdown(
                value = uiState.reportOptions2?.xAxis ?: "",
                label = "X Axis",
                options = ReportXAxisConstants.X_AXIS_OPTIONS,
                onOptionSelected = {
                    val updatedOptions = uiState.reportOptions2?.copy(xAxis = it.value.toString())
                        ?: ReportOptions2(xAxis = it.value.toString())
                    onReportChanged(updatedOptions)
                },
            )
        }

        // Series Title Input
        item {
            Column {
                Text("Series Title", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                OutlinedTextField(
                    modifier = Modifier.testTag("series_title").fillMaxWidth(),
                    value = uiState.reportOptions2?.series?.firstOrNull()?.reportSeriesTitle ?: "",
                    label = { androidx.compose.material3.Text("Series Title" + "*") },
                    singleLine = true,
                    onValueChange = { newTitle ->
//                        val updatedSeries =
//                            uiState.reportOptions2?.series?.toMutableList() ?: mutableListOf()
//
//                        if (updatedSeries.isNotEmpty()) {
//                            updatedSeries[0] = updatedSeries[0].copy(reportSeriesTitle = newTitle)
//                        } else {
//                            updatedSeries.add(ReportSeries2(reportSeriesTitle = newTitle))
//                        }
//
//                        val updatedOptions = uiState.reportOptions2?.copy(series = updatedSeries)
//                            ?: ReportOptions2(series = updatedSeries)
//
//                        onReportChanged(updatedOptions)
                    },
                    supportingText = { }
                )
            }
        }


        // Y Axis Dropdown
        item {
            EditReportDropdown(
                label = "Y Axis",
                options = ReportSeriesYAxisConstants.Y_AXIS_OPTIONS,
                value = "",
                onOptionSelected = {

                }
            )
        }

//         Subgroup Dropdown
        item {
            EditReportDropdown(
                label = "Subgroup by",
                options = ReportXAxisConstants.X_AXIS_OPTIONS,
                onOptionSelected = {

                },
                value = ""
            )
        }

        // Chart Type Dropdown
        item {
            EditReportDropdown(
                label = "Chart Type",
                options = ReportSeriesVisualTypeConstants.VISUAL_TYPE_OPTIONS,
                onOptionSelected = {
                },
                value = ""
            )
        }

        // Time Range Dropdown
        item {
            EditReportDropdown(
                label = "Time Range",
                options = ReportTimeRangeConstants.TIME_RANGE_OPTIONS,
                onOptionSelected = {

                },

                value = ""
            )
        }

        // Filters Section
        item {
            Text(
                text = "Filters",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
//        itemsIndexed(uiState.reportOptions2?.series[0].reportSeriesFilters) { index, filter ->
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text("filter.description", modifier = Modifier.padding(end = 8.dp))
//                Icon(
//                    imageVector = Icons.Filled.Close,
//                    contentDescription = "Remove filter",
//                    modifier = Modifier
//                        .padding(8.dp)
//                        .clickable {
//                            onRemoveFilter(index)
//                        },
//                )
//            }
//        }

        item {
            Button(onClick = { onAddFilter() }, modifier = Modifier.fillMaxWidth()) {
                Text("+ Add Filter")
            }
        }

        item {
            Button(onClick = { onAddSeries() }, modifier = Modifier.fillMaxWidth()) {
                Text("+ Add Series")
            }
        }
    }
}

@Composable
fun EditReportDropdown(
    value: String,
    label: String,
    options: List<MessageIdOption2>,
    onOptionSelected: (MessageIdOption2) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    supportingText: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        UstadExposedDropDownMenuField(
            value = options.firstOrNull { it.value.toString() == value },
            label = label,
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