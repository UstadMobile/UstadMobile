package com.ustadmobile.core.impl

import app.cash.turbine.test
import com.google.gson.Gson
import com.ustadmobile.core.account.*
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_SESSION_PREFKEY
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.userAtServer
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.message.DoorMessage
import com.ustadmobile.door.replication.DoorReplicationEntity
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.*
import java.io.ByteArrayOutputStream
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds


class UstadAccountManagerTest : AbstractMainDispatcherTest(){

    inner class AccountManagerMockServerDispatcher : Dispatcher() {

        var authUmAccount = UmAccount(42L, "bob", "", mockServerUrl)

        var registerUmAccount = authUmAccount

        var authResponseCode = 200

        private val _registrationRequestsReceived = mutableListOf<RegisterRequest>()

        val registerRequestsReceived: List<RegisterRequest>
            get() = _registrationRequestsReceived.toList()

        private fun UmAccount.toMockResponse() : MockResponse{
            return MockResponse()
                .setBody(Json.encodeToString(UmAccount.serializer(), this))
                .setResponseCode(authResponseCode)
                .addHeader("Content-Type", "application/json; charset=utf-8")
        }

        override fun dispatch(request: RecordedRequest): MockResponse {
            return when {
                request.path?.startsWith("/auth/login") == true -> {
                    authUmAccount.toMockResponse()
                }
                request.path?.startsWith("/auth/register") == true -> {
                    val registerRequest = json.decodeFromString(RegisterRequest.serializer(),
                        request.body.readString(Charsets.UTF_8))
                    _registrationRequestsReceived.add(registerRequest)
                    registerUmAccount.toMockResponse()
                }

                request.path?.startsWith("/UmAppDatabase/SiteDao/getSiteAsync") == true -> {
                    MockResponse()
                        .setBody(Json.encodeToString(Site.serializer(), Site().apply {
                            authSalt = randomString(20)
                        }))
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                }

                request.path?.startsWith("/auth/person") == true -> {
                    authUmAccount.toMockResponse()
                }

                else -> MockResponse().setResponseCode(404)
            }
        }
    }

    lateinit var mockSystemImpl: UstadMobileSystemImpl

    lateinit var apiUrlConfig: ApiUrlConfig

    val appContext = Any()

    lateinit var mockWebServer: MockWebServer

    lateinit var mockDispatcher:AccountManagerMockServerDispatcher

    lateinit var mockServerUrl: String

    private lateinit var di: DI

    private lateinit var endpointScope: EndpointScope

    private lateinit var json: Json

    private lateinit var repo: UmAppDatabase

    private lateinit var db: UmAppDatabase

