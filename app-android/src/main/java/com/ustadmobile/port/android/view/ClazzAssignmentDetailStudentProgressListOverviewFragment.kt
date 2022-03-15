package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressOverviewListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter


class ClazzAssignmentDetailStudentProgressListOverviewFragment(): UstadListViewFragment<PersonWithAttemptsSummary, PersonWithAttemptsSummary>(),
        ClazzAssignmentDetailStudentProgressOverviewListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener {

    private var progressSummaryAdapter: AssignmentProgressSummaryRecyclerAdapter? = null
    private var mPresenter: ClazzAssignmentDetailStudentProgressOverviewListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithAttemptsSummary>?
        get() = mPresenter

    override var autoMergeRecyclerViewAdapter: Boolean = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ClazzAssignmentDetailStudentProgressOverviewListPresenter(requireContext(),
                arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))
        progressSummaryAdapter = AssignmentProgressSummaryRecyclerAdapter(null)
        mDataRecyclerViewAdapter = ContentEntryDetailAttemptsListFragment.PersonWithStatementDisplayListRecyclerAdapter(mPresenter)

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

}