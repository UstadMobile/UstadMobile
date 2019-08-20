package com.ustadmobile.port.android.view

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.controller.HomePresenter
import com.ustadmobile.core.controller.HomePresenter.Companion.MASTER_SERVER_ROOT_ENTRY_UID
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AboutView
import com.ustadmobile.core.view.ContentEditorView.Companion.CONTENT_ENTRY_UID
import com.ustadmobile.core.view.ContentEntryEditView
import com.ustadmobile.core.view.ContentEntryEditView.Companion.CONTENT_ENTRY_LEAF
import com.ustadmobile.core.view.ContentEntryEditView.Companion.CONTENT_TYPE
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_FOLDER
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.sharedse.network.NetworkManagerBle
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton

class HomeActivity : UstadBaseWithContentOptionsActivity(), HomeView, ViewPager.OnPageChangeListener {

    private lateinit var presenter: HomePresenter

    private lateinit var downloadAllBtn: FloatingTextButton

    private lateinit var profileImage: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        downloadAllBtn = findViewById(R.id.download_all)

        val toolbar = findViewById<Toolbar>(R.id.entry_toolbar)
        coordinatorLayout = findViewById(R.id.coordinationLayout)
        profileImage = findViewById(R.id.profile_image)
        setSupportActionBar(toolbar)
        findViewById<TextView>(R.id.toolBarTitle).setText(R.string.app_name)

        val viewPager = findViewById<ViewPager>(R.id.library_viewpager)
        viewPager.adapter = LibraryPagerAdapter(supportFragmentManager, this)
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)

        downloadAllBtn.setOnClickListener {
            presenter.handleDownloadAllClicked()
        }

        viewPager.addOnPageChangeListener(this)

        presenter = HomePresenter(this, UMAndroidUtil.bundleToMap(intent.extras),this)
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        profileImage.setOnClickListener {
            presenter.handleClickPersonIcon()
        }
    }

    override fun loadProfileIcon(profileUrl: String) {
        UMAndroidUtil.loadImage(profileUrl,R.drawable.ic_account_circle_white_24dp,profileImage)
    }

    override fun showDownloadAllButton(show: Boolean) {
        downloadAllBtn.visibility = if(show) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val account = UmAccountManager.getActiveAccount(this)
        val showControls = UstadMobileSystemImpl.instance.getAppConfigString(
                AppConfig.KEY_SHOW_CONTENT_EDITOR_CONTROLS, null, this)!!.toBoolean()
        menuInflater.inflate(R.menu.menu_home_activity, menu)
        menu.findItem(R.id.create_new_content).isVisible = showControls && account != null && account.personUid != 0L
        return super.onCreateOptionsMenu(menu)
    }


    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        presenter.handleShowDownloadButton(position == 0)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_open_about -> UstadMobileSystemImpl.instance.go(AboutView.VIEW_NAME, this)
            R.id.action_clear_history -> {
                GlobalScope.launch {
                    val database = UmAppDatabase.getInstance(this)
                    database.networkNodeDao.deleteAllAsync()
                    database.entryStatusResponseDao.deleteAllAsync()
                    database.downloadJobItemHistoryDao.deleteAllAsync()
                    database.downloadJobDao.deleteAllAsync()
                    database.downloadJobItemDao.deleteAllAsync()
                    database.contentEntryStatusDao.deleteAllAsync()
                }
                networkManagerBle?.clearHistories()
            }
            R.id.create_new_content -> {
                val args = HashMap<String,String?>()
                args.putAll(UMAndroidUtil.bundleToMap(intent.extras))
                args[CONTENT_TYPE] = CONTENT_CREATE_FOLDER.toString()
                args[CONTENT_ENTRY_LEAF] = false.toString()
                args[CONTENT_ENTRY_UID] = 0.toString()
                args[ARG_CONTENT_ENTRY_UID] = MASTER_SERVER_ROOT_ENTRY_UID.toString()
                UstadMobileSystemImpl.instance.go(ContentEntryEditView.VIEW_NAME, args,
                        this)
            }
            R.id.action_send_feedback -> hearShake()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle)
        val impl = UstadMobileSystemImpl.instance
        runAfterGrantingPermission(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                Runnable { networkManagerBle.checkP2PBleServices() },
                impl.getString(MessageID.location_permission_title, this),
                impl.getString(MessageID.location_permission_message, this))
    }

    class LibraryPagerAdapter internal constructor(fragmentManager: FragmentManager, private val context: Context) : FragmentPagerAdapter(fragmentManager) {
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
                    bundle.putString(ARG_CONTENT_ENTRY_UID, MASTER_SERVER_ROOT_ENTRY_UID.toString())
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


}
