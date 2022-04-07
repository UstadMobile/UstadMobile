package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzDetailBinding
import com.ustadmobile.core.controller.ClazzDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.port.android.view.ext.createTabLayoutStrategy
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter


interface ClazzDetailFragmentEventHandler {

}

class ClazzDetailFragment: UstadDetailFragment<Clazz>(), ClazzDetailView, ClazzDetailFragmentEventHandler {

    private var mBinding: FragmentClazzDetailBinding? = null

    private var mPresenter: ClazzDetailPresenter? = null

    private var mPagerAdapter: ViewNameListFragmentPagerAdapter? = null

    private var mediator: TabLayoutMediator? = null

    override var tabs: List<String>? = null
        set(value) {
            if(field == value)
                return

            field = value

            if(value == null)
                return

            field = value
            mPagerAdapter = ViewNameListFragmentPagerAdapter(childFragmentManager, lifecycle,
                    value, VIEWNAME_TO_FRAGMENT_MAP)

            val pager = mBinding?.fragmentClazzDetailViewpager ?: return
            val tabList = mBinding?.fragmentClazzTabs?.tabs ?: return

            pager.adapter = mPagerAdapter

            mediator = TabLayoutMediator(tabList, pager,
                VIEWNAME_TO_TITLE_MAP.createTabLayoutStrategy(value, requireContext()))
            mediator?.attach()
        }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View

        //The fab will be managed by the underlying tabs
        fabManagementEnabled = false

        mBinding = FragmentClazzDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentClazzTabs.tabs.tabGravity = TabLayout.GRAVITY_FILL
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = ClazzDetailPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()

        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
        mediator = null
        mBinding?.fragmentClazzDetailViewpager?.adapter = null
        mPagerAdapter = null
        mBinding = null
        mPresenter = null
        entity = null
        tabs = null
    }

    override var entity: Clazz? = null
        set(value) {
            field = value
            ustadFragmentTitle = value?.clazzName
            mBinding?.clazz = value
        }

    companion object {
        val VIEWNAME_TO_FRAGMENT_MAP = mapOf<String, Class<out Fragment>>(
                ClazzDetailOverviewView.VIEW_NAME to ClazzDetailOverviewFragment::class.java,
                ClazzMemberListView.VIEW_NAME to ClazzMemberListFragment::class.java,
                ClazzLogListAttendanceView.VIEW_NAME to ClazzLogListAttendanceFragment::class.java,
                CourseGroupSetListView.VIEW_NAME to CourseGroupSetListFragment::class.java
        )

        val VIEWNAME_TO_TITLE_MAP = mapOf(
                ClazzDetailOverviewView.VIEW_NAME to R.string.overview,
                ClazzMemberListView.VIEW_NAME to R.string.members,
                ClazzLogListAttendanceView.VIEW_NAME to R.string.attendance,
                CourseGroupSetListView.VIEW_NAME to R.string.groups
        )

    }

}