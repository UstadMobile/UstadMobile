package com.ustadmobile.core.test.clientservertest

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.domain.assignment.submitmark.SubmitMarkUseCase
import com.ustadmobile.core.domain.assignment.submittername.GetAssignmentSubmitterNameUseCase
import com.ustadmobile.core.domain.xapi.coursegroup.CreateXapiGroupForCourseGroupUseCase
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.domain.xxhash.XXStringHasherCommonJvm
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.impl.nav.NavResultReturnerImpl
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.http.DoorHttpServerConfig
import com.ustadmobile.door.log.NapierDoorLogger
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.rest.InsertDefaultSiteCallback
import com.ustadmobile.lib.rest.personAuthRegisterRoute
import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationServer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on
import org.kodein.di.provider
import org.kodein.di.registerContextTranslator
import org.kodein.di.scoped
import org.kodein.di.singleton
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.nio.file.Files
import java.util.Properties
import kotlin.random.Random

private fun clientServerCommonDiModule(
    endpointScope: EndpointScope,
    baseTmpDir: File,
    db: UmAppDatabase,
    name: String,
) = DI.Module(name) {
    bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
        NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
    }

    bind<Pbkdf2Params>() with singleton {
        Pbkdf2Params()
    }

    bind<AuthManager>() with scoped(endpointScope).singleton {
        AuthManager(context, di)
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton() {
        db
    }

    bind<Settings>() with  singleton {
        PropertiesSettings(
            delegate = Properties(),
            onModify = {
                //Do nothing
            }
        )
    }

    bind<SupportedLanguagesConfig>() with singleton {
        SupportedLanguagesConfig(
            systemLocales = listOf("en-US"),
            settings = instance(),
        )
    }

    bind<UstadMobileSystemImpl>() with singleton {
        UstadMobileSystemImpl(
            settings = instance(),
            langConfig = instance()
        )
    }

    bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
        XmlPullParserFactory.newInstance().also {
            it.isNamespaceAware = true
        }
    }

    bind<UstadAccountManager>() with singleton {
        UstadAccountManager(settings = instance(), di)
    }

    bind<ApiUrlConfig>() with singleton {
        ApiUrlConfig(presetApiUrl = null)
    }
}


/**
 * Run an integration test that requires a server and client(s). This is intended to be used to test
 * multi-user components e.g. where a student submits an assignment, and then the teacher grades
 * the assignment, where one user posts a discussion post and then another user replies, etc.
 */
fun clientServerIntegrationTest(
    numClients: Int = 2,
    adminUsername: String = "admin",
    adminPassword: String = "admin",
    block: suspend ClientServerIntegrationTestContext.() -> Unit
) {
    val json = Json { encodeDefaults = true }
    val doorServerConfig = DoorHttpServerConfig(json, logger = NapierDoorLogger())
    val serverDb: UmAppDatabase = DatabaseBuilder.databaseBuilder(
        UmAppDatabase::class,
        "jdbc:sqlite::memory:", nodeId = 1L)
        .name("serverdb")
        .addCallback(InsertDefaultSiteCallback())
        .build()

    val okHttpClient = OkHttpClient.Builder().build()
    val httpClient = HttpClient {
        install(ContentNegotiationClient) {
            json(json = json)
        }
    }

    val mockSnackBarDispatcher: SnackBarDispatcher = mock { }
    val tempDir = Files.createTempDirectory("client-server-integration-test").toFile()

    val serverEndpointScope = EndpointScope()
    val serverDi = DI {
        bind<OkHttpClient>() with singleton { okHttpClient }
        bind<HttpClient>() with singleton { httpClient }
        bind<Json>() with singleton { json }
        import(clientServerCommonDiModule(
            endpointScope = serverEndpointScope,
            baseTmpDir = tempDir,
            db = serverDb,
            name = "Server-DI"
        ))

        registerContextTranslator { call: ApplicationCall ->
            Endpoint("localhost")
        }

        onReady {
            val localhostEndpoint = Endpoint("localhost")
            val authManager: AuthManager = on(localhostEndpoint).instance()
            val adminPerson = Person(username = adminUsername, firstNames = "Admin", lastName = "User")
            runBlocking {
                val adminPersonUid = serverDb.insertPersonAndGroup(adminPerson).personUid
                authManager.setAuth(adminPersonUid, adminPassword)
            }
        }
    }

    val server = embeddedServer(Netty, 8094) {
        install(ContentNegotiationServer) {
            json(json = json)
        }

        di {
            extend(serverDi)
        }
        routing {
            personAuthRegisterRoute()

            route("UmAppDatabase") {
                UmAppDatabase_KtorRoute(doorServerConfig) { serverDb }
            }
        }
    }
    server.start()
    val serverUrl = "http://localhost:8094/"

    val clients = (0..numClients).map {
        val clientEndpointScope = EndpointScope()
        val clientDb = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class,
            "jdbc:sqlite::memory:", nodeId = it.toLong())
            .name("client$it")
            .logger(NapierDoorLogger())
            .build()
        val clientRepo = clientDb.asRepository(
            RepositoryConfig.repositoryConfig(
                context = Any(),
                endpoint = "${serverUrl}UmAppDatabase/",
                nodeId = it.toLong(),
                auth = "auth$it",
                httpClient = httpClient,
                okHttpClient = okHttpClient,
                json = json,
            ))

        ClientServerTestClient(
            clientNum = it,
            di = DI {
                bind<OkHttpClient>() with singleton { okHttpClient }
                bind<HttpClient>() with singleton { httpClient }
                bind<Json>() with singleton { json }
                import(clientServerCommonDiModule(
                    clientEndpointScope,
                    baseTmpDir = File(tempDir, "client$it").also { it.mkdirs() },
                    db = clientDb,
                    name = "Client $it-DI"
                ))
                bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(clientEndpointScope).singleton {
                    clientRepo
                }

                bind<NavResultReturner>() with singleton {
                    spy(NavResultReturnerImpl())
                }

                bind<SnackBarDispatcher>() with singleton {
                    mockSnackBarDispatcher
                }

                bind<GetAssignmentSubmitterNameUseCase>() with scoped(clientEndpointScope).singleton {
                    GetAssignmentSubmitterNameUseCase(clientRepo, instance())
                }

                bind<SubmitMarkUseCase>() with scoped(clientEndpointScope).provider {
                    SubmitMarkUseCase(
                        repo = clientRepo,
                        endpoint = context,
                        createXapiGroupUseCase = instance(),
                        xapiStatementResource = mock { },
                    )
                }

                bind<CreateXapiGroupForCourseGroupUseCase>() with scoped(clientEndpointScope).provider {
                    CreateXapiGroupForCourseGroupUseCase(
                        repo = clientRepo,
                        endpoint = context,
                        stringHasher = instance(),
                    )
                }

                bind<XXStringHasher>() with singleton {
                    XXStringHasherCommonJvm()
                }

                registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
            },
            serverDi = serverDi,
            diEndpointScope = clientEndpointScope,
            serverUrl = "http://localhost:8094/"
        )
    }

    val testContext = ClientServerIntegrationTestContext(
        serverDi = serverDi,
        serverDb = serverDb,
        clients = clients,
    )


    try {
        runBlocking {
            block(testContext)
        }
    }finally {
        clients.forEach {
            it.close()
        }

        server.stop()
        serverDb.close()
        httpClient.close()
        tempDir.deleteRecursively()
    }

}