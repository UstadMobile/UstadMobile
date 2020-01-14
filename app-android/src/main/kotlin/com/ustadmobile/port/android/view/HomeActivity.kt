package com.ustadmobile.port.android.view

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.HomePresenter
import com.ustadmobile.core.controller.HomePresenter.Companion.MASTER_SERVER_ROOT_ENTRY_UID
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.AboutView
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_EDIT_BUTTONS_CONTROL_FLAG
import com.ustadmobile.core.view.ContentEntryListView.Companion.EDIT_BUTTONS_NEWFOLDER
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.sharedse.network.NetworkManagerBle
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_home.*
import ru.dimorinny.floatingtextbutton.FloatingTextButton


class HomeActivity : UstadBaseWithContentOptionsActivity(), HomeView, ViewPager.OnPageChangeListener{

    private lateinit var presenter: HomePresenter

    private lateinit var downloadAllBtn: FloatingTextButton

    private lateinit var profileImage: CircleImageView

    val impl = UstadMobileSystemImpl.instance

    /**
     * In case we have addition bottom nav items, add icons here and map to their labels
     */
    private val bottomLabelToIconMap = mapOf(
            MessageID.reports to R.drawable.ic_pie_chart_black_24dp,
            MessageID.contents to R.drawable.ic_local_library_black_24dp
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        downloadAllBtn = findViewById(R.id.download_all)

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
    }

    override fun showDownloadAllButton(show: Boolean) {
        downloadAllBtn.visibility = if(show) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun setLoggedPerson(person: Person) {}

    override fun setOptions(options: List<Pair<Int, String>>) {
        options.forEach {
            val navIcon = bottomLabelToIconMap[it.first]
            if(navIcon != null){
                val navigationItem = AHBottomNavigationItem(
                        impl.getString(it.first, this), navIcon)
                umBottomNavigation.addItem(navigationItem)
            }

        }
        umBottomNavigation.visibility = if(options.size > 1) View.VISIBLE else View.GONE

        umBottomNavigation.defaultBackgroundColor = ContextCompat.getColor(this, R.color.icons)
        umBottomNavigation.accentColor = ContextCompat.getColor(this, R.color.primary)
        umBottomNavigation.inactiveColor = ContextCompat.getColor(this, R.color.text_secondary)
        umBottomNavigation.isBehaviorTranslationEnabled = false
        umBottomNavigation.currentItem = 0
        umBottomNavigation.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        umBottomNavigation.setOnTabSelectedListener { position: Int, _: Boolean ->
            handleFragmentTransaction(options[position].second)
            true
        }
        handleFragmentTransaction(options[0].second)
    }

    private fun handleFragmentTransaction(params: String){
        val bundle = UMAndroidUtil.mapToBundle(UMFileUtil.parseURLQueryString(params))
        if(bundle != null){

            val selectedFragment = when {
                params.contains(ContentEntryListView.VIEW_NAME) -> {
                    bundle.putString(ARG_CONTENT_ENTRY_UID, MASTER_SERVER_ROOT_ENTRY_UID.toString())
                    bundle.putString(ARG_EDIT_BUTTONS_CONTROL_FLAG, EDIT_BUTTONS_NEWFOLDER.toString())
                    ContentEntryListFragment.newInstance(bundle)
                }
                else -> {
                    ReportDashboard()
                }
            }
            val fragmentManager:FragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.um_host_fragment,selectedFragment,selectedFragment.tag)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
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
            R.id.action_open_about -> UstadMobileSystemImpl.instance.go(AboutView.VIEW_NAME, this)
            R.id.action_send_feedback -> hearShake()
            R.id.action_share_app -> presenter.handleClickShareApp()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle)
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

}
