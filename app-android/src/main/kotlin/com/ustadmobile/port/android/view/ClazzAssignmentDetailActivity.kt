package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityClazzAssignmentDetailBinding
import com.ustadmobile.core.controller.ClazzAssignmentDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzAssignmentDetailAssignmentView
import com.ustadmobile.core.view.ClazzAssignmentDetailProgressView
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter


/**
 * The ClassDetail activity.
 *
 * This Activity extends UstadBaseActivity and implements ClazzDetailView
 */
class ClazzAssignmentDetailActivity : UstadBaseActivity(), ClazzAssignmentDetailView {

    private var rootView : ActivityClazzAssignmentDetailBinding? = null
    private var mPager: ViewPager?=null
    private var mPagerAdapter: ViewNameListFragmentPagerAdapter?= null
    private var toolbar: Toolbar?=null
    private var mTabLayout: TabLayout? = null
    private var mPresenter: ClazzAssignmentDetailPresenter?= null
    internal var menu: Menu? = null

    /**
     * Handles Action Bar menu button click.
     * @param item  The MenuItem clicked.
     * @return  Boolean if handled or not.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setClazzAssignment(clazzAssignment: ClazzAssignmentWithMetrics){
        rootView?.clazzassignmentwithmetrics = clazzAssignment
    }

    private val bundleMakerFn: (viewUri: String, index: Int)-> Bundle = { _, _ ->
        intent.extras?:Bundle()
    }

    /**
     * Separated out view pager setup for clarity.
     */
    private fun setupViewPager(tabs:List<String>) {

        val viewNameToTitle = mapOf(
                ClazzAssignmentDetailAssignmentView.VIEW_NAME to getText(R.string.assignments).toString(),
                ClazzAssignmentDetailProgressView.VIEW_NAME to getText(R.string.student_progress).toString()
        )
        runOnUiThread {
            mPager = rootView?.clazzAssignmentDetailViewPagerContainer
            mPagerAdapter = ViewNameListFragmentPagerAdapter(supportFragmentManager,
                    FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, tabs,
                    VIEW_NAME_TO_FRAGMENT_CLASS, viewNameToTitle,bundleMakerFn
            )

            mPager?.adapter = mPagerAdapter
            mTabLayout = rootView?.activityClazzAssignmentDetailTablayout
            mTabLayout?.tabGravity = TabLayout.GRAVITY_FILL
            mTabLayout?.setupWithViewPager(mPager)
        }
    }


    override fun setupTabs(tabs: List<String>) {
        setupViewPager(tabs)
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

        toolbar = rootView?.clazzAssignmentDetailToolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Presenter
        mPresenter = ClazzAssignmentDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter?.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

    }


    companion object{
        private val VIEW_NAME_TO_FRAGMENT_CLASS = mapOf<String, Class<out Fragment>>(
                ClazzAssignmentDetailAssignmentView.VIEW_NAME to
                        ClazzAssignmentDetailAssignmentFragment::class.java,
                ClazzAssignmentDetailProgressView.VIEW_NAME to
                        ClazzAssignmentDetailProgressFragment::class.java)

    }
}
