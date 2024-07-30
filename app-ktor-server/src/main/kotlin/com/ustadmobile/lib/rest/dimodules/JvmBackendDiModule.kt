package com.ustadmobile.lib.rest.dimodules

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.contentformats.epub.XhtmlFixer
import com.ustadmobile.core.contentformats.epub.XhtmlFixerJsoup
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.MIGRATION_144_145_SERVER
import com.ustadmobile.core.db.ext.MIGRATION_148_149_NO_OFFLINE_ITEMS
import com.ustadmobile.core.db.ext.MIGRATION_155_156_SERVER
import com.ustadmobile.core.db.ext.MIGRATION_161_162_SERVER
import com.ustadmobile.core.db.ext.MIGRATION_169_170_SERVER
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.domain.cachelock.AddRetainAllActiveUriTriggersCallback
import com.ustadmobile.core.domain.cachelock.CreateCacheLocksForActiveContentEntryVersionUseCase
import com.ustadmobile.core.domain.cachelock.Migrate131to132AddRetainActiveUriTriggers
import com.ustadmobile.core.domain.cachelock.UpdateCacheLockJoinUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CreateRetentionLocksForManifestUseCaseCommonJvm
import com.ustadmobile.core.domain.message.AddOutgoingReplicationForMessageTriggerCallback
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.rest.InsertDefaultSiteCallback
import com.ustadmobile.lib.rest.ext.dbModeProperty
import com.ustadmobile.lib.rest.ext.initAdminUser
import com.ustadmobile.lib.rest.identifier
import io.github.aakira.napier.Napier
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking
import org.kodein.di.*
import java.io.File
import com.ustadmobile.lib.rest.ext.absoluteDataDir
import com.ustadmobile.lib.rest.ext.ktorInitDb
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.okhttp.UstadCacheInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.xmlpull.v1.XmlPullParserFactory
import java.io.FileReader
import java.io.FileWriter
import java.util.Locale
import java.util.Properties

/**
 * The database and use cases that need to start observing once the database is created
 */
data class DbAndObservers(
    val db: UmAppDatabase,
    val updateCacheLockJoinUseCase: UpdateCacheLockJoinUseCase,
    val createCacheLocksForActiveContentEntryVersionUseCase: CreateCacheLocksForActiveContentEntryVersionUseCase,
)

/**
 * DI Module that provides dependencies which are used both by the server and command line tools
 * e.g. password reset, any import/export, etc.
 */
