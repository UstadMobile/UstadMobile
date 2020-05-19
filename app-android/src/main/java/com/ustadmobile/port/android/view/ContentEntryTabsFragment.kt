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
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter

class ContentEntryTabsFragment : UstadBaseFragment(){

    private inner class ContentEntryTabsPagerAdapter(fm: FragmentManager, viewList: List<String>, val titleList: List<String>): ViewNameListFragmentPagerAdapter(fm,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, viewList, mapOf(ContentEntryListView.VIEW_NAME to ContentEntryListFragment::class.java),
            mapOf()) {

        override fun getPageTitle(position: Int) = titleList[position]
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_contententrytabs, container, false)

        val mTabLayout: TabLayout = rootView.findViewById(R.id.home_contententry_tabs)
        val mPager: ViewPager = rootView.findViewById(R.id.home_contententry_viewpager)

        //Unfortunately if we dont use a Handler here then the first tab will not show up on first load
        Handler().post {
            mPager.adapter = ContentEntryTabsPagerAdapter(childFragmentManager,
                    listOf(ContentEntryListView.VIEW_NAME,ContentEntryListView.VIEW_NAME),
                    listOf(getString(R.string.libraries),getString(R.string.downloaded)))
            mTabLayout.setupWithViewPager(mPager)
        }

        return rootView
    }

}