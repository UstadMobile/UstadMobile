package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter
import kotlinx.android.synthetic.main.fragment_contententry_tabs.view.*

class ContentEntryListTabsFragment : UstadBaseFragment(), ContentEntryListTabsView {

    private inner class ContentEntryTabsPagerAdapter(fm: FragmentManager, viewList: List<String>, val titleList: List<String>): ViewNameListFragmentPagerAdapter(fm,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, viewList, mapOf(ContentEntryList2View.VIEW_NAME to ContentEntryList2Fragment::class.java),
            mapOf()) {

        override fun getPageTitle(position: Int) = titleList[position]
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_contententry_tabs, container, false)

        val mTabLayout: TabLayout = rootView.findViewById(R.id.home_contententry_tabs)
        rootView.fragmentAppBar.elevation = 10f
        val mPager: ViewPager = rootView.findViewById(R.id.home_contententry_viewpager)

        //Unfortunately if we dont use a Handler here then the first tab will not show up on first load
        Handler().post {
            val defArgs = "${ContentEntryList2View.VIEW_NAME}?${ARG_PARENT_ENTRY_UID}=" +
                    "${arguments?.get(ARG_PARENT_ENTRY_UID).toString()}&$ARG_CONTENT_FILTER="

            mPager.adapter = ContentEntryTabsPagerAdapter(childFragmentManager,
                    listOf("$defArgs$ARG_LIBRARIES_CONTENT", "$defArgs$ARG_DOWNLOADED_CONTENT"),
                    listOf(getString(R.string.libraries), getString(R.string.downloaded)))
            mTabLayout.setupWithViewPager(mPager)
        }

        return rootView
    }

    override val viewContext: Any
        get() = requireContext()

}