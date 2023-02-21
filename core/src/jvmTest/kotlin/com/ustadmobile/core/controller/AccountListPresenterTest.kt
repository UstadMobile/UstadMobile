
package com.ustadmobile.core.controller

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.UserSession
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.json.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.door.lifecycle.*
import com.ustadmobile.util.test.rules.CoroutineDispatcherRule
import com.ustadmobile.util.test.rules.bindPresenterCoroutineRule
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import org.junit.Assert
import org.junit.Rule

class AccountListPresenterTest {

    private lateinit var mockView: AccountListView

    private lateinit var context: Any

    private lateinit var accountManager: UstadAccountManager

    private val defaultTimeout:Long = 5000

    private lateinit var impl: UstadMobileSystemImpl

    private val mockActiveSessionsLive = MutableLiveData<List<UserSessionWithPersonAndEndpoint>>()

    private val mockActiveSessionLive = MutableLiveData<UserSessionWithPersonAndEndpoint?>()

    private lateinit var mockedAccountListObserver:Observer<List<UmAccount>>

    private lateinit var mockedAccountObserver:Observer<UmAccount>

    private val accountList = listOf(UmAccount(1,"dummy",null,""))

    @JvmField
    @Rule
    val dispatcherRule = CoroutineDispatcherRule()

    private val defaultSessionList = listOf(UserSessionWithPersonAndEndpoint(
        userSession = UserSession().apply {
            usUid = 42
            usPersonUid = 50
            usStatus = UserSession.STATUS_ACTIVE
        },
        person = Person().apply {
            personUid = 50
            firstNames = "Bob"
            lastName = "Jones"
        },
        endpoint = Endpoint("https://orgname.ustadmobile.app/")
    ))

    private val secondAccountList = listOf(UserSessionWithPersonAndEndpoint(
        userSession = UserSession().apply {
            usUid = 52
            usPersonUid = 60
            usStatus = UserSession.STATUS_ACTIVE
        },
        person = Person().apply {
            personUid = 60
            firstNames = "Joe"
            lastName = "Doe"
        },
        endpoint = Endpoint("https://orgname.ustadmobile.app/")
    ))

    private lateinit var mockedLifecycleOwner: LifecycleOwner

    private lateinit var di: DI

