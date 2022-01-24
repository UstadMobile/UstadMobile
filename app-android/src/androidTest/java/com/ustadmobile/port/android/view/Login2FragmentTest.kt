package com.ustadmobile.port.android.view

import android.app.Application
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.port.android.screen.LoginScreen
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.port.android.util.getApplicationDi
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import junit.framework.Assert.assertEquals
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


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

    private fun launchFragment(serverUrl: String = mockWebServer.url("/").toString(),
                       fillAllFields: Boolean = false,
                       userRegistrationAllowed: Boolean = false,
                       guestLoginAllowed: Boolean = false
    ) {
        val workspace = Site().apply {
            siteName = ""
            guestLogin = guestLoginAllowed
            registrationAllowed = userRegistrationAllowed
            authSalt = randomString(20)
        }

        val di = getApplicationDi()
        val repo : UmAppDatabase = di.on(Endpoint(serverUrl)).direct.instance(tag = DoorTag.TAG_REPO)
        repo.siteDao.insert(workspace)

        val args = mutableMapOf(
            UstadView.ARG_SITE to Json.encodeToString(Site.serializer(), workspace),
            UstadView.ARG_NEXT to ContentEntryList2View.VIEW_NAME)

        args[UstadView.ARG_SERVER_URL] = serverUrl

        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
            fragmentArgs = args.toBundle()) {
            Login2Fragment().also {
                it.installNavController(systemImplNavRule.navController,
                initialDestId = R.id.login_dest)
            }
        }

        if (fillAllFields) {
            LoginScreen{
                userNameTextInput {
                    edit {
                        typeText(this@LoginScreen.VALID_USER)
                    }
                }
                passwordTextInput {
                    edit {
                        typeText(this@LoginScreen.VALID_PASS)
                    }
                }

                closeSoftKeyboard()

                loginButton {
                    click()
                }
            }
        }

    }


    @AdbScreenRecord("given registration is allowed when logging in then should show create account button")
    @Test
    fun givenRegistrationIsAllowed_whenLogin_shouldShowRegisterButton() {

        init {

        }.run {
            LoginScreen {
                launchFragment(userRegistrationAllowed = true)
                createAccount {
                    isDisplayed()
                }
            }
        }


    }


    @AdbScreenRecord("given registration is not allowed when logging in then should hide create account button")
    @Test
    fun givenRegistrationIsNotAllowed_whenLogin_shouldNotShowRegisterButton() {


        init {

        }.run {
            LoginScreen {
                launchFragment(userRegistrationAllowed = false)
                createAccount {
                    isNotDisplayed()
                }
            }
        }
    }


    @AdbScreenRecord("given connect as guest is allowed when logging in then should show connect as guest button")
    @Test
    fun givenGuestConnectionIsAllowed_whenLogin_shouldShowConnectAsGuestButton() {



        init {

        }.run {
            LoginScreen {
                launchFragment(guestLoginAllowed = true)
                connectAsGuest {
                    isDisplayed()
                }
            }
        }

    }


    @AdbScreenRecord("given connect as guest is not allowed when logging in then should hide connect as guest button")
    @Test
    fun givenGuestConnectionIsNotAllowed__whenLogin_shouldNotShowConnectAsGuestButton() {


        init {

        }.run {
            LoginScreen {
                launchFragment(guestLoginAllowed = false)
                connectAsGuest {
                    isNotDisplayed()
                }
            }
        }

    }


    @AdbScreenRecord("given create account button is visible when clicked should go to account creation screen")
    @Test
    fun givenCreateAccountIsVisible_whenClicked_shouldOpenAccountCreationSection() {
        init {

        }.run {
            LoginScreen {
                launchFragment(userRegistrationAllowed = true)
                createAccount {
                    click()
                }
                assertEquals("It navigated to register age redirect",
                        R.id.register_age_redirect_dest, systemImplNavRule.navController.currentDestination?.id)
            }
        }

    }

    @AdbScreenRecord("given connect as guest button is visible when clicked should go to content screen")
    @Test
    fun givenConnectAsGuestIsVisible_whenClicked_shouldOpenContentSection() {

        init {

        }.run {
            LoginScreen {
                launchFragment(guestLoginAllowed = true)
                connectAsGuest {
                    isClickable()
                    click()
                }
                flakySafely {
                    assertEquals("It navigated to account creation screen",
                            R.id.content_entry_list_dest, systemImplNavRule.navController.currentDestination?.id)
                }
            }
        }


    }


    @AdbScreenRecord("given valid username and password when handle login clicked should go to the destination")
    @Test
    fun givenValidUsernameAndPassword_whenHandleLoginClicked_shouldCallSystemImplGo() {

        init{

        }.run {

            LoginScreen {
                mockWebServer.dispatcher = object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        return MockResponse()
                                .setBody(Gson().toJson(UmAccount(42, VALID_USER, "auth", "")))
                                .setHeader("Content-Type", "application/json")
                    }
                }

                val httpUrl = mockWebServer.url("/").toString()

                launchFragment(httpUrl, fillAllFields = true)

            }

            flakySafely {
                assertEquals("It navigated to the default screen",
                        R.id.content_entry_list_dest, systemImplNavRule.navController.currentDestination?.id)
            }

        }



    }

    @AdbScreenRecord("given invalid username and password when handle login clicked should show password and username errors")
    @Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {



        init{
            mockWebServer.dispatcher = object: Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    return MockResponse().setResponseCode(403)
                }
            }

        }.run {

            val httpUrl = mockWebServer.url("/").toString()

            LoginScreen {
                launchFragment(httpUrl, fillAllFields = true)
                loginErrorText {
                    isDisplayed()
                    hasText(context.getString(R.string.wrong_user_pass_combo))
                }
            }

        }


    }


    @AdbScreenRecord("given server is offline when handle login clicked should shoe network errors ")
    @Test
    fun givenServerOffline_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        init{
            mockWebServer.shutdown()
        }.run {
            val httpUrl = mockWebServer.url("/").toString()
            LoginScreen {
                launchFragment(httpUrl, fillAllFields = true)
                loginErrorText {
                    hasText(context.getString(R.string.login_network_error))
                    isDisplayed()
                }
            }

        }

    }

    @AdbScreenRecord("given login form without filling it when clicked should show required fields errors")
    @Test
    fun givenFieldsAreNotFilled_whenHandleLoginCalled_thenShouldShowErrors() {
        init{
            mockWebServer.shutdown()

        }.run {
            val httpUrl = mockWebServer.url("/").toString()

            LoginScreen{
                launchFragment(httpUrl, fillAllFields = true)
                userNameTextInput{
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
                passwordTextInput{
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
            }
        }

    }

}
