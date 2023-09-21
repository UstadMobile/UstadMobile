package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentReportFilterEditBinding
import com.toughra.ustadmobile.databinding.ItemUidlabelFilterListBinding
import com.ustadmobile.core.impl.locale.entityconstants.ConditionConstants
import com.ustadmobile.core.impl.locale.entityconstants.ContentCompletionStatusConstants
import com.ustadmobile.core.impl.locale.entityconstants.FieldConstants
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.viewmodel.ReportFilterEditUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.core.R as CR

interface ReportFilterEditFragmentEventHandler {

    fun onClickNewItemFilter()

    fun onClickRemoveUidAndLabel(uidAndLabel: UidAndLabel)
}
class ReportFilterEditFragment : UstadBaseMvvmFragment() {

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ReportFilterEditScreen(
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
                    label = stringResource(CR.string.report_filter_edit_field),
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
                        label = stringResource(CR.string.report_filter_edit_condition),
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
                        label = stringResource(CR.string.report_filter_edit_values),
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
                UstadTextEditField(
                    value = uiState.reportFilter?.reportFilterValue ?: "",
                    label = stringResource(id = CR.string.report_filter_edit_values),
                    error = uiState.valuesError,
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
                    UstadTextEditField(
                        modifier = Modifier.weight(0.5F),
                        value = uiState.reportFilter?.reportFilterValueBetweenX ?: "",
                        label = stringResource(id = CR.string.from),
                        error = uiState.valuesError,
                        enabled = uiState.fieldsEnabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            onReportFilterChanged(uiState.reportFilter?.shallowCopy{
                                reportFilterValueBetweenX = it
                            })
                        }
                    )

                    UstadTextEditField(
                        modifier = Modifier.weight(0.5F),
                        value = uiState.reportFilter?.reportFilterValueBetweenY ?: "",
                        label = stringResource(id = CR.string.toC),
                        error = uiState.valuesError,
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
                text = { Text(uiState.createNewFilter) },
                icon = {
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
                    icon = { Spacer(modifier = Modifier.size(24.dp)) },
                    text = { Text(uidAndLabel.labelName ?: "") },
                    trailing = {
                        IconButton(onClick = { onClickRemoveFilter(uidAndLabel) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(id = CR.string.delete),
                            )
                        }
                    }
                )
            }
        }

    }
}

@Composable
@Preview
fun ReportFilterEditScreenPreview() {
    val uiStateVal = ReportFilterEditUiState(
        uidAndLabelList = listOf(
            UidAndLabel().apply {
                uid = 1
                labelName = "First Filter"
            },
            UidAndLabel().apply {
                uid = 2
                labelName = "Second Filter"
            }
        ),
        createNewFilter = "Create new filter",
        reportFilterValueVisible = true
    )
    MdcTheme {
        ReportFilterEditScreen(uiStateVal)
    }
}