package com.ustadmobile.port.android.view

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.BasePointActivity2Presenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.BasePointView2
import com.ustadmobile.port.android.sync.UmAppDatabaseSyncWorker
import org.acra.util.ToastSender.sendToast
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * The new Base Point screen. This Activity has a bottom Navigation bar with buttons. We are using
 * an external component to achieve this vs Google's own (some differences when you start adding
 * more than 3).
 *
 * This Activity extends UstadBaseActivity and implements BasePointView
 */
class BasePointActivity2 : UstadBaseActivity(), BasePointView2 {

    private var mPager: ViewPager? = null
    private var mPagerAdapter: BasePointViewPagerAdapter? = null

    private var toolbar: Toolbar? = null

    private var shareAppDialog: AlertDialog? = null

    private var mPresenter: BasePointActivity2Presenter? = null

    private var mOptionsMenu: Menu? = null

    private var classesFragment: ClazzListFragment? = null
    private var peopleListFragment: PeopleListFragment? = null
    private var reportSelectionFragment: ReportSelectionFragment? = null
    private var newFrag: FeedListFragment? = null

    private var lastSyncTime: Long = 0
    private var syncing = false

    /**
     * ViewPager set up in its own method for clarity.
     */
    private fun setupViewPager() {
        mPager = findViewById(R.id.container_feedlist)
        mPagerAdapter = BasePointViewPagerAdapter(supportFragmentManager)
        mPager!!.setAdapter(mPagerAdapter)
    }

