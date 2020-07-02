package com.ustadmobile.port.android.view

import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert.assertEquals
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("Login screen Test")
class Login2FragmentTest {

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

    private val context = getInstrumentation().targetContext

    private lateinit var mockWebServer: MockWebServer


    @Before
    fun setUp(){
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @AdbScreenRecord("given create account button is visible when clicked should go to account creation screen")
    @Test
    fun givenCreateAccountIsVisible_whenClicked_shouldOpenAccountCreationSection(){
        launchFragment()
        onView(withId(R.id.create_account)).perform(click())
        assertEquals("It navigated to account creation screen",
                R.id.person_edit_dest, systemImplNavRule.navController.currentDestination?.id)
    }

    @AdbScreenRecord("given connect as guest button is visible when clicked should go to content screen")
    @Test
    fun givenConnectAsGuestIsVisible_whenClicked_shouldOpenContentSection(){
        launchFragment()
        onView(withId(R.id.connect_as_guest)).perform(click())
        assertEquals("It navigated to account creation screen",
                R.id.home_content_dest, systemImplNavRule.navController.currentDestination?.id)

    }


    @AdbScreenRecord("given valid username and password when handle login clicked should go to the destination")
    @Test
    fun givenValidUsernameAndPassword_whenHandleLoginClicked_shouldCallSystemImplGo() {
        mockWebServer.enqueue(MockResponse()
                .setBody(Gson().toJson(UmAccount(42, VALID_USER, "auth", null)))
                .setHeader("Content-Type", "application/json"))

        val httpUrl = mockWebServer.url("/").toString()

        launchFragment(httpUrl, fillAllFields = true)

        assertEquals("It navigated to the default screen",
                R.id.home_content_dest, systemImplNavRule.navController.currentDestination?.id)
    }

    @AdbScreenRecord("given invalid username and password when handle login clicked should show password and username errors")
    @Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        mockWebServer.enqueue(MockResponse().setResponseCode(403))
        val httpUrl = mockWebServer.url("/").toString()
        launchFragment(httpUrl, fillAllFields = true)

        onView(allOf(withId(R.id.login_error_text), withText(
                context.getString(R.string.wrong_user_pass_combo))))
                .check(matches(isDisplayed()))
    }


    @AdbScreenRecord("given server is offline when handle login clicked should shoe network errors ")
    @Test
    fun givenServerOffline_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        mockWebServer.shutdown()
        val httpUrl = mockWebServer.url("/").toString()
        launchFragment(httpUrl, fillAllFields = true)

        onView(allOf(withId(R.id.login_error_text),
                withText(context.getString(R.string.login_network_error))))
                .check(matches(isDisplayed()))
    }

    @AdbScreenRecord("given login form without filling it when clicked should show required fields errors")
    @Test
    fun givenFieldsAreNotFilled_whenHandleLoginCalled_thenShouldShowErrors() {
        mockWebServer.shutdown()
        val httpUrl = mockWebServer.url("/").toString()
        launchFragment(httpUrl, fillAllFields = false)

        onView(withId(R.id.username_view)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))

        onView(withId(R.id.password_view)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))
    }


    private fun launchFragment(serverUrl: String? = null, fillAllFields: Boolean = false){
        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_SERVER_URL to serverUrl,
                        ARG_NEXT to ContentEntryListTabsView.VIEW_NAME)) {
            Login2Fragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        if(fillAllFields){
            onView(withId(R.id.person_username)).perform(typeText(VALID_USER))
            onView(withId(R.id.person_password)).perform(typeText(VALID_PASS))
        }

        onView(withId(R.id.login_button)).perform(click())
    }

    companion object {

        private const val VALID_USER = "JohnDoe"

        private const val VALID_PASS = "password"
    }

}