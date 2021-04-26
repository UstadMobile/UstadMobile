package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.lib.db.entities.ClazzAssignment


class ClazzAssignmentDetailStudentProgressFragment(): UstadListViewFragment<ClazzAssignment, ClazzAssignment>(),
        ClazzAssignmentDetailStudentProgressView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ClazzAssignmentDetailStudentProgressPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzAssignment>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ClazzAssignmentDetailStudentProgressPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = ClazzAssignmentDetailStudentProgressRecyclerAdapter(mPresenter)
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
        get() = TODO("Provide repo e.g. dbRepo.ClazzAssignmentDao")

}