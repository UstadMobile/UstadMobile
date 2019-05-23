package com.ustadmobile.port.android.view

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AboutView
import com.ustadmobile.core.view.DummyView
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroidBle
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DummyActivity : UstadBaseActivity(), DummyView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dummy)

        val toolbar = findViewById<Toolbar>(R.id.entry_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.app_name)

        val viewPager = findViewById<ViewPager>(R.id.library_viewpager)
        viewPager.adapter = LibraryPagerAdapter(supportFragmentManager, this)
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_dummy_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        if (itemId == R.id.action_open_about) {
            UstadMobileSystemImpl.instance.go(AboutView.VIEW_NAME, this)
        } else if (itemId == R.id.action_clear_history) {
            GlobalScope.launch {
                val database = UmAppDatabase.getInstance(this)
                database.networkNodeDao.deleteAllAsync()
                database.entryStatusResponseDao.deleteAllAsync()
                database.downloadJobItemHistoryDao.deleteAllAsync()
                database.downloadJobDao.deleteAllAsync()
                database.downloadJobItemDao.deleteAllAsync()
                database.contentEntryStatusDao.deleteAllAsync()
            }
            networkManagerBle.clearHistories()
        } else if (itemId == R.id.action_send_feedback) {
            hearShake()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle)
        val impl = UstadMobileSystemImpl.instance
        runAfterGrantingPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                Runnable { (networkManagerBle as NetworkManagerAndroidBle).checkP2PBleServices() },
                impl.getString(MessageID.location_permission_title, this),
                impl.getString(MessageID.location_permission_message, this))
    }

    class LibraryPagerAdapter internal constructor(fragmentManager: FragmentManager, private val context: Context) : FragmentPagerAdapter(fragmentManager) {
        private val impl: UstadMobileSystemImpl

        init {
            impl = UstadMobileSystemImpl.instance
        }

        // Returns total number of pages
        override fun getCount(): Int {
            return NUM_ITEMS
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment? {
            val bundle = Bundle()

            when (position) {
                0 // Fragment # 0 - This will show FirstFragment
                -> {
                    bundle.putString(ARG_CONTENT_ENTRY_UID, MASTER_SERVER_ROOT_ENTRY_UID.toString())
                    return ContentEntryListFragment.newInstance(bundle)
                }
                1 // Fragment # 0 - This will show FirstFragment different title
                -> {
                    bundle.putString(ARG_DOWNLOADED_CONTENT, "")
                    return ContentEntryListFragment.newInstance(bundle)
                }
                else -> return null
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
            private val NUM_ITEMS = 2
        }

    }

    companion object {


        val MASTER_SERVER_ROOT_ENTRY_UID = -4103245208651563007L
    }
}
