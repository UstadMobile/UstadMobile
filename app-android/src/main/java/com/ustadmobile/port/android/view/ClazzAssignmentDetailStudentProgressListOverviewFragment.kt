package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressOverviewListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.lib.db.entities.StudentAssignmentProgress
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter


class ClazzAssignmentDetailStudentProgressListOverviewFragment(): UstadListViewFragment<PersonWithAttemptsSummary, PersonWithAttemptsSummary>(),
        ClazzAssignmentDetailStudentProgressOverviewListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener {

    private var studentProgressAdapter: StudentAssignmentProgressRecyclerAdapter? = null
    private var mPresenter: ClazzAssignmentDetailStudentProgressOverviewListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithAttemptsSummary>?
        get() = mPresenter

    override var autoMergeRecyclerViewAdapter: Boolean = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ClazzAssignmentDetailStudentProgressOverviewListPresenter(requireContext(),
                arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        studentProgressAdapter = StudentAssignmentProgressRecyclerAdapter(null)
        mDataRecyclerViewAdapter = ContentEntryDetailAttemptsListFragment.PersonWithStatementDisplayListRecyclerAdapter(mPresenter)

        mMergeRecyclerViewAdapter = ConcatAdapter(studentProgressAdapter, mDataRecyclerViewAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter


        return view
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

    override var studentProgress: StudentAssignmentProgress? = null
        get() = field
        set(value) {
            field = value
            studentProgressAdapter?.studentAssignmentProgressVal = studentProgress
        }

}