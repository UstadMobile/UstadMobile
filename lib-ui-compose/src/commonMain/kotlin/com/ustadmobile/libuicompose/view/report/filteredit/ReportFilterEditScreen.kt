package com.ustadmobile.libuicompose.view.report.filteredit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.FilterType
import com.ustadmobile.core.domain.report.model.GenderType
import com.ustadmobile.core.domain.report.model.ReportFilter3
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditUiState
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditViewModel
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.view.report.edit.ExposedDropdownMenu
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun ReportFilterEditScreen(
    viewModel: ReportFilterEditViewModel
) {
    val uiState: ReportFilterEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        ReportFilterEditUiState(), Dispatchers.Main.immediate
    )
    ReportFilterEditScreen(
        uiState,
        onEntityChanged = viewModel::onEntityChanged
    )
}

@Composable
fun ReportFilterEditScreen(
    uiState: ReportFilterEditUiState = ReportFilterEditUiState(),
    onEntityChanged: (ReportFilter3?) -> Unit = {}
) {
    UstadLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        item {
            UstadInputFieldLayout(
                modifier = Modifier.fillMaxWidth(),
            ) {
                ExposedDropdownMenu(
                    selectedValue = uiState.filters?.reportFilterField,
                    label = { Text(stringResource(MR.strings.field) + "*") },
                    options = FilterType.entries,
                    onOptionSelected = { selectedOption ->
                        val updatedOptions =
                            uiState.filters?.copy(
                                reportFilterField = selectedOption,
                                reportFilterValue = null,
                                reportFilterCondition = null
                            )
                        onEntityChanged(updatedOptions)
                    }
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                ExposedDropdownMenu(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    label = { Text(stringResource(MR.strings.condition) + "*") },
                    selectedValue = uiState.filters?.reportFilterCondition,
                    options = uiState.filterConditionOptions?.comparisonTypes ?: emptyList(),
                    onOptionSelected = { selectedOption ->
                        val updatedOptions =
                            uiState.filters?.copy(reportFilterCondition = selectedOption)
                        onEntityChanged(updatedOptions)
                    }
                )


                // Dynamically switch between input types for the value field (weight = 3)
                if (uiState.filters?.reportFilterField == FilterType.PERSON_GENDER) {
                    ExposedDropdownMenu(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(MR.strings.value) + "*") },
                        selectedValue = GenderType.entries.firstOrNull { it.name == uiState.filters?.reportFilterValue },
                        options = GenderType.entries,
                        onOptionSelected = { selectedGender ->
                            val updatedFilter =
                                uiState.filters?.copy(reportFilterValue = selectedGender.name)
                            onEntityChanged(updatedFilter)
                        }
                    )

                } else {
                    OutlinedTextField(
                        label = { Text(stringResource(MR.strings.value) + "*") },
                        value = uiState.filters?.reportFilterValue ?: "",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            val updatedOptions = uiState.filters?.copy(reportFilterValue = it)
                            onEntityChanged(updatedOptions)
                        }
                    )
                }
            }
        }
    }
}
