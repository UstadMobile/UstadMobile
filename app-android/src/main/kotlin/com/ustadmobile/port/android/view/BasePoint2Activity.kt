package com.ustadmobile.port.android.view

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.work.WorkManager

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.BasePoint2Presenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.BasePoint2View
import com.ustadmobile.port.android.sync.UmAppDatabaseSyncWorker
import com.ustadmobile.port.android.view.BasePoint2Activity.BasePointViewPagerAdapter.Companion.BASEPOINT_ITEM_COUNT

import java.io.File
import java.util.WeakHashMap
import java.util.concurrent.TimeUnit

import java.security.AccessController.getContext


class BasePoint2Activity : UstadBaseActivity(), BasePoint2View {

    private var toolbar: Toolbar? = null

    private var mPager: ViewPager? = null
    private var mPagerAdapter: BasePointViewPagerAdapter? = null

    //Share app alert dialog
    private var shareAppDialog: AlertDialog? = null

    private var mPresenter: BasePoint2Presenter? = null

    private var mOptionsMenu: Menu? = null

    private var saleListFragment: SaleListFragment? = null
    private var comingSoonFragment: ComingSoonFragment? = null
    private var catalogListFragment: CatalogListFragment? = null

    internal lateinit var bottomNavigation: AHBottomNavigation
    private var ab: ActionBar? = null

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_basepoint, menu)
        mOptionsMenu = menu

        //Search stuff
        val searchItem = menu.findItem(R.id.menu_basepoint_search)

        searchItem.setOnMenuItemClickListener { item ->
            when (mPager!!.currentItem) {
                VIEW_POSITION_POSITION_CATALOG -> {
                }
                VIEW_POSITION_POSITION_INVENTORY -> {
                }
                VIEW_POSITION_POSITION_SALES -> saleListFragment!!.goToSearch()
                VIEW_POSITION_POSITION_COURSES -> {
                }
                VIEW_POSITION_POSITION_REPORTS -> {
                }
                else -> {
                }
            }
            true
        }

        return true
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_basepoint2)

        //set up view pager.
        setUpViewPager()

        //Set up Toolbar:
        toolbar = findViewById(R.id.activity_basepoint2_toolbar)
        toolbar!!.title = "Ustad Mobile"
        setSupportActionBar(toolbar)

        ab = supportActionBar
        ab!!.setHomeAsUpIndicator(R.drawable.ic_account_circle_white_36dp)
        ab!!.setDisplayHomeAsUpEnabled(true)

        //Call the Presenter
        mPresenter = BasePoint2Presenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Get the bottom navigation component
        bottomNavigation = findViewById<View>(R.id.activity_basepoint2_bottom_navigation)

        //Style it
        bottomNavigation.setDefaultBackgroundColor(getContextCompatColorFromColor(
                R.color.primary, applicationContext))
        bottomNavigation.setAccentColor(getContextCompatColorFromColor(
                R.color.text_primary, applicationContext))
        bottomNavigation.setInactiveColor(getContextCompatColorFromColor(
                R.color.bottom_navigation_unselected, applicationContext))
        bottomNavigation.setBehaviorTranslationEnabled(false)
        bottomNavigation.setNotificationBackgroundColor(getContextCompatColorFromColor(
                R.color.text_primary, applicationContext))
        bottomNavigation.setUseElevation(true, 2L)

        //Create the items to be added
        val catalog_item = AHBottomNavigationItem(R.string.catalog,
                R.drawable.ic_list_black_24dp, R.color.default_back_color)
        val inventory_item = AHBottomNavigationItem(R.string.inventory,
                R.drawable.ic_assignment_black_24dp, R.color.default_back_color)
        val sales_item = AHBottomNavigationItem(R.string.sales,
                R.drawable.ic_payment_note_cash_black_24dp, R.color.default_back_color)
        val courses_item = AHBottomNavigationItem(R.string.courses,
                R.drawable.ic_collections_bookmark_black_24dp, R.color.default_back_color)
        val reports_item = AHBottomNavigationItem(R.string.reports,
                R.drawable.ic_insert_chart_black_24dp, R.color.default_back_color)


        bottomNavigation.addItem(catalog_item)
        bottomNavigation.addItem(inventory_item)
        bottomNavigation.addItem(sales_item)
        bottomNavigation.addItem(courses_item)
        bottomNavigation.addItem(reports_item)

        //Telling navigation to always show the text on the items. Unlike Google's
        // own implementation.
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW)

        //Click listeners for the items.
        bottomNavigation.setOnTabSelectedListener({ position, wasSelected ->

            if (!wasSelected) {
                mPagerAdapter!!.getItem(position.toInt())
                mPager!!.currentItem = position.toInt()
            }

            //Update title
            when (position) {
                0 -> updateTitle(getText(R.string.catalog).toString())
                1 -> updateTitle(getText(R.string.inventory).toString())
                2 -> updateTitle(getText(R.string.sales).toString())
                3 -> updateTitle(getText(R.string.courses).toString())
                4 -> updateTitle(getText(R.string.mne_dashboard).toString())
            }
            true
        })

        // Setting the very 1st item as default home screen.
        bottomNavigation.setCurrentItem(2)

        mPresenter!!.updateDueCountOnView()

    }

    override fun updateImageOnView(imagePath: String) {
        val output = File(imagePath)

        if (output.exists()) {
            val profileImage = Uri.fromFile(output)

            val iconDimen = dpToPx(36)
            runOnUiThread {
                Picasso.get()
                        .load(profileImage)
                        .transform(CircleTransform())
                        .resize(iconDimen, iconDimen)
                        .centerCrop()
                        .into(object : Target {
                            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                                val d = BitmapDrawable(resources, bitmap)
                                ab!!.setHomeAsUpIndicator(d)
                                ab!!.setDisplayHomeAsUpEnabled(true)
                            }

                            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {

                            }

                            override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
                        })
            }

        }

    }

    private fun setUpViewPager() {
        mPager = findViewById(R.id.activity_basepoint2_viewpager)
        mPagerAdapter = BasePointViewPagerAdapter(supportFragmentManager)
        mPager!!.adapter = mPagerAdapter
    }

    /**
     * Updates the toolbar's title
     * @param title The string of the title to be set to the toolbar
     */
    fun updateTitle(title: String) {
        toolbar!!.title = title
    }

    override fun showCatalog(show: Boolean) {

    }

    override fun showInventory(show: Boolean) {

    }

    override fun showSales(show: Boolean) {

    }

    override fun showCourses(show: Boolean) {

    }

    override fun shareAppSetupFile(filePath: String) {

    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        when (item.itemId) {
            android.R.id.home -> {
                mPresenter!!.handleClickPersonIcon()
                return true
            }
        }
        if (i == R.id.menu_basepoint_about) {
            mPresenter!!.handleClickAbout()
        }
        if (i == R.id.menu_basepoint_sync) {
            forceSync()
        }

        if (i == R.id.menu_basepoint_search) {
            when (mPager!!.currentItem) {
                VIEW_POSITION_POSITION_CATALOG -> catalogListFragment!!.searchCatalog("")
                VIEW_POSITION_POSITION_INVENTORY -> {
                }
                VIEW_POSITION_POSITION_SALES -> saleListFragment!!.goToSearch()
                VIEW_POSITION_POSITION_COURSES -> {
                }
                VIEW_POSITION_POSITION_REPORTS -> {
                }
                else -> {
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun forceSync() {

        //TODO: KMP Sync stuff
        //        WorkManager.getInstance().cancelAllWorkByTag(UmAppDatabaseSyncWorker.TAG);
        //        UmAppDatabaseSyncWorker.queueSyncWorker(100, TimeUnit.MILLISECONDS);

        updateSyncing()
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

    override fun sendToast(messageId: Int) {
        val impl = UstadMobileSystemImpl.instance
        val message = impl.getString(messageId, getContext())

        runOnUiThread {
            Toast.makeText(
                    applicationContext,
                    message,
                    Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun checkPermissions() {

    }

    override fun showShareAppDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.share_application)
        builder.setView(R.layout.fragment_share_app_dialog)
        builder.setPositiveButton(R.string.share, null)
        builder.setNegativeButton(R.string.cancel, null)
        shareAppDialog = builder.create()
        shareAppDialog!!.setOnShowListener { dialogInterface ->
            val okButton = shareAppDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener { v -> mPresenter!!.handleConfirmShareApp() }
        }
        shareAppDialog!!.show()
    }

    override fun dismissShareAppDialog() {
        shareAppDialog!!.dismiss()
        shareAppDialog = null
    }

    override fun updateNotificationForSales(number: Int) {
        //Sprint 2
        //Send notification to 2nd last item (sales)
        var nString = number.toString()
        if (number == 0) {
            nString = ""
        }

        bottomNavigation.setNotification(nString,
                bottomNavigation.getItemsCount() - 3)
    }

    /**
     * Feed view pager's Adapter
     */
    inner class BasePointViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        //Map of position and fragment
        private val positionMap: WeakHashMap<Int, UstadBaseFragment>

        init {
            positionMap = WeakHashMap()
        }//Constructor creates the adapter

        /**
         * Generate fragment for that page/position
         *
         * @param position  position of item
         * @return  the fragment
         */
        override fun getItem(position: Int): Fragment? {

            val thisFragment = positionMap[position]
            return thisFragment ?: when (position) {
                0 -> {
                    catalogListFragment = CatalogListFragment()
                    this.positionMap[position] = catalogListFragment
                    catalogListFragment
                }
                1 -> {
                    comingSoonFragment = ComingSoonFragment()
                    this.positionMap[position] = comingSoonFragment
                    comingSoonFragment
                }
                2 -> {
                    saleListFragment = SaleListFragment.newInstance()
                    this.positionMap[position] = saleListFragment
                    saleListFragment
                }
                3 -> {
                    comingSoonFragment = ComingSoonFragment()
                    this.positionMap[position] = comingSoonFragment
                    comingSoonFragment
                }
                4 -> {
                    //TODO: KMP Sprint
                    comingSoonFragment = ComingSoonFragment()
                    this.positionMap[position] = comingSoonFragment
                    comingSoonFragment
                }
                //                        dashboardEntryListFragment = DashboardEntryListFragment.newInstance();
                //                        this.positionMap.put(position, dashboardEntryListFragment);
                //                        return dashboardEntryListFragment;
                else -> null
            }

        }

        override fun getCount(): Int {

            return BASEPOINT_ITEM_COUNT
        }

        companion object {
            private val BASEPOINT_ITEM_COUNT = 5
        }
    }

    companion object {
        //private DashboardEntryListFragment dashboardEntryListFragment;

        val VIEW_POSITION_POSITION_CATALOG = 0
        val VIEW_POSITION_POSITION_INVENTORY = 1
        val VIEW_POSITION_POSITION_SALES = 2
        val VIEW_POSITION_POSITION_COURSES = 3
        val VIEW_POSITION_POSITION_REPORTS = 4


        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }
}
