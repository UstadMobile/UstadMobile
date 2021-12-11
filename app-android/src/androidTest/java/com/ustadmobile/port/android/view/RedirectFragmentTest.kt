/*
package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.view.UstadView.Companion.ARG_FROM
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("Redirect screen Test")
class RedirectFragmentTest {

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    @AdbScreenRecord("given app launched when navigating from on-boarding screen should navigate to get started")
    @Test
    fun givenAppLaunched_whenNavigateFromOnBoardingScreen_thenShouldNavigateToGetStarted() {
        launchFragment(true)

       assertEquals("It navigated to get started screen",
                R.id.account_get_started_dest, systemImplNavRule.navController.currentDestination?.id)
    }

    @AdbScreenRecord("given app launched when navigating from other screen should navigate to content list")
    @Test
    fun givenAppLaunched_whenNavigateFromOtherScreens_thenShouldNavigateContentList() {
        launchFragment()

        assertEquals("It navigated to content entry list screen",
                R.id.content_entry_list_dest, systemImplNavRule.navController.currentDestination?.id)
    }

    private fun launchFragment(isFromOnBoard: Boolean = false){
        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = if(isFromOnBoard) bundleOf(ARG_FROM
                        to OnBoardingView.VIEW_NAME) else bundleOf()) {
            RedirectFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)
    }

}*/
