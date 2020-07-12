package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentReportEditBinding
import com.toughra.ustadmobile.databinding.ItemContentReportEditBinding
import com.toughra.ustadmobile.databinding.ItemPersonReportEditBinding
import com.toughra.ustadmobile.databinding.ItemVerbReportEditBinding
import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.VerbEntityListView.Companion.ARG_EXCLUDE_VERBUIDS_LIST
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList


interface ReportEditFragmentEventHandler {
    fun onClickNewPerson()
    fun onClickRemovePerson(person: ReportFilterWithDisplayDetails)
    fun onClickNewVerbDisplay()
    fun onClickRemoveVerb(verb: ReportFilterWithDisplayDetails)
    fun onClickAddNewContentFilter()
    fun onClickRemoveContent(content: ReportFilterWithDisplayDetails)
}

class ReportEditFragment : UstadEditFragment<ReportWithFilters>(), ReportEditView, ReportEditFragmentEventHandler,  DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<MessageIdOption> {
    private var mBinding: FragmentReportEditBinding? = null

    private var mPresenter: ReportEditPresenter? = null

    override val viewContext: Any
        get() = requireContext()

    override val mEditPresenter: UstadEditPresenter<*, ReportWithFilters>?
        get() = mPresenter

    private var personRecyclerAdapter: PersonRecyclerAdapter? = null

    private var personRecyclerView: RecyclerView? = null

    private val personObserver = Observer<List<ReportFilterWithDisplayDetails>?> { t ->
        personRecyclerAdapter?.submitList(t)
    }

    private var verbDisplayRecyclerAdapter: VerbDisplayRecyclerAdapter? = null

    private var verbDisplayRecyclerView: RecyclerView? = null

    private val verbDisplayObserver = Observer<List<ReportFilterWithDisplayDetails>?> { t ->
        verbDisplayRecyclerAdapter?.submitList(t)
    }

    private var contentDisplayRecyclerAdapter: ContentDisplayRecyclerAdapter? = null

    private var contentDisplayRecyclerView: RecyclerView? = null

    private val contentDisplayObserver = Observer<List<ReportFilterWithDisplayDetails>?> { t ->
        contentDisplayRecyclerAdapter?.submitList(t)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentReportEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
            it.xAxisSelectionListener = this
        }

