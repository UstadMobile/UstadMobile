package com.ustadmobile.core.impl

import com.google.gson.Gson
import com.ustadmobile.core.account.*
import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_PREFKEY
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.userAtServer
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.io.ByteArrayOutputStream

class UstadAccountManagerTest {

    lateinit var mockSystemImpl: UstadMobileSystemImpl

    val appContext = Any()

    lateinit var mockWebServer: MockWebServer

    lateinit var mockServerUrl: String

    private lateinit var di: DI

    private lateinit var endpointScope: EndpointScope

    private lateinit var json: Json

    private fun MockWebServer.enqueueValidAccountResponse(umAccount: UmAccount =
                                                                  UmAccount(42L, "bob", "", mockServerUrl)) {
        enqueue(MockResponse()
                .setBody(Json.encodeToString(UmAccount.serializer(), umAccount))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json; charset=utf-8"))
    }

    @Before
    fun setup() {
        mockSystemImpl = mock {
            on { getAppConfigString(eq(AppConfig.KEY_API_URL), any(), any()) }
                    .thenReturn("http://app.ustadmobile.com/")
        }

        mockWebServer = MockWebServer().also {
            it.start()
        }

        mockServerUrl = mockWebServer.url("/").toString()

        endpointScope = EndpointScope()

        json = Json { encodeDefaults = true }

        di = DI {
            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(endpointScope).singleton {
                mock<UmAppDatabase>(extraInterfaces = arrayOf(DoorDatabaseSyncRepository::class)) {
                    on { (this as DoorDatabaseSyncRepository).clientId }.thenReturn(42)
                }
            }

            bind<HttpClient>() with singleton {
                HttpClient(OkHttp) {
                    install(JsonFeature)
                    install(HttpTimeout)
                }
            }

            bind<Gson>() with singleton {
                Gson()
            }
        }
    }

    @Test
    fun givenNoUserInPrefKeys_whenInitialized_shouldInitGuestAccountOnDefaultServer() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)
        val activeAccount = accountManager.activeAccount
        Assert.assertEquals("Initial account has personUid = 0", 0L, activeAccount.personUid)
        Assert.assertEquals("Initial account uses default apiUrl",
                "http://app.ustadmobile.com/", activeAccount.endpointUrl)
    }

    @Test
    fun givenValidLoginCredentials_whenLoginCalledForFirstLogin_shouldInitLoginDetailsAndRemoveGuestAccount() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)

        val loggedInAccount = UmAccount(42L, "bob", "",
                mockServerUrl)

        mockWebServer.enqueueValidAccountResponse(loggedInAccount)

        runBlocking {
            accountManager.login("bob", "password", mockServerUrl,
                    replaceActiveAccount = true)
        }

        Assert.assertEquals("Active account is the newly logged in account",
                loggedInAccount.userAtServer, accountManager.activeAccount.userAtServer)
        Assert.assertEquals("There is one stored account", 1,
                accountManager.storedAccounts.size)
        argumentCaptor<String> {
            verify(mockSystemImpl).setAppPref(eq(ACCOUNTS_PREFKEY), capture(), any())
            val accountSaved = json.decodeFromString(UstadAccounts.serializer(), firstValue)
            Assert.assertEquals("Saved account as active", loggedInAccount.userAtServer,
                    accountSaved.currentAccount)
        }
    }

    @Test
    fun givenValidLoginCredentials_whenLoginCalledForSecondAccountOnSameServer_shouldAddAccount() {
        val savedAccount = UmAccount(50L, "joe", "", mockServerUrl)
        whenever(mockSystemImpl.getAppPref(eq(ACCOUNTS_PREFKEY), any())).thenReturn(
                json.encodeToString(UstadAccounts.serializer(),
                        UstadAccounts(savedAccount.userAtServer, listOf(savedAccount))))

        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)

        val loggedInAccount = UmAccount(42L, "bob", "", mockServerUrl)
        mockWebServer.enqueueValidAccountResponse(loggedInAccount)

        runBlocking {
            accountManager.login("bob", "password", mockServerUrl)
        }

        Assert.assertEquals("There are two stored accounts", 2,
                accountManager.storedAccounts.size)
        Assert.assertEquals("Active account is the newly logged in account",
                loggedInAccount.userAtServer, accountManager.activeAccount.userAtServer)

        argumentCaptor<String> {
            verify(mockSystemImpl).setAppPref(eq(ACCOUNTS_PREFKEY), capture(), any())
            val accountSaved = json.decodeFromString(UstadAccounts.serializer(), firstValue)
            Assert.assertEquals("Saved account as active", loggedInAccount.userAtServer,
                    accountSaved.currentAccount)
            Assert.assertEquals("Two accounts were saved", 2,
                accountSaved.storedAccounts.size)
        }
    }

    @Test
    fun givenInvalidLoginCredentials_whenLoginCalled_thenShouldThrowException() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)

        mockWebServer.enqueue(MockResponse()
                .setResponseCode(403))

        var exception: Exception? = null
        try {
            runBlocking {
                accountManager.login("bob", "wrong", mockServerUrl)
            }
        }catch(e: Exception) {
            exception = e
        }

        Assert.assertTrue("Got UnauthorizedException when providing invalid login credentials",
            exception is UnauthorizedException)
    }

    @Test
    fun givenUnreachableServer_whenLoginCalled_thenShouldThrowException() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)

        var exception: Exception? = null

        try {
            runBlocking {
                accountManager.login("bob", "password", "http://inaccessible/")
            }
        }catch(e: Exception) {
            exception = e
        }

        Assert.assertNotNull("Got exception when attempting to login to an inaccessible server",
            exception)
    }

    @Test
    fun givenTwoStoredAccounts_whenSetActiveAccountCalled_thenActiveAccountShouldChangeAndAllRemainInStoredAccounts() {
        val storedAccounts = UstadAccounts("bob@$mockServerUrl",
                listOf(UmAccount(1, "bob", "", mockServerUrl),
                        UmAccount(2, "joe", "", mockServerUrl)))

        whenever(mockSystemImpl.getAppPref(eq(ACCOUNTS_PREFKEY), any())).thenReturn(
            json.encodeToString(UstadAccounts.serializer(), storedAccounts))

        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)

        accountManager.activeAccount = storedAccounts.storedAccounts[1]

        Assert.assertEquals("Active account is updated when calling setActiveAccount",
                "joe@$mockServerUrl", accountManager.activeAccount.userAtServer)
        Assert.assertEquals("AccountManager still has both accounts stored", 2,
            accountManager.storedAccounts.size)
        argumentCaptor<String> {
            verify(mockSystemImpl).setAppPref(eq(ACCOUNTS_PREFKEY), capture(), any())
            val accountSaved = json.decodeFromString(UstadAccounts.serializer(), firstValue)
            Assert.assertEquals("Saved account as active", "joe@$mockServerUrl",
                    accountSaved.currentAccount)
        }
    }

    @Test
    fun givenMultipleStoredAccounts_whenActiveAccountRemoved_thenLastUsedAccountShouldBeActive() {
        val timeNow = System.currentTimeMillis()
        val storedAccounts = UstadAccounts("bob@$mockServerUrl",
                listOf(UmAccount(1, "bob", "", mockServerUrl),
                        UmAccount(2, "joe", "", mockServerUrl),
                        UmAccount(3, "harry", "", mockServerUrl)),
                mapOf("bob@$mockServerUrl" to timeNow - 5000,
                    "joe@$mockServerUrl" to timeNow - 15000,
                    "harry@$mockServerUrl" to timeNow - 5000))
        whenever(mockSystemImpl.getAppPref(eq(ACCOUNTS_PREFKEY), any())).thenReturn(
                Json.encodeToString(UstadAccounts.serializer(), storedAccounts))

        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)

        accountManager.removeAccount(storedAccounts.storedAccounts[0])

        Assert.assertEquals("Most recently used account after account that was removed is now active",
            "harry@$mockServerUrl", accountManager.activeAccount.userAtServer)
        argumentCaptor<String> {
            verify(mockSystemImpl, atLeastOnce()).setAppPref(eq(ACCOUNTS_PREFKEY), capture(), any())
            val accountSaved = json.decodeFromString(UstadAccounts.serializer(), lastValue)
            Assert.assertEquals("Fallback account is saved as active acount", "harry@$mockServerUrl",
                    accountSaved.currentAccount)
        }
    }

    @Test
    fun givenOneStoredAccount_whenActiveAccountRemoved_thenDefaultAccountShouldBeActive() {
        val timeNow = System.currentTimeMillis()
        val storedAccounts = UstadAccounts("bob@$mockServerUrl",
                listOf(UmAccount(1, "bob", "", mockServerUrl)),
                mapOf("bob@$mockServerUrl" to timeNow - 5000))
        whenever(mockSystemImpl.getAppPref(eq(ACCOUNTS_PREFKEY), any())).thenReturn(
                json.encodeToString(UstadAccounts.serializer(), storedAccounts))

        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)

        accountManager.removeAccount(storedAccounts.storedAccounts[0])

        Assert.assertEquals("After removing the only stored account, the default account is now active",
            "guest@http://app.ustadmobile.com/", accountManager.activeAccount.userAtServer)
    }

    @Test
    fun givenValidRegistrationRequest_whenNewAccountRequested_thenShouldBeRequestedOnServerAndActive() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)

        val personToRegister = PersonWithAccount().apply {
            firstNames = "Mary"
            lastName = "Poppins"
            phoneNum = "1234567"
            emailAddr = "mary@email.com"
            username = "mary"
            newPassword = "password"
        }

        val accountResponse = UmAccount(42L, "mary", "", "")
        mockWebServer.enqueue(MockResponse()
                .setBody(json.encodeToString(UmAccount.serializer(), accountResponse))
                .addHeader("Content-Type", "application/json; charset=utf-8"))

        val accountRegistered = runBlocking {
            accountManager.register(personToRegister, mockServerUrl)
        }

        Assert.assertEquals("Active account is the account registered",
                "mary@$mockServerUrl", accountManager.activeAccount.userAtServer)

        val request = mockWebServer.takeRequest()
        val byteArrayOut = ByteArrayOutputStream()
        request.body.writeTo(byteArrayOut)
        val bodyStr = byteArrayOut.toByteArray().decodeToString()

        val registerRequest = Gson().fromJson(bodyStr, RegisterRequest::class.java)
        Assert.assertEquals("Got expected register request", "mary",
            registerRequest.person.username)
    }


}