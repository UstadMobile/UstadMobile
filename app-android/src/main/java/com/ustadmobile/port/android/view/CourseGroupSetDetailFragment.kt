package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCourseGroupMemberPersonDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.CourseGroupSetDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseGroupSetDetailView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


interface CourseGroupMemberPersonDetailFragmentEventHandler {

}

class CourseGroupSetDetailFragment: UstadDetailFragment<CourseGroupSet>(), CourseGroupSetDetailView, CourseGroupMemberPersonDetailFragmentEventHandler {

    private var repo: UmAppDatabase? = null
    private var memberListAdapter: CourseGroupMemberPersonListRecyclerAdapter? = null
    private var detailMergerRecyclerView: RecyclerView? = null
    private var mBinding: FragmentCourseGroupMemberPersonDetailBinding? = null

    private var mPresenter: CourseGroupSetDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentCourseGroupMemberPersonDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        detailMergerRecyclerView =
            rootView.findViewById(R.id.fragment_course_groupset_detail_overview)

        memberListAdapter = CourseGroupMemberPersonListRecyclerAdapter()

        detailMergerRecyclerView?.adapter = memberListAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        val accountManager: UstadAccountManager by instance()
        repo = di.direct.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter = CourseGroupSetDetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var memberList:  List<CourseGroupMemberPerson>? = null
        set(value) {
            field = value
            memberListAdapter?.submitList(value)
        }

    override var entity: CourseGroupSet? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = value?.cgsName
        }

}