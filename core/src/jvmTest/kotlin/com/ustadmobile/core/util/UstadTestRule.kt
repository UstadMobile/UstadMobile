package com.ustadmobile.core.util

import com.google.gson.Gson
import com.ustadmobile.core.account.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.RepSubscriptionInitListener
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.io.ext.siteDataSubDir
import com.ustadmobile.core.view.ContainerMounter
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
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.util.test.nav.TestUstadNavController
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.json.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.net.URL
import java.nio.file.Files
import javax.naming.InitialContext
import kotlin.random.Random
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import kotlinx.serialization.json.Json

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
class UstadTestRule(
    val repoReplicationSubscriptionEnabled: Boolean = false,
    val repSubscriptionInitListener: RepSubscriptionInitListener? = null
): TestWatcher() {

    lateinit var coroutineDispatcher: ExecutorCoroutineDispatcher

    lateinit var endpointScope: EndpointScope

    private lateinit var systemImplSpy: UstadMobileSystemImpl

    lateinit var diModule: DI.Module

    lateinit var httpClient: HttpClient

    lateinit var okHttpClient: OkHttpClient

    lateinit var tempFolder: File

    private val xppFactory = XmlPullParserFactory.newInstance().also {
        it.isNamespaceAware = true
    }


    override fun starting(description: Description?) {
        tempFolder = Files.createTempDirectory("ustadtestrule").toFile()

        endpointScope = EndpointScope()
        systemImplSpy = spy(UstadMobileSystemImpl(xppFactory, tempFolder))
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
            bind<UstadAccountManager>() with singleton {
                UstadAccountManager(instance(), Any(), di)
            }
            bind<Json>() with singleton {
                Json { encodeDefaults = true }
            }


            bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
                NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                val attachmentsDir = File(tempFolder.siteDataSubDir(this@singleton.context),
                        UstadMobileSystemCommon.SUBDIR_ATTACHMENTS_NAME)
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                spy(DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
                        "jdbc:sqlite:build/tmp/$dbName.sqlite", attachmentDir = attachmentsDir)
                    .addMigrations(*migrationList().toTypedArray())
                    .addSyncCallback(nodeIdAndAuth)
                    .addCallback(ContentJobItemTriggersCallback())
                    .build()
                    .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId))
            }


            bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                spy(instance<UmAppDatabase>(tag = DoorTag.TAG_DB).asRepository(repositoryConfig(
                    Any(), UMFileUtil.joinPaths(context.url, "UmAppDatabase/"), nodeIdAndAuth.nodeId,
                    nodeIdAndAuth.auth, instance(), instance()
                ) {
                    attachmentsDir = File(tempFolder, "attachments").absolutePath
                    this.useReplicationSubscription = repoReplicationSubscriptionEnabled
                    this.replicationSubscriptionInitListener = repSubscriptionInitListener
                })
                ).also {
                    it.siteDao.insert(Site().apply {
                        siteName = "Test"
                        authSalt = randomString(16)
                    })
                }
            }

            bind<ClientId>(tag = UstadMobileSystemCommon.TAG_CLIENT_ID) with scoped(EndpointScope.Default).singleton {
                val repo: UmAppDatabase by di.on(Endpoint(context.url)).instance(tag = DoorTag.TAG_REPO)
                val nodeId = (repo as? DoorDatabaseRepository)?.config?.nodeId
                    ?: throw IllegalStateException("Could not open repo for endpoint ${context.url}")
                ClientId(nodeId.toInt())
            }

            bind<NetworkManagerBle>() with singleton {
                mock {
                    on { connectivityStatus }.thenReturn(mock {})
                }
            }

            bind<ContainerMounter>() with singleton { EmbeddedHTTPD(0, di).also { it.start() } }

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
            bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(endpointScope).singleton {
                val containerFolder = File(tempFolder, "containerDir")
                containerFolder.mkdirs()
                containerFolder
            }
            bind<XmlPullParserFactory>(tag = DiTag.XPP_FACTORY_NSUNAWARE) with singleton {
                XmlPullParserFactory.newInstance()
            }

            bind<UstadNavController>() with singleton {
                spy(TestUstadNavController(di))
            }

            bind<CoroutineScope>(tag = DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with singleton {
                GlobalScope
            }

            bind<Pbkdf2Params>() with singleton {
                Pbkdf2Params(iterations = 10000, keyLength = 512)
            }

            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
        }
    }

    override fun finished(description: Description?) {
        httpClient.close()
        //coroutineDispatcher.close()
        tempFolder.deleteRecursively()
    }

}