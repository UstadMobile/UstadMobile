package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
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
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.BasePointPresenter
import com.ustadmobile.core.controller.HomePresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.NavigationItem
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.port.android.sync.UmAppDatabaseSyncWorker
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
class BasePointActivity : UstadBaseActivity(), BasePointView {


    private var mPager: ViewPager? = null
    private var mPagerAdapter: BasePointViewPagerAdapter? = null

    private var toolbar: Toolbar? = null
    private lateinit var toolbarTitle: TextView
    private lateinit var appBarLayout: AppBarLayout

    private var shareAppDialog: AlertDialog? = null

    private var mPresenter: BasePointPresenter? = null

    private var mOptionsMenu: Menu? = null

    //For search purposes:
    private var classesFragment: ClazzListFragment? = null
    private var peopleListFragment: PeopleListFragment? = null

    private var lastSyncTime: Long = 0
    private var syncing = false

    private var bottomCount: Int = -1
    private var navToFragment:HashMap<Int, UstadBaseFragment> = HashMap()

    private lateinit var bottomNavigation: AHBottomNavigation

    //TODO: Observe personPicture and update this
    private lateinit var profileImage: CircleImageView

    private lateinit var downloadAllBtn: FloatingTextButton

    private var onContent:Boolean = false

    private val viewNameToFragment = mapOf<String, Class<*>>(
            FeedListView.VIEW_NAME to FeedListFragment::class.java,
            ContentEntryListView.VIEW_NAME to ContentEntryListFragment::class.java,
            ContentEntryListView.VIEW_NAME to ContentListFragment::class.java,
            ClazzListView.VIEW_NAME to ClazzListFragment::class.java,
            PeopleListView.VIEW_NAME to PeopleListFragment::class.java,
            BaseReportView.VIEW_NAME to ReportSelectionFragment::class.java)

    /**
     * ViewPager set up in its own method for clarity.
     */
    private fun setupViewPager() {
        mPager = findViewById(R.id.container_feedlist)
        mPagerAdapter = BasePointViewPagerAdapter(supportFragmentManager)
        mPager!!.adapter = mPagerAdapter
    }

    private fun setupBottomNavigation(){
        //Get the bottom navigation component.
        bottomNavigation = findViewById(R.id.bottom_navigation)

        //Style
        bottomNavigation.defaultBackgroundColor = fetchColor(R.color.primary)
        bottomNavigation.accentColor = fetchColor(R.color.just_black)
        bottomNavigation.inactiveColor = fetchColor(R.color.bottom_nav_yourInactiveColor)
        bottomNavigation.isBehaviorTranslationEnabled = false
        bottomNavigation.setUseElevation(true, 2F)
    }

    override fun setupNavigation(items: List<NavigationItem>) {

        bottomNavigation.removeAllItems()
        bottomCount=0
        for (everyItem: NavigationItem in items){
            val theFragment = viewNameToFragment[everyItem.viewName]
            val fragment = theFragment!!.newInstance() as UstadBaseFragment

            val navItem = AHBottomNavigationItem(getString(fragment.title!!),
                    getDrawable(fragment.icon!!),
                    ContextCompat.getColor(applicationContext, R.color.icons))
            bottomNavigation.addItem(navItem)
            navToFragment[this.bottomCount] = fragment

            when {
                everyItem.viewName == FeedListView.VIEW_NAME ->
                    VIEW_POSITION_POSITION_FEED = this.bottomCount
                everyItem.viewName == ClazzListView.VIEW_NAME ->
                    VIEW_POSITION_POSITION_CLASSES= this.bottomCount
                everyItem.viewName == PeopleListView.VIEW_NAME ->
                    VIEW_POSITION_POSITION_PEOPLE= this.bottomCount
                everyItem.viewName == BaseReportView.VIEW_NAME ->
                    VIEW_POSITION_POSITION_REPORTS= this.bottomCount
                everyItem.viewName == ContentListView.VIEW_NAME ->
                    VIEW_POSITION_POSITION_CONTENT = this.bottomCount
            }
            bottomCount++

        }

        //Telling navigation to always show the text on the items. Unlike Google's
        // own implementation.
        bottomNavigation.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW

        //Click listeners for the items.
        bottomNavigation.setOnTabSelectedListener { position: Int, wasSelected:Boolean ->

            if (!wasSelected) {
                mPagerAdapter!!.getItem(position)
                mPager!!.currentItem = position
            }

            //Update title
            when (position) {
                position -> {
                    val fragment = navToFragment[position]
                    updateTitle(getString(fragment!!.title!!))
                    onContent = if (fragment is ContentListFragment ){
                        appBarLayout.elevation = 0F
                        true
                    }else{
                        appBarLayout.elevation = 10F
                        false
                    }
                }

            }
            true
        }

        // Setting the very 1st item as default home screen.
        bottomNavigation.currentItem = 0
    }


