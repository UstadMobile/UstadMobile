package com.ustadmobile.port.android.view

import android.app.Application
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("Get started screen Test")
@ExperimentalStdlibApi
class WorkspaceEnterLinkFragmentTest {

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

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @AdbScreenRecord("given valid workspace link when checked should show enable button")
    @Test
    fun givenValidWorkSpaceLink_whenCheckedAndIsValid_shouldAllowToGoToNextScreen() {
        launchFragment("ustadmobile.com/lms/hosting/")

        onView(withId(R.id.next_button)).check(matches(isDisplayed()))

        onView(withId(R.id.workspace_link_view)).check(matches(
                not(hasInputLayoutError(context.getString(R.string.invalid_url)))))
    }

    @AdbScreenRecord("given invalid workspace link when checked should not show next button")
    @Test
    fun givenInValidWorkSpaceLink_whenCheckedAndIsValid_shouldNotAllowToGoToNextScreen() {
        launchFragment("https://dummy.com")

        onView(withId(R.id.next_button)).check(matches(not(isDisplayed())))

        onView(withId(R.id.workspace_link_view)).check(matches(
                hasInputLayoutError(context.getString(R.string.invalid_url))))
    }


    private fun launchFragment(workSpaceLink: String){
        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            WorkspaceEnterLinkFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        onView(withId(R.id.organisation_link)).perform(typeText(workSpaceLink))
    }

}