package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentEditBinding
import com.toughra.ustadmobile.databinding.ItemContentEntryBasicTitleListBinding
import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.port.android.util.ext.*
import com.ustadmobile.port.android.view.binding.isSet
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady


class ClazzAssignmentEditFragment: UstadEditFragment<ClazzAssignment>(), ClazzAssignmentEditView, DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<IdOption> {

    private var mBinding: FragmentClazzAssignmentEditBinding? = null

    private var mPresenter: ClazzAssignmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzAssignment>?
        get() = mPresenter


    class ContentEntryListAdapterRA(
            var oneToManyEditListener: OneToManyJoinEditListener<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?)
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
                                this.binding.oneToManyJoinListener = oneToManyEditListener
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

    private var deadlineDateListener: View.OnClickListener = View.OnClickListener {
        val entityVal = entity
        entityVal?.caDeadlineDate = Long.MAX_VALUE
        entityVal?.caGracePeriodDate = Long.MAX_VALUE
        entityVal?.caLateSubmissionType = 0
        entityVal?.caLateSubmissionPenalty = 0
        entity = entityVal
    }

    var currentDeadlineDate: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzAssignmentEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.typeSelectionListener = this
            it.caDeadlineDateTextinput.setEndIconOnClickListener(deadlineDateListener)
            it.caDeadlineDate.doAfterTextChanged {
                if(it?.isNullOrEmpty() == true){
                    return@doAfterTextChanged
                }
                if(it.toString() == currentDeadlineDate){
                    mBinding?.takeIf { bind -> bind.lateSubmissionVisibility == View.GONE }.also {
                        mBinding?.lateSubmissionVisibility = View.VISIBLE
                    }
                    return@doAfterTextChanged
                }
                mBinding?.lateSubmissionVisibility = View.VISIBLE
                currentDeadlineDate = it.toString()
            }
        }

        contentRecyclerView = rootView.findViewById(R.id.ca_recyclerview_content)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.new_assignment, R.string.edit_assignment)

        mPresenter = ClazzAssignmentEditPresenter(requireContext(), arguments.toStringMap(),
                this, viewLifecycleOwner, di)

        mBinding?.contentOneToManyListener = mPresenter?.contentOneToManyJoinListener
        contentRecyclerAdapter = ContentEntryListAdapterRA(mPresenter?.contentOneToManyJoinListener)
        contentRecyclerView?.adapter = contentRecyclerAdapter
        contentRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter?.onCreate(backStackSavedState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        contentRecyclerView?.adapter = null
        contentRecyclerAdapter = null
        contentRecyclerView = null
    }

    override var entity: ClazzAssignment? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzAssignment = value
            mBinding?.lateSubmissionVisibility = if(value?.caDeadlineDate?.isSet == true){
                View.VISIBLE
            }else{
                View.GONE
            }
            mBinding?.penaltyVisiblity = if(
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


    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: IdOption) {
        mBinding?.penaltyVisiblity = if(
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