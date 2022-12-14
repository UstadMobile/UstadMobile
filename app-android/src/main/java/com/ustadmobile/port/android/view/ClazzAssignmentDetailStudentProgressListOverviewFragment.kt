package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemAssignmentDetailAttemptBinding
import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressOverviewListPresenter
import com.ustadmobile.core.controller.SubmissionSummaryListener
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.PersonGroupAssignmentSummary
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class ClazzAssignmentDetailStudentProgressListOverviewFragment(): UstadListViewFragment<PersonGroupAssignmentSummary, PersonGroupAssignmentSummary>(),
        ClazzAssignmentDetailStudentProgressOverviewListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener {

    private var progressSummaryAdapter: AssignmentProgressSummaryRecyclerAdapter? = null
    private var mPresenter: ClazzAssignmentDetailStudentProgressOverviewListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonGroupAssignmentSummary>?
        get() = mPresenter

    override var autoMergeRecyclerViewAdapter: Boolean = false


    class PersonWithAssignmentStatementDisplayListRecyclerAdapter(var listener: SubmissionSummaryListener?):
            SelectablePagedListAdapter<PersonGroupAssignmentSummary,
                    PersonWithAssignmentStatementDisplayListRecyclerAdapter
                    .PersonWithStatementDisplayListViewHolder>(DIFF_CALLBACK) {

        class PersonWithStatementDisplayListViewHolder(val itemBinding: ItemAssignmentDetailAttemptBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonWithStatementDisplayListViewHolder {
            val itemBinding = ItemAssignmentDetailAttemptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.listener = listener
            itemBinding.selectablePagedListAdapter = this
            return PersonWithStatementDisplayListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PersonWithStatementDisplayListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.person = item
            holder.itemView.tag = item?.submitterUid
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            listener = null
        }

    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))
        progressSummaryAdapter = AssignmentProgressSummaryRecyclerAdapter(null)

        mPresenter = ClazzAssignmentDetailStudentProgressOverviewListPresenter(requireContext(),
                arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = PersonWithAssignmentStatementDisplayListRecyclerAdapter(mPresenter)

        mMergeRecyclerViewAdapter = ConcatAdapter(mUstadListHeaderRecyclerViewAdapter,progressSummaryAdapter, mDataRecyclerViewAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter


        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        super.onClick(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzAssignmentDao

    private val progressSummaryObserver = Observer<AssignmentProgressSummary?> {
        t ->  progressSummaryAdapter?.assignmentProgressSummaryVal = t
    }

    override var progressSummary: LiveData<AssignmentProgressSummary?>? = null
        get() = field
        set(value) {
            field?.removeObserver(progressSummaryObserver)
            field = value
            value?.observe(viewLifecycleOwner, progressSummaryObserver)
        }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonGroupAssignmentSummary> = object
            : DiffUtil.ItemCallback<PersonGroupAssignmentSummary>() {
            override fun areItemsTheSame(oldItem: PersonGroupAssignmentSummary,
                                         newItem: PersonGroupAssignmentSummary): Boolean {
                return oldItem.submitterUid == newItem.submitterUid
            }

            override fun areContentsTheSame(oldItem: PersonGroupAssignmentSummary,
                                            newItem: PersonGroupAssignmentSummary): Boolean {
                return oldItem.name == newItem.name
                        && oldItem.latestPrivateComment == newItem.latestPrivateComment
                        && oldItem.fileSubmissionStatus == newItem.fileSubmissionStatus
            }
        }
    }

}