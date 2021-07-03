package com.ustadmobile.core.impl

import com.google.gson.Gson
import com.ustadmobile.core.account.*
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_SESSION_PREFKEY
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.userAtServer
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.door.ext.clearAllTablesAndResetSync
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.*
import java.io.ByteArrayOutputStream
import javax.naming.InitialContext
import kotlin.random.Random

class UstadAccountManagerTest {

    lateinit var mockSystemImpl: UstadMobileSystemImpl

    val appContext = Any()

    lateinit var mockWebServer: MockWebServer

    lateinit var mockServerUrl: String

    private lateinit var di: DI

    private lateinit var endpointScope: EndpointScope

    private lateinit var json: Json

    private lateinit var repo: UmAppDatabase

    private lateinit var db: UmAppDatabase

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

        val nodeIdAndAuth = NodeIdAndAuth(Random.nextInt(0, Int.MAX_VALUE),
            randomUuid().toString())

        di = DI {
            bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
                nodeIdAndAuth
            }

            bind<Pbkdf2Params>() with singleton {
                Pbkdf2Params(iterations = 10000, keyLength = 512)
            }

            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(UmAppDatabase.getInstance(Any(), dbName, nodeIdAndAuth).also {
                    it.clearAllTablesAndResetSync(nodeIdAndAuth.nodeId, isPrimary = false)
                })
            }

            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(endpointScope).singleton {
                spy(instance<UmAppDatabase>(tag = UmAppDatabase.TAG_DB).asRepository(
                    RepositoryConfig.repositoryConfig(
                        Any(), context.url, nodeIdAndAuth.nodeId, nodeIdAndAuth.auth,
                        instance(), instance()
                    )
                ))
            }

            bind<OkHttpClient>() with singleton {
                OkHttpClient().newBuilder().build()
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


        repo = di.on(Endpoint(mockServerUrl)).direct.instance(tag = DoorTag.TAG_REPO)
        repo.siteDao.insert(Site().apply {
            authSalt = randomString(20)
        })
    }

    private fun Person.createTestUserSession(repo: UmAppDatabase): UserSession {
        val nodeClientId = (repo as DoorDatabaseSyncRepository).clientId
        return UserSession().apply {
            usStartTime = systemTimeInMillis()
            usClientNodeId = nodeClientId
            usSessionType = UserSession.TYPE_STANDARD
            usStatus = UserSession.STATUS_ACTIVE
            usPersonUid = this@createTestUserSession.personUid
        }
    }

    @Test
    fun givenNoUserInPrefKeys_whenInitialized_shouldInitGuestAccountOnDefaultServer() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, endpointScope, di)
        val activeAccount = accountManager.activeAccount
        Assert.assertEquals("Initial account has personUid = 0", 0L, activeAccount.personUid)
        Assert.assertEquals("Initial account uses default apiUrl",
                "http://app.ustadmobile.com/", activeAccount.endpointUrl)
    }

    @Test
    fun givenValidLoginCredentials_whenLoginCalledForFirstLogin_shouldInitLogin() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, endpointScope, di)
        val loggedInAccount = UmAccount(42L, "bob", "",
            mockServerUrl)

        runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                personUid = 42L
                firstNames = "bob"
                lastName = "jones"
                username = "bob"
            })
        }

        mockWebServer.enqueueValidAccountResponse(loggedInAccount)

        runBlocking {
            accountManager.login("bob", "password", mockServerUrl,
                    replaceActiveAccount = true)
        }

        Assert.assertEquals("Active account is the newly logged in account",
                loggedInAccount.userAtServer, accountManager.activeAccount.userAtServer)

        val storedAccounts = runBlocking {
            accountManager.storedAccountsLive.waitUntil { it.size == 1 }
        }
        Assert.assertEquals("There is one stored account", 1,
                storedAccounts?.getValue()?.size)
        argumentCaptor<String> {
            verify(mockSystemImpl).setAppPref(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY), capture(), any())
            val accountSaved = json.decodeFromString(UserSessionWithPersonAndEndpoint.serializer(),
                firstValue)
            Assert.assertEquals("Saved account as active", loggedInAccount.personUid,
                    accountSaved.person.personUid)
        }
    }

    @Test
    fun givenValidLoginCredentials_whenLoginCalledForSecondAccountOnSameServer_shouldAddAccount() {
        val savedAccount = UmAccount(50L, "joe", "", mockServerUrl)
        val savedPerson = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                username = "joe"
                personUid = 50
                firstNames = "Joe"
                lastName = "Doe"
            })
        }

        runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                username = "bob"
                personUid = 42L
                firstNames = "Bob"
                lastName = "Jones"
            })
        }

        val savedSession = runBlocking {
            savedPerson.createTestUserSession(repo).apply {
                usUid = repo.userSessionDao.insertSession(this)
            }
        }

        val savedSessionWithPersonAndEndpoint = UserSessionWithPersonAndEndpoint(savedSession,
            savedPerson, Endpoint(mockServerUrl))

        mockSystemImpl.stub {
            on {
                getAppPref(eq(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY), any())
            }.thenReturn(mockServerUrl)
            on {
                getAppPref(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY), any())
            }.thenReturn(json.encodeToString(UserSessionWithPersonAndEndpoint.serializer(),
                savedSessionWithPersonAndEndpoint))
        }

        val accountManager = UstadAccountManager(mockSystemImpl, appContext, endpointScope, di)

        val loggedInAccount = UmAccount(42L, "bob", "", mockServerUrl)
        mockWebServer.enqueueValidAccountResponse(loggedInAccount)

        runBlocking {
            accountManager.login("bob", "password", mockServerUrl, true)
        }

        val storedAccounts = runBlocking {
            accountManager.storedAccountsLive.waitUntil {
                it.size == 2
            }
        }
        Assert.assertEquals("There are two stored accounts", 2,
                storedAccounts.getValue()?.size)
        Assert.assertEquals("Active account is the newly logged in account",
                loggedInAccount.userAtServer, accountManager.activeAccount.userAtServer)

        argumentCaptor<String> {
            verify(mockSystemImpl).setAppPref(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY), capture(), any())
            val accountSaved = json.decodeFromString(UserSessionWithPersonAndEndpoint.serializer(),
                firstValue)
            Assert.assertEquals("Saved account as active", loggedInAccount.personUid,
                    accountSaved.person.personUid)
        }
    }

    @Test
    fun givenInvalidLoginCredentials_whenLoginCalled_thenShouldThrowException() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, endpointScope, di)

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
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, endpointScope, di)

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
        val bobPerson = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                firstNames = "Bob"
                lastName = "jones"
                username = "bob"
            })
        }

        val joePerson = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                firstNames = "Joe"
                lastName = "Jones"
                username = "joe"
            })
        }

        val bobPersonSession = bobPerson.createTestUserSession(repo)
        val joePersonSession = joePerson.createTestUserSession(repo)

        runBlocking {
            bobPersonSession.usUid = repo.userSessionDao.insertSession(bobPersonSession)
            joePersonSession.usUid = repo.userSessionDao.insertSession(joePersonSession)
        }

        val activeUserSession = UserSessionWithPersonAndEndpoint(bobPersonSession, bobPerson,
            Endpoint(mockServerUrl))

        val joePersonSessionWithPersonAndEndpoint = UserSessionWithPersonAndEndpoint(joePersonSession,
            joePerson, Endpoint(mockServerUrl))

        mockSystemImpl.stub {
            on {
                getAppPref(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY), any())
            }.thenReturn(
                json.encodeToString(UserSessionWithPersonAndEndpoint.serializer(),
                    activeUserSession))
            on {
                getAppPref(eq(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY), any())
            }.thenReturn(mockServerUrl)
            on {
                getAppPref(eq(ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION), any())
            }.thenReturn(
                json.encodeToString(ListSerializer(String.serializer()), listOf(mockServerUrl)))
        }


        val accountManager = UstadAccountManager(mockSystemImpl, appContext, endpointScope, di)
        accountManager.activeSession = joePersonSessionWithPersonAndEndpoint

        Assert.assertEquals("Active account is updated when calling setActiveAccount",
                "joe@$mockServerUrl", accountManager.activeAccount.userAtServer)
        runBlocking {
            accountManager.storedAccountsLive.waitUntil(5000) {
                it.size == 2
            }
        }

        Assert.assertEquals("AccountManager still has both accounts stored", 2,
            accountManager.storedAccountsLive.getValue()?.size)
        argumentCaptor<String> {
            verify(mockSystemImpl).setAppPref(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY), capture(), any())
            val accountSaved = json.decodeFromString(UserSessionWithPersonAndEndpoint.serializer(),
                firstValue)
            Assert.assertEquals("Saved account as active matches username",
                accountSaved.person.username, "joe")
            Assert.assertEquals("Saved account on expected endpoint", mockServerUrl,
                accountSaved.endpoint.url)
        }
    }

    //TODO: this
