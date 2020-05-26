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
import com.ustadmobile.core.controller.HomePresenter.Companion.ARG_HOME_CONTENTENTRYLIST_TITLELIST
import com.ustadmobile.core.controller.HomePresenter.Companion.ARG_HOME_CONTENTENTRYLIST_VIEWLIST
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter

/**
 * Holds a Fragment that will setup subfragments of ContentEntryList. Arguments must be in the form of:
 *
 * 0=TabTitleMessageID;(URL Encoded arguments for ContentEntryList for first tab)
*  1=TableTitleMesageID;(URL Encoded arguments for ContentEntryList for second tab)
*
 */
class HomeContentEntryTabsFragment : UstadBaseFragment(){

    private inner class ContentEntryTabsPagerAdapter(fm: FragmentManager, viewList: List<String>, val titleList: List<String>): ViewNameListFragmentPagerAdapter(fm,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, viewList, mapOf(),
            mapOf()) {

        override fun getPageTitle(position: Int) = titleList[position]
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_contententrytabs, container, false)
        val impl = UstadMobileSystemImpl.instance

        val mTabLayout: TabLayout = rootView.findViewById(R.id.home_contententry_tabs)
        val mPager: ViewPager = rootView.findViewById(R.id.home_contententry_viewpager)

        val viewListArg = arguments?.getString(ARG_HOME_CONTENTENTRYLIST_VIEWLIST) ?: "[]"
        val titleListArg = arguments?.getString(ARG_HOME_CONTENTENTRYLIST_TITLELIST) ?: "0"

        val viewList: List<String> = viewListArg.split(',').map { UMURLEncoder.decodeUTF8(it) }
        val titleList: List<String> = titleListArg.split(',')
                .map { impl.getString(it.toInt(), requireContext()) }

        //Unfortunately if we dont use a Handler here then the first tab will not show up on first load
        Handler().post {
            mPager.adapter = ContentEntryTabsPagerAdapter(childFragmentManager, viewList, titleList)
            mTabLayout.setupWithViewPager(mPager)
        }

        return rootView
    }

}