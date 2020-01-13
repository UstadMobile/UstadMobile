package com.ustadmobile.staging.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryListPresenter.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.controller.ContentListPresenter
import com.ustadmobile.core.controller.HomePresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.port.android.view.ContentEntryListFragment
import com.ustadmobile.port.android.view.UstadBaseFragment
import ru.dimorinny.floatingtextbutton.FloatingTextButton

/**
 * FeedListFragment Android fragment extends UstadBaseFragment - fragment responsible for displaying
 * the feed page and actions on them depending on the feed.
 */
class ContentListFragment : UstadBaseFragment(), ContentListView,
        ViewPager.OnPageChangeListener {

    private lateinit var downloadAllBtn: FloatingTextButton
    internal lateinit var rootContainer: View
    private lateinit var mPresenter: ContentListPresenter
    private lateinit var pullToRefresh: SwipeRefreshLayout

    private lateinit var viewPager:ViewPager

    private var allowRefresh:Boolean = false

    override fun showDownloadAllButton(show: Boolean) {
        downloadAllBtn.visibility = if(show) View.VISIBLE else View.GONE
    }

    override val viewContext: Any
        get() = context!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.fragment_content_list, container,
                false)
        setHasOptionsMenu(true)

        pullToRefresh = rootContainer.findViewById(R.id.fragment_content_list_swiperefreshlayout)

        downloadAllBtn = rootContainer.findViewById(R.id.fragment_content_list_download_all)

        setUpViewPager()

        //Create presenter and call its onCreate()
        mPresenter = ContentListPresenter(context!!, UMAndroidUtil.bundleToMap(
                arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        downloadAllBtn.setOnClickListener {
            mPresenter.handleDownloadAllClicked()
        }

        return rootContainer
    }

    fun setUpViewPager(){
        viewPager = rootContainer.findViewById(R.id.fragment_content_list_library_viewpager)
        viewPager.adapter = null
        viewPager.adapter = LibraryPagerAdapter(childFragmentManager!!, context!!)
        val tabLayout = rootContainer.findViewById<TabLayout>(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(this)
    }

    init {
        val args = Bundle()
        arguments = args
        icon = R.drawable.ic_collections_bookmark_black_24dp
        title = R.string.bottomnav_content_title
    }


//    //TODO: Testing reloading tabs
//    private fun refreshTabs():Boolean{
//        if(allowRefresh) {
//            allowRefresh = false
//
//            getFragmentManager()!!.beginTransaction().detach(
//                    this).attach(this).commit()
//            setUpViewPager()
//            return false
//        }
//        return true
//    }
//
//    //TODO: Testing reloading tabs
//    override fun onResume() {
//        super.onResume()
//        //allowRefresh = refreshTabs()
//    }

    class LibraryPagerAdapter internal constructor(fragmentManager: FragmentManager,
                                                   private val context: Context)
        : FragmentPagerAdapter(fragmentManager) {

        private val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

        // Returns total number of pages
        override fun getCount(): Int {
            return NUM_ITEMS
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment? {
            val bundle = Bundle()

            return when (position) {
                0 // Fragment # 0 - This will show FirstFragment
                -> {
                    bundle.putString(ARG_CONTENT_ENTRY_UID,
                            HomePresenter.MASTER_SERVER_ROOT_ENTRY_UID.toString())
                    ContentEntryListFragment.newInstance(bundle)
                }
                1 // Fragment # 0 - This will show FirstFragment different title
                -> {
                    bundle.putString(ARG_DOWNLOADED_CONTENT, "")
                    ContentEntryListFragment.newInstance(bundle)

                }
                else -> null
            }
        }

        // Returns the page title for the top indicator
        override fun getPageTitle(position: Int): CharSequence? {

            when (position) {
                0 -> return impl.getString(MessageID.libraries, context)
                1 -> return impl.getString(MessageID.downloaded, context)
            }
            return null

        }

        companion object {
            private const val NUM_ITEMS = 2
        }

    }


    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int,
                                positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        mPresenter.handleShowDownloadButton(position == 0)
    }

    companion object {


        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment ContainerPageFragment.
         */
        fun newInstance(): ContentListFragment {

            val fragment = ContentListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
