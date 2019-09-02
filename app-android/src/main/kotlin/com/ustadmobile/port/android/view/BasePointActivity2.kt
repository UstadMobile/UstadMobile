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
import android.widget.TextView
import androidx.lifecycle.Observer
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
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.NavigationItem
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.port.android.sync.UmAppDatabaseSyncWorker
import de.hdodenhof.circleimageview.CircleImageView
import org.acra.util.ToastSender.sendToast
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


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
    private lateinit var toolbarTitle: TextView

    private var shareAppDialog: AlertDialog? = null

    private var mPresenter: BasePointActivity2Presenter? = null

    private var mOptionsMenu: Menu? = null

    private var classesFragment: ClazzListFragment? = null
    private var peopleListFragment: PeopleListFragment? = null
    private var reportSelectionFragment: ReportSelectionFragment? = null
    private var newFrag: FeedListFragment? = null

    private var lastSyncTime: Long = 0
    private var syncing = false

    private var bottomCount: Int = -1
    private var navToFragment:HashMap<Int, UstadBaseFragment> = HashMap()

    private lateinit var bottomNavigation: AHBottomNavigation

    private lateinit var profileImage: CircleImageView

    private lateinit var downloadAllBtn: FloatingTextButton


    private val viewNameToFragment = mapOf<String, Class<*>>(
            FeedListView.VIEW_NAME to FeedListFragment::class.java,
            ContentEntryListView.VIEW_NAME to ContentEntryListFragment::class.java,
            ClazzListView.VIEW_NAME to ClazzListFragment::class.java,
            PeopleListView.VIEW_NAME to PeopleListFragment::class.java,
            BaseReportView.VIEW_NAME to ReportSelectionFragment::class.java)

    /**
     * ViewPager set up in its own method for clarity.
     */
    private fun setupViewPager() {
        mPager = findViewById(R.id.container_feedlist)
        mPagerAdapter = BasePointViewPagerAdapter(supportFragmentManager)
        mPager!!.setAdapter(mPagerAdapter)
    }

    private fun setupBottomNavigation(){
        //Get the bottom navigation component.
        bottomNavigation = findViewById(R.id.bottom_navigation)

        //Style
        bottomNavigation.setDefaultBackgroundColor(fetchColor(R.color.primary))
        bottomNavigation.setAccentColor(fetchColor(R.color.just_black))
        bottomNavigation.setInactiveColor(fetchColor(R.color.bottom_nav_yourInactiveColor))
        bottomNavigation.setBehaviorTranslationEnabled(false)
        bottomNavigation.setUseElevation(true, 2F)
    }


    override fun setupNavigation(items: List<NavigationItem>) {

        bottomNavigation.removeAllItems()
        bottomCount=0
        for (everyItem: NavigationItem in items){
            val theFragment = viewNameToFragment.get(everyItem.viewName)
            val fragment = theFragment!!.newInstance() as UstadBaseFragment

            val nav_item = AHBottomNavigationItem(getString(fragment.title!!),
                    getDrawable(fragment.icon!!),
                    ContextCompat.getColor(applicationContext, R.color.icons))
            bottomNavigation.addItem(nav_item)
            navToFragment.put(bottomCount, fragment)
            bottomCount++


            if(everyItem.viewName.equals(FeedListView.VIEW_NAME)){
                VIEW_POSITION_POSITION_FEED = bottomCount
            }else if(everyItem.viewName.equals(ClazzListView.VIEW_NAME)){
                VIEW_POSITION_POSITION_CLASSES=bottomCount
            }else if(everyItem.viewName.equals(PeopleListView.VIEW_NAME)){
                VIEW_POSITION_POSITION_PEOPLE=bottomCount
            }else if(everyItem.viewName.equals(BaseReportView.VIEW_NAME)){
                VIEW_POSITION_POSITION_REPORTS=bottomCount
            }else if(everyItem.viewName.equals(ContentEntryListView.VIEW_NAME)){
                VIEW_POSITION_POSITION_CONTENT = bottomCount
            }

        }

        //Telling navigation to always show the text on the items. Unlike Google's
        // own implementation.
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW)

        //Click listeners for the items.
        bottomNavigation.setOnTabSelectedListener { position: Int, wasSelected:Boolean ->

            if (!wasSelected) {
                //mPagerAdapter.notifyDataSetChanged();
                mPagerAdapter!!.getItem(position.toInt())
                //mPagerAdapter.notifyDataSetChanged();
                mPager!!.setCurrentItem(position)
            }

            //Update title
            when (position) {
                position -> updateTitle(getString(navToFragment.get(position)!!.title!!))

            }
            true
        }

        // Setting the very 1st item as default home screen.
        bottomNavigation.setCurrentItem(0)
    }


    override fun loadProfileIcon(profileUrl: String) {
        UMAndroidUtil.loadImage(profileUrl,R.drawable.ic_account_circle_white_24dp, profileImage)
    }

    override fun showDownloadAllButton(show: Boolean) {
        downloadAllBtn.visibility = if(show) View.VISIBLE else View.GONE
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

        //Toolbar
        toolbar = findViewById(R.id.base_point_2_toolbar)
        toolbar!!.setTitle("Ustad Mobile")
        setSupportActionBar(toolbar)

        profileImage = findViewById(R.id.base_point2_profile_image)

        toolbarTitle = findViewById(R.id.base_point2_toolbar_title)

        //TODO: Move download all button to Content entry fragment
        //downloadAllBtn = findViewById(R.id.download_all)

        //Style bottom navigation
        setupBottomNavigation()

        mPresenter = BasePointActivity2Presenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //set up view pager
        setupViewPager()

        profileImage.setOnClickListener {
            mPresenter!!.handleClickPersonIcon()
        }

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
        val showBU = UstadMobileSystemImpl.instance.getAppConfigString(
                AppConfig.BULK_UPLOAD_VISIBILITY, null, this)
        if(showBU == null){
            if (bulkUploadMenuItem != null) {
                bulkUploadMenuItem.isVisible = false
            }
        }else {
            if (bulkUploadMenuItem != null) {
                if(showBU.toLowerCase().equals("false")){
                    bulkUploadMenuItem.isVisible = false
                }else {
                    bulkUploadMenuItem.isVisible = show
                }
            }
        }
    }

    override fun showSettings(show: Boolean) {

        val allClazzSettingsMenuItem = mOptionsMenu!!.findItem(R.id.menu_settings_gear)
        if (allClazzSettingsMenuItem != null) {
            allClazzSettingsMenuItem.isVisible = show
        }
    }

    override fun updatePermissionCheck() {
        //TODO: Check this after redo
        if (classesFragment != null) {
            classesFragment!!.forceCheckPermissions()
        }
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
        sendToast(applicationContext,"Sync started", 42)
        updateSyncing()

    }

    private fun observeSyncing() {
        WorkManager.getInstance().getWorkInfosByTagLiveData(UmAppDatabaseSyncWorker.TAG).observe(
                this, Observer{ workInfos ->
            for (wi in workInfos) {
                if (wi.getState().isFinished()) {
                    lastSyncTime = UMCalendarUtil.getDateInMilliPlusDays(0)
                    syncing = false
                    checkSyncFinished()
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
        val searchView = menu.findItem(R.id.menu_basepoint_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))

        searchView.setMaxWidth(Integer.MAX_VALUE)

        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
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

            override fun onQueryTextChange(query: String): Boolean {
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


        searchView.setOnCloseListener{

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
        }
        return true
    }

    /**
     * Updates the toolbar's title
     * @param title The string of the title to be set to the toolbar
     */
    fun updateTitle(title: String) {
        toolbar!!.setTitle(title)
        toolbarTitle.setText(title)

    }

    override fun showShareAppDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.share_application)
        builder.setView(R.layout.fragment_share_app_dialog)
        builder.setPositiveButton(R.string.share, null)
        builder.setNegativeButton(R.string.cancel, null)
        shareAppDialog = builder.create()
        shareAppDialog!!.setOnShowListener{ dialogInterface ->
            val okButton = shareAppDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener({ v -> mPresenter!!.handleConfirmShareApp() })
        }
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
        override fun getCount(): Int {
            return bottomCount
        }

        //Map of position and fragment
        internal var positionMap: WeakHashMap<Int, UstadBaseFragment>

        init {
            positionMap = WeakHashMap()
        }

        /**
         * Generate fragment for that page/position
         *
         * @param position  position of item
         * @return  the fragment
         */
        override fun getItem(position: Int): Fragment? {
            val thisFragment = positionMap[position]

            if(thisFragment!=null){
                return thisFragment
            }

            when (position) {
                position -> {
                    val f = navToFragment.get(position)
                    this.positionMap[position] = f
                    return f
                }

            }
            return thisFragment
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

        var VIEW_POSITION_POSITION_FEED = -1
        var VIEW_POSITION_POSITION_CLASSES = -1
        var VIEW_POSITION_POSITION_PEOPLE = -1
        var VIEW_POSITION_POSITION_REPORTS = -1
        var VIEW_POSITION_POSITION_CONTENT = -1

    }


}
