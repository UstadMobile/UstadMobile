package com.ustadmobile.sharedse.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.mockito.kotlin.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.core.contentformats.xapi.ContextDeserializer
import com.ustadmobile.core.contentformats.xapi.StatementDeserializer
import com.ustadmobile.core.contentformats.xapi.StatementSerializer
import com.ustadmobile.core.db.ext.preload
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import okhttp3.OkHttpClient
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.nio.file.Files
import javax.naming.InitialContext
import kotlin.random.Random
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.runBlocking

fun DI.onActiveAccount(): DI {
    val accountManager: UstadAccountManager by instance()
    return on(accountManager.activeAccount)
}

fun DI.onActiveAccountDirect() = direct.on(direct.instance<UstadAccountManager>().activeAccount)

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
class UstadTestRule: TestWatcher() {

    var endpointScope: EndpointScope? = null

    private lateinit var systemImplSpy: UstadMobileSystemImpl

    lateinit var diModule: DI.Module

    lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var tmpFolder: File


    override fun starting(description: Description?) {
        endpointScope = EndpointScope()
        tmpFolder = Files.createTempDirectory("testrule").toFile()
        systemImplSpy = spy(UstadMobileSystemImpl(XmlPullParserFactory.newInstance(), tmpFolder))
        okHttpClient = OkHttpClient()
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
            bind<NodeIdAndAuth>() with scoped(endpointScope!!).singleton {
                NodeIdAndAuth(Random.nextLong(), randomUuid().toString())
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope!!).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite:build/tmp/$dbName.sqlite")
                    .addSyncCallback(nodeIdAndAuth)
                    .build()
                    .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
                    .also { runBlocking { it.preload() } })
            }

            bind<HttpClient>() with singleton{
                httpClient
            }

            bind<OkHttpClient>() with singleton {
                okHttpClient
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope!!).singleton {
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                spy(instance<UmAppDatabase>(tag = DoorTag.TAG_DB).asRepository(repositoryConfig(Any(),
                    context.url, nodeIdAndAuth.nodeId, nodeIdAndAuth.auth, instance(), instance()))
                ).also {
                    it.siteDao.insert(Site().apply {
                        siteName = "Test"
                        authSalt = randomString(16)
                    })
                }
            }

            bind<ContainerMounter>() with singleton { EmbeddedHTTPD(0, di).also { it.start() } }

            bind<Pbkdf2Params>() with singleton {
                Pbkdf2Params()
            }

            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }

            bind<Gson>() with singleton {
                val builder = GsonBuilder()
                builder.registerTypeAdapter(Statement::class.java, StatementSerializer())
                builder.registerTypeAdapter(Statement::class.java, StatementDeserializer())
                builder.registerTypeAdapter(ContextActivity::class.java, ContextDeserializer())
                builder.create()
            }

            bind<CoroutineScope>(tag = DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with singleton {
                GlobalScope
            }
        }
    }

    override fun finished(description: Description?) {
        httpClient.close()
    }

}