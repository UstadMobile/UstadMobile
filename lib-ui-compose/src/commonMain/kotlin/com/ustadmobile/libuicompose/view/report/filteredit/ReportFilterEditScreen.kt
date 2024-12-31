package com.ustadmobile.libuicompose.view.report.filteredit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.Comparisons
import com.ustadmobile.core.domain.report.model.FilterType
import com.ustadmobile.core.domain.report.model.ReportFilter3
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.ReportFilterEditUiState
import com.ustadmobile.core.viewmodel.ReportFilterEditViewModel
import com.ustadmobile.libuicompose.components.UstadExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        item {
            UstadInputFieldLayout(
                modifier = Modifier.fillMaxWidth(),
            ) {
                EditFilterDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.filters?.reportFilterField?.value?.toString() ?: "",
                    label = stringResource(MR.strings.field),
                    options = FilterType.entries.map { filterType ->
                        MessageIdOption2(
                            stringResource = FilterType.getStringResourceForFilterType(filterType),
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
                        println("updatedOptions: $updatedOptions")
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
                    EditFilterDropdown(
                        label = stringResource(MR.strings.condition),
                        value = uiState.filters?.reportFilterCondition?.value?.toString() ?: "",
                        options = Comparisons.entries.map { comparison ->
                            MessageIdOption2(
                                stringResource = Comparisons.getStringResourceForComparison(
                                    comparison
                                ),
                                value = comparison.value
                            )
                        },
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
                        .weight(0.5F),
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    Text(
                        text = stringResource(MR.strings.value),
                        fontWeight = FontWeight.SemiBold,
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

@Composable
fun EditFilterDropdown(
    value: String?,
    label: String,
    options: List<MessageIdOption2>,
    onOptionSelected: (MessageIdOption2) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    supportingText: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier) {
        Text(text = label, fontWeight = FontWeight.SemiBold, maxLines = 1)
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