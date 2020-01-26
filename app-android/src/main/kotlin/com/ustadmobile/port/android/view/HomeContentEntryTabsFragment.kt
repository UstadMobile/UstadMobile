package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import java.util.*

/**
 * Holds a Fragment that will setup subfragments of ContentEntryList. Arguments must be in the form of:
 *
 * 0=TabTitleMessageID;(URL Encoded arguments for ContentEntryList for first tab)
*  1=TableTitleMesageID;(URL Encoded arguments for ContentEntryList for second tab)
*
 */
class HomeContentEntryTabsFragment : UstadBaseFragment(){

    lateinit var rootContainer: View

    lateinit var mTabLayout : TabLayout

    lateinit var mPager: ViewPager

    val weakFragmentMap = WeakHashMap<Int, Fragment>()

    private inner class ContentEntryTabsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return weakFragmentMap[position] ?: ContentEntryListFragment().also {
                var positionArgs = arguments?.getString(position.toString()) ?:
                throw IllegalArgumentException("HomeContentEntryTabsFragment: did not find " +
                        "argument for position: $position")

                positionArgs = positionArgs.substringAfter(';')
                it.arguments = UMAndroidUtil.mapToBundle(UMFileUtil.parseURLQueryString(positionArgs))
                weakFragmentMap[position] = it
            }
        }

        override fun getCount(): Int {
            return arguments?.size() ?: 0
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val messageId = arguments?.getString(position.toString())?.substringBefore(';')
                    ?.toInt() ?: MessageID.error
            return UstadMobileSystemImpl.instance.getString(messageId, requireContext())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootContainer = inflater.inflate(R.layout.fragment_contententrytabs, container, false)


        mTabLayout = rootContainer.findViewById(R.id.home_contententry_tabs)
        mPager = rootContainer.findViewById(R.id.home_contententry_viewpager)

        //Unfortunately if we dont use a Handler here then the first tab will not show up on first load
        Handler().post {
            mPager.adapter = ContentEntryTabsPagerAdapter(childFragmentManager)
            mTabLayout.setupWithViewPager(mPager)
        }

        return rootContainer
    }
}