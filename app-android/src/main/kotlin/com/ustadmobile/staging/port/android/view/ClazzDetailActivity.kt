package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.port.android.view.UstadBaseActivity
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter


/**
 * The ClassDetail activity.
 *
 * This Activity extends UstadBaseActivity and implements ClazzDetailView
 */
class ClazzDetailActivity : UstadBaseActivity(), ClazzDetailView {

    private lateinit var mPager: ViewPager
    private lateinit var mPagerAdapter: ViewNameListFragmentPagerAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var mTabLayout: TabLayout
    private lateinit var mPresenter: ClazzDetailPresenter
    private var currentClazzUid: Long = 0L
    private var settingsVisibility: Boolean = false
    internal lateinit var menu: Menu

    private val bundleMakerFn: (viewUri: String, index: Int)-> Bundle = { _, _ ->
        val bundle = Bundle()
        bundle.putString(ARG_CLAZZ_UID, currentClazzUid.toString())
        bundle.putString(UstadView.ARG_CLAZZ_UID, currentClazzUid.toString())
        bundle
    }


    /**
     * Separated out view pager setup for clarity.
     */
    private fun setupViewPager(tabs: List<String>) {

        val viewNameToTitle = mapOf(
                ClazzStudentListView.VIEW_NAME to getText(R.string.people).toString(),
                ClassLogListView.VIEW_NAME to getText(R.string.attendance).toString(),
                ClazzActivityListView.VIEW_NAME to getText(R.string.activity).toString(),
                SELAnswerListView.VIEW_NAME to getText(R.string.sel_caps).toString(),
                ClazzAssignmentListView.VIEW_NAME to getText(R.string.assignments).toString()
        )

        runOnUiThread {
            mPager = findViewById(R.id.class_detail_view_pager_container)
            mPagerAdapter = ViewNameListFragmentPagerAdapter(supportFragmentManager,
                    FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, tabs,
                    VIEW_NAME_TO_FRAGMENT_CLASS, viewNameToTitle,bundleMakerFn
                    )
            mPager.adapter = mPagerAdapter

            mTabLayout = findViewById(R.id.activity_class_detail_tablayout)
            mTabLayout.tabGravity = TabLayout.GRAVITY_FILL
            mTabLayout.setupWithViewPager(mPager)
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
        //Setting layout:
        setContentView(R.layout.activity_clazz_detail)

        if (intent?.extras?.containsKey(ARG_CLAZZ_UID) == true) {
            currentClazzUid = intent.extras?.get(ARG_CLAZZ_UID).toString().toLong()
        }
        toolbar = findViewById(R.id.class_detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Presenter
        mPresenter = ClazzDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
        mPresenter.checkPermissions()

    }

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_clazzdetail, menu)
        val settingsGearMenuItem = menu.findItem(R.id.menu_clazzdetail_gear)
        settingsGearMenuItem.isVisible = settingsVisibility
        return true
    }


    /**
     * Handles Action Bar menu button click.
     * @param item  The MenuItem clicked.
     * @return  Boolean if handled or not.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        //If this activity started from other activity
        return when (item.itemId) {
            R.id.menu_clazzdetail_gear -> {
                mPresenter.handleClickClazzEdit()
                super.onOptionsItemSelected(item)
            }
            R.id.menu_clazzdetail_search -> {
                mPresenter.handleClickSearch()
                super.onOptionsItemSelected(item)
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun setClazz(clazz: Clazz) {
        runOnUiThread {
            toolbar.title = clazz.clazzName?:""
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun setSettingsVisibility(visible: Boolean) {
        settingsVisibility = visible
    }

    companion object{
        private val VIEW_NAME_TO_FRAGMENT_CLASS = mapOf<String, Class<out Fragment>>(
                ClazzStudentListView.VIEW_NAME to ClazzStudentListFragment::class.java,
                ClassLogListView.VIEW_NAME to ClazzLogListFragment::class.java,
                ClazzActivityListView.VIEW_NAME to ClazzActivityListFragment::class.java,
                SELAnswerListView.VIEW_NAME to SELAnswerListFragment::class.java,
                ClazzAssignmentListView.VIEW_NAME to ClazzAssignmentListFragment::class.java)
    }
}
