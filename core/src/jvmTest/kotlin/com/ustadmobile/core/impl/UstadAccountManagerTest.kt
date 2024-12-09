package com.ustadmobile.core.impl

import app.cash.turbine.test
import com.google.gson.Gson
import com.russhwolf.settings.Settings
import com.ustadmobile.core.account.*
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_SESSION_PREFKEY
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION
import com.ustadmobile.core.db.UmAppDataLayer
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.userAtServer
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorConstants
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
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
                        .setBody(
                            Json.encodeToString(
                                DoorMessage.serializer(),
                                DoorMessage(
                                    what = DoorMessage.WHAT_REPLICATION_PULL,
                                    fromNode = 1L,
                                    toNode = 2L,
                                    replications = listOf(
                                        DoorReplicationEntity(
                                            tableId = Site.TABLE_ID,
                                            orUid = 1L,
                                            entity = json.encodeToJsonElement(
                                                Site.serializer(),
                                                Site().apply {
                                                    siteUid = 10042
                                                    siteLct = systemTimeInMillis()
                                                    authSalt = randomString(20)
                                                }
                                            ).jsonObject
                                        )
                                    )
                                )
                            )
                        )
                        .setResponseCode(200)
                        .addHeader(DoorConstants.HEADER_NODE_ID, "1")
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                }

                request.path?.startsWith("/auth/person") == true -> {
                    authUmAccount.toMockResponse()
                }

                else -> MockResponse().setResponseCode(404)
            }
        }
    }

    lateinit var mockSettings: Settings

    lateinit var systemUrlConfig: SystemUrlConfig

    lateinit var mockWebServer: MockWebServer

    lateinit var mockDispatcher:AccountManagerMockServerDispatcher

    lateinit var mockServerUrl: String

    private lateinit var di: DI

    private lateinit var learningSpaceScope: LearningSpaceScope

    private lateinit var json: Json

    private lateinit var repo: UmAppDatabase

    private lateinit var db: UmAppDatabase

    @Before
    fun setup() {
        systemUrlConfig = SystemUrlConfig(
            "http://app.ustadmobile.com/", "app.ustadmobile.com",
            presetLearningSpaceUrl = "http://app.ustadmobile.com/"
        )
        mockSettings = mock { }

        mockWebServer = MockWebServer().also {
            it.start()
            mockServerUrl = it.url("/").toString()
            mockDispatcher = AccountManagerMockServerDispatcher()
            it.dispatcher = mockDispatcher
        }

        learningSpaceScope = LearningSpaceScope()

        json = Json { encodeDefaults = true }

        val nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE),
            randomUuid().toString())

        di = DI {
            bind<NodeIdAndAuth>() with scoped(learningSpaceScope).singleton {
                nodeIdAndAuth
            }

            bind<AuthManager>() with scoped(learningSpaceScope).singleton {
                AuthManager(context, di)
            }

            bind<SystemUrlConfig>() with singleton { systemUrlConfig }

            bind<Pbkdf2Params>() with singleton {
                Pbkdf2Params(iterations = 10000, keyLength = 512)
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(learningSpaceScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                DatabaseBuilder.databaseBuilder(
                    UmAppDatabase::class, "jdbc:sqlite:build/tmp/$dbName.sqlite",
                        nodeId = nodeIdAndAuth.nodeId)
                    .addSyncCallback(nodeIdAndAuth)
                    .build()
                    .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
            }

            bind<UmAppDataLayer>() with scoped(learningSpaceScope).singleton {
                val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)

                val repo = spy(
                    db.asRepository(
                        RepositoryConfig.repositoryConfig(
                            Any(), "${context.url}UmAppDatabase/", nodeIdAndAuth.nodeId, nodeIdAndAuth.auth,
                            instance(), instance()
                        )
                    )
                )

                UmAppDataLayer(localDb = db, repository = repo)
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


        repo = di.on(LearningSpace(mockServerUrl)).direct.instance<UmAppDataLayer>().requireRepository()
        repo.siteDao().insert(Site().apply {
            siteUid = 10042
            siteLct = systemTimeInMillis()
            authSalt = randomString(20)
        })
        db = di.on(LearningSpace(mockServerUrl)).direct.instance(tag = DoorTag.TAG_DB)
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
        val accountManager = UstadAccountManager(mockSettings, di)
        val activeAccount = accountManager.currentAccount
        Assert.assertEquals("Initial account has personUid = 0", 0L, activeAccount.personUid)
        Assert.assertEquals("Initial account uses default presetLsUrl",
                "http://app.ustadmobile.com/", activeAccount.endpointUrl)
    }

    @Test
    fun givenValidLoginCredentials_whenLoginCalledForFirstLogin_shouldInitLogin() {
        val accountManager = UstadAccountManager(mockSettings, di)
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
            verify(mockSettings).putString(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY), capture())
            val accountSaved = json.decodeFromString(UserSessionWithPersonAndLearningSpace.serializer(),
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
                usUid = repo.userSessionDao().insertSession(this)
            }
        }

        val savedSessionWithPersonAndEndpoint = UserSessionWithPersonAndLearningSpace(savedSession,
            savedPerson, LearningSpace(mockServerUrl))

        mockSettings.stub {
            on {
                getStringOrNull(eq(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY))
            }.thenReturn(mockServerUrl)
            on {
                getStringOrNull(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY))
            }.thenReturn(json.encodeToString(UserSessionWithPersonAndLearningSpace.serializer(),
                savedSessionWithPersonAndEndpoint))
        }

        val accountManager = UstadAccountManager(mockSettings, di)

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
            verify(mockSettings).putString(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY), capture())
            val accountSaved = json.decodeFromString(UserSessionWithPersonAndLearningSpace.serializer(),
                firstValue)
            Assert.assertEquals("Saved account as active", loggedInAccount.personUid,
                    accountSaved.person.personUid)
        }
    }

    @Test
    fun givenInvalidLoginCredentials_whenLoginCalled_thenShouldThrowException() {
        val accountManager = UstadAccountManager(mockSettings, di)

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
        val accountManager = UstadAccountManager(mockSettings, di)

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
        val accountManager = UstadAccountManager(mockSettings, di)

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
            bobPersonSession.usUid = repo.userSessionDao().insertSession(bobPersonSession)
            joePersonSession.usUid = repo.userSessionDao().insertSession(joePersonSession)
        }

        val activeUserSession = UserSessionWithPersonAndLearningSpace(bobPersonSession, bobPerson,
            LearningSpace(mockServerUrl))

        val joePersonSessionWithPersonAndEndpoint = UserSessionWithPersonAndLearningSpace(joePersonSession,
            joePerson, LearningSpace(mockServerUrl))

        mockSettings.stub {
            on {
                getStringOrNull(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY))
            }.thenReturn(
                json.encodeToString(UserSessionWithPersonAndLearningSpace.serializer(),
                    activeUserSession))
            on {
                getStringOrNull(eq(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY))
            }.thenReturn(mockServerUrl)
            on {
                getStringOrNull(eq(ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION))
            }.thenReturn(
                json.encodeToString(ListSerializer(String.serializer()), listOf(mockServerUrl)))
        }


        val accountManager = UstadAccountManager(mockSettings, di)
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
            verify(mockSettings).putString(eq(ACCOUNTS_ACTIVE_SESSION_PREFKEY), capture())
            val accountSaved = json.decodeFromString(UserSessionWithPersonAndLearningSpace.serializer(),
                firstValue)
            Assert.assertEquals("Saved account as active matches username",
                accountSaved.person.username, "joe")
            Assert.assertEquals("Saved account on expected endpoint", mockServerUrl,
                accountSaved.learningSpace.url)
        }
    }

    @Test
    fun givenValidRegistrationRequest_whenNewAccountRequested_thenShouldBeRequestedOnServerAndActive() {
        val accountManager = UstadAccountManager(mockSettings, di)

        val personToRegister = Person().apply {
            firstNames = "Mary"
            lastName = "Poppins"
            phoneNum = "1234567"
            emailAddr = "mary@email.com"
            username = "mary"
        }

        val accountResponse = UmAccount(42L, "mary", "", "")
        mockDispatcher.registerUmAccount = accountResponse

        runBlocking {
            accountManager.register(personToRegister, password = "password", mockServerUrl)
        }

        Assert.assertEquals("Active account is the account registered",
                "mary@$mockServerUrl", accountManager.currentAccount.userAtServer)

        val registerRequest = mockDispatcher.registerRequestsReceived.first()
        Assert.assertEquals("Got expected register request", "mary",
            registerRequest.person.username)
        runBlocking {
            assertNotNull(db.personDao().findByUsernameAsync(registerRequest.person.username ?: ""))
        }

    }


    @Test
    fun givenActiveAccount_whenIncomingReplicationMakesUserSessionInactive_thenShouldEndSession() {
        val accountManager = UstadAccountManager(mockSettings, di)
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
                what = DoorMessage.WHAT_REPLICATION_PULL,
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