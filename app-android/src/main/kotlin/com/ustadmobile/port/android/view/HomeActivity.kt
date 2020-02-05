package com.ustadmobile.port.android.view

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.HomePresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.staging.core.view.SearchableListener
import com.ustadmobile.staging.port.android.view.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_home.*
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.File
import java.util.*


class HomeActivity : UstadBaseWithContentOptionsActivity(), HomeView, ViewPager.OnPageChangeListener{

    private lateinit var presenter: HomePresenter

    private lateinit var downloadAllBtn: FloatingTextButton

    private lateinit var profileImage: CircleImageView

    val impl = UstadMobileSystemImpl.instance

    private var options: List<Pair<Int, String>> = listOf()

    private lateinit var mPager: ViewPager

    //Menu options
    private lateinit var mOptionsMenu: Menu
    //Show settings flag
    private var personAdmin = false
    //The toolbar title
    private lateinit var toolbarTitle: TextView
    //The search view
    private lateinit var searchView : SearchView
    //A reference to current fragment so we can search for search
    //private var currentFragment : UstadBaseFragment ?= null
    private var currentFragmentPosition : Int = 0
    private var fragmentPosMap: MutableMap<Int, UstadBaseFragment> = WeakHashMap()

    private inner class HomePagerAdapter(fm: FragmentManager, val options: List<Pair<Int, String>>)
        : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            var thisFragment = fragmentPosMap[position]
            if(thisFragment == null) {
                val viewUri = options[position].second // the ViewName followed by ? and any arguments
                val viewName = viewUri.substringBefore('?')
                val fragmentClass = VIEW_NAME_TO_FRAGMENT_CLASS[viewName]
                if(fragmentClass == null) {
                    throw IllegalArgumentException("HomeActivity does not know Fragment to " +
                            "create for $viewName")
                }

                thisFragment = fragmentClass.newInstance()
                thisFragment.arguments =
                        UMAndroidUtil.mapToBundle(UMFileUtil.parseURLQueryString(viewUri))
                fragmentPosMap[position] = thisFragment
            }


