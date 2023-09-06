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
import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.locale.entityconstants.ConditionConstants
import com.ustadmobile.core.impl.locale.entityconstants.ContentCompletionStatusConstants
import com.ustadmobile.core.impl.locale.entityconstants.FieldConstants
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ReportFilterEditView
import com.ustadmobile.core.viewmodel.ReportFilterEditUiState
import com.ustadmobile.door.lifecycle.LiveData
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
class ReportFilterEditFragment : UstadEditFragment<ReportFilter>(), ReportFilterEditView,
        DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<IdOption>,
        ReportFilterEditFragmentEventHandler{

    private var mBinding: FragmentReportFilterEditBinding? = null

    private var mPresenter: ReportFilterEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ReportFilter>?
        get() = mPresenter


    override var conditionsOptions: List<ReportFilterEditPresenter.ConditionMessageIdOption>? = null
        set(value) {
            field = value
            mBinding?.conditionOptions = value
            mBinding?.fragmentReportFilterEditDialogConditionText?.text?.clear()
            mBinding?.fragmentReportFilterEditDialogValuesText?.text?.clear()
            mBinding?.fragmentReportFilterEditDialogValuesNumberText?.text?.clear()
            mBinding?.fragmentReportFilterEditDialogValuesBetweenXText?.text?.clear()
            mBinding?.fragmentReportFilterEditDialogValuesBetweenYText?.text?.clear()
            uidAndLabelFilterItemRecyclerAdapter?.submitList(listOf())
            mPresenter?.clearUidAndLabelList()
        }

    override var dropDownValueOptions: List<MessageIdOption>? = null
        set(value) {
            field = value
            mBinding?.dropDownValueOptions = value
        }

    override var valueType: ReportFilterEditPresenter.FilterValueType? = null
        set(value) {
            field = value
            mBinding?.fragmentReportFilterEditDialogValuesNumberTextinputlayout?.visibility =
                    if(value == ReportFilterEditPresenter.FilterValueType.INTEGER)
                        View.VISIBLE else View.GONE
            mBinding?.fragmentReportFilterEditDialogValuesDropdownTextinputlayout?.visibility =
                    if(value == ReportFilterEditPresenter.FilterValueType.DROPDOWN)
                        View.VISIBLE else View.GONE
            mBinding?.itemFilterRv?.visibility =
                    if(value == ReportFilterEditPresenter.FilterValueType.LIST)
                        View.VISIBLE else View.GONE
            mBinding?.itemFilterCreateNew?.itemCreatenewLayout?.visibility =
            if(value == ReportFilterEditPresenter.FilterValueType.LIST)
                View.VISIBLE else View.GONE
            mBinding?.fragmentReportFilterEditDialogValuesBetweenXTextinputlayout?.visibility =
                    if(value == ReportFilterEditPresenter.FilterValueType.BETWEEN)
                        View.VISIBLE else View.GONE
            mBinding?.fragmentReportFilterEditDialogValuesBetweenYTextinputlayout?.visibility =
                    if(value == ReportFilterEditPresenter.FilterValueType.BETWEEN)
                        View.VISIBLE else View.GONE
        }

    override var fieldErrorText: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.fieldErrorText = value
            mBinding?.fragmentReportFilterEditDialogFieldTextinputlayout
                    ?.isErrorEnabled = value != null
        }
    override var conditionsErrorText: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.conditionsErrorText = value
            mBinding?.fragmentReportFilterEditDialogConditionTextinputlayout
                    ?.isErrorEnabled = value != null
        }
    override var valuesErrorText: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.valuesErrorText = value
            val errorEnabled = value != null
            mBinding?.fragmentReportFilterEditDialogValuesBetweenXTextinputlayout
                    ?.isErrorEnabled = errorEnabled
            mBinding?.fragmentReportFilterEditDialogValuesBetweenYTextinputlayout
                    ?.isErrorEnabled = errorEnabled
            mBinding?.fragmentReportFilterEditDialogValuesNumberTextinputlayout
                    ?.isErrorEnabled = errorEnabled
            mBinding?.fragmentReportFilterEditDialogValuesDropdownTextinputlayout
                    ?.isErrorEnabled = errorEnabled
        }

    override var uidAndLabelList: LiveData<List<UidAndLabel>>? = null
        get() = field
        set(value) {
            field?.removeObserver(uidAndLabelFilterItemObserver)
            field = value
            value?.observe(this, uidAndLabelFilterItemObserver)
        }

    override var createNewFilter: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.createNewFilter = value
        }

    override var fieldOptions: List<ReportFilterEditPresenter.FieldMessageIdOption>? = null
        set(value) {
            field = value
            mBinding?.fieldOptions = value
        }

    override var fieldsEnabled: Boolean = false
        set(value){
            super.fieldsEnabled = value
            field = value
        }

    override var entity: ReportFilter? = null
        get() = field
        set(value) {
            field = value
            mBinding?.reportFilter = value
        }


    class UidAndLabelFilterRecyclerAdapter(val activityEventHandler: ReportFilterEditFragmentEventHandler,
                                           var presenter: ReportFilterEditPresenter?): ListAdapter<UidAndLabel, UidAndLabelFilterRecyclerAdapter.UidAndLabelFilterItemViewHolder>(DIFF_CALLBACK_UID_LABEL) {

        class UidAndLabelFilterItemViewHolder(val binding: ItemUidlabelFilterListBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UidAndLabelFilterItemViewHolder {
            val viewHolder = UidAndLabelFilterItemViewHolder(ItemUidlabelFilterListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.eventHandler = activityEventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: UidAndLabelFilterItemViewHolder, position: Int) {
            holder.binding.uidAndLabel = getItem(position)
        }
    }

    private var uidAndLabelFilterItemRecyclerAdapter: UidAndLabelFilterRecyclerAdapter? = null

    private val uidAndLabelFilterItemObserver = Observer<List<UidAndLabel>?> {
        t -> uidAndLabelFilterItemRecyclerAdapter?.submitList(t)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentReportFilterEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fieldSelectionListener = this
            it.conditionSelectionListener = this
            uidAndLabelFilterItemRecyclerAdapter = UidAndLabelFilterRecyclerAdapter(this, null)
            it.itemFilterRv.adapter = uidAndLabelFilterItemRecyclerAdapter
            it.itemFilterRv.layoutManager = LinearLayoutManager(requireContext())
            it.activityEventHandler = this
        }

        mPresenter = ReportFilterEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(CR.string.edit_filters, CR.string.edit_filters)
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
        uidAndLabelFilterItemRecyclerAdapter?.presenter = mPresenter

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_done -> {
                onSaveStateToBackStackStateHandle()
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(CR.string.edit_filters, CR.string.edit_filters)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        uidAndLabelFilterItemRecyclerAdapter = null
    }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: IdOption) {
        mPresenter?.handleFieldOptionSelected(selectedOption)
        mPresenter?.handleConditionOptionSelected(selectedOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onClickNewItemFilter() {
        onSaveStateToBackStackStateHandle()
        if(entity?.reportFilterField == ReportFilter.FIELD_CONTENT_ENTRY) {
            mPresenter?.handleAddContentClicked()
        }else if(entity?.reportFilterField == ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON){
            mPresenter?.handleAddLeavingReasonClicked()
        }
    }

    override fun onClickRemoveUidAndLabel(uidAndLabel: UidAndLabel) {
        mPresenter?.handleRemoveUidAndLabel(uidAndLabel)
    }

    companion object {

        val DIFF_CALLBACK_UID_LABEL = object: DiffUtil.ItemCallback<UidAndLabel>() {
            override fun areItemsTheSame(oldItem: UidAndLabel, newItem: UidAndLabel): Boolean {
                return oldItem.uid == newItem.uid
            }

            /**
             * When using two-way binding we need to be sure that we are saving to the same
             * object in memory
             */
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: UidAndLabel,
                newItem: UidAndLabel
            ): Boolean {
                return oldItem === newItem
            }
        }

    }
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