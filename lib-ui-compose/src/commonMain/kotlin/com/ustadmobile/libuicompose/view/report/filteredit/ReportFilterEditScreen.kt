package com.ustadmobile.libuicompose.view.report.filteredit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.Comparisons
import com.ustadmobile.core.domain.report.model.FilterType
import com.ustadmobile.core.domain.report.model.ReportFilter3
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditUiState
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditViewModel
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import com.ustadmobile.libuicompose.view.report.edit.LabeledDropdownMenu
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
                LabeledDropdownMenu(
                    selectedValue = uiState.filters?.reportFilterField?.value ?: 0,
                    label = stringResource(MR.strings.field),
                    options = FilterType.entries.map { filterType ->
                        MessageIdOption2(
                            stringResource = filterType.stringResource,
                            value = filterType.value
                        )
                    },
                    onOptionSelected = { selectedOption ->
                        val selectedFilterType =
                            FilterType.entries.firstOrNull { it.value == selectedOption.value }
                                ?: FilterType.PERSON_AGE
                        val updatedOptions =
                            uiState.filters?.copy(reportFilterField = selectedFilterType)
                        onEntityChanged(updatedOptions)
                    }
                )
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                UstadInputFieldLayout(
                    modifier = Modifier
                        .weight(0.3F),
                ) {
                    LabeledDropdownMenu(
                        label = stringResource(MR.strings.condition),
                        selectedValue = uiState.filters?.reportFilterCondition?.value ?:0,
                        options = uiState.filterConditionList?.comparisonTypes?.map { comparison ->
                            MessageIdOption2(
                                stringResource = comparison.stringResource,
                                value = comparison.value
                            )
                        } ?: emptyList(),
                        onOptionSelected = { selectedOption ->
                            val selectedComparison =
                                Comparisons.entries.firstOrNull { it.value == selectedOption.value }
                                    ?: Comparisons.EQUALS
                            val updatedOptions =
                                uiState.filters?.copy(reportFilterCondition = selectedComparison)
                            onEntityChanged(updatedOptions)
                        }
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(0.5F)
                        .defaultScreenPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    Text(
                        text = stringResource(MR.strings.value),
                    )
                    androidx.compose.material.OutlinedTextField(
                        value = uiState.filters?.reportFilterValue.toString(),
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
