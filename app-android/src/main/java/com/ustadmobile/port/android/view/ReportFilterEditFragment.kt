package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentReportFilterEditBinding
import com.toughra.ustadmobile.databinding.ItemUidlabelFilterListBinding
import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ReportFilterEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList


interface ReportFilterEditFragmentEventHandler {

    fun onClickNewItemFilter()

    fun onClickRemoveUidAndLabel(uidAndLabel: UidAndLabel)
}
class ReportFilterEditFragment : UstadEditFragment<ReportFilter>(), ReportFilterEditView,
        DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<IdOption>,
        ReportFilterEditFragmentEventHandler{

    private var mBinding: FragmentReportFilterEditBinding? = null

    private var mPresenter: ReportFilterEditPresenter? = null

    override val viewContext: Any
        get() = requireContext()

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

    override var uidAndLabelList: DoorLiveData<List<UidAndLabel>>? = null
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
                di, viewLifecycleOwner)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.edit_filters, R.string.edit_filters)


        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())

        findNavController().currentBackStackEntry?.savedStateHandle?.observeResult(this,
                ContentEntry::class.java) {
            val entry = it.firstOrNull() ?: return@observeResult

            mPresenter?.handleAddOrEditUidAndLabel(UidAndLabel().apply {
                uid = entry.contentEntryUid
                labelName = entry.title
            })
        }

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
        setEditFragmentTitle(R.string.edit_filters, R.string.edit_filters)
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
            navigateToPickEntityFromList(ContentEntry::class.java, R.id.content_entry_list_dest,
                    bundleOf(ContentEntryList2View.ARG_CONTENT_FILTER to
                                    ContentEntryList2View.ARG_LIBRARIES_CONTENT,
                            UstadView.ARG_PARENT_ENTRY_UID to UstadView.MASTER_SERVER_ROOT_ENTRY_UID.toString()))
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

            override fun areContentsTheSame(oldItem: UidAndLabel, newItem: UidAndLabel): Boolean {
                return oldItem.uid == newItem.uid
                        && oldItem.labelName == newItem.labelName
            }
        }

    }
}