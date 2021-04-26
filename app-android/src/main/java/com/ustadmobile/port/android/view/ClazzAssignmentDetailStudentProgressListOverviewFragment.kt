package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressOverviewListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary


class ClazzAssignmentDetailStudentProgressListOverviewFragment(): UstadListViewFragment<PersonWithAttemptsSummary, PersonWithAttemptsSummary>(),
        ClazzAssignmentDetailStudentProgressOverviewListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener {

    private var mPresenter: ClazzAssignmentDetailStudentProgressOverviewListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithAttemptsSummary>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ClazzAssignmentDetailStudentProgressOverviewListPresenter(requireContext(),
                arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = ContentEntryDetailAttemptsListFragment.PersonWithStatementDisplayListRecyclerAdapter(mPresenter)
        return view
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = TODO("Provide repo e.g. dbRepo.ClazzAssignmentWithMetricsDao")

    override var clazzAssignmentWithMetrics: ClazzAssignmentWithMetrics?
        get() = TODO("Not yet implemented")
        set(value) {

        }

}