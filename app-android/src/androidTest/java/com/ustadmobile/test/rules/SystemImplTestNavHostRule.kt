package com.ustadmobile.test.rules

import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.port.android.generated.MessageIDMap
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
        (navController as TestNavHostController).setViewModelStore(ViewModelStore())
        navController.setGraph(R.navigation.mobile_navigation)
        UstadMobileSystemImpl.instance.navController = navController
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
    }

    override fun finished(description: Description?) {
        UstadMobileSystemImpl.instance.navController = null
    }

}