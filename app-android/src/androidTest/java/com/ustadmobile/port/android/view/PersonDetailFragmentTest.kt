package com.ustadmobile.port.android.view

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert.assertEquals
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("PersonDetail screen Test")
class PersonDetailFragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

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

    @AdbScreenRecord("given person detail when username is null and account management allowed then should show create account option")
    @Test
    fun givenPersonDetail_whenUsernameIsNullAndAccountManagementAllowed_shouldShowCreateAccountOption(){
        launchFragment()
        onView(withId(R.id.create_account_view)).check(matches(isDisplayed()))
        onView(withId(R.id.change_account_password_view)).check(matches(not(isDisplayed())))
    }

    @AdbScreenRecord("given person detail when username is not null and account management allowed then should show change password option")
    @Test
    fun givenPersonDetail_whenUsernameIsNotNullAndAccountManagementAllowed_shouldShowChangePasswordOption(){
        launchFragment(true)
        onView(withId(R.id.change_account_password_view)).check(matches(isDisplayed()))
        onView(withId(R.id.create_account_view)).check(matches(not(isDisplayed())))
    }

    @AdbScreenRecord("given change password visible when clicked should open person account screen")
    @Test
    fun givenChangePasswordVisible_whenClicked_shouldOpenPersonAccountSection(){
        launchFragment(true)
        onView(withId(R.id.change_account_password_view)).check(matches(isDisplayed()))
        onView(withId(R.id.change_account_password_view)).perform(click())
        assertEquals("It navigated to person account screen",
                R.id.person_account_edit_dest, systemImplNavRule.navController.currentDestination?.id)
    }

    @AdbScreenRecord("given create account visible when clicked should open person edit screen")
    @Test
    fun givenCreateAccountVisible_whenClicked_shouldOpenPersonEditScreen(){
        launchFragment()
        onView(withId(R.id.create_account_view)).check(matches(isDisplayed()))
        onView(withId(R.id.create_account_view)).perform(click())
        assertEquals("It navigated to account creation screen",
                R.id.person_edit_dest, systemImplNavRule.navController.currentDestination?.id)
    }

    private fun launchFragment(includeUserName: Boolean = false){

        val person = PersonWithDisplayDetails().apply {
            firstNames = "Jones"
            lastName = "Doe"
            if(includeUserName){
                username = "jones.doe"
            }
            personUid = dbRule.db.personDao.insert(this)
        }

        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = args.toBundle()) {
            PersonDetailFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)
    }

}