package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
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
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert.assertEquals
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule


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


    @AdbScreenRecord("given valid username and password when handle login clicked should go to the destination")
    //@Test
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
    //@Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        mockWebServer.enqueue(MockResponse().setResponseCode(403))
        val httpUrl = mockWebServer.url("/").toString()
        launchFragment(httpUrl, fillAllFields = true)

        onView(allOf(withId(R.id.snackbar_text), withText(
                context.getString(R.string.wrong_user_pass_combo))))
                .check(matches(isDisplayed()))
    }


    @AdbScreenRecord("given server is offline when handle login clicked should shoe network errors ")
    //@Test
    fun givenServerOffline_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        mockWebServer.shutdown()
        val httpUrl = mockWebServer.url("/").toString()
        launchFragment(httpUrl, fillAllFields = true)
        onView(allOf(withId(R.id.snackbar_text), withText(
                context.getString(R.string.login_network_error))))
                .check(matches(isDisplayed()))
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