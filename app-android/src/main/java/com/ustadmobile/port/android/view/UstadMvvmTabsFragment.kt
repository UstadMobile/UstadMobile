package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.toughra.ustadmobile.databinding.FragmentClazzDetailBinding
import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.port.android.view.util.TabItemFragmentStateAdapter

/**
 * Fragment that handles TabItems from any ViewModel that controls tabs e.g. ClazzDetail,
 * ClazzAssignmentDetail, ContentEntryDetail, etc.
 *
 * Child fragments should observe the viewmodel (e.g. launch in onViewCreated), then set mTabs.
 *
 * @param viewNameToFragmentMap Map of ViewName (e.g. ClazzDetailOverView.VIEW_NAME) to the Fragment
 * class that will be displayed that tab.
 */
abstract class UstadMvvmTabsFragment(
    private val viewNameToFragmentMap: Map<String, Class<out Fragment>>
): UstadBaseMvvmFragment() {

    private var mBinding: FragmentClazzDetailBinding? = null

    private var mPagerAdapter: TabItemFragmentStateAdapter? = null

    private var mediator: TabLayoutMediator? = null

    protected var mTabs: List<TabItem> = emptyList()
        set(value) {
            if(field == value)
                return

            field = value
            mPagerAdapter?.setItems(value)
        }

    private val tabConfigStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position ->
        tab.text = mTabs[position].label
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View

        mBinding = FragmentClazzDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentClazzTabs.tabs.tabGravity = TabLayout.GRAVITY_FILL

            mPagerAdapter = TabItemFragmentStateAdapter(
                childFragmentManager, lifecycle, viewNameToFragmentMap
            )
            it.fragmentClazzDetailViewpager.adapter = mPagerAdapter

            mediator = TabLayoutMediator(
                it.fragmentClazzTabs.tabs, it.fragmentClazzDetailViewpager, tabConfigStrategy
            ).also {
                it.attach()
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
        mTabs = emptyList()

        super.onDestroyView()
    }

}