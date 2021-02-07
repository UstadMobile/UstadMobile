package com.ustadmobile.test.rules

import android.content.Context
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.test.port.android.util.getApplicationDi
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * Sets the navController on UstadMobileSystemImpl for the test. This should be used on
 * FragmentScenarios.
 */
class SystemImplTestNavHostRule  : TestWatcher() {

    lateinit var navController: NavController
        private set

    lateinit var impl: UstadMobileSystemImpl
        private set

    override fun starting(description: Description?) {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        (navController as TestNavHostController).setViewModelStore(ViewModelStore())
        navController.setGraph(R.navigation.mobile_navigation)
        val di = getApplicationDi()
        impl = di.direct.instance()
        impl.navController = navController
        impl.messageIdMap = MessageIDMap.ID_MAP
    }

    override fun finished(description: Description?) {
        impl.navController = null
    }

}