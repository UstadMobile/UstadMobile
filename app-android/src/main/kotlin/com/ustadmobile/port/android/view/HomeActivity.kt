package com.ustadmobile.port.android.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_RECYCLED_CONTENT
import com.ustadmobile.core.controller.HomePresenter
import com.ustadmobile.core.controller.HomePresenter.Companion.MASTER_SERVER_ROOT_ENTRY_UID
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
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.sharedse.network.NetworkManagerBle
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import android.content.Intent.ACTION_SEND
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ustadmobile.core.db.UmAppDatabase
import kotlinx.coroutines.Dispatchers
import java.io.File


class HomeActivity : UstadBaseWithContentOptionsActivity(), HomeView, ViewPager.OnPageChangeListener {

    private lateinit var presenter: HomePresenter

    private lateinit var downloadAllBtn: FloatingTextButton

    private lateinit var profileImage: CircleImageView

    private var shareAppDialog: AlertDialog? = null

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

        presenter = HomePresenter(this, UMAndroidUtil.bundleToMap(intent.extras),
                this, UmAppDatabase.getInstance(this).personDao,
                UstadMobileSystemImpl.instance)
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

    override fun setLoggedPerson(person: Person) {}

    override fun showReportMenu(show: Boolean) {}

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
            R.id.action_open_about -> UstadMobileSystemImpl.instance.go(AboutView.VIEW_NAME, this)
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
            R.id.action_share_app -> presenter.handleClickShareApp()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle)
        val impl = UstadMobileSystemImpl.instance
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
                        runAfterGrantingPermission(locationPermissionArr, afterPermissionGrantedRunnable,
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


    class LibraryPagerAdapter internal constructor(fragmentManager: FragmentManager, private val context: Context) : FragmentPagerAdapter(fragmentManager) {
        private val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

        // Returns total number of pages
        override fun getCount(): Int {
            val activeAccount = UmAccountManager.getActiveAccount(context)
            return if( activeAccount != null && activeAccount.personUid != 0L) 3 else 2
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment? {
            val bundle = Bundle()
            bundle.putString(ARG_CONTENT_ENTRY_UID, MASTER_SERVER_ROOT_ENTRY_UID.toString())
            when (position) {
                0 // Fragment # 0 - This will show FirstFragment
                ->  bundle.putString(ARG_LIBRARIES_CONTENT, "")
                1 // Fragment # 1 - This will show FirstFragment different title
                ->   bundle.putString(ARG_DOWNLOADED_CONTENT, "")
                2 // Fragment # 2 - This will show FirstFragment different title
                ->  bundle.putString(ARG_RECYCLED_CONTENT, "")
            }
            return ContentEntryListFragment.newInstance(bundle)
        }

        // Returns the page title for the top indicator
        override fun getPageTitle(position: Int): CharSequence? {

            when (position) {
                0 -> return impl.getString(MessageID.libraries, context)
                1 -> return impl.getString(MessageID.downloaded, context)
                2 -> return impl.getString(MessageID.recycled, context)
            }
            return null

        }
    }


}
