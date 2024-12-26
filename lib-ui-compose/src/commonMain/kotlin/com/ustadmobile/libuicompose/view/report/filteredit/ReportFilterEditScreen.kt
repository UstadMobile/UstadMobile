package com.ustadmobile.libuicompose.view.report.filteredit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.locale.entityconstants.FieldConstants
import com.ustadmobile.core.impl.locale.entityconstants.FilterFieldConstants
import com.ustadmobile.core.viewmodel.ReportFilterEditUiState
import com.ustadmobile.core.viewmodel.ReportFilterEditViewModel
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
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
    ReportFilterEditScreen(uiState)
}

@Composable
fun ReportFilterEditScreen(
    uiState: ReportFilterEditUiState = ReportFilterEditUiState(),
    onReportFilterChanged: (ReportFilter?) -> Unit = {},

    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        item {
            Column {
                androidx.compose.material.Text(
                    "Field",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                UstadInputFieldLayout(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    UstadMessageIdOptionExposedDropDownMenuField(
                        modifier = Modifier.fillMaxWidth(),
                        value = 0,
                        label = "Person age",
                        options = FilterFieldConstants.FILTER_OPTIONS,
                        onOptionSelected = {
                        },
                    )
                }
            }

        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Column(modifier = Modifier.weight(0.3F)) {
                    androidx.compose.material.Text(
                        "Condition",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    UstadMessageIdOptionExposedDropDownMenuField(
                        value = 0,
                        label = stringResource(MR.strings.report_filter_edit_field),
                        options = FieldConstants.FIELD_MESSAGE_IDS,
                        onOptionSelected = {
                        },
                    )
                }
                Column(modifier = Modifier.weight(0.5F)) {
                    androidx.compose.material.Text(
                        "Value",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
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