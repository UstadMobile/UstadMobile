package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemAssignmentDetailAttemptBinding
import com.ustadmobile.core.controller.AttemptListListener
import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressOverviewListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class ClazzAssignmentDetailStudentProgressListOverviewFragment(): UstadListViewFragment<PersonWithAttemptsSummary, PersonWithAttemptsSummary>(),
        ClazzAssignmentDetailStudentProgressOverviewListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener {

    private var progressSummaryAdapter: AssignmentProgressSummaryRecyclerAdapter? = null
    private var mPresenter: ClazzAssignmentDetailStudentProgressOverviewListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithAttemptsSummary>?
        get() = mPresenter

    override var autoMergeRecyclerViewAdapter: Boolean = false


    class PersonWithAssignmentStatementDisplayListRecyclerAdapter(var listener: AttemptListListener?):
            SelectablePagedListAdapter<PersonWithAttemptsSummary,
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
            holder.itemView.tag = item?.personUid
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

    override var progressSummary: DoorLiveData<AssignmentProgressSummary?>? = null
        get() = field
        set(value) {
            field?.removeObserver(progressSummaryObserver)
            field = value
            value?.observe(viewLifecycleOwner, progressSummaryObserver)
        }

    override var showMarked: Boolean = false
        set(value) {
            field = value
            progressSummaryAdapter?.showMarked = value
        }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithAttemptsSummary> = object
            : DiffUtil.ItemCallback<PersonWithAttemptsSummary>() {
            override fun areItemsTheSame(oldItem: PersonWithAttemptsSummary,
                                         newItem: PersonWithAttemptsSummary): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithAttemptsSummary,
                                            newItem: PersonWithAttemptsSummary): Boolean {
                return oldItem == newItem
            }
        }
    }

}