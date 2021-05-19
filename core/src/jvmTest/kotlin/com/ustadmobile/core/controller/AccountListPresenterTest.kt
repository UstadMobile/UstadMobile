
package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.door.*
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class AccountListPresenterTest {

    private lateinit var mockView: AccountListView

    private lateinit var context: Any

    private lateinit var accountManager: UstadAccountManager

    private val defaultTimeout:Long = 5000

    private lateinit var impl: UstadMobileSystemImpl

    private val  accountListLive = DoorMutableLiveData<List<UmAccount>>()

    private val  activeAccountLive = DoorMutableLiveData<UmAccount>()

    private lateinit var mockedAccountListObserver:DoorObserver<List<UmAccount>>

    private lateinit var mockedAccountObserver:DoorObserver<UmAccount>

    private val accountList = listOf(UmAccount(1,"dummy",null,""))

    private lateinit var mockedLifecycleOwner: DoorLifecycleOwner

    private lateinit var di: DI

    @Before
    fun setup() {

        mockView = mock { }
        impl = mock{
            on { getAppConfigDefaultFirstDest(any()) }.thenReturn(ContentEntryListTabsView.VIEW_NAME)
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
            on{storedAccountsLive}.thenReturn(accountListLive)
            on{activeAccountLive}.thenReturn(activeAccountLive)
            on{activeAccount}.thenAnswer { activeAccountLive.getValue()!! }
        }
        context = Any()

        mockedAccountListObserver = mock{
            on{ onChanged(any()) }.thenAnswer{ accountList }
        }

        mockedAccountObserver = mock{
            on{ onChanged(any()) }.thenAnswer{ accountList[0] }
        }

        mockedLifecycleOwner = mock {
            on { currentState }.thenReturn(UstadBaseController.STARTED)
        }

        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { impl }
            bind<UstadAccountManager>() with singleton { accountManager }
            bind<HttpClient>() with singleton {
                HttpClient(OkHttp) {
                    install(JsonFeature)
                    install(HttpTimeout)
                }
            }
        }
    }

    @Test
    fun givenStoreAccounts_whenAppLaunched_thenShouldShowAllAccounts(){
        accountListLive.observeForever(mockedAccountListObserver)
        accountListLive.sendValue(accountList)
        activeAccountLive.setVal(accountList.first())

        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)
        presenter.onCreate(null)

        argumentCaptor<List<UmAccount>>{
            verify(mockedAccountListObserver, timeout(defaultTimeout).atLeastOnce()).onChanged(capture())
            assertTrue("Account list was displayed", accountList.containsAll(lastValue))
        }
    }

    @Test
    fun givenActiveAccountExists_whenAppLaunched_thenShouldShowIt(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        presenter.onCreate(null)

        activeAccountLive.observeForever(mockedAccountObserver)
        activeAccountLive.sendValue(accountList[0])
        argumentCaptor<UmAccount>{
            verify(mockedAccountObserver, timeout(defaultTimeout).atLeastOnce()).onChanged(capture())
            assertEquals("Active account was displayed", accountList[0], lastValue)
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
        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        val account = UmAccount(1,"dummy", null,"")
        presenter.onCreate(null)

        presenter.handleClickDeleteAccount(account)

        argumentCaptor<UmAccount>{
            verify(accountManager).removeAccount(capture(), any(), any())
            assertTrue("Expected account was removed from the device",
                    account == firstValue)
        }
    }

    @Test
    fun givenLogoutButton_whenClicked_thenShouldRemoveAccountFromTheDevice(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        val account = UmAccount(1,"dummy", null,"")
        presenter.onCreate(null)

        activeAccountLive.sendValue(account)

        presenter.handleClickLogout(account)
        argumentCaptor<UmAccount>{
            verify(accountManager).removeAccount(capture(), any(), any())
            assertTrue("Expected account was removed from the device",
                    account == firstValue)
        }
    }


    @Test
    fun givenAccountList_whenAccountIsClicked_shouldBeActive(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        val account = UmAccount(1,"dummy", null,"")
        presenter.onCreate(null)

        activeAccountLive.sendValue(account)

        presenter.handleClickAccount(account)
        argumentCaptor<UmAccount>{
            verify(accountManager).activeAccount = capture()
            assertTrue("Expected account was set active",
                    account == firstValue)
        }
    }


    @Test
    fun givenProfileButton_whenClicked_thenShouldGoToProfileView(){

        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        val account = UmAccount(1,"dummy", null,"")
        presenter.onCreate(null)

        presenter.handleClickProfile(account.personUid)

        argumentCaptor<Map<String,String>>{
            verify(impl).go(eq(PersonDetailView.VIEW_NAME), capture(), any())
            assertTrue("Person details view was opened with right person id",
                    account.personUid == firstValue[ARG_ENTITY_UID]?.toLong())
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
        val accountList = listOf(
            UmAccount(1, "bob", "", "https://endpoint1.com/",
                "Bob", "Jones"),
            UmAccount(2, "joe", "", activeEndpointArg,
                "Joe", "Jones"))

        activeAccountLive.setVal(accountList[0])
        accountListLive.setVal(accountList)

        val presenter = AccountListPresenter(context,
            mapOf(AccountListView.ARG_FILTER_BY_ENDPOINT to activeEndpointArg), mockView, di,
            mockedLifecycleOwner)
        presenter.onCreate(mapOf())

        //wait for the list to be set on the view
        verify(mockView, timeout(5000).atLeastOnce()).activeAccountLive = any()
        presenter.handleClickAddAccount()

        verify(mockView, timeout(5000).atLeastOnce()).accountListLive = argWhere {
            it.getValue()?.size == 1 && (it.getValue()?.first() as UmAccount).endpointUrl == activeEndpointArg
        }

        verify(impl, timeout(5000)).go(eq(Login2View.VIEW_NAME), argWhere { argMap ->
            argMap[ARG_SITE]?.let {
                Json.decodeFromString(Site.serializer(), it)
            } ?: return@argWhere false

            argMap[ARG_SERVER_URL] == activeEndpointArg
        }, any())
    }

    @Test
    fun givenFilterByUriArgProvidedAndServerOffline_whenCreatedAndClickAddAccount_thenShouldShowError() {
        val activeEndpointArg = "http://localhost/offline/"
        val accountList = listOf(
            UmAccount(1, "bob", "", "https://endpoint1.com/",
                "Bob", "Jones"),
            UmAccount(2, "joe", "", activeEndpointArg,
                "Joe", "Jones"))

        activeAccountLive.setVal(accountList[0])
        accountListLive.setVal(accountList)

        val presenter = AccountListPresenter(context,
            mapOf(AccountListView.ARG_FILTER_BY_ENDPOINT to activeEndpointArg), mockView, di,
            mockedLifecycleOwner)
        presenter.onCreate(mapOf())

        //wait for the list to be set on the view
        verify(mockView, timeout(5000).atLeastOnce()).activeAccountLive = any()
        presenter.handleClickAddAccount()

        verify(mockView, timeout(5000).atLeastOnce()).showSnackBar(
            eq(impl.getString(MessageID.login_network_error, context)), any(), any())
    }


    @Test
    fun givenAboutButton_whenClicked_thenShouldGoToAboutView(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di, mockedLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickAbout()

        verify(impl).goToViewLink(eq(AboutView.VIEW_NAME), any(), any())
    }
}