    @Before
    fun setup() {

        mockView = mock { }
        impl = mock{
            on { getAppConfigDefaultFirstDest(any()) }.thenReturn(ContentEntryList2View.VIEW_NAME)
            on { getString(any(), any()) }.thenAnswer {
                val messageId = it.getArgument<Int>(0)
                if(messageId == MessageID.logged_in_as) {
                    "Logged in as  %1\$s on %2\$s"
                }else {
                    messageId.toString()
                }
            }
        }

        accountManager = mock{
            on { activeUserSessionsLive }.thenReturn(mockActiveSessionsLive)
            on { activeUserSessionLive }.thenReturn(mockActiveSessionLive)
        }
        context = Any()

        mockedAccountListObserver = mock{
            on{ onChanged(any()) }.thenAnswer{ accountList }
        }

        mockedAccountObserver = mock{
            on{ onChanged(any()) }.thenAnswer{ accountList[0] }
        }

        mockedLifecycleOwner = mockLifecycleOwner(DoorState.STARTED)

        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { impl }
            bind<UstadAccountManager>() with singleton { accountManager }
            bind<HttpClient>() with singleton {
                HttpClient(OkHttp) {
                    install(ContentNegotiation) {
                        gson()
                    }
                    install(HttpTimeout)
                }
            }
            bindPresenterCoroutineRule(dispatcherRule)
        }
    }

    @Test
    fun givenStoreAccounts_whenAppLaunched_thenShouldShowAllAccounts(){
        mockActiveSessionsLive.setValue(defaultSessionList)

        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)
        presenter.onCreate(null)

        argumentCaptor<LiveData<List<UserSessionWithPersonAndEndpoint>>>{
            verify(mockView, timeout(5000)).accountListLive = capture()
            //This should be the mediator
            runBlocking {
                val liveData = lastValue.waitUntil<List<UserSessionWithPersonAndEndpoint>>(5000) {
                    it.first().person.personUid == 50L
                }

                Assert.assertTrue(liveData.getValue()?.any { it.person.personUid == 50L } == true)
            }
        }
    }

    @Test
    fun givenActiveAccountExists_whenAppLaunched_thenShouldShowIt(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        presenter.onCreate(null)

        mockActiveSessionLive.setValue(defaultSessionList[0])

        nullableArgumentCaptor<LiveData<UserSessionWithPersonAndEndpoint?>> {
            verify(mockView, timeout(defaultTimeout)).activeAccountLive = capture()
            runBlocking {
                lastValue!!.waitUntil<UserSessionWithPersonAndEndpoint?> { it != null }
                Assert.assertEquals("LiveData was provided where value provided the active account",
                    defaultSessionList.first().person.personUid,
                    lastValue!!.getValue()?.person?.personUid)
            }
        }
    }

    @Test
    fun givenSelectServerAllowed_whenAccountButtonClicked_thenShouldOpenGetStartedScreen(){
        impl.stub {
            on{getAppConfigBoolean(eq(AppConfig.KEY_ALLOW_SERVER_SELECTION), any())}.thenReturn(true)
        }
        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        presenter.onCreate(null)
        presenter.handleClickAddAccount()

        argumentCaptor<String>{
            verify(impl).go(viewName = capture(), args = any(), context=  any())
            assertTrue("Get started screen was opened", SiteEnterLinkView.VIEW_NAME == firstValue)
        }
    }

    @Test
    fun givenSelectServerNotAllowed_whenAccountButtonClicked_thenShouldOpenLoginScreen(){
        impl.stub {
            on{getAppConfigBoolean(any(), any())}.thenReturn(false)
        }
        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        presenter.onCreate(null)
        presenter.handleClickAddAccount()

        argumentCaptor<String>{
            verify(impl).go(capture(), any(), any())
            assertTrue("Login screen was opened", Login2View.VIEW_NAME == firstValue)
        }
    }


    @Test
    fun givenDeleteAccountButton_whenClicked_thenShouldRemoveAccountFromTheDevice(){
        mockActiveSessionsLive.setValue(defaultSessionList)
        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)
        presenter.onCreate(null)

        presenter.handleClickDeleteSession(defaultSessionList[0])
        verifyBlocking(accountManager) {
            endSession(eq(defaultSessionList[0]), any(), any())
        }

    }

    @Test
    fun givenOneAccountOnDeviceAndServerSelectionAllowed_whenLogoutButtonClicked_thenEndSessionAndShouldRedirectToSiteEnterLinkView(){
        mockActiveSessionLive.setValue(defaultSessionList[0])
        mockActiveSessionsLive.setValue(defaultSessionList)

        accountManager.stub {
            onBlocking {
                activeSessionCount()
            }.thenReturn(0)
        }

        impl.stub {
            on{getAppConfigBoolean(eq(AppConfig.KEY_ALLOW_SERVER_SELECTION), any())}.thenReturn(true)
        }

        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickLogout(defaultSessionList[0])

        verifyBlocking(accountManager, timeout(5000)) {
            endSession(argWhere {
                it.userSession.usUid == defaultSessionList[0].userSession.usUid
            }, any(), any())
        }

        verify(impl, timeout(5000)).go(eq(SiteEnterLinkView.VIEW_NAME), any(), any(),
            any())
    }

    @Test
    fun givenOneAccountOnDeviceAndServerSelectionNotAllowed_whenLogoutButtonClicked_thenEndSessionAndShouldRedirectToLoginView() {
        mockActiveSessionLive.setValue(defaultSessionList[0])
        mockActiveSessionsLive.setValue(defaultSessionList)

        accountManager.stub {
            onBlocking {
                activeSessionCount()
            }.thenReturn(0)
        }

        impl.stub {
            on{getAppConfigBoolean(eq(AppConfig.KEY_ALLOW_SERVER_SELECTION), any())}
                .thenReturn(false)
        }


        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickLogout(defaultSessionList[0])

        verifyBlocking(accountManager, timeout(5000)) {
            endSession(argWhere {
                it.userSession.usUid == defaultSessionList[0].userSession.usUid
            }, any(), any())
        }

        verify(impl, timeout(5000)).go(eq(Login2View.VIEW_NAME), any(), any(),
            any())
    }

    @Test
    fun givenMultipleAccountsOnDevice_whenLogoutButtonClicked_thenShouldEndSessionAndRedirectToAccountListInPickerMode() {
        mockActiveSessionsLive.setValue(defaultSessionList + secondAccountList)
        mockActiveSessionLive.setValue(defaultSessionList[0])

        accountManager.stub {
            onBlocking {
                activeSessionCount()
            }.thenReturn(1)
        }

        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        presenter.onCreate(null)
        presenter.handleClickLogout(defaultSessionList[0])


        verifyBlocking(accountManager, timeout(5000)) {
            endSession(argWhere {
                it.userSession.usUid == defaultSessionList[0].userSession.usUid
            }, any(), any())
        }

        verify(impl, timeout(5000)).go(eq(AccountListView.VIEW_NAME),
            argWhere {
                it[AccountListView.ARG_ACTIVE_ACCOUNT_MODE] == AccountListView.ACTIVE_ACCOUNT_MODE_INLIST
                        && it[UstadView.ARG_LISTMODE] == ListViewMode.PICKER.toString()
            }, any(), any())
    }



    @Test
    fun givenAccountList_whenAccountIsClicked_shouldBeActive(){
        mockActiveSessionsLive.setValue(defaultSessionList + secondAccountList)
        mockActiveSessionLive.setValue(defaultSessionList[0])

        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        presenter.onCreate(null)


        presenter.handleClickUserSession(secondAccountList[0])

        verify(accountManager).activeSession = argWhere {
            it.userSession.usUid == secondAccountList[0].userSession.usUid
        }

        verify(impl).goToViewLink(argWhere {
            it.startsWith(ContentEntryList2View.VIEW_NAME)
        }, any(), argWhere {
            it.popUpToViewName == UstadView.ROOT_DEST && !it.popUpToInclusive
        })

    }


    @Test
    fun givenProfileButton_whenClicked_thenShouldGoToProfileView(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickProfile(42L)

        argumentCaptor<Map<String,String>>{
            verify(impl).go(eq(PersonDetailView.VIEW_NAME), capture(), any())
            assertEquals("Person details view was opened with right person id",
                    42L, firstValue[ARG_ENTITY_UID]?.toLong())
        }
    }

    @Test
    fun givenFilterByServerUriArg_whenCreatedAndClickAddAccount_thenShouldFilterAccountsAndGoDirectToLoginForServer() {
        val site = Site().also {
            it.siteName = "Test Site"
        }
        val mockWebServer = MockWebServer().also {
            it.start()
            it.enqueue(MockResponse()
                .setBody(Json.encodeToString(Site.serializer(), site))
                .addHeader("Content-Type", "application/json"))
        }

        val activeEndpointArg = mockWebServer.url("/").toString()
        val sessionList = listOf(
            UserSessionWithPersonAndEndpoint(
                UserSession().apply {
                    usUid = 1
                    usPersonUid = 1
                },
                Person().apply {
                    personUid = 1
                    username = "bob"
                    firstNames = "bob"
                    lastName = "jones"
                },
                Endpoint("https://endpoint1.com/")),
            UserSessionWithPersonAndEndpoint(
                UserSession().apply {
                    usUid = 2
                    usPersonUid = 2
                },
                Person().apply {
                    personUid = 2
                    username = "joe"
                    firstNames = "Joe"
                    lastName = "Jones"
                },
                Endpoint(activeEndpointArg)
            )
        )

        mockActiveSessionLive.setValue(sessionList[0])
        mockActiveSessionsLive.setValue(sessionList)

        val presenter = AccountListPresenter(context,
            mapOf(AccountListView.ARG_FILTER_BY_ENDPOINT to activeEndpointArg), mockView, di,
            mockedLifecycleOwner)
        presenter.onCreate(mapOf())

        //wait for the list to be set on the view
        verify(mockView, timeout(5000).atLeastOnce()).activeAccountLive = any()

        presenter.handleClickAddAccount()

        //Verify that the account list was filtered as per the argument provided
        argumentCaptor<LiveData<List<UserSessionWithPersonAndEndpoint>>> {
            verify(mockView, timeout(5000)).accountListLive = capture()
            runBlocking {
                firstValue.waitUntil<List<UserSessionWithPersonAndEndpoint>> {
                    it.size == 1 && it.first().endpoint.url == activeEndpointArg
                }
            }
        }

        verify(impl, timeout(5000)).go(eq(Login2View.VIEW_NAME), argWhere { argMap ->
            argMap[ARG_SERVER_URL] == activeEndpointArg
        }, any())
    }

    @Test
    fun givenAboutButton_whenClicked_thenShouldGoToAboutView(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickAbout()

        verify(impl).goToViewLink(eq(AboutView.VIEW_NAME), any(), any())
    }
}