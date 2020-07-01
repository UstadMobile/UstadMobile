package com.ustadmobile.core.controller

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.util.test.ext.bindJndiForActiveEndpoint
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import javax.naming.InitialContext


class Login2PresenterTest {

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var view: Login2View

    private lateinit var presenter:Login2Presenter

    private val context = Any()

    private lateinit var mockWebServer: MockWebServer

    private lateinit var mockPersonDao: PersonDao

    private val defaultTimeout: Long = 5000

    @Before
    fun setUp(){
        view = mock {
            on { runOnUiThread(ArgumentMatchers.any()) }.doAnswer { invocation ->
                Thread(invocation.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        impl = mock ()
        mockPersonDao = mock {}
        presenter = Login2Presenter(context, mapOf(), view, impl, mockPersonDao)
        mockWebServer = MockWebServer()
        mockWebServer.start()



    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }


    @Test
    fun givenAppPrefCreateAccountVisibilityIsSetToTrue_whenLogin_shouldShowRegisterLabel(){
        whenever(impl.getAppConfigString(any(), any(), any())).thenReturn  ("true")
        presenter.onCreate(mapOf())
        verify(view).createAccountVisible = eq(true)
    }


    @Test
    fun givenAppPrefCreateAccountVisibilityIsSetToFalse_whenLogin_shouldNotShowRegisterLabel(){
        whenever(impl.getAppConfigString(any(), any(), any())).thenReturn  ("false")
        presenter.onCreate(mapOf())
        verify(view).createAccountVisible = eq(false)
    }


    @Test
    fun givenAppPrefConnectAsGuestVisibilityIsSetToTrue_whenLogin_shouldShowRegisterLabel(){
        whenever(impl.getAppConfigString(any(), any(), any())).thenReturn  ("true")
        presenter.onCreate(mapOf())
        verify(view).connectAsGuestVisible = eq(true)
    }


    @Test
    fun givenAppPrefConnectAsGuestVisibilityIsSetToFalse_whenLogin_shouldNotShowRegisterLabel(){
        whenever(impl.getAppConfigString(any(), any(), any())).thenReturn  ("false")
        presenter.onCreate(mapOf())
        verify(view).connectAsGuestVisible = eq(false)
    }


    @Test
    fun givenCreateAccountIsVisible_whenClicked_shouldOpenAccountCreationSection(){
        whenever(impl.getAppConfigString(any(), any(), any())).thenReturn  ("true")
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
                .setBody(Gson().toJson(UmAccount(42, VALID_USER, "auth", null)))
                .setHeader("Content-Type", "application/json"))

        val httpUrl = mockWebServer.url("/").toString()

        InitialContext().bindJndiForActiveEndpoint(httpUrl)

        val presenter = Login2Presenter(context,
                mapOf(ARG_SERVER_URL to httpUrl,
                        ARG_NEXT to destination), view, impl, mockPersonDao)
        presenter.onCreate(null)

        presenter.handleLogin(VALID_USER, VALID_PASS)

        argumentCaptor<String>{
            verify(impl, timeout(defaultTimeout)).go(capture(), any())
            Assert.assertEquals("Next destination was opened",
                    destination, firstValue)
        }

        val activeAccount = UmAccountManager.getActiveAccount(context)
        Assert.assertNotNull(activeAccount)

        val requestMade = mockWebServer.takeRequest()
        Assert.assertEquals("/Login/login?username=$VALID_USER&password=$VALID_PASS",
                requestMade.path)

        verifyBlocking(mockPersonDao) { findByUid(activeAccount!!.personUid) }
    }

    @Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        mockWebServer.enqueue(MockResponse().setResponseCode(403))
        val httpUrl = mockWebServer.url("/").toString()
        val presenter = Login2Presenter(context,
                mapOf(ARG_SERVER_URL to httpUrl), view, impl, mockPersonDao)
        presenter.onCreate(null)

        presenter.handleLogin(VALID_USER, "wrongpassword")

        val expectedErrorMsg = impl.getString(
                MessageID.wrong_user_pass_combo, context)

        verify(view, timeout(defaultTimeout)).showSnackBar(expectedErrorMsg)
        verify(impl, timeout(defaultTimeout).atLeastOnce()).getString(MessageID.wrong_user_pass_combo, context)
        verify(view, timeout(defaultTimeout)).clearFields()

        val requestMade = mockWebServer.takeRequest()
        Assert.assertEquals("/Login/login?username=$VALID_USER&password=wrongpassword",
                requestMade.path)
    }


    @Test
    fun givenServerOffline_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        mockWebServer.shutdown()
        val httpUrl = mockWebServer.url("/").toString()
        val presenter = Login2Presenter(context,
                mapOf(ARG_SERVER_URL to httpUrl), view, impl, mockPersonDao)
        presenter.onCreate(null)

        presenter.handleLogin(VALID_USER, VALID_PASS)

        val expectedErrorMsg = impl.getString(
                MessageID.login_network_error, Any())
        verify(view, timeout(defaultTimeout)).showSnackBar(expectedErrorMsg)
        verify(impl, timeout(defaultTimeout)).getString(MessageID.login_network_error, context)
    }



    companion object {

        private const val VALID_USER = "JohnDoe"

        private const val VALID_PASS = "password"
    }


}
