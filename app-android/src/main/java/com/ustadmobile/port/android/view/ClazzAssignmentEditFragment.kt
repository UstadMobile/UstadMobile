package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentEditBinding
import com.toughra.ustadmobile.databinding.ItemContentEntryBasicTitleListBinding
import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.port.android.util.ext.*
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady


interface ClazzAssignmentEditFragmentEventHandler {
    fun onClickNewContent()
    fun onClickDeleteContent(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)
}

class ClazzAssignmentEditFragment: UstadEditFragment<ClazzAssignment>(), ClazzAssignmentEditView, ClazzAssignmentEditFragmentEventHandler, DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<IdOption> {

    private var mBinding: FragmentClazzAssignmentEditBinding? = null

    private var mPresenter: ClazzAssignmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzAssignment>?
        get() = mPresenter


    class ContentEntryListAdapterRA(
            val activityEventHandler: ClazzAssignmentEditFragmentEventHandler)
        : ListAdapter<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
            ContentEntryListAdapterRA.ContentEntryListAdapterRAViewHolder>(ContentEntryList2Fragment.DIFF_CALLBACK) {

        class ContentEntryListAdapterRAViewHolder(
                val binding: ItemContentEntryBasicTitleListBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : ContentEntryListAdapterRAViewHolder {
            val viewHolder = ContentEntryListAdapterRAViewHolder(
                    ItemContentEntryBasicTitleListBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false)).apply{
                                this.binding.selectablePagedListAdapter = activityEventHandler
            }
            return viewHolder
        }

        override fun onBindViewHolder(holder: ContentEntryListAdapterRAViewHolder, position: Int) {
            holder.binding.entry = getItem(position)
        }
    }

    private var contentRecyclerAdapter: ContentEntryListAdapterRA? = null
    private var contentRecyclerView: RecyclerView? = null
    private val contentObserver = Observer<List<
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?>{
        t -> contentRecyclerAdapter?.submitList(t)
    }

    private var deadlineDateListener: View.OnClickListener? = View.OnClickListener {
        deadlineDate = Long.MAX_VALUE
    }

    private var deadlineDateTimeListener: View.OnClickListener? = View.OnClickListener {
        deadlineTime = 0
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzAssignmentEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
            it.typeSelectionListener = this
            it.caDeadlineDateTextinput.setEndIconOnClickListener(deadlineDateListener)
            it.caDeadlineDatetimeTextinput.setEndIconOnClickListener(deadlineDateTimeListener)
        }

        contentRecyclerView = rootView.findViewById(R.id.ca_recyclerview_content)
        contentRecyclerAdapter = ContentEntryListAdapterRA(this)
        contentRecyclerView?.adapter = contentRecyclerAdapter
        contentRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        mPresenter = ClazzAssignmentEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.new_assignment, R.string.edit_assignment)

        mPresenter?.onCreate(backStackSavedState)

        val navController = findNavController()

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
                ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::class.java){
            val contentEntrySelected = it.firstOrNull()?:return@observeResult
            mPresenter?.handleAddOrEditContent(contentEntrySelected)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        contentRecyclerView?.adapter = null
        contentRecyclerAdapter = null
        contentRecyclerView = null
        deadlineDateListener = null
        deadlineDateTimeListener = null
    }

    override var entity: ClazzAssignment? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzAssignment = value
            mBinding?.lateSubmissionVisibility = if(
                    value?.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY){
                View.VISIBLE
            }else{
                View.GONE
            }
            mBinding?.gracePeriodVisibility = if(
                    value?.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY ||
                    value?.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_ACCEPT){
                View.VISIBLE
            }else{
                View.GONE
            }

        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
    override var startDate: Long = 0L
        get() = mBinding?.startDate ?: 0L
        set(value) {
            field = value
            mBinding?.startDate = value
        }

    override var startTime: Long = 0L
        get() = mBinding?.startTime ?: 0L
        set(value) {
            field = value
            mBinding?.startTime = value
        }

    override var deadlineDate: Long = 0L
        get() = mBinding?.deadlineDate ?: 0L
        set(value) {
            field = value
            mBinding?.deadlineDate = value
        }
    override var deadlineTime: Long = 0L
        get() = mBinding?.deadlineTime ?: 0L
        set(value) {
            field = value
            mBinding?.deadlineTime = value
        }
    override var gracePeriodDate: Long = 0L
        get() = mBinding?.gracePeriodDate ?: 0L
        set(value) {
            field = value
            mBinding?.gracePeriodDate = value
        }

    override var gracePeriodTime: Long = 0L
        get() = mBinding?.gracePeriodTime ?: 0L
        set(value) {
            field = value
            mBinding?.gracePeriodTime = value
        }

    override var caGracePeriodError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caGracePeriodError = value
        }
    override var caDeadlineError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caDeadlineError = value
        }
    override var caTitleError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caTitleError = value
        }
    override var caStartDateError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caStartDateError = value
        }

    override var timeZone: String? = null
        set(value) {
            val newText = getText(R.string.class_timezone).toString() + " " + value
            mBinding?.timeZone = newText
            field = newText
        }

    override var lateSubmissionOptions: List<ClazzAssignmentEditPresenter.LateSubmissionOptionsMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
            mBinding?.lateSubmissionOptions = value
        }

    override var clazzAssignmentContent: DoorMutableLiveData
            <List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>? = null
        set(value) {
            field?.removeObserver(contentObserver)
            field = value
            value?.observeIfFragmentViewIsReady(this, contentObserver)
        }

    override fun onClickNewContent() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(
                ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::class.java,
                R.id.content_entry_list_dest,
                bundleOf(ContentEntryList2View.ARG_CLAZZ_ASSIGNMENT_FILTER to
                        entity?.caUid.toString(),
                        ContentEntryList2View.ARG_CONTENT_FILTER to
                                ContentEntryList2View.ARG_LIBRARIES_CONTENT,
                        UstadView.ARG_PARENT_ENTRY_UID to UstadView.MASTER_SERVER_ROOT_ENTRY_UID.toString(),
                        ContentEntryList2View.ARG_SELECT_FOLDER_VISIBLE to false.toString()))

    }

    override fun onClickDeleteContent(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        mPresenter?.handleRemoveContent(entry)
    }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: IdOption) {
        mBinding?.lateSubmissionVisibility = if(
                selectedOption.optionId == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY){
            View.VISIBLE
        }else{
            View.GONE
        }
        mBinding?.gracePeriodVisibility =if(
                selectedOption.optionId == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY ||
                selectedOption.optionId == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_ACCEPT){
            View.VISIBLE
        }else{
            View.GONE
        }
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {

    }

}