package com.ustadmobile.core.controller

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.core.view.Register2View
import com.ustadmobile.lib.db.entities.UmAccount
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class LoginPresenterTest {

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var view: LoginView

    private lateinit var presenter:LoginPresenter

    private val context = Any()

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp(){
        view = mock {
            on { runOnUiThread(any()) }.doAnswer {
                Thread(it.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        impl = mock ()
        presenter = LoginPresenter(context, mapOf(), view, impl)
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }


    @Test
    fun givenAppPrefRegistrationVisibilityIsSetToTrue_whenLogin_shouldShowRegisterLabel(){
        doAnswer {
            "true"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        presenter.onCreate(mapOf())

        verify(view).setRegistrationLinkVisible(eq(true))
    }


    @Test
    fun givenAppPrefRegistrationVisibilityIsSetToFalse_whenLogin_shouldNotShowRegisterLabel(){
        doAnswer {
            "false"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        presenter.onCreate(mapOf())

        verify(view).setRegistrationLinkVisible(eq(false))
    }


    @Test
    fun givenRegisterLinkIsVisible_whenClicked_shouldOpenRegistrationSection(){
        doAnswer {
            "true"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        presenter.onCreate(mapOf())

        presenter.handleClickCreateAccount()

        verify(impl, times(1)).go(any(), any(), any())

    }

    @Test
    fun givenValidUsernameAndPassword_whenClicked_shouldCallSystemImplGo() {
        mockWebServer.enqueue(MockResponse()
                .setBody(Gson().toJson(UmAccount(42, VALID_USER, "auth", null)))
                .setHeader("Content-Type", "application/json"))

        val httpUrl = mockWebServer.url("/").toString()

        val presenter = LoginPresenter(context,
                mapOf(LoginPresenter.ARG_SERVER_URL to httpUrl,
                        LoginPresenter.ARG_NEXT to "somewhere"), view, impl)

        presenter.handleClickLogin(VALID_USER, VALID_PASS, httpUrl)

        verify(impl, timeout(5000 )).go("somewhere",
                context)

        val activeAccount = UmAccountManager.getActiveAccount(context)
        Assert.assertNotNull(activeAccount)

        val requestMade = mockWebServer.takeRequest()
        Assert.assertEquals("/Login/login?username=$VALID_USER&password=$VALID_PASS",
                requestMade.path)
    }

    @Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        mockWebServer.enqueue(MockResponse().setResponseCode(403))
        val httpUrl = mockWebServer.url("/").toString()
        val presenter = LoginPresenter(context,
                mapOf(LoginPresenter.ARG_SERVER_URL to httpUrl), view, impl)
        presenter.handleClickLogin(VALID_USER, "wrongpassword", httpUrl)

        val expectedErrorMsg = UstadMobileSystemImpl.instance.getString(
                MessageID.wrong_user_pass_combo, context)

        verify<LoginView>(view, timeout(5000)).setErrorMessage(expectedErrorMsg)
        verify(impl, timeout(5000)).getString(MessageID.wrong_user_pass_combo, context)
        verify<LoginView>(view, timeout(5000)).setPassword("")

        val requestMade = mockWebServer.takeRequest()
        Assert.assertEquals("/Login/login?username=$VALID_USER&password=wrongpassword",
                requestMade.path)
    }


    @Test
    fun givenServerOffline_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        mockWebServer.shutdown()
        val httpUrl = mockWebServer.url("/").toString()
        val presenter = LoginPresenter(context,
                mapOf(LoginPresenter.ARG_SERVER_URL to httpUrl), view, impl)
        presenter.handleClickLogin(VALID_USER, VALID_PASS, httpUrl)
        val expectedErrorMsg = UstadMobileSystemImpl.instance.getString(
                MessageID.login_network_error, Any())
        verify<LoginView>(view, timeout(5000)).setErrorMessage(expectedErrorMsg)
        verify(impl, timeout(5000)).getString(MessageID.login_network_error, context)
    }

    @Test
    fun givenNoRegistrationCode_whenUserClicksRegister_thenGotToRegisterView(){

        presenter.handleClickCreateAccount()

        verify(impl, timeout(5000)).go(eq(Register2View.VIEW_NAME), any(), any())

    }

    private fun createPresenterClickRegisterAndEnterRegistrationCode(registrationCodeEntered: String) {
        whenever(impl.getAppConfigString(eq(AppConfig.KEY_SHOW_REGISTER_CODE), any(), any())).doReturn("123")
        whenever(impl.getString(eq(MessageID.enter_register_code), any())).doReturn("Enter Registration Code")
        whenever(impl.getString(eq(MessageID.ok), any())).doReturn("Ok")
        whenever(impl.getString(eq(MessageID.cancel), any())).doReturn("Cancel")
        whenever(impl.getString(eq(MessageID.invalid_register_code), any())).doReturn("Invalid Registration Code")

        mockWebServer.shutdown()
        val httpUrl = mockWebServer.url("/").toString()
        val presenter = LoginPresenter(context,
                mapOf(LoginPresenter.ARG_SERVER_URL to httpUrl), view, impl)

        presenter.handleClickCreateAccount()

        verify(view, timeout(1000)).showRegisterCodeDialog(eq("Enter Registration Code"), eq("Ok"), eq("Cancel"))

        presenter.handleRegisterCodeDialogEntered(registrationCodeEntered)


    }

    @Test
    fun givenRegistrationCode_whenUserClicksRegister_thenShowDialogAndCheckCodeAndGoToRegisterViewifCorrect(){

        createPresenterClickRegisterAndEnterRegistrationCode("123")

        verify(impl, timeout(5000)).go(eq(Register2View.VIEW_NAME), any(), any())

    }

    @Test
    fun givenRegistrationCode_whenUserClicksRegister_thenShowDialogAndCheckCodeAndDisplaySnackBarForIncorrectCode(){

        createPresenterClickRegisterAndEnterRegistrationCode("111")

        verify(view, timeout(5000)).showSnackBarNotification(eq("Invalid Registration Code"), any(), any())

    }



    companion object {

        private val VALID_USER = "bobjones"

        private val VALID_PASS = "secret"
    }


}
