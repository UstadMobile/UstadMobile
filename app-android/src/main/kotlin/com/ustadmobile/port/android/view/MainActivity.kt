package com.ustadmobile.port.android.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.DbPreloadWorker
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AboutView
import com.ustadmobile.core.view.SettingsView


class MainActivity : AppCompatActivity(), UstadListViewActivityWithFab, NavController.OnDestinationChangedListener {

    private lateinit var appBarConfiguration : AppBarConfiguration

    override val activityFloatingActionButton: ExtendedFloatingActionButton?
        get() = findViewById(R.id.activity_listfragmelayout_behaviornt_fab)

    private lateinit var mAppBar: AppBarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAppBar = findViewById(R.id.appBar)
        val toolbar = findViewById<Toolbar>(R.id.activity_main_toolbar)
        setSupportActionBar(toolbar)

        val host: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.activity_main_navhost_fragment) as NavHostFragment? ?: return
        val navController = host.navController
        navController.addOnDestinationChangedListener(this)

        appBarConfiguration = AppBarConfiguration(navController.graph)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav?.setupWithNavController(navController)
        setupActionBarWithNavController(navController)

        DbPreloadWorker.queuePreloadWorker(applicationContext)
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        val ustadDestination = UstadMobileSystemImpl.instance.destinationProvider.lookupDestinationById(destination.id)

        if(ustadDestination?.hasFab != true) {
            activityFloatingActionButton?.visibility = View.GONE
            activityFloatingActionButton?.setOnClickListener(null)
        }

        mAppBar.setExpanded(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.activity_main_navhost_fragment).navigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_main_settings -> handleClickSettings()
        }
        return item.onNavDestinationSelected(findNavController(R.id.activity_main_navhost_fragment))
                || super.onOptionsItemSelected(item)
    }

    /**
     * When settings gear clicked in the menu options - Goes to the settings activity.
     */
    fun handleClickSettings() {
        UstadMobileSystemImpl.instance.go(SettingsView.VIEW_NAME, mapOf(), this)

    }
}
