package com.ustadmobile.test.rules

import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Sets the navController on UstadMobileSystemImpl for the test. This should be used on
 * FragmentScenarios.
 */
class SystemImplTestNavHostRule  : TestWatcher() {

    lateinit var navController: NavController
        private set

    override fun starting(description: Description?) {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.setGraph(R.navigation.mobile_navigation)
        UstadMobileSystemImpl.instance.navController = navController
    }

    override fun finished(description: Description?) {
        UstadMobileSystemImpl.instance.navController = null
    }

}