    override fun loadProfileIcon(profileUrl: String) {
        UMAndroidUtil.loadImage(profileUrl,R.drawable.ic_account_circle_white_24dp, profileImage)
    }

    override fun loadProfileImage(imagePath: String) {
        val output = File(imagePath)

        if (output.exists()) {
            val imageUri = Uri.fromFile(output)

            runOnUiThread {
                Picasso
                        .get()
                        .load(imageUri)
                        .resize(dpToPxImagePerson(), dpToPxImagePerson())
                        .centerCrop()
                        .into(profileImage)

            }

        }
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
        toolbar!!.title = "Ustad Mobile"
        setSupportActionBar(toolbar)

        appBarLayout = findViewById(R.id.base_point2_appbar)

        profileImage = findViewById(R.id.base_point2_profile_image)

        toolbarTitle = findViewById(R.id.base_point2_toolbar_title)

        //Style bottom navigation
        setupBottomNavigation()

        mPresenter = BasePointPresenter(this,
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
                "$applicationId.provider",
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

    /**
     * Sets bulk upload menu item
     */
    @SuppressLint("DefaultLocale")
    override fun showBulkUploadForAdmin(show: Boolean) {
        val bulkUploadMenuItem = mOptionsMenu!!.findItem(R.id.menu_action_bulk_upload_master)
        val showBU = UstadMobileSystemImpl.instance.getAppConfigString(
                AppConfig.BULK_UPLOAD_VISIBILITY, null, this)
        if(showBU == null){
            if (bulkUploadMenuItem != null) {
                bulkUploadMenuItem.isVisible = false
            }
        }else {
            if (bulkUploadMenuItem != null) {
                if(showBU.toLowerCase() == "false"){
                    bulkUploadMenuItem.isVisible = false
                }else {
                    bulkUploadMenuItem.isVisible = show
                }
            }
        }
    }

    /**
     * Checks if a given config from appconfig.properties is set to true or not
     */
    @SuppressLint("DefaultLocale")
    fun isConfigTrue(configString:String):Boolean{
        val impl = UstadMobileSystemImpl.instance
        val property = impl.getAppConfigString(configString, null, this)
        return if(property==null){
            false
        }else{
            if(property.toLowerCase() == "false"){
                false
            }else property.toLowerCase() == "true"

        }
    }

    override fun showSettings(show: Boolean) {

        val settingsMenuItem = mOptionsMenu!!.findItem(R.id.menu_action_settings)
        if (settingsMenuItem != null) {
            settingsMenuItem.isVisible = show
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
        //If this activity started from other activity
        when (item.itemId) {
            R.id.menu_action_share -> mPresenter!!.handleClickShareIcon()
            R.id.menu_action_bulk_upload_master -> mPresenter!!.handleClickBulkUpload()
            R.id.menu_action_logout -> {
                finishAffinity()
                mPresenter!!.handleLogOut()
            }
            R.id.menu_action_settings -> mPresenter!!.handleClickSettingsIcon()
            R.id.menu_action_about -> mPresenter!!.handleClickAbout()
            R.id.menu_action_sync -> forceSync()
            R.id.menu_action_clear_history -> {
                GlobalScope.launch {
                    val database = UmAccountManager.getActiveDatabase(this)
                    database.networkNodeDao.deleteAllAsync()
                    database.downloadJobItemHistoryDao.deleteAllAsync()
                    database.downloadJobDao.deleteAllAsync()
                    database.contentEntryStatusDao.deleteAllAsync()
                }

            }
            R.id.menu_action_create_new_content -> {
                val args = HashMap<String,String?>()
                args.putAll(UMAndroidUtil.bundleToMap(intent.extras))
                args[ContentEntryEditView.CONTENT_TYPE] = ContentEntryListView.CONTENT_CREATE_FOLDER.toString()
                args[ContentEntryEditView.CONTENT_ENTRY_LEAF] = false.toString()
                //TODO: Check if we want this still
//                UstadMobileSystemImpl.instance.go(ContentEntryEditView.VIEW_NAME, args,
//                        this)
            }
            R.id.menu_action_send_feedback -> hearShake()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun checkSyncFinished() {
        if (mOptionsMenu == null)
            return
        if (lastSyncTime > 0) {
            val syncItem = mOptionsMenu!!.findItem(R.id.menu_action_sync)
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

    private fun updateSyncing() {
        if (mOptionsMenu == null)
            return

        val syncItem = mOptionsMenu!!.findItem(R.id.menu_action_sync) ?: return

        val syncingString = getString(R.string.syncing)
        syncItem.title = syncingString

        val textWithColor = SpannableString(syncingString)
        textWithColor.setSpan(ForegroundColorSpan(
                ContextCompat.getColor(applicationContext, R.color.text_secondary)), 0,
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
                this, Observer{ workInfoList ->
            for (wi in workInfoList) {
                if (wi.state.isFinished) {
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
     * Set content list fragment related menu items's visibility
     */
    private fun setContentFragmentMenuItems(override:Boolean){

        if(mOptionsMenu != null) {
            val createNewContentItem = mOptionsMenu!!.findItem(R.id.menu_action_create_new_content)
            val clearHistoryItem = mOptionsMenu!!.findItem(R.id.menu_action_clear_history)

            if (override) {
                //Create content button
                val account = UmAccountManager.getActiveAccount(this)
                val showControls = UstadMobileSystemImpl.instance.getAppConfigString(
                        AppConfig.KEY_SHOW_CONTENT_EDITOR_CONTROLS,
                        null, this)!!.toBoolean()
                createNewContentItem!!.isVisible = showControls
                        && account != null
                        && account.personUid != 0L

                //Clear history visibility
                clearHistoryItem!!.isVisible =
                        isConfigTrue(AppConfig.ACTION_CLEAR_HISTORY_VISIBILITY)
            } else {
                createNewContentItem!!.isVisible = false
            }
        }
    }
    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_home, menu)

        //Get menu items:
        val shareMenuItem = menu.findItem(R.id.menu_action_share)
        val bulkUploadMenuItem = menu.findItem(R.id.menu_action_bulk_upload_master)
        val settingsMenuItem = menu.findItem(R.id.menu_action_settings)
        val logoutMenuItem = menu.findItem(R.id.menu_action_logout)
        val createNewContentItem = menu.findItem(R.id.menu_action_create_new_content)

        //Get menu item icons
        val shareMenuIcon = AppCompatResources.getDrawable(applicationContext,
                R.drawable.ic_share_white_24dp)
        val bulkUploadMenuIcon = AppCompatResources.getDrawable(applicationContext,
                R.drawable.ic_file_upload_white_24dp)
        val settingsMenuIcon = AppCompatResources.getDrawable(applicationContext,
                R.drawable.ic_settings_white_24dp)
        val logoutMenuIcon = AppCompatResources.getDrawable(applicationContext,
                R.drawable.ic_dropout_bcd4_24dp)
        val createNewContentIcon = AppCompatResources.getDrawable(applicationContext,
                R.drawable.ic_create_new_folder_white_24dp)

        //Tint the icons according to theme
        shareMenuIcon!!.setColorFilter(ContextCompat.getColor(applicationContext, R.color.icons),
                PorterDuff.Mode.SRC_IN)
        bulkUploadMenuIcon!!.setColorFilter(ContextCompat.getColor(applicationContext, R.color.icons),
                PorterDuff.Mode.SRC_IN)
        settingsMenuIcon!!.setColorFilter(ContextCompat.getColor(applicationContext, R.color.icons), PorterDuff.Mode.SRC_IN)
        logoutMenuIcon!!.setColorFilter(ContextCompat.getColor(applicationContext, R.color.icons), PorterDuff.Mode.SRC_IN)
        createNewContentIcon!!.setColorFilter(ContextCompat.getColor(applicationContext, R.color.icons),
                PorterDuff.Mode.SRC_IN)

        //Set tinted icons to the menu items:
        shareMenuItem.icon = shareMenuIcon
        bulkUploadMenuItem.icon = bulkUploadMenuIcon
        settingsMenuItem.icon = settingsMenuIcon
        logoutMenuItem.icon = logoutMenuIcon
        createNewContentItem!!.icon = createNewContentIcon

        mOptionsMenu = menu

        //Set visibility:
        setContentFragmentMenuItems(onContent)
        shareMenuItem.isVisible = isConfigTrue(AppConfig.ACTION_SHARE_APP_VISIBILITY)

        //Observe logged in person so that the UI reflects if the logged in user changes
        mPresenter!!.getLoggedInPerson()

        checkSyncFinished()

        //Search stuff
        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.menu_action_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))
        searchView.maxWidth = Integer.MAX_VALUE

        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // filter recycler view when query submitted
                searchLogic(mPager!!.currentItem, query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                // filter recycler view when query changed
                searchLogic(mPager!!.currentItem, query)
                return false
            }
        })

        searchView.setOnCloseListener{
            // filter recycler view when query submitted
            searchLogic(mPager!!.currentItem, "")
            false
        }
        return true
    }

    private fun searchLogic(position:Int, query:String){
        when (position) {
            VIEW_POSITION_POSITION_FEED -> {
            }
            VIEW_POSITION_POSITION_CLASSES -> {
                if(classesFragment!=null) {
                    classesFragment!!.searchClasses(query)
                }
            }
            VIEW_POSITION_POSITION_PEOPLE -> {
                if(peopleListFragment != null) {
                    peopleListFragment!!.searchPeople(query)
                }
            }
            VIEW_POSITION_POSITION_REPORTS -> {
            }
            else -> {
            }
        }
    }

    /**
     * Updates the toolbar's title
     * @param title The string of the title to be set to the toolbar
     */
    private fun updateTitle(title: String) {
        toolbar!!.title = title
        toolbarTitle.text = title
    }

    override fun showShareAppDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.share_application)
        builder.setView(R.layout.fragment_share_app_dialog)
        builder.setPositiveButton(R.string.share, null)
        builder.setNegativeButton(R.string.cancel, null)
        shareAppDialog = builder.create()
        shareAppDialog!!.setOnShowListener{
            val okButton = shareAppDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener{ mPresenter!!.handleConfirmShareApp() }
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
    private inner class BasePointViewPagerAdapter internal constructor(fm: FragmentManager)
        : FragmentStatePagerAdapter(fm) {

        override fun getCount(): Int {
            return bottomCount
        }

        //Map of position and fragment
        internal var positionMap: WeakHashMap<Int, UstadBaseFragment> = WeakHashMap()

        /**
         * Generate fragment for that page/position
         *
         * @param position  position of item
         * @return  the fragment
         */
        override fun getItem(position: Int): Fragment? {
            var thisFragment = positionMap[position]

            if(thisFragment!=null){
                return thisFragment
            }

            val f = navToFragment[position]
            this.positionMap[position] = f

            when (position) {

                VIEW_POSITION_POSITION_FEED -> {
                    return f
                }
                VIEW_POSITION_POSITION_CLASSES -> {
                    classesFragment = f as ClazzListFragment?
                    return f
                }
                VIEW_POSITION_POSITION_PEOPLE -> {
                    peopleListFragment = f as PeopleListFragment?
                    return f
                }
                VIEW_POSITION_POSITION_REPORTS -> {
                    return f
                }

                position -> {
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
    private fun fetchColor(color: Int): Int {
        return ContextCompat.getColor(this, color)
    }

    companion object {

        var VIEW_POSITION_POSITION_FEED = -1
        var VIEW_POSITION_POSITION_CLASSES = -1
        var VIEW_POSITION_POSITION_PEOPLE = -1
        var VIEW_POSITION_POSITION_REPORTS = -1
        var VIEW_POSITION_POSITION_CONTENT = -1

        private val IMAGE_PERSON_THUMBNAIL_WIDTH = 26


        private fun dpToPxImagePerson(): Int {
            return (IMAGE_PERSON_THUMBNAIL_WIDTH * Resources.getSystem().displayMetrics.density).toInt()
        }
    }


}
