/*
package com.ustadmobile.port.android.view

import android.app.Application
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.init
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.gson.Gson
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.port.android.screen.LoginScreen
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
class Login2FragmentTest : TestCase(){

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @AdbScreenRecord("given registration is allowed when logging in then should show create account button")
    @Test
    fun givenRegistrationIsAllowed_whenLogin_shouldShowRegisterButton() {

        LoginScreen {
            launchFragment(registration = true, systemImplNavRule = systemImplNavRule)
            createAccount {
                isDisplayed()
            }
        }
    }


    @AdbScreenRecord("given registration is not allowed when logging in then should hide create account button")
    @Test
    fun givenRegistrationIsNotAllowed_whenLogin_shouldNotShowRegisterButton() {
        LoginScreen {
            launchFragment(registration = false, systemImplNavRule = systemImplNavRule)
            createAccount {
                isNotDisplayed()
            }
        }
    }


    @AdbScreenRecord("given connect as guest is allowed when logging in then should show connect as guest button")
    @Test
    fun givenGuestConnectionIsAllowed_whenLogin_shouldShowConnectAsGuestButton() {

        LoginScreen {
            launchFragment(guestConnection = true, systemImplNavRule = systemImplNavRule)
            connectAsGuest {
                isDisplayed()
            }
        }
    }


    @AdbScreenRecord("given connect as guest is not allowed when logging in then should hide connect as guest button")
    @Test
    fun givenGuestConnectionIsNotAllowed__whenLogin_shouldNotShowConnectAsGuestButton() {
        LoginScreen {
            launchFragment(guestConnection = false, systemImplNavRule = systemImplNavRule)
            connectAsGuest {
                isNotDisplayed()
            }
        }
    }


    @AdbScreenRecord("given create account button is visible when clicked should go to account creation screen")
    @Test
    fun givenCreateAccountIsVisible_whenClicked_shouldOpenAccountCreationSection() {
        LoginScreen {
            launchFragment(registration = true, systemImplNavRule = systemImplNavRule)
            createAccount {
                click()
            }
            assertEquals("It navigated to account creation screen",
                    R.id.person_edit_register_dest, systemImplNavRule.navController.currentDestination?.id)
        }
    }

    @AdbScreenRecord("given connect as guest button is visible when clicked should go to content screen")
    @Test
    fun givenConnectAsGuestIsVisible_whenClicked_shouldOpenContentSection() {
        LoginScreen {
            launchFragment(guestConnection = true, systemImplNavRule = systemImplNavRule)
            connectAsGuest {
                click()
            }
            assertEquals("It navigated to account creation screen",
                    R.id.home_content_dest, systemImplNavRule.navController.currentDestination?.id)
        }

    }


    @AdbScreenRecord("given valid username and password when handle login clicked should go to the destination")
    @Test
    fun givenValidUsernameAndPassword_whenHandleLoginClicked_shouldCallSystemImplGo() {

        init{

        }.run {

            LoginScreen {

                mockWebServer.enqueue(MockResponse()
                        .setBody(Gson().toJson(UmAccount(42, VALID_USER, "auth", "")))
                        .setHeader("Content-Type", "application/json"))

                val httpUrl = mockWebServer.url("/").toString()

                launchFragment(httpUrl, fillAllFields = true, systemImplNavRule = systemImplNavRule)

            }

            flakySafely {
                assertEquals("It navigated to the default screen",
                        R.id.home_content_dest, systemImplNavRule.navController.currentDestination?.id)
            }

        }



    }

    @AdbScreenRecord("given invalid username and password when handle login clicked should show password and username errors")
    @Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        mockWebServer.enqueue(MockResponse().setResponseCode(403))
        val httpUrl = mockWebServer.url("/").toString()

        LoginScreen {
            launchFragment(httpUrl, fillAllFields = true, systemImplNavRule = systemImplNavRule)
            loginErrorText {
                isDisplayed()
                hasText(context.getString(R.string.wrong_user_pass_combo))
            }
        }


    }


    @AdbScreenRecord("given server is offline when handle login clicked should shoe network errors ")
    @Test
    fun givenServerOffline_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        mockWebServer.shutdown()
        val httpUrl = mockWebServer.url("/").toString()

        LoginScreen {
            launchFragment(httpUrl, fillAllFields = true, systemImplNavRule = systemImplNavRule)
            loginErrorText {
                hasText(context.getString(R.string.login_network_error))
                isDisplayed()
            }
        }
    }

    @AdbScreenRecord("given login form without filling it when clicked should show required fields errors")
    @Test
    fun givenFieldsAreNotFilled_whenHandleLoginCalled_thenShouldShowErrors() {
        mockWebServer.shutdown()
        val httpUrl = mockWebServer.url("/").toString()

        LoginScreen{
            launchFragment(httpUrl, fillAllFields = true, systemImplNavRule = systemImplNavRule)
            userNameTextInput{
                hasInputLayoutError(context.getString(R.string.field_required_prompt))
            }
            passwordTextInput{
                hasInputLayoutError(context.getString(R.string.field_required_prompt))
            }
        }

    }

}*/
