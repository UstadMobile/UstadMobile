package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.toughra.ustadmobile.databinding.FragmentClazzDetailBinding
import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.ClazzDetailViewModel
import com.ustadmobile.port.android.view.util.TabItemFragmentStateAdapter


class ClazzDetailFragment: UstadBaseMvvmFragment() {

    private var mBinding: FragmentClazzDetailBinding? = null


    private var mPagerAdapter: TabItemFragmentStateAdapter? = null

    private var mediator: TabLayoutMediator? = null

    private var mTabs: List<TabItem> = emptyList()
        set(value) {
            field = value
            mPagerAdapter?.setItems(value)
        }

    private val tabConfigStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position ->
        tab.text = mTabs[position].label
    }

    private val viewModel: ClazzDetailViewModel by ustadViewModels(::ClazzDetailViewModel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View

        mBinding = FragmentClazzDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentClazzTabs.tabs.tabGravity = TabLayout.GRAVITY_FILL

            mPagerAdapter = TabItemFragmentStateAdapter(
                childFragmentManager, lifecycle, VIEWNAME_TO_FRAGMENT_MAP
            )
            it.fragmentClazzDetailViewpager.adapter = mPagerAdapter

            mediator = TabLayoutMediator(
                it.fragmentClazzTabs.tabs, it.fragmentClazzDetailViewpager, tabConfigStrategy
            ).also{
                it.attach()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect {
                mTabs = it.tabs
            }
        }

        return rootView
    }

    override fun onDestroyView() {
        mediator?.detach()
        mBinding?.fragmentClazzDetailViewpager?.adapter = null
        mPagerAdapter = null
        mBinding = null
        mediator = null

        super.onDestroyView()
    }

    companion object {
        val VIEWNAME_TO_FRAGMENT_MAP = mapOf<String, Class<out Fragment>>(
                ClazzDetailOverviewView.VIEW_NAME to ClazzDetailOverviewFragment::class.java,
                ClazzMemberListView.VIEW_NAME to ClazzMemberListFragment::class.java,
                ClazzLogListAttendanceView.VIEW_NAME to ClazzLogListAttendanceFragment::class.java,
                CourseGroupSetListView.VIEW_NAME to CourseGroupSetListFragment::class.java
        )

    }

}