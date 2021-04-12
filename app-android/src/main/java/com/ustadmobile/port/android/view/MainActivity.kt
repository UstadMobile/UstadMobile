package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
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
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.util.DeleteTempFilesNavigationListener
import com.ustadmobile.port.android.util.ext.getActivityContext
import com.ustadmobile.port.android.view.util.UstadActivityWithProgressBar
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar_material_collapsing.*
import kotlinx.android.synthetic.main.appbar_material_collapsing.view.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.util.*


class MainActivity : UstadBaseActivity(), UstadListViewActivityWithFab,
        UstadActivityWithProgressBar,
        NavController.OnDestinationChangedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override val activityFloatingActionButton: ExtendedFloatingActionButton?
        get() = activity_listfragmelayout_behaviornt_fab

    override val activityProgressBar: ProgressBar?
        get() = main_progress_bar

    private lateinit var mBinding: ActivityMainBinding

    private val impl : UstadMobileSystemImpl by instance()

    private val accountManager: UstadAccountManager by instance()

    private var mIsAdmin: Boolean? = null

    private var searchView: SearchView? = null

    private val destinationProvider: DestinationProvider by instance()

    //This is actually managed by the underlying fragments.
    override var loading: Boolean
        get() = false
        set(value) {}

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
        loadFromUriString(uri)

    }

    private fun loadFromUriString(uri: String?){

        impl.go(uri, getActivityContext())

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(mBinding.root.toolbar)

        val host: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.activity_main_navhost_fragment) as NavHostFragment? ?: return
        val navController = host.navController
        navController.addOnDestinationChangedListener(this)
        navController.addOnDestinationChangedListener(DeleteTempFilesNavigationListener(this))
        val navGraph = navController.graph
        navController.setGraph(navGraph, intent.extras)
        appBarConfiguration = AppBarConfiguration(navGraph)
        mBinding.bottomNavView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, AppBarConfiguration(mBinding.bottomNavView.menu))

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
        (mBinding.root.collapsing_toolbar.layoutParams as? AppBarLayout.LayoutParams)?.scrollFlags = scrollFlags

        mBinding.bottomNavView.visibility = if(ustadDestination?.hideBottomNavigation == true) {
            View.GONE
        } else {
            slideBottomNavigation(true)
            View.VISIBLE
        }
    }

    fun onAppBarExpand(expand: Boolean){
        mBinding.root.appbar.setExpanded(expand)
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
        menu.findItem(R.id.menu_main_profile).isVisible = mainScreenItemsVisible
        searchView = menu.findItem(R.id.menu_search).actionView as SearchView

        if (mainScreenItemsVisible) {
            setUserProfile(menu.findItem(R.id.menu_main_profile))
        }
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_main_settings -> handleClickSettings()
        }
        return item.onNavDestinationSelected(findNavController(R.id.activity_main_navhost_fragment))
                || super.onOptionsItemSelected(item)
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
        val profileIconLetter = accountManager.activeAccount.firstName?.first().toString()
        val profileLetterView: TextView = menuItem.actionView.findViewById(R.id.person_name_letter)
        profileLetterView.text = profileIconLetter.toUpperCase(Locale.ROOT)
        profileLetterView.setOnClickListener { impl.go(AccountListView.VIEW_NAME, mapOf(), this) }
    }

    companion object {
        val BOTTOM_NAV_DEST = listOf(R.id.home_content_dest, R.id.home_clazzlist_dest,
                R.id.home_personlist_dest, R.id.home_schoollist_dest, R.id.report_list_dest)

    }
}