@OptIn(ExperimentalXmlUtilApi::class)
fun makeJvmBackendDiModule(
    config: ApplicationConfig,
    json: Json,
    contextScope: EndpointScope = EndpointScope.Default,
) = DI.Module("JvmBackendDiModule") {
    val dataDirPath = config.absoluteDataDir()
    dataDirPath.takeIf { !it.exists() }?.mkdirs()

    val dbMode = config.dbModeProperty()

    bind<Json>() with singleton {
        json
    }

    bind<XapiJson>() with singleton { XapiJson() }

    bind<File>(tag = DiTag.TAG_CONTEXT_DATA_ROOT) with scoped(contextScope).singleton {
        File(dataDirPath, context.identifier(dbMode)).also {
            it.takeIf { !it.exists() }?.mkdirs()
        }
    }

    bind<Settings>() with singleton {
        val propertiesFile = File(dataDirPath, UstadMobileSystemImpl.PREFS_FILENAME)

        PropertiesSettings(
            delegate = Properties().also { props ->
                if(propertiesFile.exists()) {
                    FileReader(propertiesFile).use { fileReader ->
                        props.load(fileReader)
                    }
                }
            },
            onModify = { props ->
                FileWriter(propertiesFile).use { fileWriter ->
                    props.store(fileWriter, null)
                }
            }
        )
    }

    bind<UstadMobileSystemImpl>() with singleton {
        UstadMobileSystemImpl(
            settings = instance(),
            langConfig = instance(),
        )
    }

    bind<AuthManager>() with scoped(contextScope).singleton {
        AuthManager(context, di).also { authManager ->
            val db: UmAppDatabase = on(context).instance(tag = DoorTag.TAG_DB)
            runBlocking {
                db.initAdminUser(context, authManager, di,
                    config.propertyOrNull("ktor.ustad.adminpass")?.getString())
            }
        }
    }

    bind<Pbkdf2Params>() with singleton {
        val numIterations = UstadMobileConstants.PBKDF2_ITERATIONS
        val keyLength = UstadMobileConstants.PBKDF2_KEYLENGTH

        Pbkdf2Params(numIterations, keyLength)
    }

    bind<DbAndObservers>() with scoped(contextScope).singleton {
        instance<File>(DiTag.TAG_CONTEXT_DATA_ROOT) //Ensure data dir for context is created

        val dbHostName = context.identifier(dbMode, "UmAppDatabase")
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        val dbUrl = config.property("ktor.database.url").getString()
            .replace("(hostname)", dbHostName)
            .replace("(datadir)", config.absoluteDataDir().absolutePath)
        if(dbUrl.startsWith("jdbc:postgresql"))
            Class.forName("org.postgresql.Driver")
        val db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
            dbUrl = dbUrl,
            dbUsername = config.propertyOrNull("ktor.database.user")?.getString(),
            dbPassword = config.propertyOrNull("ktor.database.password")?.getString(),
            nodeId = nodeIdAndAuth.nodeId,
        )
            .addSyncCallback(nodeIdAndAuth)
            .addCallback(InsertDefaultSiteCallback())
            .addCallback(AddRetainAllActiveUriTriggersCallback())
            .addCallback(AddOutgoingReplicationForMessageTriggerCallback())
            .addMigrations(*migrationList().toTypedArray())
            .addMigrations(Migrate131to132AddRetainActiveUriTriggers)
            .addMigrations(MIGRATION_144_145_SERVER)
            .addMigrations(MIGRATION_148_149_NO_OFFLINE_ITEMS)
            .addMigrations(MIGRATION_155_156_SERVER)
            .addMigrations(MIGRATION_161_162_SERVER)
            .addMigrations(MIGRATION_169_170_SERVER)
            .build().also {
                it.ktorInitDb(di)
            }
        val cache: UstadCache = instance()


        DbAndObservers(
            db = db,
            updateCacheLockJoinUseCase = UpdateCacheLockJoinUseCase(
                db = db,
                cache = cache,
            ),
            createCacheLocksForActiveContentEntryVersionUseCase = CreateCacheLocksForActiveContentEntryVersionUseCase(
                db = db,
                httpClient = instance(),
                json = instance(),
                endpoint = context,
                createRetentionLocksForManifestUseCase = CreateRetentionLocksForManifestUseCaseCommonJvm(
                    cache = cache,
                ),
            )
        )
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(contextScope).singleton {
        Napier.d("creating database for context: ${context.url}")
        instance<DbAndObservers>().db
    }

    bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
        val settings: Settings = instance()
        val contextIdentifier: String = context.identifier(dbMode)
        settings.getOrGenerateNodeIdAndAuth(contextIdentifier)
    }

    bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
        XmlPullParserFactory.newInstance().also {
            it.isNamespaceAware = true
        }
    }

    bind<XML>() with singleton {
        XML {
            defaultPolicy {
                unknownChildHandler  = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }
    }

    bind<XhtmlFixer>() with singleton {
        XhtmlFixerJsoup(xml = instance())
    }

    bind<SupportedLanguagesConfig>() with singleton {
        SupportedLanguagesConfig(
            systemLocales = listOf(Locale.getDefault().language),
            settings = instance(),
        )
    }

    bind<UstadCache>() with singleton {
        val dbUrl = "jdbc:sqlite:(datadir)/ustadcache.db"
            .replace("(datadir)", config.absoluteDataDir().absolutePath)
        UstadCacheBuilder(
            dbUrl = dbUrl,
            logger = NapierLoggingAdapter(),
            storagePath = Path(
                File(config.absoluteDataDir(), "httpfiles").absolutePath.toString()
            ),
        ).build()
    }


    bind<OkHttpClient>() with singleton {
        OkHttpClient.Builder()
            .dispatcher(
                Dispatcher().also {
                    it.maxRequests = 30
                    it.maxRequestsPerHost = 10
                }
            )
            .addInterceptor(
                UstadCacheInterceptor(
                    cache = instance(),
                    tmpDirProvider = { File(config.absoluteDataDir(), "httpfiles") },
                    logger = NapierLoggingAdapter(),
                    json = json,
                )
            )
            .build()
    }

    bind<HttpClient>() with singleton {
        HttpClient(OkHttp) {

            install(ContentNegotiation) {
                json(json = instance())
            }
            install(HttpTimeout)

            val dispatcher = Dispatcher()
            dispatcher.maxRequests = 30
            dispatcher.maxRequestsPerHost = 10

            engine {
                preconfigured = instance()
            }

        }
    }

}
