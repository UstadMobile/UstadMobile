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
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.port.android.screen.GetStartedScreen
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
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("given get started option when use public library clicked should go to login screen")
    @Test
    fun givenGetStartedOptions_whenUsePublicLibraryClicked_thenShouldGoToLoginScreen() {
        launchFragment()

        GetStartedScreen{

            publicLibView{
                isDisplayed()
                click()
            }

        }

        assertEquals("It navigated to login screen",
                R.id.login_dest, systemImplNavRule.navController.currentDestination?.id)
    }

    @AdbScreenRecord("given get started option when use join existing workspace clicked should allow to enter workspace link")
    @Test
    fun givenGetStartedOptions_whenJoinExistingWorkSpaceIsClicked_thenShouldAllowToEnterWorkSpaceLink() {
        launchFragment()

        GetStartedScreen{

            workspaceView{
                isDisplayed()
                click()
            }

        }

        assertEquals("It navigated to workspace enter link screen",
                R.id.workspace_enterlink_dest, systemImplNavRule.navController.currentDestination?.id)
    }


    @AdbScreenRecord("given get started option when create workspace clicked should open a browser window")
    @Test
    fun givenGetStartedOptions_whenCreateNewWorkSpaceIsClicked_thenShouldOpenABrowserWindow() {
        launchFragment()

        GetStartedScreen{

            createWorkSpaceView.isDisplayed()
            Intents.init()
            createWorkSpaceView.click()
            intended(hasAction(Intent.ACTION_VIEW))
            Intents.release()

        }
    }


    private fun launchFragment(){
        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            GetStartedFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }
    }

}