            return thisFragment
        }

        override fun getCount() = options.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mPager = findViewById(R.id.home_view_pager)
        downloadAllBtn = findViewById(R.id.download_all)
        toolbarTitle = findViewById(R.id.toolBarTitle)

        val toolbar = findViewById<Toolbar>(R.id.entry_toolbar)
        coordinatorLayout = findViewById(R.id.coordinationLayout)
        profileImage = findViewById(R.id.profile_image)
        setSupportActionBar(toolbar)
        findViewById<TextView>(R.id.toolBarTitle).setText(R.string.app_name)

        downloadAllBtn.setOnClickListener {
            presenter.handleDownloadAllClicked()
        }

        presenter = HomePresenter(this, UMAndroidUtil.bundleToMap(intent.extras),
                this, UmAccountManager.getActiveDatabase(this).personDao,
                UstadMobileSystemImpl.instance)
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        profileImage.setOnClickListener {
            presenter.handleClickPersonIcon()
        }
    }

    override fun loadProfileIcon(profileUrl: String) {
        UMAndroidUtil.loadImage(profileUrl,R.drawable.ic_account_circle_white_24dp,profileImage)
        if(profileUrl.isNotEmpty()){
            loadProfileImage(profileUrl)
        }
    }

    private fun loadProfileImage(imagePath: String) {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home_activity, menu)
        mOptionsMenu = menu

        val settingsMenuItem = mOptionsMenu.findItem(R.id.menu_home_activity_settings)

        if (settingsMenuItem != null) {
            settingsMenuItem.isVisible = personAdmin
        }

        //Search stuff
        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.menu_home_activity_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))
        searchView.maxWidth = Integer.MAX_VALUE

        updateSearchVisibility()

        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // filter recycler view when query submitted
                searchLogic(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                // filter recycler view when query changed
                searchLogic(query)
                return false
            }
        })

        searchView.setOnCloseListener{
            // filter recycler view when query submitted
            searchLogic( "")
            false
        }

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Search logic for triggering search listener for current fragment for a given query string
     */
    private fun searchLogic(query: String){
        val currentFragment = fragmentPosMap[currentFragmentPosition]
        val currentFragmentNN = currentFragment
        if(currentFragmentNN != null && currentFragmentNN is SearchableListener){
            (currentFragmentNN as SearchableListener).onSearchQueryUpdated(query)
        }
    }

    override fun setLoggedPerson(person: Person) {
        //Show settings based on if person is admin or not.
        personAdmin = person.admin
        showSettings(person.admin)
    }

    override fun setOptions(options: List<Pair<Int, String>>) {
        this.options = options

        options.forEach {
            val navIcon = BOTTOM_LABEL_MESSAGEID_TO_ICON_MAP[it.first]
            if(navIcon != null){
                val navigationItem = AHBottomNavigationItem(
                        impl.getString(it.first, this), navIcon)
                umBottomNavigation.addItem(navigationItem)
            }
        }

        if(options.size > 1) {
            umBottomNavigation.visibility = View.VISIBLE

            mPager.apply {
                setPadding(paddingLeft, paddingTop, paddingRight, TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics).toInt())
            }
        }else {
            umBottomNavigation.visibility = View.INVISIBLE
            mPager.apply {
                setPadding(paddingLeft, paddingTop, paddingRight, 0)
            }
        }

        umBottomNavigation.defaultBackgroundColor =
                ContextCompat.getColor(this, R.color.bottomnav_background)
        umBottomNavigation.accentColor =
                ContextCompat.getColor(this, R.color.bottomnav_accent)
        umBottomNavigation.inactiveColor =
                ContextCompat.getColor(this, R.color.bottomnav_inactive)
        umBottomNavigation.isBehaviorTranslationEnabled = false
        umBottomNavigation.currentItem = 0
        umBottomNavigation.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW

        //Update title for bottom navigation selected. Also update current Item
        updateTitle(options[0].first)
        updateCurrentFragment(0)
        umBottomNavigation.setOnTabSelectedListener { position: Int, _: Boolean ->
            mPager.setCurrentItem(position)
            updateElevation(options[position].second)

            //Update title
            updateTitle(options[position].first)

            //Update search visibility
            updateSearchVisibility()

            //Update current fragment
            updateCurrentFragment(position)

            true
        }

        mPager.adapter = HomePagerAdapter(supportFragmentManager, options)
        updateElevation(options[0].second)

    }

    private fun updateElevation(optionUri: String) {
        val viewName = optionUri.substringBefore('?')
        findViewById<AppBarLayout>(R.id.appBar).elevation = if(viewName == HomePresenter.HOME_CONTENTENTRYLIST_TABS_VIEWNAME) {
            0f
        }else {
            10f
        }
    }

    private fun updateSearchVisibility(){
        //Set visibility based on current fragment
        val currentFragment = fragmentPosMap[currentFragmentPosition]
        searchView.isVisible = currentFragment is SearchableListener
        val searchViewMenuItem = mOptionsMenu.findItem(R.id.menu_home_activity_search)
        if(searchViewMenuItem != null){
            searchViewMenuItem.isVisible = currentFragment is SearchableListener
        }
    }

    private fun updateCurrentFragment(position: Int){
        currentFragmentPosition = position
    }

    private fun updateTitle(pos: Int){
        toolbarTitle.text = impl.getString(pos, viewContext)
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        presenter.handleShowDownloadButton(position == 0)
    }

    override fun restartUI() {}

    override fun showLanguageOptions() {}

    override fun setCurrentLanguage(language: String?) {}

    override fun setLanguageOption(languages: MutableList<String>) { }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_open_about ->
                UstadMobileSystemImpl.instance.go(AboutView.VIEW_NAME, this)
            R.id.action_send_feedback -> hearShake()
            R.id.action_share_app -> presenter.handleClickShareApp()
            R.id.menu_home_activity_settings -> presenter.handleClickSettings()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle)
        //Checks if we want to show it
        if(!presenter.showLocationPermission){
            return
        }
        val locationPermissionArr =arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
        val dialogTitle =impl.getString(MessageID.location_permission_title, this)
        val dialogMessage =impl.getString(MessageID.location_permission_message, this)
        val afterPermissionGrantedRunnable = Runnable { networkManagerBle.checkP2PBleServices() }

        runAfterGrantingPermission(locationPermissionArr, afterPermissionGrantedRunnable,
                dialogTitle, dialogMessage) {
            val alertDialog = AlertDialog.Builder(this@HomeActivity)
                    .setTitle(R.string.location_permission_title)
                    .setView(R.layout.view_locationpermission_dialogcontent)
                    .setNegativeButton(getString(android.R.string.cancel)
                    ) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                        runAfterGrantingPermission(locationPermissionArr,
                                afterPermissionGrantedRunnable,
                                dialogTitle, dialogMessage)
                    }
                    .create()

            alertDialog.setOnShowListener {
                alertDialog.findViewById<Button>(R.id.view_locationpermission_showmore_button)!!.setOnClickListener {view ->
                    val extraInfo = alertDialog.findViewById<TextView>(R.id.view_locationpermission_extra_details)!!
                    val button = view as Button
                    if(extraInfo.visibility == View.GONE) {
                        extraInfo.visibility = View.VISIBLE
                        button.text = resources.getText(R.string.less_information)
                        button.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                R.drawable.ic_keyboard_arrow_up_black_24dp, 0)
                    }else {
                        extraInfo.visibility = View.GONE
                        button.text = resources.getText(R.string.more_information)
                        button.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                R.drawable.ic_keyboard_arrow_down_black_24dp, 0)
                    }
                }
            }

            alertDialog
        }
    }

    override fun showShareAppDialog() {
        val dialog = ShareAppOfflineDialogFragment()
        dialog.show(supportFragmentManager, "SHARE_APP_DIALOG")
    }

    /**
     * Shows settings based on given parameter. Also sets variable
     */
    private fun showSettings(show: Boolean) {
        if(::mOptionsMenu.isInitialized) {
            val settingsMenuItem = mOptionsMenu.findItem(R.id.menu_home_activity_settings)
            if (settingsMenuItem != null) {
                settingsMenuItem.isVisible = show
            }

            //Bulk upload for ClassBook
            val bulkUploadMenuItem = mOptionsMenu.findItem(R.id.menu_action_bulk_upload_master)
            if(bulkUploadMenuItem != null){
                val bulkUploadVisibility = impl.getAppConfigString(
                        AppConfig.BULK_UPLOAD_VISIBILITY, null, this)!!.toBoolean()
                if(bulkUploadVisibility){
                    bulkUploadMenuItem.isVisible = show
                }
            }
        }
    }

    companion object {
        private val VIEW_NAME_TO_FRAGMENT_CLASS = mapOf(
                ContentEntryListView.VIEW_NAME to HomeContentEntryTabsFragment::class.java,
                ReportDashboardView.VIEW_NAME to ReportDashboardFragment::class.java,
                FeedListView.VIEW_NAME to FeedListFragment::class.java,
                ContentEntryListView.VIEW_NAME to ContentEntryListFragment::class.java,
                ContentEntryListView.VIEW_NAME to ContentListFragment::class.java,
                ClazzListView.VIEW_NAME to ClazzListFragment::class.java,
                PeopleListView.VIEW_NAME to PeopleListFragment::class.java,
                BaseReportView.VIEW_NAME to ReportSelectionFragment::class.java,
                HomePresenter.HOME_CONTENTENTRYLIST_TABS_VIEWNAME to HomeContentEntryTabsFragment::class.java)

        /**
         * In case we have addition bottom nav items, add icons here and map to their labels
         */
        private val BOTTOM_LABEL_MESSAGEID_TO_ICON_MAP = mapOf(
                MessageID.reports to R.drawable.ic_pie_chart_black_24dp,
                MessageID.contents to R.drawable.ic_local_library_black_24dp,
                MessageID.bottomnav_feed_title to FeedListFragment.icon,
                MessageID.bottomnav_content_title to ContentListFragment.icon,
                MessageID.bottomnav_classes_title to ClazzListFragment.icon,
                MessageID.bottomnav_people_title to PeopleListFragment.icon,
                MessageID.bottomnav_reports_title to ReportSelectionFragment.icon
        )

        private val IMAGE_PERSON_THUMBNAIL_WIDTH = 26

        private fun dpToPxImagePerson(): Int {
            return (IMAGE_PERSON_THUMBNAIL_WIDTH *
                    Resources.getSystem().displayMetrics.density).toInt()
        }
    }
}
