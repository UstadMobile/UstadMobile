package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityMainBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.DbPreloadWorker
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.port.android.util.DeleteTempFilesNavigationListener
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar_material_collapsing.view.*
import kotlinx.coroutines.CompletableDeferred
import org.kodein.di.instance
import java.util.*


class MainActivity : UstadBaseActivity(), UstadListViewActivityWithFab,
        NavController.OnDestinationChangedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override val activityFloatingActionButton: ExtendedFloatingActionButton?
        get() = activity_listfragmelayout_behaviornt_fab

    private lateinit var mBinding: ActivityMainBinding

    private val impl = UstadMobileSystemImpl.instance

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
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination,
                                      arguments: Bundle?) {
        invalidateOptionsMenu()
        mBinding.root.appbar.setExpanded(true)

        val layoutParams = (mBinding.bottomNavView.layoutParams as? CoordinatorLayout.LayoutParams)
        val bottomNavBehavior = layoutParams?.behavior as? HideBottomViewOnScrollBehavior
        bottomNavBehavior?.slideUp(mBinding.bottomNavView)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.activity_main_navhost_fragment).navigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val currentFrag =
                findNavController(R.id.activity_main_navhost_fragment).currentDestination?.id ?: 0
        val mainScreenItemsVisible = BOTTOM_NAV_DEST.contains(currentFrag)
        menu.findItem(R.id.menu_main_settings).isVisible = mainScreenItemsVisible
        menu.findItem(R.id.menu_main_profile).isVisible = mainScreenItemsVisible
        mBinding.bottomNavView.visibility = if(DEST_TO_HIDE_BOTTOM_NAV.contains(currentFrag))
            View.GONE else View.VISIBLE
        if(mainScreenItemsVisible){
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
        val fragment = supportFragmentManager.primaryNavigationFragment?.
        childFragmentManager?.fragments?.get(0)
        if((fragment as? FragmentBackHandler)?.onHostBackPressed() != true){
            super.onBackPressed()
        }
    }

    override val viewContext: Any
        get() = this

    /**
     * When settings gear clicked in the menu options - Goes to the settings activity.
     */
    private fun handleClickSettings() {
        impl.go(SettingsView.VIEW_NAME, mapOf(), this)
    }

    private fun setUserProfile(menuItem: MenuItem){
        val accountManager: UstadAccountManager by instance()
        val profileIconLetter = accountManager.activeAccount.firstName?.first().toString()
        val profileLetterView:TextView = menuItem.actionView.findViewById(R.id.person_name_letter)
        profileLetterView.text = profileIconLetter.toUpperCase(Locale.ROOT)
        profileLetterView.setOnClickListener { impl.go(AccountListView.VIEW_NAME, mapOf(), this) }
    }

    companion object {
        val BOTTOM_NAV_DEST = listOf(R.id.home_content_dest, R.id.home_clazzlist_dest,
                R.id.home_personlist_dest, R.id.home_schoollist_dest, R.id.report_list_dest)

        val DEST_TO_HIDE_BOTTOM_NAV = listOf(R.id.login_dest, R.id.account_get_started_dest,
                R.id.workspace_enterlink_dest, R.id.settings_list_dest)
    }
}
