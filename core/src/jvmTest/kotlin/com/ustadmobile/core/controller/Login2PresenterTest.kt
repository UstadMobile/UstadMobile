package com.ustadmobile.core.controller

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_WORKSPACE
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.util.test.ext.bindJndiForActiveEndpoint
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import org.mockito.ArgumentMatchers
import javax.naming.InitialContext


class Login2PresenterTest {

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var view: Login2View


    private val context = Any()

    private lateinit var mockWebServer: MockWebServer

    private lateinit var mockPersonDao: PersonDao

    private lateinit var accountManager: UstadAccountManager

    private val defaultTimeout: Long = 5000

    private lateinit var di : DI

    @Before
    fun setUp(){
        view = mock {
            on { runOnUiThread(ArgumentMatchers.any()) }.doAnswer { invocation ->
                Thread(invocation.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        impl = mock ()
        accountManager = mock{}
        mockPersonDao = mock {}
        mockWebServer = MockWebServer()
        mockWebServer.start()

        di = DI {
            bind<UstadAccountManager>() with singleton { accountManager }
            bind<UstadMobileSystemImpl>() with singleton { impl }
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun createParams(registration:Boolean = false, guestConnection:Boolean = false,
                             extraParam: Map<String, String> = mapOf()): Map<String,String>{
        val workspace = WorkSpace().apply {
            name = ""
            guestLogin = guestConnection
            registrationAllowed = registration
        }
        var args = mapOf(ARG_WORKSPACE to Json.stringify(WorkSpace.serializer(), workspace))
        args = args.plus(extraParam)
        return args
    }


    @Test
    fun givenRegistrationIsAllowed_whenLogin_shouldShowRegisterButton(){
        val presenter = Login2Presenter(context, createParams(registration = true), view, di)
        presenter.onCreate(mapOf())
        verify(view).createAccountVisible = eq(true)
    }


    @Test
    fun givenRegistrationIsNotAllowed_whenLogin_shouldNotShowRegisterButton(){
        val presenter = Login2Presenter(context,createParams(registration = false), view, di)
        presenter.onCreate(mapOf())
        verify(view).createAccountVisible = eq(false)
    }


    @Test
    fun givenGuestConnectionIsAllowed_whenLogin_shouldShowConnectAsGuestButton(){
        val presenter = Login2Presenter(context,createParams(guestConnection = true), view, di)
        presenter.onCreate(mapOf())
        verify(view).connectAsGuestVisible = eq(true)
    }


    @Test
    fun givenGuestConnectionIsNotAllowed__whenLogin_shouldNotShowConnectAsGuestButton(){
        val presenter = Login2Presenter(context,createParams(guestConnection = false), view, di)
        presenter.onCreate(mapOf())
        verify(view).connectAsGuestVisible = eq(false)
    }


    @Test
    fun givenCreateAccountIsVisible_whenClicked_shouldOpenAccountCreationSection(){
        whenever(impl.getAppConfigString(any(), any(), any())).thenReturn  ("true")
        val presenter = Login2Presenter(context, createParams(registration = true), view, di)
        presenter.onCreate(mapOf())
        presenter.handleCreateAccount()
        argumentCaptor<String>{
            verify(impl).go(capture(), any(), any())
            Assert.assertEquals("Account creation screen was opened",
                    PersonEditView.VIEW_NAME, firstValue)
        }

    }

    @Test
    fun givenConnectAsGuestIsVisible_whenClicked_shouldOpenContentSection(){
        whenever(impl.getAppConfigString(any(), any(), any())).thenReturn  ("true")
        val presenter = Login2Presenter(context, createParams(guestConnection = true), view, di)
        presenter.onCreate(mapOf())
        presenter.handleConnectAsGuest()
        argumentCaptor<String>{
            verify(impl).go(capture(), any(), any())
            Assert.assertEquals("Content screen was opened",
                    ContentEntryListTabsView.VIEW_NAME, firstValue)
        }

    }

    @Test
    fun givenValidUsernameAndPassword_whenHandleLoginClicked_shouldCallSystemImplGo() {
        val destination = "dummyDestination"
        mockWebServer.enqueue(MockResponse()
                .setBody(Gson().toJson(UmAccount(42, VALID_USER, "auth", "")))
                .setHeader("Content-Type", "application/json"))

        val httpUrl = mockWebServer.url("/").toString()

        InitialContext().bindJndiForActiveEndpoint(httpUrl)

        val presenter = Login2Presenter(context,
                createParams(extraParam = mapOf(ARG_SERVER_URL to httpUrl,
                        ARG_NEXT to destination)), view, di)
        presenter.onCreate(null)

        presenter.handleLogin(VALID_USER, VALID_PASS)

        argumentCaptor<String>{
            verify(impl, timeout(defaultTimeout)).go(capture(), any())
            Assert.assertEquals("Next destination was opened",
                    destination, firstValue)
        }

        verifyBlocking(accountManager) { login(VALID_USER, VALID_PASS, httpUrl) }
    }

    @Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        accountManager = mock{
            onBlocking{login(any(), any(), any(), any())}.then{
                throw UnauthorizedException("Access denied")
            }
        }
        mockWebServer.enqueue(MockResponse().setResponseCode(403))
        val httpUrl = mockWebServer.url("/").toString()
        val presenter = Login2Presenter(context, createParams(extraParam =
        mapOf(ARG_SERVER_URL to httpUrl)), view, di)
        presenter.onCreate(null)

        presenter.handleLogin(VALID_USER, "wrongpassword")

        val expectedErrorMsg = impl.getString(
                MessageID.wrong_user_pass_combo, context)

        verify(view, timeout(defaultTimeout)).errorMessage = expectedErrorMsg
        verify(impl, timeout(defaultTimeout).atLeastOnce()).getString(MessageID.wrong_user_pass_combo, context)
        verify(view, timeout(defaultTimeout)).clearFields()
    }


    @Test
    fun givenServerOffline_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        accountManager = mock{
            onBlocking{login(any(), any(), any(), any())}.then{
                throw throw IllegalStateException("Server error")
            }
        }
        mockWebServer.shutdown()
        val httpUrl = mockWebServer.url("/").toString()
        val presenter = Login2Presenter(context,
                createParams(extraParam = mapOf(ARG_SERVER_URL to httpUrl)), view, di)
        presenter.onCreate(null)

        presenter.handleLogin(VALID_USER, VALID_PASS)

        val expectedErrorMsg = impl.getString(
                MessageID.login_network_error, Any())
        verify(view, timeout(defaultTimeout)).errorMessage = expectedErrorMsg
        verify(impl, timeout(defaultTimeout)).getString(MessageID.login_network_error, context)
    }



    companion object {

        private const val VALID_USER = "JohnDoe"

        private const val VALID_PASS = "password"
    }


}