    /**
     * The overridden onCreate method does the following:
     *
     * 1. Creates, names, styles and sets the Bottom Navigation
     * 2. Sets the default location (Feed)
     * 3. Sets the toolbar title upon navigation
     *
     * @param savedInstanceState        The application's bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Setting layout:
        setContentView(R.layout.activity_base_point2)

        //set up view pager
        setupViewPager()

        //Toolbar
        toolbar = findViewById(R.id.base_point_2_toolbar)
        toolbar!!.setTitle("Ustad Mobile")
        setSupportActionBar(toolbar)

        mPresenter = BasePointActivity2Presenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Get the bottom navigation component.
        val bottomNavigation = findViewById<View>(R.id.bottom_navigation)

        //Style
        bottomNavigation.setDefaultBackgroundColor(fetchColor(R.color.primary))
        bottomNavigation.setAccentColor(fetchColor(R.color.just_black))
        bottomNavigation.setInactiveColor(fetchColor(R.color.bottom_nav_yourInactiveColor))
        bottomNavigation.setBehaviorTranslationEnabled(false)
        bottomNavigation.setUseElevation(true, 2L)

        //Create the items to be added
        val feed_item = AHBottomNavigationItem(R.string.feed,
                R.drawable.ic_today_black_48dp, R.color.default_back_color)
        val classes_item = AHBottomNavigationItem(R.string.classes,
                R.drawable.ic_people_black_48dp, R.color.default_back_color)
        val people_item = AHBottomNavigationItem(R.string.people,
                R.drawable.ic_person_black_24dp, R.color.default_back_color)
        val report_item = AHBottomNavigationItem(R.string.reports,
                R.drawable.ic_insert_chart_black_24dp, R.color.default_back_color)


        //Add the items
        bottomNavigation.addItem(feed_item)
        bottomNavigation.addItem(classes_item)
        bottomNavigation.addItem(people_item)
        bottomNavigation.addItem(report_item)

        //Telling navigation to always show the text on the items. Unlike Google's
        // own implementation.
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW)

        //Click listeners for the items.
        bottomNavigation.setOnTabSelectedListener({ position, wasSelected ->

            if (!wasSelected) {
                //mPagerAdapter.notifyDataSetChanged();
                mPagerAdapter!!.getItem(position.toInt())
                //mPagerAdapter.notifyDataSetChanged();
                mPager!!.setCurrentItem(position)
            }

            //Update title
            when (position) {
                0 -> updateTitle(getText(R.string.feed).toString())
                1 -> updateTitle(getText(R.string.my_classes).toString())
                2 -> updateTitle(getText(R.string.people).toString())
                3 -> updateTitle(getText(R.string.reports).toString())
            }
            true
        })

        // Setting the very 1st item as default home screen.
        bottomNavigation.setCurrentItem(0)

        syncing = mPresenter!!.isSyncStarted

        //Observe syncing
        observeSyncing()

    }


    override fun shareAppSetupFile(filePath: String) {
        val applicationId = packageName
        val sharedUri = FileProvider.getUriForFile(this,
                "$applicationId.fileprovider",
                File(filePath))
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "*/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(shareIntent)
        }

        dismissShareAppDialog()
    }

    override fun showBulkUploadForAdmin(show: Boolean) {
        val bulkUploadMenuItem = mOptionsMenu!!.findItem(R.id.menu_basepoint_bulk_upload_master)
        if (bulkUploadMenuItem != null) {
            bulkUploadMenuItem.isVisible = show
        }
    }

    override fun showSettings(show: Boolean) {

        val allClazzSettingsMenuItem = mOptionsMenu!!.findItem(R.id.menu_settings_gear)
        if (allClazzSettingsMenuItem != null) {
            allClazzSettingsMenuItem.isVisible = show
        }
    }

    override fun updatePermissionCheck() {
        if (classesFragment != null) {
            classesFragment!!.forceCheckPermissions()
        }
    }


    override fun onResume() {
        super.onResume()
    }

    /**
     * Handles Action Bar menu button click.
     * @param item  The MenuItem clicked.
     * @return  Boolean if handled or not.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        val i = item.itemId
        //If this activity started from other activity
        if (i == R.id.menu_basepoint_share) {
            mPresenter!!.handleClickShareIcon()
        } else if (i == R.id.menu_basepoint_bulk_upload_master) {
            mPresenter!!.handleClickBulkUpload()
        } else if (i == R.id.menu_basepoint_logout) {
            finishAffinity()
            mPresenter!!.handleLogOut()
        } else if (i == R.id.menu_settings_gear) {
            mPresenter!!.handleClickSettingsIcon()
        } else if (i == R.id.menu_basepoint_search) {
            mPresenter!!.handleClickSearchIcon()
        } else if (i == R.id.menu_basepoint_about) {
            mPresenter!!.handleClickAbout()
        } else if (i == R.id.menu_basepoint_sync) {
            forceSync()
        }

        return super.onOptionsItemSelected(item)
    }

    fun checkSyncFinished() {
        if (mOptionsMenu == null)
            return
        if (lastSyncTime > 0) {
            val syncItem = mOptionsMenu!!.findItem(R.id.menu_basepoint_sync)
            val updatedTitle = (getString(R.string.refresh) + " ("
                    //+ getString(R.string.last_synced)
                    + UMCalendarUtil.getPrettyTimeFromLong(lastSyncTime, null) + ")")
            syncItem.title = updatedTitle
            syncItem.isEnabled = true
        }
        if (syncing) {
            updateSyncing()
        }

    }

    fun updateSyncing() {
        if (mOptionsMenu == null)
            return

        val syncItem = mOptionsMenu!!.findItem(R.id.menu_basepoint_sync) ?: return

        val syncingString = getString(R.string.syncing)
        syncItem.title = syncingString
        //syncItem.setActionView(R.color.enable_disable_text);
        //syncItem.getActionView().setBackgroundResource(R.color.enable_disable_text);
        val textWithColor = SpannableString(syncingString)
        textWithColor.setSpan(ForegroundColorSpan(resources.getColor(R.color.text_secondary)), 0,
                textWithColor.length, 0)
        syncItem.title = textWithColor
        syncItem.isEnabled = false
    }

    override fun forceSync() {
        WorkManager.getInstance().cancelAllWorkByTag(UmAppDatabaseSyncWorker.TAG)
        UmAppDatabaseSyncWorker.queueSyncWorkerWithPolicy(1000, TimeUnit.MILLISECONDS,
                ExistingWorkPolicy.KEEP)
        syncing = true
        sendToast("Sync started")
        updateSyncing()

    }

    private fun observeSyncing() {
        WorkManager.getInstance().getWorkInfosByTagLiveData(UmAppDatabaseSyncWorker.TAG).observe(
                this, { workInfos ->
            for (wi in workInfos) {
                if (wi.getState().isFinished()) {
                    lastSyncTime = System.currentTimeMillis()
                    syncing = false
                    checkSyncFinished()
                } else {
                    //updateSyncing();
                }
            }
        })
    }

    override fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_share, menu)

        //tint
        val shareMenuItem = menu.findItem(R.id.menu_basepoint_share)
        val bulkUploadMenuItem = menu.findItem(R.id.menu_basepoint_bulk_upload_master)
        val settingsMenuItem = menu.findItem(R.id.menu_settings_gear)
        val logoutMenuItem = menu.findItem(R.id.menu_basepoint_logout)

        val syncMenuItem = menu.findItem(R.id.menu_basepoint_sync)

        val shareMenuIcon = AppCompatResources.getDrawable(applicationContext,
                R.drawable.ic_share_white_24dp)
        val bulkUploadMenuIcon = AppCompatResources.getDrawable(applicationContext, R.drawable.ic_file_upload_white_24dp)
        val settingsMenuIcon = AppCompatResources.getDrawable(applicationContext, R.drawable.ic_settings_white_24dp)
        val logoutMenuIcon = AppCompatResources.getDrawable(applicationContext, R.drawable.ic_dropout_bcd4_24dp)

        assert(shareMenuIcon != null)
        shareMenuIcon!!.setColorFilter(resources.getColor(R.color.icons), PorterDuff.Mode.SRC_IN)
        assert(bulkUploadMenuIcon != null)
        bulkUploadMenuIcon!!.setColorFilter(resources.getColor(R.color.icons), PorterDuff.Mode.SRC_IN)
        assert(settingsMenuIcon != null)
        settingsMenuIcon!!.setColorFilter(resources.getColor(R.color.icons), PorterDuff.Mode.SRC_IN)
        assert(logoutMenuIcon != null)
        logoutMenuIcon!!.setColorFilter(resources.getColor(R.color.icons), PorterDuff.Mode.SRC_IN)

        shareMenuItem.setIcon(shareMenuIcon)
        bulkUploadMenuItem.setIcon(bulkUploadMenuIcon)
        settingsMenuItem.setIcon(settingsMenuIcon)
        logoutMenuItem.setIcon(logoutMenuIcon)

        mOptionsMenu = menu
        mPresenter!!.getLoggedInPerson()


        checkSyncFinished()

        //Search stuff
        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.menu_basepoint_search)
                .actionView as SearchView
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))

        searchView.setMaxWidth(Integer.MAX_VALUE)

        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener() {
            fun onQueryTextSubmit(query: String): Boolean {
                // filter recycler view when query submitted
                when (mPager!!.getCurrentItem()) {
                    VIEW_POSITION_POSITION_FEED -> {
                    }
                    VIEW_POSITION_POSITION_CLASSES -> classesFragment!!.searchClasses(query)
                    VIEW_POSITION_POSITION_PEOPLE -> peopleListFragment!!.searchPeople(query)
                    VIEW_POSITION_POSITION_REPORTS -> {
                    }
                    else -> {
                    }
                }

                return false
            }

            fun onQueryTextChange(query: String): Boolean {
                // filter recycler view when text is changed

                // filter recycler view when query submitted
                when (mPager!!.getCurrentItem()) {
                    VIEW_POSITION_POSITION_FEED -> {
                    }
                    VIEW_POSITION_POSITION_CLASSES -> classesFragment!!.searchClasses(query)
                    VIEW_POSITION_POSITION_PEOPLE -> peopleListFragment!!.searchPeople(query)
                    VIEW_POSITION_POSITION_REPORTS -> {
                    }
                    else -> {
                    }
                }
                return false
            }
        })


        searchView.setOnCloseListener({

            // filter recycler view when query submitted
            when (mPager!!.getCurrentItem()) {
                VIEW_POSITION_POSITION_FEED -> {
                }
                VIEW_POSITION_POSITION_CLASSES -> classesFragment!!.searchClasses("")
                VIEW_POSITION_POSITION_PEOPLE -> peopleListFragment!!.searchPeople("")
                VIEW_POSITION_POSITION_REPORTS -> {
                }
                else -> {
                }
            }
            false
        })
        return true
    }

    /**
     * Updates the toolbar's title
     * @param title The string of the title to be set to the toolbar
     */
    fun updateTitle(title: String) {
        toolbar!!.setTitle(title)
    }

    override fun showShareAppDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.share_application)
        builder.setView(R.layout.fragment_share_app_dialog)
        builder.setPositiveButton(R.string.share, null)
        builder.setNegativeButton(R.string.cancel, null)
        shareAppDialog = builder.create()
        shareAppDialog!!.setOnShowListener({ dialogInterface ->
            val okButton = shareAppDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener({ v -> mPresenter!!.handleConfirmShareApp() })
        })
        shareAppDialog!!.show()
    }

    override fun dismissShareAppDialog() {
        shareAppDialog!!.dismiss()
        shareAppDialog = null
    }

    /**
     * Feed view pager's Adapter
     */
    private inner class BasePointViewPagerAdapter//Constructor creates the adapter
    internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        //Map of position and fragment
        internal var positionMap: WeakHashMap<Int, UstadBaseFragment>

        //return positionMap.size();
        val count: Int
            get() = 4

        init {
            positionMap = WeakHashMap()
        }

        /**
         * Generate fragment for that page/position
         *
         * @param position  position of item
         * @return  the fragment
         */
        fun getItem(position: Int): Fragment? {
            val thisFragment = positionMap[position]
            return thisFragment ?: when (position) {
                0 -> {
                    newFrag = FeedListFragment.newInstance()
                    this.positionMap[position] = newFrag
                    newFrag
                }
                1 -> {
                    classesFragment = ClazzListFragment.newInstance()
                    this.positionMap[position] = classesFragment
                    classesFragment
                }

                2 -> {
                    peopleListFragment = PeopleListFragment.newInstance()
                    this.positionMap[position] = peopleListFragment
                    peopleListFragment
                }

                3 -> {
                    reportSelectionFragment = ReportSelectionFragment.newInstance()
                    this.positionMap[position] = reportSelectionFragment
                    reportSelectionFragment
                }

                else -> null
            }
        }
    }

    /**
     * Get color from ContextCompat
     *
     * @param color The color code
     * @return  the color
     */
    fun fetchColor(color: Int): Int {
        return ContextCompat.getColor(this, color)
    }

    companion object {

        val VIEW_POSITION_POSITION_FEED = 0
        val VIEW_POSITION_POSITION_CLASSES = 1
        val VIEW_POSITION_POSITION_PEOPLE = 2
        val VIEW_POSITION_POSITION_REPORTS = 3
    }


}
