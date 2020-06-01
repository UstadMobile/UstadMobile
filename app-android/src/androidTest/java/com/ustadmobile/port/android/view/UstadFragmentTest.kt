package com.ustadmobile.port.android.view

import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.UmAccount

abstract class UstadFragmentTest {

    protected lateinit var navController: NavController

    protected lateinit var db: UmAppDatabase

    fun setupNavController(){
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.setGraph(R.navigation.mobile_navigation)
        UstadMobileSystemImpl.instance.navController = navController
    }

    fun setupDbWithAccount(personUid: Long = 7L, username: String = "bond", auth: String = "", endpoint: String = "http://localhost/") {
        val activeAccount = UmAccount(personUid, "bond", "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())
        db = UmAccountManager.getActiveDatabase(ApplicationProvider.getApplicationContext())
        db.clearAllTables()
    }

}