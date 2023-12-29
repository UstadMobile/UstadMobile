package com.ustadmobile.port.desktop

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.epub.XhtmlFixer
import com.ustadmobile.core.contentformats.epub.XhtmlFixerJsoup
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentUseCase
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentUseCaseJvm
import com.ustadmobile.core.domain.language.SetLanguageUseCaseJvm
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.StringProviderJvm
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerJvm
import com.ustadmobile.core.schedule.initQuartzDb
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.util.ext.bindDataSourceIfNotExisting
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.io.File
import java.util.Locale
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
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
import org.kodein.di.on
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties
import javax.naming.InitialContext

const val TAG_APP_HOME = "AppHome"

const val TAG_DATA_DIR = "DataDir"

const val TAG_CACHE_DIR = "CacheDir"

fun ustadAppHomeDir(): File {
    return System.getProperty("app_home")?.let { File(it) } ?: File(System.getProperty("user.dir"))
}

fun ustadAppDataDir(): File {
    return File(ustadAppHomeDir(), "data")
}

val DesktopHttpModule = DI.Module("Desktop-HTTP") {
    val cacheLogger = NapierLoggingAdapter()

    bind<File>(tag = TAG_CACHE_DIR) with singleton {
        val dataDir: File = instance(tag = TAG_DATA_DIR)
        File(dataDir, "httpfiles").also {
            if(!it.exists())
                it.mkdirs()
        }
    }

    bind<UriHelper>() with singleton {
        UriHelperJvm(
            mimeTypeHelperImpl = FileMimeTypeHelperImpl(),
            httpClient = instance(),
            okHttpClient = instance()
        )
    }

    bind<UstadCache>() with singleton {
        val dataDir: File = instance(tag = TAG_DATA_DIR)
        dataDir.takeIf { !it.exists() }?.mkdirs()

        val dbUrl = "jdbc:sqlite:(datadir)/ustadcache.db"
            .replace("(datadir)", dataDir.absolutePath)
        UstadCacheBuilder(
            dbUrl = dbUrl,
            storagePath = Path(
                File(dataDir, "httpfiles").absolutePath.toString()
            ),
            logger = cacheLogger,

        ).build()
    }

    bind<OkHttpClient>() with singleton {
        val interceptorTmpDir = instance<File>(tag = TAG_CACHE_DIR)

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
                    cacheDir = interceptorTmpDir,
                    logger = cacheLogger,
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

@OptIn(ExperimentalXmlUtilApi::class)
val DesktopDiModule = DI.Module("Desktop-Main") {
    bind<SupportedLanguagesConfig>() with singleton {
        SupportedLanguagesConfig(
            systemLocales = listOf(SetLanguageUseCaseJvm.REAL_SYSTEM_DEFAULT.language),
            settings = instance(),
        )
    }

    bind<StringProvider>() with singleton { StringProviderJvm(Locale.getDefault()) }

    bind<UstadAccountManager>() with singleton {
        UstadAccountManager(instance(), di)
    }

    bind<ApiUrlConfig>() with singleton {
        ApiUrlConfig(presetApiUrl = null)
    }

    bind<File>(tag = TAG_APP_HOME) with singleton {
        ustadAppHomeDir()
    }

    bind<File>(tag = TAG_DATA_DIR) with singleton {
        File(instance<File>(tag = TAG_APP_HOME), "data")
    }

    bind<File>(tag = DiTag.TAG_CONTEXT_DATA_ROOT) with scoped(EndpointScope.Default).singleton {
        val dataDir: File = instance(tag = TAG_DATA_DIR)
        File(dataDir, sanitizeDbNameFromUrl(context.url)).also {
            it.takeIf { !it.exists() }?.mkdirs()
        }
    }

    bind<Settings>() with singleton {
        val propertiesFile = File(instance<File>(tag = TAG_DATA_DIR),
            UstadMobileSystemImpl.PREFS_FILENAME)

        PropertiesSettings(
            delegate = Properties().also {props ->
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

    bind<AuthManager>() with scoped(EndpointScope.Default).singleton {
        AuthManager(context, di)
    }

    bind<Pbkdf2Params>() with singleton {
        val numIterations = UstadMobileConstants.PBKDF2_ITERATIONS
        val keyLength = UstadMobileConstants.PBKDF2_KEYLENGTH

        Pbkdf2Params(numIterations, keyLength)
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(EndpointScope.Default).singleton {
        val contextDataDir: File = on(context).instance(tag = DiTag.TAG_CONTEXT_DATA_ROOT)
        val dbUrl = "jdbc:sqlite:${contextDataDir.absolutePath}/UmAppDatabase.db"
        val nodeIdAndAuth: NodeIdAndAuth = instance()

        DatabaseBuilder.databaseBuilder(UmAppDatabase::class, dbUrl, nodeIdAndAuth.nodeId)
            .addSyncCallback(nodeIdAndAuth)
            .addCallback(ContentJobItemTriggersCallback())
            .addMigrations(*migrationList().toTypedArray())
            .build()
    }

    bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
        val settings: Settings = instance()
        val contextIdentifier = sanitizeDbNameFromUrl(context.url)
        settings.getOrGenerateNodeIdAndAuth(contextIdentifier)
    }

    bind<Json>() with singleton {
        Json {
            encodeDefaults = true
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

    bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(EndpointScope.Default).singleton {
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
        db.asRepository(
            RepositoryConfig.repositoryConfig(
                context = Any(),
                endpoint = "${context.url}UmAppDatabase/",
                nodeId = nodeIdAndAuth.nodeId,
                auth = nodeIdAndAuth.auth,
                httpClient = instance(),
                okHttpClient = instance(),
                json = instance()
            )
        )
    }

    bind<ImportContentUseCase>() with scoped(EndpointScope.Default).singleton {
        ImportContentUseCaseJvm(
            db = instance(tag = DoorTag.TAG_DB),
            scheduler = instance(),
            endpoint = context
        )
    }

    bind<ClazzLogCreatorManager>() with singleton {
        ClazzLogCreatorManagerJvm(di)
    }

    bind<Scheduler>() with singleton {
        val dataDir: File = instance(tag = TAG_DATA_DIR)
        val dbUrl = "jdbc:sqlite:${dataDir.absolutePath}/quartz.db"
        val dbProperties = Properties().also {
            it["url"] = dbUrl
            it["driver"] = "org.sqlite.JDBC"
            it["user"] = ""
            it["password"] = ""
        }

        InitialContext().apply {
            bindDataSourceIfNotExisting("quartzds", dbProperties)
            initQuartzDb("java:/comp/env/jdbc/quartzds")
        }

        StdSchedulerFactory.getDefaultScheduler().also {
            it.context.put("di", di)
        }
    }

    bind<GenderConfig>() with singleton {
        GenderConfig()
    }

    bind<File>(tag = DiTag.TAG_TMP_DIR) with singleton {
        File(ustadAppDataDir(), "tmp").also {
            if(!it.exists())
                it.mkdirs()
        }
    }

    onReady {
        instance<File>(tag = TAG_DATA_DIR).takeIf { !it.exists() }?.mkdirs()
        instance<Scheduler>().start()
        Runtime.getRuntime().addShutdownHook(Thread{
            instance<Scheduler>().shutdown()
        })
    }

}
