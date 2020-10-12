package com.ustadmobile.port.android.view

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert.assertEquals
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@AdbScreenRecord("MainActivity test")
class MainActivityTest {
    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    @AdbScreenRecord("given app launched when user clicks profile icon should open account list")
    @Test
    fun givenAppLaunched_whenUserClicksOnProfileIcon_thenShouldOpenAccountListScreen() {

        launchActivity<MainActivity>()
                .withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)


        onView(withText("G")).check(matches(isDisplayed()))

        onView(withId(R.id.person_name_letter)).check(matches(isDisplayed())).perform(click())

        assertEquals("It navigated account list screen",
                R.id.account_list_dest, systemImplNavRule.navController.currentDestination?.id)
    }

    @AdbScreenRecord("given app launched when user navigates away from top screens then should hide bottom nav")
    @Test
    fun givenAppLaunched_whenUserNavigatesToScreenWithNoBottomNav_thenShouldHideBottomNav() {

        val scenario = launchActivity<MainActivity>()
                .withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        scenario.clickOptionMenu(R.id.settings_list_dest)

        onView(withId(R.id.bottom_nav_view)).check(matches(not(isDisplayed())))
    }


}
