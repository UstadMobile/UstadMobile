package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContententryTabsBinding
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DISPLAY_CONTENT_BY_OPTION
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DISPLAY_CONTENT_BY_PARENT
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter

class ContentEntryListTabsFragment : UstadBaseFragment(), ContentEntryListTabsView {

    private inner class ContentEntryTabsPagerAdapter(fa: FragmentManager, lifecycle: Lifecycle, viewList: List<String>): ViewNameListFragmentPagerAdapter(fa,
            lifecycle, viewList, mapOf(ContentEntryList2View.VIEW_NAME to ContentEntryList2Fragment::class.java)) {

    }

    private var mBinding: FragmentContententryTabsBinding? = null
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentContententryTabsBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mTabLayout: TabLayout = view.findViewById(R.id.tabs)
        val mPager: ViewPager2 = view.findViewById(R.id.home_contententry_viewpager)

        val defArgs = "${ContentEntryList2View.VIEW_NAME}?${ARG_PARENT_ENTRY_UID}=" +
                "${arguments?.get(ARG_PARENT_ENTRY_UID).toString()}&$ARG_DISPLAY_CONTENT_BY_OPTION="

        mPager.adapter = ContentEntryTabsPagerAdapter(childFragmentManager,
                lifecycle, listOf("$defArgs$ARG_DISPLAY_CONTENT_BY_PARENT"))
        val titleList = listOf(getString(R.string.libraries), getString(R.string.downloaded))

        mediator = TabLayoutMediator(mTabLayout, mPager) { tab, position ->
            tab.text = titleList[position]
        }
        mediator?.attach()

    }

    override fun onDestroyView() {
        mediator?.detach()
        mediator = null
        mBinding?.homeContententryViewpager?.adapter = null
        mBinding = null
        super.onDestroyView()
    }
}