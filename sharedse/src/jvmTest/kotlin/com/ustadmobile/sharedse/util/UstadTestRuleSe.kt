package com.ustadmobile.sharedse.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.preload
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.kodein.di.registerContextTranslator
import org.kodein.di.scoped
import org.kodein.di.singleton
import org.mockito.kotlin.spy
import java.io.File
import java.nio.file.Files
import java.util.Locale
import java.util.Properties
import kotlin.random.Random

fun DI.onActiveAccount(): DI {
    val accountManager: UstadAccountManager by instance()
    return on(accountManager.currentAccount)
}

fun DI.onActiveAccountDirect() = direct.on(direct.instance<UstadAccountManager>().currentAccount)

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


    override fun starting(description: Description) {
        endpointScope = EndpointScope()
        tmpFolder = Files.createTempDirectory("testrule").toFile()

        val settings: Settings = PropertiesSettings(
            delegate = Properties(),
            onModify = {
                //do nothing
            }
        )
        val langConfig = SupportedLanguagesConfig(
            systemLocales = listOf(Locale.getDefault().language),
            settings = settings,
        )

        systemImplSpy = spy(UstadMobileSystemImpl(settings, langConfig))
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
            bind<ApiUrlConfig>() with singleton { ApiUrlConfig(null) }
            bind<UstadAccountManager>() with singleton {
                UstadAccountManager(instance(), di)
            }
            bind<Settings>() with singleton { settings }
            bind<NodeIdAndAuth>() with scoped(endpointScope!!).singleton {
                NodeIdAndAuth(Random.nextLong(), randomUuid().toString())
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope!!).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                spy(DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
                        "jdbc:sqlite:build/tmp/$dbName.sqlite",
                    nodeId = nodeIdAndAuth.nodeId
                    )
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

            bind<Pbkdf2Params>() with singleton {
                Pbkdf2Params()
            }

            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }

            bind<Gson>() with singleton {
                val builder = GsonBuilder()
                builder.create()
            }

        }
    }

    override fun finished(description: Description?) {
        httpClient.close()
    }

}