package com.ustadmobile.core.impl

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_PREFKEY
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.account.UstadAccountManager.Companion.MANIFEST_DEFAULT_SERVER
import com.ustadmobile.core.account.UstadAccounts
import com.ustadmobile.core.util.ext.userAtServer
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.util.test.ext.bindNewSqliteDataSourceIfNotExisting
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import javax.naming.InitialContext

class UstadAccountManagerTest {

    lateinit var mockSystemImpl: UstadMobileSystemImpl

    val appContext = Any()

    lateinit var mockDbOpener: UstadAccountManager.DbOpener

    private val openDbs = mutableMapOf<String, UmAppDatabase>()

    lateinit var mockWebServer: MockWebServer

    lateinit var mockServerUrl: String

    private fun MockWebServer.enqueueValidAccountResponse(umAccount: UmAccount =
                                                                  UmAccount(42L, "bob", "", mockServerUrl)) {
        enqueue(MockResponse()
                .setBody(Json.stringify(UmAccount.serializer(), umAccount))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json; charset=utf-8"))
    }

    @Before
    fun setup() {
        mockSystemImpl = mock {
            on { getManifestPreference(eq(MANIFEST_DEFAULT_SERVER), any()) }
                    .thenReturn("http://app.ustadmobile.com/")
        }

        openDbs.clear()

        mockDbOpener = mock {
            on { openDb(any(), any()) }.thenAnswer { invocation ->
                val dbName = invocation.arguments[1] as String
                openDbs.getOrPut(dbName) {
                    InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                    UmAppDatabase.getInstance(invocation.arguments[0], dbName).apply { clearAllTables() }
                }
            }
        }

        mockWebServer = MockWebServer().also {
            it.start()
        }

        mockServerUrl = mockWebServer.url("/").toString()
    }

    @Test
    fun givenNoUserInPrefKeys_whenInitialized_shouldInitGuestAccountOnDefaultServer() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, mockDbOpener)
        val activeAccount = accountManager.activeAccount
        Assert.assertEquals("Initial account has personUid = 0", 0L, activeAccount.personUid)
        Assert.assertEquals("Initial account uses default apiUrl",
                "http://app.ustadmobile.com/", activeAccount.endpointUrl)
    }

    @Test
    fun givenValidLoginCredentials_whenLoginCalledForFirstLogin_shouldInitLoginDetailsAndRemoveGuestAccount() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, mockDbOpener)

        val loggedInAccount = UmAccount(42L, "bob", "",
                mockServerUrl)

        mockWebServer.enqueueValidAccountResponse(loggedInAccount)

        runBlocking {
            accountManager.login("bob", "password", mockServerUrl,
                    replaceActiveAccount = true)
        }

        val dbName = sanitizeDbNameFromUrl(mockServerUrl)
        val dbPair = accountManager.storedDatabases[dbName]
        Assert.assertEquals("Active account is the newly logged in account",
                loggedInAccount.userAtServer, accountManager.activeAccount.userAtServer)
        Assert.assertEquals("Database was opened", openDbs[dbName], dbPair?.db)
        Assert.assertEquals("There is one stored account", 1,
                accountManager.storedAccounts.size)
    }

    @Test
    fun givenValidLoginCredentials_whenLoginCalledForSecondAccountOnSameServer_shouldAddAccountNotDatabase() {
        val savedAccount = UmAccount(50L, "joe", "", mockServerUrl)
        whenever(mockSystemImpl.getAppPref(eq(ACCOUNTS_PREFKEY), any())).thenReturn(
                Json.stringify(UstadAccounts.serializer(),
                        UstadAccounts(savedAccount.userAtServer, listOf(savedAccount))))

        val accountManager = UstadAccountManager(mockSystemImpl, appContext, mockDbOpener)

        val loggedInAccount = UmAccount(42L, "bob", "", mockServerUrl)
        mockWebServer.enqueueValidAccountResponse(loggedInAccount)

        runBlocking {
            accountManager.login("bob", "password", mockServerUrl)
        }

        val dbName = sanitizeDbNameFromUrl(mockServerUrl)
        val dbPair = accountManager.storedDatabases[dbName]
        Assert.assertEquals("Database was opened", openDbs[dbName], dbPair?.db)
        Assert.assertEquals("There are two stored accounts", 2,
                accountManager.storedAccounts.size)
        Assert.assertEquals("Active account is the newly logged in account",
                loggedInAccount.userAtServer, accountManager.activeAccount.userAtServer)
        Assert.assertEquals("There is one active database only (as this is the same server as previous account)",
            1, accountManager.storedDatabases.size)
    }


    @Test
    fun givenValidLoginCredentials_whenLoginCalledForSecondAccountOnDifferentServer_shouldAddAccountAndDatabase() {
        val savedAccount = UmAccount(50L, "joe", "", "http://another.server.app")
        whenever(mockSystemImpl.getAppPref(eq(ACCOUNTS_PREFKEY), any())).thenReturn(
                Json.stringify(UstadAccounts.serializer(),
                        UstadAccounts(savedAccount.userAtServer, listOf(savedAccount))))

        val accountManager = UstadAccountManager(mockSystemImpl, appContext, mockDbOpener)

        val loggedInAccount = UmAccount(42L, "bob", "", mockServerUrl)
        mockWebServer.enqueueValidAccountResponse(loggedInAccount)

        runBlocking {
            accountManager.login("bob", "password", mockServerUrl)
        }

        val dbName = sanitizeDbNameFromUrl(mockServerUrl)
        val dbPair = accountManager.storedDatabases[dbName]
        Assert.assertEquals("Database was opened", openDbs[dbName], dbPair?.db)
        Assert.assertEquals("There are two stored accounts", 2,
                accountManager.storedAccounts.size)
        Assert.assertEquals("Active account is the newly logged in account",
                loggedInAccount.userAtServer, accountManager.activeAccount.userAtServer)
        Assert.assertEquals("There are two active databases only (as accounts are on different servers)",
                2, accountManager.storedDatabases.size)
    }


    @Test
    fun givenInvalidLoginCredentials_whenLoginCalled_thenShouldThrowException() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, mockDbOpener)

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
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, mockDbOpener)

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
                Json.stringify(UstadAccounts.serializer(), storedAccounts))

        val accountManager = UstadAccountManager(mockSystemImpl, appContext, mockDbOpener)

        accountManager.activeAccount = storedAccounts.storedAccounts[1]

        Assert.assertEquals("Active account is updated when calling setActiveAccount",
                "joe@$mockServerUrl", accountManager.activeAccount.userAtServer)
        Assert.assertEquals("AccountManager still has both accounts stored", 2,
            accountManager.storedAccounts.size)
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
                Json.stringify(UstadAccounts.serializer(), storedAccounts))

        val accountManager = UstadAccountManager(mockSystemImpl, appContext, mockDbOpener)

        accountManager.removeAccount(storedAccounts.storedAccounts[0])

        Assert.assertEquals("Most recently used account after account that was removed is now active",
            "harry@$mockServerUrl", accountManager.activeAccount.userAtServer)
    }

    @Test
    fun givenOneStoredAccount_whenActiveAccountRemoved_thenDefaultAccountShouldBeActive() {
        val timeNow = System.currentTimeMillis()
        val storedAccounts = UstadAccounts("bob@$mockServerUrl",
                listOf(UmAccount(1, "bob", "", mockServerUrl)),
                mapOf("bob@$mockServerUrl" to timeNow - 5000))
        whenever(mockSystemImpl.getAppPref(eq(ACCOUNTS_PREFKEY), any())).thenReturn(
                Json.stringify(UstadAccounts.serializer(), storedAccounts))

        val accountManager = UstadAccountManager(mockSystemImpl, appContext, mockDbOpener)

        accountManager.removeAccount(storedAccounts.storedAccounts[0])

        Assert.assertEquals("After removing the only stored account, the default account is now active",
            "guest@http://app.ustadmobile.com/", accountManager.activeAccount.userAtServer)
    }

    @Test
    fun givenValidRegistrationRequest_whenNewAccountRequested_thenShouldBeRequestedOnServerAndActive() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, mockDbOpener)

        val personToRegister = Person().apply {
            firstNames = "Mary"
            lastName = "Poppins"
            phoneNum = "1234567"
            emailAddr = "mary@email.com"
            username = "mary"
        }

        val accountResponse = UmAccount(42L, "mary", "", null)
        mockWebServer.enqueue(MockResponse()
                .setBody(Json.stringify(UmAccount.serializer(), accountResponse))
                .addHeader("Content-Type", "application/json; charset=utf-8"))

        val accountRegistered = runBlocking {
            accountManager.register(personToRegister, "password", mockServerUrl)
        }

        Assert.assertEquals("Active account is the account registered",
                "mary@$mockServerUrl", accountManager.activeAccount.userAtServer)

    }

    fun givenInvalidRegistrationRequest_whenNewAccountRequested_thenShouldThrowException() {

    }

}