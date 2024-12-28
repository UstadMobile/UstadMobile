package com.ustadmobile.libuicompose.view.report.filteredit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportFilter2
import com.ustadmobile.core.impl.locale.entityconstants.FieldConstants
import com.ustadmobile.core.impl.locale.entityconstants.FilterFieldConstants
import com.ustadmobile.core.util.MessageIdOption3
import com.ustadmobile.core.viewmodel.ReportFilterEditUiState
import com.ustadmobile.core.viewmodel.ReportFilterEditViewModel
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.view.report.reportedit.EditReportDropdown
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
    onEntityChanged: (ReportFilter2?) -> Unit = {}
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
                    value = null,
                    label = stringResource(MR.strings.field),
                    options = FilterFieldConstants.FILTER_OPTIONS,
                    onOptionSelected = {selectedOption->

                    },
                )
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                UstadInputFieldLayout(
                    modifier = Modifier
                        .weight(0.2F),
                ) {
                    EditFilterDropdown(
                        label = stringResource(MR.strings.condition),
                        options = FilterFieldConstants.FILTER_OPTIONS,
                        onOptionSelected = {
                        },
                        value = null,
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
                        value = "",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
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
    options: List<MessageIdOption3>,
    onOptionSelected: (MessageIdOption3) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    supportingText: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        UstadExposedDropDownMenuField(
            value = options.firstOrNull { it.value == value },
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