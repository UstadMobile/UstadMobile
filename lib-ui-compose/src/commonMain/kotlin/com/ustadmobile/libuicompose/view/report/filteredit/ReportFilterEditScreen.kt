package com.ustadmobile.libuicompose.view.report.filteredit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.locale.entityconstants.ConditionConstants
import com.ustadmobile.core.impl.locale.entityconstants.ContentCompletionStatusConstants
import com.ustadmobile.core.impl.locale.entityconstants.FieldConstants
import com.ustadmobile.core.viewmodel.ReportFilterEditUiState
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.UidAndLabel
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadOutlinedTextField


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReportFilterEditScreen(
    uiState: ReportFilterEditUiState = ReportFilterEditUiState(),
    onClickNewItemFilter: () -> Unit = {},
    onReportFilterChanged: (ReportFilter?) -> Unit = {},
    onClickEditFilter: (UidAndLabel?) -> Unit = {},
    onClickRemoveFilter: (UidAndLabel?) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {

        item {
            UstadInputFieldLayout(
                modifier = Modifier.fillMaxWidth(),
                errorText = uiState.fieldError,
            ) {
                UstadMessageIdOptionExposedDropDownMenuField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.reportFilter?.reportFilterField ?: 0,
                    label = stringResource(MR.strings.report_filter_edit_field),
                    options = FieldConstants.FIELD_MESSAGE_IDS,
                    isError = uiState.fieldError != null,
                    enabled = uiState.fieldsEnabled,
                    onOptionSelected = {
                        onReportFilterChanged(uiState.reportFilter?.shallowCopy{
                            reportFilterField = it.value
                        })
                    },
                )
            }

        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                UstadInputFieldLayout(
                    modifier = Modifier.fillMaxWidth(),
                    errorText = uiState.conditionsError,
                ) {
                    UstadMessageIdOptionExposedDropDownMenuField(
                        modifier = Modifier.weight(0.5F),
                        value = uiState.reportFilter?.reportFilterCondition ?: 0,
                        label = stringResource(MR.strings.report_filter_edit_condition),
                        options = ConditionConstants.CONDITION_MESSAGE_IDS,
                        isError = uiState.conditionsError != null,
                        enabled = uiState.fieldsEnabled,
                        onOptionSelected = {
                            onReportFilterChanged(uiState.reportFilter?.shallowCopy{
                                reportFilterCondition = it.value
                            })
                        },
                    )
                }


                UstadInputFieldLayout(
                    modifier = Modifier.fillMaxWidth(),
                    errorText = uiState.valuesError,
                ) {
                    UstadMessageIdOptionExposedDropDownMenuField(
                        modifier = Modifier.weight(0.5F),
                        value = uiState.reportFilter?.reportFilterDropDownValue ?: 0,
                        label = stringResource(MR.strings.report_filter_edit_values),
                        options = ContentCompletionStatusConstants.CONTENT_COMPLETION_STATUS_MESSAGE_IDS,
                        isError = uiState.valuesError != null,
                        enabled = uiState.fieldsEnabled,
                        onOptionSelected = {
                            onReportFilterChanged(uiState.reportFilter?.shallowCopy{
                                reportFilterDropDownValue = it.value
                            })
                        },
                    )
                }

            }
        }

        if (uiState.reportFilterValueVisible){
            item {
                UstadOutlinedTextField(
                    value = uiState.reportFilter?.reportFilterValue ?: "",
                    label = stringResource(MR.strings.report_filter_edit_values),
                    errorText = uiState.valuesError,
                    enabled = uiState.fieldsEnabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        onReportFilterChanged(uiState.reportFilter?.shallowCopy{
                            reportFilterValue = it
                        })
                    }
                )
            }
        }

        if (uiState.reportFilterBetweenValueVisible){
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    UstadOutlinedTextField(
                        modifier = Modifier.weight(0.5F),
                        value = uiState.reportFilter?.reportFilterValueBetweenX ?: "",
                        label = stringResource(MR.strings.from),
                        errorText = uiState.valuesError,
                        enabled = uiState.fieldsEnabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            onReportFilterChanged(uiState.reportFilter?.shallowCopy{
                                reportFilterValueBetweenX = it
                            })
                        }
                    )

                    UstadOutlinedTextField(
                        modifier = Modifier.weight(0.5F),
                        value = uiState.reportFilter?.reportFilterValueBetweenY ?: "",
                        label = stringResource(MR.strings.toC),
                        errorText = uiState.valuesError,
                        enabled = uiState.fieldsEnabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            onReportFilterChanged(uiState.reportFilter?.shallowCopy{
                                reportFilterValueBetweenY = it
                            })
                        }
                    )
                }
            }
        }

        item {
            ListItem(
                modifier = Modifier.clickable {
                    onClickNewItemFilter()
                },
                headlineContent = { Text(uiState.createNewFilter) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "",
                    )
                }
            )
        }

        if (uiState.reportFilterUidAndLabelListVisible){
            items(
                items = uiState.uidAndLabelList,
                key = { it.uid }
            ){ uidAndLabel ->

                ListItem(
                    modifier = Modifier.clickable { onClickEditFilter(uidAndLabel) },
                    leadingContent = { Spacer(modifier = Modifier.size(24.dp)) },
                    headlineContent = { Text(uidAndLabel.labelName ?: "") },
                    trailingContent = {
                        IconButton(onClick = { onClickRemoveFilter(uidAndLabel) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(MR.strings.delete),
                            )
                        }
                    }
                )
            }
        }

    }
}