        personRecyclerView = rootView.findViewById(R.id.fragment_edit_who_filter_list)
        personRecyclerAdapter = PersonRecyclerAdapter(this, null)
        personRecyclerView?.adapter = personRecyclerAdapter
        personRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        verbDisplayRecyclerView = rootView.findViewById(R.id.fragment_edit_did_filter_list)
        verbDisplayRecyclerAdapter = VerbDisplayRecyclerAdapter(this, null)
        verbDisplayRecyclerView?.adapter = verbDisplayRecyclerAdapter
        verbDisplayRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        contentDisplayRecyclerView = rootView.findViewById(R.id.fragment_edit_content_filter_list)
        contentDisplayRecyclerAdapter = ContentDisplayRecyclerAdapter(this, null)
        contentDisplayRecyclerView?.adapter = contentDisplayRecyclerAdapter
        contentDisplayRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = ReportEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        personRecyclerAdapter?.presenter = mPresenter
        verbDisplayRecyclerAdapter?.presenter = mPresenter
        contentDisplayRecyclerAdapter?.presenter = mPresenter


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.report)

        val navController = findNavController()

        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Person::class.java) {
            val person = it.firstOrNull() ?: return@observeResult
            val filterDetail = ReportFilterWithDisplayDetails()
            filterDetail.entityUid = person.personUid
            filterDetail.entityType = ReportFilter.PERSON_FILTER
            filterDetail.person = person
            mPresenter?.handleAddOrEditPerson(filterDetail)
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                VerbDisplay::class.java) {
            val verb = it.firstOrNull() ?: return@observeResult
            val filterDetail = ReportFilterWithDisplayDetails()
            filterDetail.entityType = ReportFilter.VERB_FILTER
            filterDetail.entityUid = verb.verbUid
            filterDetail.verb = VerbEntity(verb.verbUid, verb.urlId)
            filterDetail.xlangMapDisplay = verb.display
            mPresenter?.handleAddOrEditVerbDisplay(filterDetail)
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                ContentEntry::class.java) {
            val entry = it.firstOrNull() ?: return@observeResult
            val filterDetail = ReportFilterWithDisplayDetails()
            filterDetail.entityType = ReportFilter.CONTENT_FILTER
            filterDetail.entityUid = entry.contentEntryUid
            filterDetail.contentEntry = entry
            mPresenter?.handleAddOrEditContent(filterDetail)
        }

    }


    class ContentDisplayRecyclerAdapter(val activityEventHandler: ReportEditFragmentEventHandler,
                                        var presenter: ReportEditPresenter?) : ListAdapter<ReportFilterWithDisplayDetails, ContentDisplayRecyclerAdapter.ContentDisplayViewHolder>(DIFF_CALLBACK_PERSON) {

        class ContentDisplayViewHolder(val binding: ItemContentReportEditBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentDisplayViewHolder {
            val viewHolder = ContentDisplayViewHolder(ItemContentReportEditBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.activityEventHandler = activityEventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: ContentDisplayViewHolder, position: Int) {
            holder.binding.filter = getItem(position)
        }
    }

    override var contentFilterList: DoorMutableLiveData<List<ReportFilterWithDisplayDetails>>? = null
        get() = field
        set(value) {
            field?.removeObserver(contentDisplayObserver)
            field = value
            value?.observe(this, contentDisplayObserver)
        }


    class PersonRecyclerAdapter(val activityEventHandler: ReportEditFragmentEventHandler,
                                var presenter: ReportEditPresenter?) : ListAdapter<ReportFilterWithDisplayDetails, PersonRecyclerAdapter.PersonViewHolder>(DIFF_CALLBACK_PERSON) {

        class PersonViewHolder(val binding: ItemPersonReportEditBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
            val viewHolder = PersonViewHolder(ItemPersonReportEditBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.activityEventHandler = activityEventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
            holder.binding.filter = getItem(position)
        }
    }

    override fun onClickNewPerson() {
        onSaveStateToBackStackStateHandle()
        val list = personFilterList?.value?.map { it.entityUid }
        navigateToPickEntityFromList(ReportFilterWithDisplayDetails::class.java,
                R.id.person_list_dest,
                bundleOf(PersonListView.ARG_EXCLUDE_PERSONUIDS_LIST to list?.joinToString()))

    }

    override fun onClickRemovePerson(person: ReportFilterWithDisplayDetails) {
        mPresenter?.handleRemovePerson(person)
    }

    override var personFilterList: DoorMutableLiveData<List<ReportFilterWithDisplayDetails>>? = null
        set(value) {
            field?.removeObserver(personObserver)
            field = value
            value?.observe(this, personObserver)
        }


    class VerbDisplayRecyclerAdapter(val activityEventHandler: ReportEditFragmentEventHandler,
                                     var presenter: ReportEditPresenter?) : ListAdapter<ReportFilterWithDisplayDetails, VerbDisplayRecyclerAdapter.VerbDisplayViewHolder>(DIFF_CALLBACK_PERSON) {

        class VerbDisplayViewHolder(val binding: ItemVerbReportEditBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerbDisplayViewHolder {
            val viewHolder = VerbDisplayViewHolder(ItemVerbReportEditBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.activityEventHandler = activityEventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: VerbDisplayViewHolder, position: Int) {
            holder.binding.filter = getItem(position)
        }
    }

    override var verbFilterList: DoorMutableLiveData<List<ReportFilterWithDisplayDetails>>? = null
        set(value) {
            field?.removeObserver(verbDisplayObserver)
            field = value
            value?.observe(this, verbDisplayObserver)
        }


    override fun onClickNewVerbDisplay() {
        onSaveStateToBackStackStateHandle()
        val list = verbFilterList?.value?.map { it.entityUid }
        navigateToPickEntityFromList(VerbDisplay::class.java,
                R.id.verb_list_dest,
                bundleOf(ARG_EXCLUDE_VERBUIDS_LIST to list?.joinToString()))
    }

    override fun onClickRemoveVerb(verb: ReportFilterWithDisplayDetails) {
        mPresenter?.handleRemoveVerb(verb)
    }

    override fun onClickAddNewContentFilter() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(ContentEntry::class.java,
                R.id.content_entry_list_dest)
    }

    override fun onClickRemoveContent(content: ReportFilterWithDisplayDetails) {
        mPresenter?.handleRemoveContent(content)
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

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        personRecyclerView = null
        personRecyclerAdapter = null
        verbDisplayRecyclerAdapter = null
        verbDisplayRecyclerView = null
        contentDisplayRecyclerAdapter = null
        contentDisplayRecyclerView = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.report)
    }

    override var entity: ReportWithFilters? = null
        get() = field
        set(value) {
            field = value
            mBinding?.report = value
            mBinding?.chartOptions = this.chartOptions
            mBinding?.xAxisOptions = this.xAxisOptions
            mBinding?.yAxisOptions = this.yAxisOptions
            mBinding?.subGroupOptions = this.groupOptions
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var titleErrorText: String? = ""
        get() = field
        set(value) {
            field = value
            mBinding?.titleErrorText = value
        }

    override var chartOptions: List<ReportEditPresenter.ChartTypeMessageIdOption>? = null

    override var yAxisOptions: List<ReportEditPresenter.YAxisMessageIdOption>? = null

    override var xAxisOptions: List<ReportEditPresenter.XAxisMessageIdOption>? = null

    override var groupOptions: List<ReportEditPresenter.GroupByMessageIdOption>? = null
        get() = field
        set(value){
            field = value
            mBinding?.subGroupOptions = value
        }

    companion object {

        val DIFF_CALLBACK_PERSON = object : DiffUtil.ItemCallback<ReportFilterWithDisplayDetails>() {
            override fun areItemsTheSame(oldItem: ReportFilterWithDisplayDetails, newItem: ReportFilterWithDisplayDetails): Boolean {
                return oldItem.reportFilterUid == newItem.reportFilterUid
            }

            override fun areContentsTheSame(oldItem: ReportFilterWithDisplayDetails,
                                            newItem: ReportFilterWithDisplayDetails): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: MessageIdOption) {
        mPresenter?.handleXAxisSelected(selectedOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {

    }

}