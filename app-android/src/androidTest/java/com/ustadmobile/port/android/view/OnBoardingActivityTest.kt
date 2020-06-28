package com.ustadmobile.port.android.view

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@AdbScreenRecord("OnBoarding test")
class OnBoardingActivityTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

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

    @AdbScreenRecord("given onboarding when user clicks arabic, activity recreated in arabic")
    @Test
    fun givenOnBoardingDisplays_whenUserClicksOnArabic_thenActivityRecreatedInArabic() {

        val activityScenario = launchActivity<OnBoardingActivity>()
                .withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        onView(withId(R.id.language_options_autocomplete_textview)).check(matches(isDisplayed())).perform(click())

        onView(withText("العربية"))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(click())

        onView(withText("العربية")).check(matches(isDisplayed()))

    }

    @AdbScreenRecord("given on boarding when user clicks on getstarted then goes to main activity")
    @Test
    fun givenOnBoardingDisplays_whenUserClicksGetStarted_thenGoesToMainActivity() {

        Intents.init()

        val activityScenario = launchActivity<OnBoardingActivity>()

        onView(withId(R.id.get_started_btn)).perform(click())

        intended(hasComponent(hasClassName(MainActivity::class.java.name)))

        Intents.release()
    }



}