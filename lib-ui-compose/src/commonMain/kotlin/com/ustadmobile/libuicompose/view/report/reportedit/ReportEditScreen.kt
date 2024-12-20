package com.ustadmobile.libuicompose.view.report.reportedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.impl.locale.entityconstants.ReportSeriesVisualTypeConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportSeriesYAxisConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportTimeRangeConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportXAxisConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.report.ReportUiState
import com.ustadmobile.core.viewmodel.report.ReportViewModel
import com.ustadmobile.libuicompose.components.UstadExposedDropDownMenuField
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ReportEditScreen(viewModel: ReportViewModel) {

    val uiState: ReportUiState by viewModel.uiState.collectAsState(ReportUiState())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding()
    ) {
        item {
            // Title Input
            EditReportTextField(
                label = "Title",
                placeholder = "Report title",
                onValueChange = { viewModel.onSeriesTitleChanged(it) },
                value = ""
            )
        }
        // X Axis Dropdown
        item {
            EditReportDropdown(
                value =uiState.selectedXAxis,
                label = "X Axis",
                options = ReportXAxisConstants.X_AXIS_OPTIONS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = { viewModel.onXAxisChanged(it) },
                isError = uiState.xAxisError != null,
            )
        }

        // Series Title Input
        item {
            EditReportTextField(
                label = "Series Title",
                value = uiState.seriesTitle,
                onValueChange = { viewModel.onSeriesTitleChanged(it) },
                placeholder = ""
            )
        }

        // Y Axis Dropdown
        item {
            EditReportDropdown(
                label = "Y Axis",
                options = ReportSeriesYAxisConstants.Y_AXIS_OPTIONS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = { viewModel.onYAxisChanged(it) },
                value = uiState.selectedYAxis
            )
        }

        // Subgroup Dropdown
        item {
            EditReportDropdown(
                label = "Subgroup by",
                options = ReportXAxisConstants.X_AXIS_OPTIONS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = { viewModel.onSubgroupChanged(it) },
                value = uiState.selectedYAxis
            )
        }

        // Chart Type Dropdown
        item {
            EditReportDropdown(
                label = "Chart Type",
                options = ReportSeriesVisualTypeConstants.VISUAL_TYPE_OPTIONS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = { viewModel.onChartTypeChanged(it) },
                value = uiState.selectedChartType
            )
        }

        // Time Range Dropdown
        item {
            EditReportDropdown(
                label = "Time Range",
                options = ReportTimeRangeConstants.TIME_RANGE_OPTIONS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = { viewModel.onTimeRangeChanged(it) },
                value = uiState.selectedTimeRange
            )
        }

        // Filters Section
        item {
            Text(
                text = "Filters",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        items(uiState.filters.size) { filter ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("filter.description", modifier = Modifier.padding(end = 8.dp))
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove filter",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { viewModel.onRemoveFilter(0) },
                )
            }
        }

        item {
            Button(onClick = { viewModel.onAddFilter() }, modifier = Modifier.fillMaxWidth()) {
                Text("+ Add Filter")
            }
        }

        item {
            Button(onClick = { viewModel.onAddSeries() }, modifier = Modifier.fillMaxWidth()) {
                Text("+ Add Series")
            }
        }
    }

}

@Composable
fun EditReportTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
            },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth()
        )
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
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        UstadExposedDropDownMenuField(
            value = options.firstOrNull { it.value == value },
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