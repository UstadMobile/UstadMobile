package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityClazzAssignmentDetailBinding
import com.ustadmobile.core.controller.ClazzAssignmentDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.staging.port.android.view.ClazzAssignmentDetailAssignmentFragment
import com.ustadmobile.staging.port.android.view.ClazzAssignmentDetailProgressFragment
import java.util.*


/**
 * The ClassDetail activity.
 *
 * This Activity extends UstadBaseActivity and implements ClazzDetailView
 */
class ClazzAssignmentDetailActivity : UstadBaseActivity(), ClazzAssignmentDetailView,
        TabLayout.OnTabSelectedListener {

    private var rootView : ActivityClazzAssignmentDetailBinding? = null
    private var mPager: ViewPager?=null
    private var mPagerAdapter: ClassAssginmentDetailViewPagerAdapter?= null
    private var toolbar: Toolbar?=null
    private var mTabLayout: TabLayout? = null
    private var mPresenter: ClazzAssignmentDetailPresenter?= null
    internal var menu: Menu? = null
    private val fragPosMap = HashMap<Int, Class<*>>()
    private var currentClazzUid : Long = 0L
    private var clazzAssignmentUid : Long = 0L
    private var previousPosition = 0


    override fun setClazzAssignment(clazzAssignment: ClazzAssignmentWithMetrics){
        rootView?.clazzassignmentwithmetrics = clazzAssignment
    }

    /**
     * Separated out view pager setup for clarity.
     */
    private fun setupViewPager() {

        runOnUiThread {
            mPager = rootView?.clazzAssignmentDetailViewPagerContainer
            mPagerAdapter = ClassAssginmentDetailViewPagerAdapter(supportFragmentManager)
            var fragCount = 0


            mPagerAdapter?.addFragments(fragCount,
                    ClazzAssignmentDetailAssignmentFragment.newInstance(intent.extras))
            fragPosMap[fragCount++] = ClazzAssignmentDetailAssignmentFragment::class.java

            mPagerAdapter?.addFragments(fragCount,
                    ClazzAssignmentDetailProgressFragment.newInstance(intent.extras))
            fragPosMap[fragCount++] = ClazzAssignmentDetailProgressFragment::class.java

            mPager?.adapter = mPagerAdapter

            mTabLayout = rootView?.activityClazzAssignmentDetailTablayout
            mTabLayout?.tabGravity = TabLayout.GRAVITY_FILL
            mTabLayout?.setupWithViewPager(mPager)

            mTabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if(previousPosition < 1) {
                        previousPosition = tab!!.position
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            if(previousPosition != -1){
                mTabLayout?.getTabAt(previousPosition)
            }
        }
    }


    override fun setupTabs(tabs: List<String>) {
        setupViewPager()
    }
    /**
     * The ClazzDetailActivity's onCreate get the Clazz UID from arguments given to it
     * and sets up TabLayout.
     * @param savedInstanceState    Android bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(
                this, R.layout.activity_clazz_assignment_detail)

        if (intent!!.extras!!.get(UstadView.ARG_CLAZZ_UID) is String) {
            currentClazzUid = intent.getStringExtra(UstadView.ARG_CLAZZ_UID).toString().toLong()
        } else {
            currentClazzUid = intent.getLongExtra(UstadView.ARG_CLAZZ_UID, 0L)
        }

        if (intent!!.extras!!.get(UstadView.ARG_CLAZZ_ASSIGNMENT_UID) is String) {
            clazzAssignmentUid = intent.getStringExtra(UstadView.ARG_CLAZZ_ASSIGNMENT_UID).toString().toLong()
        } else {
            clazzAssignmentUid = intent.getLongExtra(UstadView.ARG_CLAZZ_ASSIGNMENT_UID, 0L)
        }

        toolbar = rootView?.clazzAssignmentDetailToolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Presenter
        mPresenter = ClazzAssignmentDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter?.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

    }

    //Tab layout's on Tab selected
    override fun onTabSelected(tab: TabLayout.Tab) {
        mPagerAdapter?.getItem(tab.position) //Loads first fragment
        mPagerAdapter?.notifyDataSetChanged()
        mPager?.currentItem = tab.position
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabReselected(tab: TabLayout.Tab) {}


    /**
     * ClazzDetailView's view pager adapter
     */
    private inner class ClassAssginmentDetailViewPagerAdapter(fm: FragmentManager)
        : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return positionMap.size
        }

        //Map of position and fragment
        internal var positionMap: WeakHashMap<Int, UstadBaseFragment> = WeakHashMap()


        fun addFragments(pos: Int, fragment: Fragment) {
            positionMap[pos] = fragment as UstadBaseFragment
        }

        /**
         * Generates fragment for that page/position
         *
         * @param position The position of the fragment to generate
         * @return void
         */
        override fun getItem(position: Int): Fragment {
            val thisFragment = positionMap[position]
            if (thisFragment != null) {
                return thisFragment
            } else {
                val fragClass = fragPosMap[position]

                return if (fragClass == ClazzAssignmentDetailAssignmentFragment::class.java) {
                    ClazzAssignmentDetailAssignmentFragment.newInstance(intent.extras)
                } else if (fragClass == ClazzAssignmentDetailProgressFragment::class.java) {
                    ClazzAssignmentDetailProgressFragment.newInstance(intent.extras)
                } else {
                    throw IllegalArgumentException("Not available")
                }
            }
        }

        /**
         * Gets the title of the tab position
         *
         * @param position the position of the tab
         * @return void
         */
        override fun getPageTitle(position: Int): CharSequence {
            val fragClass = fragPosMap[position]
            return if (fragClass == ClazzAssignmentDetailAssignmentFragment::class.java) {
                (getText(R.string.assignment) as String).toUpperCase()
            } else if (fragClass == ClazzAssignmentDetailProgressFragment::class.java) {
                (getText(R.string.student_progress) as String).toUpperCase()
            } else {
                ""
            }
        }
    }

}