    @Before
    fun setup() {
        apiUrlConfig = ApiUrlConfig("http://app.ustadmobile.com/")
        mockSystemImpl = mock { }

        mockWebServer = MockWebServer().also {
            it.start()
            mockServerUrl = it.url("/").toString()
            mockDispatcher = AccountManagerMockServerDispatcher()
            it.dispatcher = mockDispatcher
        }

        endpointScope = EndpointScope()

        json = Json { encodeDefaults = true }

        val nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE),
            randomUuid().toString())

        di = DI {
            bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
                nodeIdAndAuth
            }

            bind<ApiUrlConfig>() with singleton { apiUrlConfig }

            bind<Pbkdf2Params>() with singleton {
                Pbkdf2Params(iterations = 10000, keyLength = 512)
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                DatabaseBuilder.databaseBuilder(
                    UmAppDatabase::class, "jdbc:sqlite:build/tmp/$dbName.sqlite",
                        nodeId = nodeIdAndAuth.nodeId)
                    .addSyncCallback(nodeIdAndAuth)
                    .build()
                    .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
                spy(instance<UmAppDatabase>(tag = DoorTag.TAG_DB).asRepository(
                    RepositoryConfig.repositoryConfig(
                        Any(), context.url, nodeIdAndAuth.nodeId, nodeIdAndAuth.auth,
                        instance(), instance()
                    ) {

                    }
                ))
            }

            bind<OkHttpClient>() with singleton {
                OkHttpClient().newBuilder().build()
            }

            bind<HttpClient>() with singleton {
                HttpClient(OkHttp) {
                    install(ContentNegotiation) {
                        json(json = instance())
                    }
                    install(HttpTimeout)
                }
            }

            bind<Json>() with singleton {
                Json {
                    encodeDefaults = true
                    ignoreUnknownKeys = true
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
        db = di.on(Endpoint(mockServerUrl)).direct.instance(tag = DoorTag.TAG_DB)
    }

    private fun Person.createTestUserSession(repo: UmAppDatabase): UserSession {
        val nodeClientId = (repo as DoorDatabaseRepository).config.nodeId
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
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)
        val activeAccount = accountManager.currentAccount
        Assert.assertEquals("Initial account has personUid = 0", 0L, activeAccount.personUid)
        Assert.assertEquals("Initial account uses default apiUrl",
                "http://app.ustadmobile.com/", activeAccount.endpointUrl)
    }



    @Test
    fun givenNoSiteOrPersonInDb_whenLoginCalledForfirstLogin_shouldInitLogin() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)
        db.execSQLBatch("DELETE FROM Site", "DELETE FROM Person")


        runBlocking {
            accountManager.login("bob", "password", mockServerUrl)
        }

        Assert.assertEquals("Active account is the newly logged in account",
            mockDispatcher.authUmAccount.userAtServer,
            accountManager.currentAccount.userAtServer)

        runBlocking {
            accountManager.activeUserSessionsFlow.filter {
                it.size == 1 && it.first().person.username == "bob"
            }.test(timeout = 5.seconds) {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenValidLoginCredentials_whenLoginCalledForFirstLogin_shouldInitLogin() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)
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

        mockDispatcher.authUmAccount = loggedInAccount

        runBlocking {
            accountManager.login("bob", "password", mockServerUrl)
        }

        Assert.assertEquals("Active account is the newly logged in account",
                loggedInAccount.userAtServer, accountManager.currentAccount.userAtServer)

        runBlocking {
            accountManager.activeUserSessionsFlow
                .filter { it.size == 1 && it.first().person.username == "bob" }
                .test(
                    timeout = 5.seconds,
                    name = "There is one stored account that matches the account logged in"
                ) {
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }
        }

        argumentCaptor<String> {
            verify(mockSystemImpl).setAppPref(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY), capture())
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
                getAppPref(eq(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY))
            }.thenReturn(mockServerUrl)
            on {
                getAppPref(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY))
            }.thenReturn(json.encodeToString(UserSessionWithPersonAndEndpoint.serializer(),
                savedSessionWithPersonAndEndpoint))
        }

        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)

        val loggedInAccount = UmAccount(42L, "bob", "", mockServerUrl)
        mockDispatcher.authUmAccount = loggedInAccount

        runBlocking {
            accountManager.login("bob", "password", mockServerUrl)
        }

        runBlocking {
            accountManager.activeUserSessionsFlow
                .filter { it.size == 2 }
                .test(timeout = 5.seconds, name = "There are two stored accounts") {
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }
        }

        Assert.assertEquals("Active account is the newly logged in account",
                loggedInAccount.userAtServer, accountManager.currentAccount.userAtServer)

        argumentCaptor<String> {
            verify(mockSystemImpl).setAppPref(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY), capture())
            val accountSaved = json.decodeFromString(UserSessionWithPersonAndEndpoint.serializer(),
                firstValue)
            Assert.assertEquals("Saved account as active", loggedInAccount.personUid,
                    accountSaved.person.personUid)
        }
    }

    @Test
    fun givenInvalidLoginCredentials_whenLoginCalled_thenShouldThrowException() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)

        mockDispatcher.authResponseCode = 403

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
    fun givenAccountRequiresParentalConsent_whenLoginCalled_thenShouldThrowException() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)

        mockDispatcher.authResponseCode = 424

        var exception: Exception? = null
        try {
            runBlocking {
                accountManager.login("bob", "pass", mockServerUrl)
            }
        }catch(e: Exception) {
            exception = e
        }

        Assert.assertTrue(
            "Got ConsentNotGrantedException when parental consent not granted",
            exception is ConsentNotGrantedException)
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
                getAppPref(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY))
            }.thenReturn(
                json.encodeToString(UserSessionWithPersonAndEndpoint.serializer(),
                    activeUserSession))
            on {
                getAppPref(eq(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY))
            }.thenReturn(mockServerUrl)
            on {
                getAppPref(eq(ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION))
            }.thenReturn(
                json.encodeToString(ListSerializer(String.serializer()), listOf(mockServerUrl)))
        }


        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)
        accountManager.currentUserSession = joePersonSessionWithPersonAndEndpoint

        Assert.assertEquals("Active account is updated when calling setActiveAccount",
                "joe@$mockServerUrl", accountManager.currentAccount.userAtServer)

        runBlocking {
            accountManager.activeUserSessionsFlow
                .filter { it.size == 2 }
                .test(timeout = 5.seconds, name = "AccountManager still has both accounts stored") {
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }
        }

        argumentCaptor<String> {
            verify(mockSystemImpl).setAppPref(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY), capture())
            val accountSaved = json.decodeFromString(UserSessionWithPersonAndEndpoint.serializer(),
                firstValue)
            Assert.assertEquals("Saved account as active matches username",
                accountSaved.person.username, "joe")
            Assert.assertEquals("Saved account on expected endpoint", mockServerUrl,
                accountSaved.endpoint.url)
        }
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
        mockDispatcher.registerUmAccount = accountResponse

        runBlocking {
            accountManager.register(personToRegister, mockServerUrl)
        }

        Assert.assertEquals("Active account is the account registered",
                "mary@$mockServerUrl", accountManager.currentAccount.userAtServer)

        val registerRequest = mockDispatcher.registerRequestsReceived.first()
        Assert.assertEquals("Got expected register request", "mary",
            registerRequest.person.username)
    }


    @Test
    fun givenActiveAccount_whenIncomingReplicationMakesUserSessionInactive_thenShouldEndSession() {
        val accountManager = UstadAccountManager(mockSystemImpl, appContext, di)
        val activeAccountPerson = Person().apply {
            firstNames = "Mary"
            lastName = "Poppins"
            phoneNum = "1234567"
            emailAddr = "mary@email.com"
            username = "mary"
        }

        runBlocking {
            db.insertPersonAndGroup(activeAccountPerson)
        }

        val session = runBlocking {
            accountManager.addSession(activeAccountPerson, mockServerUrl, "123")
        }
        accountManager.currentUserSession = session

        val deactivatedSession = Json.decodeFromString(
            UserSession.serializer(),
            Json.encodeToString(UserSession.serializer(), session.userSession)).also {
                it.usStatus = UserSession.STATUS_LOGGED_OUT
        }

        val deactivatedSessionObject = Json.encodeToJsonElement(UserSession.serializer(),
            deactivatedSession)


        runBlocking {
            accountManager.onIncomingMessageReceived(DoorMessage(
                what = DoorMessage.WHAT_REPLICATION,
                fromNode = 1L,
                toNode = 2L,
                replications = listOf(
                    DoorReplicationEntity(
                        tableId = UserSession.TABLE_ID,
                        orUid = 1L,
                        entity = deactivatedSessionObject.jsonObject
                    )
                )
            ))
        }

        //Current session should be replaced with guest session
        assertEquals(null, accountManager.currentUserSession.person.username)
        assertEquals("Guest", accountManager.currentUserSession.person.firstNames)
    }

}