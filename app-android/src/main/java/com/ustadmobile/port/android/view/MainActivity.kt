package com.ustadmobile.port.android.view

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityMainBinding
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.DbPreloadWorker
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.NavControllerAdapter
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.util.DeleteTempFilesNavigationListener
import com.ustadmobile.port.android.view.binding.imageForeignKeyPlaceholder
import com.ustadmobile.port.android.view.binding.setImageForeignKey
import com.ustadmobile.port.android.view.binding.setImageForeignKeyAdapter
import com.ustadmobile.port.android.view.util.UstadActivityWithProgressBar
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.*
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.*


class MainActivity : UstadBaseActivity(), UstadListViewActivityWithFab,
        UstadActivityWithProgressBar,
        NavController.OnDestinationChangedListener,
        DIAware{

    private lateinit var appBarConfiguration: AppBarConfiguration

    override val activityFloatingActionButton: ExtendedFloatingActionButton?
        //Note: do not use mBinding here because it might not be ready for the first fragment
        get() = findViewById(R.id.activity_main_extendedfab)


    override val activityProgressBar: ProgressBar?
        //Note: do not use mBinding here because it might not be ready for the first fragment
        get() = findViewById(R.id.main_progress_bar)

    private lateinit var mBinding: ActivityMainBinding

    private val impl : UstadMobileSystemImpl by instance()

    private val accountManager: UstadAccountManager by instance()

    private var mIsAdmin: Boolean? = null

    private var searchView: SearchView? = null

    private val destinationProvider: DestinationProvider by instance()

    private lateinit var navController: NavController

    private lateinit var ustadNavController: UstadNavController

    private val userProfileDrawable: Drawable? by lazy(LazyThreadSafetyMode.NONE) {
        ContextCompat.getDrawable(this, R.drawable.ic_account_circle_black_24dp)?.also {
            it.setTint(ContextCompat.getColor(this, R.color.onPrimaryColor))
        }
    }

    //Check contentonly mode. See appconfig.properties for details. When enabled, the bottom nav
    // is only visible as admin (e.g. normal users only see content)
    private val contentOnlyForNonAdmin: Boolean by lazy {
        impl.getAppConfigBoolean(AppConfig.KEY_CONTENT_ONLY_MODE, this)
    }

    //This is actually managed by the underlying fragments.
    override var loading: Boolean
        get() = false
        set(@Suppress("UNUSED_PARAMETER") value) {}

    //Observe the active account to show/hide the settings based on whether or not the user is admin
    private val mActiveUserObserver = Observer<UmAccount> {account ->
        GlobalScope.launch(Dispatchers.Main) {
            val db: UmAppDatabase = di.on(Endpoint(account.endpointUrl)).direct.instance(tag = TAG_DB)
            val isNewUserAdmin = db.personDao.personIsAdmin(account.personUid)
            if(isNewUserAdmin != mIsAdmin) {
                mIsAdmin = isNewUserAdmin
                invalidateOptionsMenu()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val uri = intent?.data?.toString()

        if(uri != null){
            impl.goToDeepLink(uri, accountManager, this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(mBinding.mainCollapsingToolbar.toolbar)

        val host: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.activity_main_navhost_fragment) as NavHostFragment? ?: return
        navController = host.navController
        ustadNavController = NavControllerAdapter(navController, destinationProvider)
        navController.addOnDestinationChangedListener(this)
        navController.addOnDestinationChangedListener(DeleteTempFilesNavigationListener(this))
        appBarConfiguration = AppBarConfiguration(navController.graph)
        mBinding.bottomNavView.setupWithNavController(navController)

        fun NavController.navigateToRootMenuItem(menuItem: MenuItem) {
            if(currentDestination?.id != menuItem.itemId)
                navigate(menuItem.itemId, args = bundleOf(), navOptions = navOptions {
                    popUpTo(R.id.redirect_dest) {
                        inclusive = false
                    }
                })
        }

        mBinding.bottomNavView.setOnItemSelectedListener {
            navController.navigateToRootMenuItem(it)
            true
        }

        mBinding.bottomNavView.setOnItemReselectedListener {
            navController.navigateToRootMenuItem(it)
        }

        setupActionBarWithNavController(navController,
            AppBarConfiguration(mBinding.bottomNavView.menu))

        DbPreloadWorker.queuePreloadWorker(applicationContext)
        accountManager.activeAccountLive.observe(this, mActiveUserObserver)
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination,
                                      arguments: Bundle?) {
        invalidateOptionsMenu()
        onAppBarExpand(true)

        val ustadDestination = destinationProvider.lookupDestinationById(destination.id)
        val scrollFlags = ustadDestination?.actionBarScrollBehavior ?:
            (AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL)
        (mBinding.mainCollapsingToolbar.collapsingToolbar.layoutParams as? AppBarLayout.LayoutParams)?.scrollFlags = scrollFlags

        val userHasBottomNav = !contentOnlyForNonAdmin || accountManager.activeAccount.admin
        mBinding.bottomNavView.visibility = if(!userHasBottomNav || ustadDestination?.hideBottomNavigation == true) {
            View.GONE
        } else {
            slideBottomNavigation(true)
            View.VISIBLE
        }
    }

    fun onAppBarExpand(expand: Boolean){
        mBinding.mainCollapsingToolbar.appbar.setExpanded(expand)
    }

    fun slideBottomNavigation(visible: Boolean) {
        val layoutParams = (mBinding.bottomNavView.layoutParams as? CoordinatorLayout.LayoutParams)
        val bottomNavBehavior = layoutParams?.behavior as? HideBottomViewOnScrollBehavior

        if (visible)
            bottomNavBehavior?.slideUp(mBinding.bottomNavView)
        else
            bottomNavBehavior?.slideDown(mBinding.bottomNavView)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.activity_main_navhost_fragment).navigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val navController = findNavController(R.id.activity_main_navhost_fragment)
        val currentFrag = navController.currentDestination?.id ?: 0
        val mainScreenItemsVisible = BOTTOM_NAV_DEST.contains(currentFrag)
        menu.findItem(R.id.menu_main_settings).isVisible = (mainScreenItemsVisible && mIsAdmin == true)
        menu.findItem(R.id.menu_share_offline).isVisible = mainScreenItemsVisible

        //Should be hidden when they are on the accounts page (only)
        val currentDestination = destinationProvider.lookupDestinationById(currentFrag)
        menu.findItem(R.id.menu_main_profile).isVisible = currentDestination?.hideAccountIcon != true

        searchView = menu.findItem(R.id.menu_search).actionView as SearchView

        setUserProfile(menu.findItem(R.id.menu_main_profile))

        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_main_settings -> handleClickSettings()
            R.id.menu_share_offline -> {
                handleConfirmShareApp()
            }
        }
        return item.onNavDestinationSelected(findNavController(R.id.activity_main_navhost_fragment))
                || super.onOptionsItemSelected(item)
    }


    private fun handleConfirmShareApp() {
        GlobalScope.launch(Dispatchers.IO) {
            val apkFile = File(applicationContext.applicationInfo.sourceDir)
            val appName = applicationContext.getString(R.string.app_name).replace(" ", "_")
            val baseName = "$appName.apk"

            val outDir = File(applicationContext.filesDir, "external")
            if (!outDir.isDirectory)
                outDir.mkdirs()

            val outFile: File

            var fileOutputStreamToClose: OutputStream? = null
            var apkFileIn: InputStream? = null
            try {
                apkFileIn = FileInputStream(apkFile)

                outFile = File(outDir, baseName)
                fileOutputStreamToClose = FileOutputStream(outFile)
                apkFileIn.copyTo(fileOutputStreamToClose)

                fileOutputStreamToClose.flush()
                fileOutputStreamToClose.close()
                apkFileIn.close()


                val applicationId = applicationContext.packageName
                val sharedUri = FileProvider.getUriForFile(applicationContext, "$applicationId.provider",
                    outFile)
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.setType("application/vnd.android.package-archive")
                shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri)
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                if (shareIntent.resolveActivity(applicationContext.packageManager) != null) {
                    applicationContext.startActivity(shareIntent)
                }else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, R.string.error_this_device_doesnt_support_bluetooth_sharing,
                            Toast.LENGTH_LONG).show()
                    }
                }
                
            }catch(e: Exception) {
                e.printStackTrace()
            }finally {
                fileOutputStreamToClose?.flush()
                fileOutputStreamToClose?.close()
            }


        }
    }

    override var networkManager: CompletableDeferred<NetworkManagerBle>? = null

    override fun onBackPressed() {
        val fragment = supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.get(0)
        when {
            searchView?.isIconified == false -> {
                searchView?.setQuery("",true)
                searchView?.isIconified = true
                return
            }
            (fragment as? FragmentBackHandler)?.onHostBackPressed() == true -> {
                return
            }
            else -> {
                super.onBackPressed()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        searchView = null
    }

    /**
     * When settings gear clicked in the menu options - Goes to the settings activity.
     */
    private fun handleClickSettings() {
        impl.go(SettingsView.VIEW_NAME, mapOf(), this)
    }

    private fun setUserProfile(menuItem: MenuItem) {
        val accountManager: UstadAccountManager by instance()

        val profileImgView: ImageView = menuItem.actionView.findViewById(R.id.person_name_profilepic)
        userProfileDrawable?.also { profileImgView.imageForeignKeyPlaceholder(it) }
        profileImgView.setImageForeignKeyAdapter(PersonDetailFragment.FOREIGNKEYADAPTER_PERSON)
        profileImgView.setImageForeignKey(accountManager.activeAccount.personUid)
        profileImgView.setOnClickListener { impl.go(AccountListView.VIEW_NAME, mapOf(), this) }
    }

    companion object {
        val BOTTOM_NAV_DEST = listOf(R.id.content_entry_list_home_dest, R.id.home_clazzlist_dest,
                R.id.home_personlist_dest, R.id.report_list_dest,
                R.id.chat_list_home_dest)

    }
}
