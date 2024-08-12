package com.ustadmobile.core.util

import com.google.gson.Gson
import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import com.ustadmobile.core.account.*
import org.mockito.kotlin.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.util.test.nav.TestUstadNavController
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import okhttp3.OkHttpClient
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.nio.file.Files
import kotlin.random.Random
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import java.util.Properties
import java.util.concurrent.CopyOnWriteArrayList

fun DI.onActiveAccount(): DI {
    val accountManager: UstadAccountManager by instance()
    return on(accountManager.activeEndpoint)
}

fun DI.onActiveAccountDirect() = direct.on(direct.instance<UstadAccountManager>().activeEndpoint)

fun DI.activeDbInstance() = onActiveAccount().instance<UmAppDatabase>(tag = DoorTag.TAG_DB)

fun DI.activeRepoInstance() = onActiveAccount().instance<UmAppDatabase>(tag = DoorTag.TAG_REPO)

fun DI.directActiveDbInstance() = onActiveAccountDirect().instance<UmAppDatabase>(tag = DoorTag.TAG_DB)

fun DI.directActiveRepoInstance() = onActiveAccountDirect().instance<UmAppDatabase>(tag = DoorTag.TAG_REPO)

/**
 * UstadTestRule makes a fresh almost-ready-to-go DI module for each test run. The DB and SystemImpl
 * are wrapped with spy, so they can be used to in verify calls.
 *
 * Simply override the built in bindings if required for specific tests
 */
class UstadTestRule(): TestWatcher() {

    lateinit var endpointScope: EndpointScope

    private lateinit var systemImplSpy: UstadMobileSystemImpl

    lateinit var diModule: DI.Module

    lateinit var httpClient: HttpClient

    lateinit var okHttpClient: OkHttpClient

    lateinit var tempFolder: File

    private val xppFactory = XmlPullParserFactory.newInstance().also {
        it.isNamespaceAware = true
    }

    private val dbsToClose = CopyOnWriteArrayList<UmAppDatabase>()

    @OptIn(ExperimentalXmlUtilApi::class)
    override fun starting(description: Description) {
        tempFolder = Files.createTempDirectory("ustadtestrule").toFile()

        endpointScope = EndpointScope()
        val settings: Settings = PropertiesSettings(
            delegate = Properties(),
            onModify = {
                //do nothing
            }
        )

        val langConfig = SupportedLanguagesConfig(
            systemLocales = kotlin.collections.listOf("en-US"),
            settings = settings,
        )

        systemImplSpy = spy(UstadMobileSystemImpl(settings, langConfig))
        //coroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

        okHttpClient = OkHttpClient.Builder().build()

        httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                gson()
            }
            install(HttpTimeout)

            engine {
                preconfigured = okHttpClient
            }
        }


        diModule = DI.Module("UstadTestRule") {
            bind<UstadMobileSystemImpl>() with singleton { systemImplSpy }
            bind<ApiUrlConfig>() with singleton {
                ApiUrlConfig(presetApiUrl = null)
            }
            bind<UstadAccountManager>() with singleton {
                UstadAccountManager(instance(), di)
            }
            bind<Json>() with singleton {
                Json { encodeDefaults = true }
            }

            bind<XapiJson>() with singleton { XapiJson() }

            bind<Settings>() with singleton {
                settings
            }

            bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
                NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                spy(DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
                        "jdbc:sqlite:build/tmp/$dbName.sqlite", nodeId = nodeIdAndAuth.nodeId)
                    .addMigrations(*migrationList().toTypedArray())
                    .addSyncCallback(nodeIdAndAuth)
                    .build()
                    .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)).also {
                        dbsToClose.add(it)
                }
            }


            bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                spy(instance<UmAppDatabase>(tag = DoorTag.TAG_DB).asRepository(repositoryConfig(
                    Any(), UMFileUtil.joinPaths(context.url, "UmAppDatabase/"), nodeIdAndAuth.nodeId,
                    nodeIdAndAuth.auth, instance(), instance()
                ) {

                })
                ).also {
                    it.siteDao().insert(Site().apply {
                        siteName = "Test"
                        authSalt = randomString(16)
                    })
                    dbsToClose.add(it)
                }
            }

            bind<ClientId>(tag = UstadMobileSystemCommon.TAG_CLIENT_ID) with scoped(EndpointScope.Default).singleton {
                val repo: UmAppDatabase by di.on(Endpoint(context.url)).instance(tag = DoorTag.TAG_REPO)
                val nodeId = (repo as? DoorDatabaseRepository)?.config?.nodeId
                    ?: throw IllegalStateException("Could not open repo for endpoint ${context.url}")
                ClientId(nodeId.toInt())
            }

            bind<Gson>() with singleton {
                Gson()
            }

            bind<OkHttpClient>() with singleton {
                okHttpClient
            }

            bind<HttpClient>() with singleton {
                httpClient
            }

            bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
                xppFactory
            }

            bind<XmlPullParserFactory>(tag = DiTag.XPP_FACTORY_NSUNAWARE) with singleton {
                XmlPullParserFactory.newInstance()
            }

            bind<UstadNavController>() with singleton {
                spy(TestUstadNavController())
            }

            bind<Pbkdf2Params>() with singleton {
                Pbkdf2Params(iterations = 10000, keyLength = 512)
            }

            bind<XML>() with singleton {
                XML {
                    defaultPolicy {
                        unknownChildHandler  = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                    }
                }
            }

            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
        }
    }

    override fun finished(description: Description) {
        httpClient.close()
        tempFolder.deleteRecursively()
        dbsToClose.mapNotNull { it as? DoorDatabaseRepository }.forEach { it.close() }
        dbsToClose.filter { it !is DoorDatabaseRepository }.forEach { it.close() }
        dbsToClose.clear()
    }

}