//    @Test
//    fun givenMultipleStoredAccounts_whenActiveAccountRemoved_thenLastUsedAccountShouldBeActive() {
//        val timeNow = System.currentTimeMillis()
//        val storedAccounts = UstadAccounts("bob@$mockServerUrl",
//                listOf(UmAccount(1, "bob", "", mockServerUrl),
//                        UmAccount(2, "joe", "", mockServerUrl),
//                        UmAccount(3, "harry", "", mockServerUrl)),
//                mapOf("bob@$mockServerUrl" to timeNow - 5000,
//                    "joe@$mockServerUrl" to timeNow - 15000,
//                    "harry@$mockServerUrl" to timeNow - 5000))
//        whenever(mockSystemImpl.getAppPref(eq(ACCOUNTS_PREFKEY), any())).thenReturn(
//                Json.encodeToString(UstadAccounts.serializer(), storedAccounts))
//
//        val accountManager = UstadAccountManager(mockSystemImpl, appContext, endpointScope, di)
//
//        accountManager.removeAccount(storedAccounts.storedAccounts[0])
//
//        Assert.assertEquals("Most recently used account after account that was removed is now active",
//            "harry@$mockServerUrl", accountManager.activeAccount.userAtServer)
//        argumentCaptor<String> {
//            verify(mockSystemImpl, atLeastOnce()).setAppPref(eq(ACCOUNTS_PREFKEY), capture(), any())
//            val accountSaved = json.decodeFromString(UstadAccounts.serializer(), lastValue)
//            Assert.assertEquals("Fallback account is saved as active acount", "harry@$mockServerUrl",
//                    accountSaved.currentAccount)
//        }
//    }
//

    @Test
    fun givenValidRegistrationRequest_whenNewAccountRequested_thenShouldBeRequestedOnServerAndActive() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, endpointScope, di)

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