package com.ustadmobile.core.controller

import com.google.gson.Gson
import org.mockito.kotlin.*
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.util.test.ext.bindJndiForActiveEndpoint
import com.ustadmobile.util.test.rules.CoroutineDispatcherRule
import com.ustadmobile.util.test.rules.bindPresenterCoroutineRule
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.kodein.di.*
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

    private lateinit var mockRepo: UmAppDatabase

    @JvmField
    @Rule
    val presenterScopeRule  = CoroutineDispatcherRule()


    @Before
    fun setUp(){
        view = mock {
            on { runOnUiThread(ArgumentMatchers.any()) }.doAnswer { invocation ->
                Thread(invocation.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }

        impl = mock {
            on {getAppConfigDefaultFirstDest(any())}.thenReturn(ContentEntryList2View.VIEW_NAME)
        }

        accountManager = mock{
            onBlocking { login(eq(VALID_USER), eq(VALID_PASS), any(), any()) }.thenAnswer {
                val url = it.arguments[2] as String
                UmAccount(personUid = 42,
                        username = VALID_USER, firstName = "user", lastName = "last", endpointUrl = url)
            }
        }



        mockPersonDao = mock {}
        mockWebServer = MockWebServer()
        mockWebServer.start()
        mockRepo = mock(extraInterfaces = arrayOf(DoorDatabaseRepository::class)) {}

        val endpointScope = EndpointScope()
        di = DI {
            bind<UstadAccountManager>() with singleton { accountManager }
            bind<UstadMobileSystemImpl>() with singleton { impl }
            bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
                mockRepo
            }

            bind<Gson>() with singleton {
                Gson()
            }

            bindPresenterCoroutineRule(presenterScopeRule)

            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun createParams(registration:Boolean = false, guestConnection:Boolean = false,
                             extraParam: Map<String, String> = mapOf()): Map<String,String>{
        val site = Site().apply {
            siteName = ""
            guestLogin = guestConnection
            registrationAllowed = registration
        }
        var args = mapOf(ARG_SITE to Json.encodeToString(Site.serializer(), site))
        args = args.plus(extraParam)
        return args
    }

    private fun enQueueLoginResponse(success: Boolean = true){
        if(success){
            mockWebServer.enqueue(MockResponse()
                    .setResponseCode(200)
                    .setBody(Gson().toJson(UmAccount(42, VALID_USER, "auth", "")))
                    .setHeader("Content-Type", "application/json"))
        }else{
            mockWebServer.enqueue(MockResponse().setResponseCode(403))
        }
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
        verify(impl).go(eq(RegisterAgeRedirectView.VIEW_NAME), any(), any())
    }

    //TODO: Rework this to use the new usersession
    //@Test
    fun givenConnectAsGuestIsVisible_whenClicked_shouldOpenContentSection(){
        val presenter = Login2Presenter(context, createParams(guestConnection = true), view, di)
        presenter.onCreate(mapOf())
        presenter.handleConnectAsGuest()
        verify(impl).go(eq(ContentEntryList2View.VIEW_NAME), any(), any(), any())
    }

    @Test
    fun givenValidUsernameAndPassword_whenFromDestinationArgumentIsProvidedAndHandleLoginClicked_shouldGoToNextScreenAndInvalidateSync() {
        val nextDestination = "nextDummyDestination"
        val fromDestination = "fromDummyDestination"
        enQueueLoginResponse()

        val httpUrl = mockWebServer.url("/").toString()

        InitialContext().bindJndiForActiveEndpoint(httpUrl)

        val presenter = Login2Presenter(context,
                createParams(extraParam = mapOf(ARG_SERVER_URL to httpUrl,
                        ARG_NEXT to nextDestination)), view, di)
        presenter.onCreate(null)

        presenter.handleLogin(VALID_USER, VALID_PASS)

        verify(impl, timeout(defaultTimeout)).go(eq(nextDestination), any(), any(),
            argWhere {
                it.popUpToViewName == UstadView.ROOT_DEST
            })

        verifyBlocking(accountManager, timeout(defaultTimeout)) { login(VALID_USER, VALID_PASS, httpUrl) }
    }

    @Test
    fun givenServerSelectionIsNotAllowedOnValidUsernameAndPassword_whenFromDestinationArgumentNotProvidedAndHandleLoginClicked_shouldGoToNextScreen() {

        impl = mock {
            on{getAppConfigBoolean(any(), any())}.thenReturn(false)
        }

        val nextDestination = "nextDummyDestination"
        enQueueLoginResponse()

        val httpUrl = mockWebServer.url("/").toString()

        InitialContext().bindJndiForActiveEndpoint(httpUrl)

        val presenter = Login2Presenter(context,
                createParams(extraParam = mapOf(ARG_SERVER_URL to httpUrl,
                        ARG_NEXT to nextDestination)), view, di)
        presenter.onCreate(null)

        presenter.handleLogin(VALID_USER, VALID_PASS)

        verify(impl, timeout(defaultTimeout)).go(eq(nextDestination), any(), any(), any())
        verifyBlocking(accountManager) { login(VALID_USER, VALID_PASS, httpUrl) }
    }


    @Test
    fun givenServerSelectionIsAllowedOnValidUsernameAndPassword_whenFromDestinationArgumentNotProvidedAndHandleLoginClicked_shouldGoToNextScreen() {

        impl = mock {
            on{getAppConfigBoolean(any(), any())}.thenReturn(true)
        }
        val nextDestination = "nextDummyDestination"


        val httpUrl = mockWebServer.url("/").toString()

        InitialContext().bindJndiForActiveEndpoint(httpUrl)

        val presenter = Login2Presenter(context,
                createParams(extraParam = mapOf(ARG_SERVER_URL to httpUrl,
                        ARG_NEXT to nextDestination)), view, di)
        presenter.onCreate(null)

        presenter.handleLogin(VALID_USER, VALID_PASS)


        verify(impl, timeout(defaultTimeout)).go(eq(nextDestination), any(), any(), any())

        verifyBlocking(accountManager) { login(VALID_USER, VALID_PASS, httpUrl) }
    }

    @Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        accountManager = mock{
            onBlocking{login(any(), any(), any(), any())}.then{
                throw UnauthorizedException("Access denied")
            }
        }
        enQueueLoginResponse(false)
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
        verify(impl, timeout(defaultTimeout).atLeastOnce()).getString(MessageID.login_network_error, context)
    }


    @Test
    fun givenUserNameOrPasswordContainsPaddingSpaces_whenHandleLoginCalled_thenShouldTrimSpace() {
        val nextDestination = "nextDummyDestination"
        val fromDestination = "fromDummyDestination"
        enQueueLoginResponse()

        val httpUrl = mockWebServer.url("/").toString()

        InitialContext().bindJndiForActiveEndpoint(httpUrl)

        val presenter = Login2Presenter(context,
                createParams(extraParam = mapOf(ARG_SERVER_URL to httpUrl,
                        ARG_NEXT to nextDestination)), view, di)
        presenter.onCreate(null)

        presenter.handleLogin(" $VALID_USER ", "$VALID_PASS ")

        verifyBlocking(accountManager, timeout(defaultTimeout)) {
            login(eq(VALID_USER), eq(VALID_PASS), eq(httpUrl), any())
        }
    }


    companion object {

        private const val VALID_USER = "JohnDoe"

        private const val VALID_PASS = "password"
    }


}
