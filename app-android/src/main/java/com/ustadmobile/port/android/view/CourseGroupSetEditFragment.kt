package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCourseGroupSetEditBinding
import com.ustadmobile.core.controller.CourseGroupSetEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseGroupSetEditView
import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson
import com.ustadmobile.lib.db.entities.CourseGroupSet


interface CourseGroupSetEditFragmentEventHandler {

    fun handleAssignRandomGroupsClicked()

    fun handleNumberOfGroupsChanged(number: Int)
}

class CourseGroupSetEditFragment: UstadEditFragment<CourseGroupSet>(), CourseGroupSetEditView, CourseGroupSetEditFragmentEventHandler {

    private var mBinding: FragmentCourseGroupSetEditBinding? = null

    private var mPresenter: CourseGroupSetEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseGroupSet>?
        get() = mPresenter

    private var headerAdapter: CourseGroupSetHeaderAdapter? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null
    private var detailMergerRecyclerView: RecyclerView? = null
    private var courseGroupMemberEditAdapter: CourseGroupMemberEditAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentCourseGroupSetEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        detailMergerRecyclerView =
            rootView.findViewById(R.id.fragment_course_groupset_edit_overview)

        headerAdapter = CourseGroupSetHeaderAdapter(this)

        courseGroupMemberEditAdapter = CourseGroupMemberEditAdapter(this)

        detailMergerRecyclerAdapter = ConcatAdapter(headerAdapter, courseGroupMemberEditAdapter)
        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = CourseGroupSetEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: CourseGroupSet? = null
        get() = field
        set(value) {
            field = value
            headerAdapter?.courseGroupSet = value
        }

    override var memberList: List<CourseGroupMemberPerson>? = null
        get() = courseGroupMemberEditAdapter?.currentList ?: listOf()
        set(value){
            field = value
            courseGroupMemberEditAdapter?.submitList(value)
            courseGroupMemberEditAdapter?.notifyDataSetChanged()
        }
    override var groupList: List<IdOption>? = null
        set(value) {
            field = value
            courseGroupMemberEditAdapter?.groupList = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
        }

    override fun handleAssignRandomGroupsClicked() {
        mPresenter?.handleAssignRandomGroupsClicked()
    }

    override fun handleNumberOfGroupsChanged(number: Int) {
        mPresenter?.handleNumberOfGroupsChanged(number)
    }
}