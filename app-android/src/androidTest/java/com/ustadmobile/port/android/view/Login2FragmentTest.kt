package com.ustadmobile.port.android.view

import android.app.Application
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.gson.Gson
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert.assertEquals
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
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

    private val context = ApplicationProvider.getApplicationContext<Application>()

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

    @AdbScreenRecord("given registration is allowed when logging in then should show create account button")
    @Test
    fun givenRegistrationIsAllowed_whenLogin_shouldShowRegisterButton(){
        launchFragment(registration = true)
        onView(withId(R.id.create_account)).check(matches(isDisplayed()))
    }


    @AdbScreenRecord("given registration is not allowed when logging in then should hide create account button")
    @Test
    fun givenRegistrationIsNotAllowed_whenLogin_shouldNotShowRegisterButton(){
        launchFragment(registration = false)
        onView(withId(R.id.create_account)).check(matches(not(isDisplayed())))
    }


    @AdbScreenRecord("given connect as guest is allowed when logging in then should show connect as guest button")
    @Test
    fun givenGuestConnectionIsAllowed_whenLogin_shouldShowConnectAsGuestButton(){
        launchFragment(guestConnection = true)
        onView(withId(R.id.connect_as_guest)).check(matches(isDisplayed()))
    }


    @AdbScreenRecord("given connect as guest is not allowed when logging in then should hide connect as guest button")
    @Test
    fun givenGuestConnectionIsNotAllowed__whenLogin_shouldNotShowConnectAsGuestButton(){
        launchFragment(guestConnection = false)
        onView(withId(R.id.connect_as_guest)).check(matches(not(isDisplayed())))
    }


    @AdbScreenRecord("given create account button is visible when clicked should go to account creation screen")
    @Test
    fun givenCreateAccountIsVisible_whenClicked_shouldOpenAccountCreationSection(){
        launchFragment(registration = true)
        onView(withId(R.id.create_account)).perform(click())
        assertEquals("It navigated to account creation screen",
                R.id.person_edit_register_dest, systemImplNavRule.navController.currentDestination?.id)
    }

    @AdbScreenRecord("given connect as guest button is visible when clicked should go to content screen")
    @Test
    fun givenConnectAsGuestIsVisible_whenClicked_shouldOpenContentSection(){
        launchFragment(guestConnection = true)
        onView(withId(R.id.connect_as_guest)).perform(click())
        assertEquals("It navigated to account creation screen",
                R.id.home_content_dest, systemImplNavRule.navController.currentDestination?.id)

    }


    @AdbScreenRecord("given valid username and password when handle login clicked should go to the destination")
    @Test
    fun givenValidUsernameAndPassword_whenHandleLoginClicked_shouldCallSystemImplGo() {
        mockWebServer.enqueue(MockResponse()
                .setBody(Gson().toJson(UmAccount(42, VALID_USER, "auth", "")))
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


    private fun launchFragment(serverUrl: String? = null, fillAllFields: Boolean = false,
                               registration:Boolean = false, guestConnection:Boolean = false){

        val workspace = WorkSpace().apply {
            name = ""
            guestLogin = guestConnection
            registrationAllowed = registration
        }
        val args = mapOf(UstadView.ARG_WORKSPACE to Json.stringify(WorkSpace.serializer(), workspace))
        val bundle = args.plus(mapOf(UstadView.ARG_SERVER_URL to serverUrl,
                ARG_NEXT to ContentEntryListTabsView.VIEW_NAME) as Map<String, String>).toBundle()

        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundle) {
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