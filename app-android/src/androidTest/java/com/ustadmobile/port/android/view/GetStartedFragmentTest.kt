package com.ustadmobile.port.android.view

import android.content.Intent
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("Get started screen Test")
@ExperimentalStdlibApi
class GetStartedFragmentTest {

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

    @AdbScreenRecord("given get started option when use public library clicked should go to login screen")
    @Test
    fun givenGetStartedOptions_whenUsePublicLibraryClicked_thenShouldGoToLoginScreen() {
        launchFragment()
        onView(withId(R.id.use_public_library_view)).check(matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.use_public_library_view)).perform(click())

        assertEquals("It navigated to login screen",
                R.id.login_dest, systemImplNavRule.navController.currentDestination?.id)
    }

    @AdbScreenRecord("given get started option when use join existing workspace clicked should allow to enter workspace link")
    @Test
    fun givenGetStartedOptions_whenJoinExistingWorkSpaceIsClicked_thenShouldAllowToEnterWorkSpaceLink() {
        launchFragment()

        onView(withId(R.id.join_workspace_view)).check(matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.join_workspace_view)).perform(click())

        assertEquals("It navigated to workspace enter link screen",
                R.id.workspace_enterlink_dest, systemImplNavRule.navController.currentDestination?.id)
    }


    @AdbScreenRecord("given get started option when create workspace clicked should open a browser window")
    @Test
    fun givenGetStartedOptions_whenCreateNewWorkSpaceIsClicked_thenShouldOpenABrowserWindow() {
        launchFragment()
        onView(withId(R.id.create_workspace_view)).check(matches(ViewMatchers.isDisplayed()))
        Intents.init()
        onView(withId(R.id.create_workspace_view)).perform(click())

        intended(hasAction(Intent.ACTION_VIEW))
    }


    private fun launchFragment(){
        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            GetStartedFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)
    }

}