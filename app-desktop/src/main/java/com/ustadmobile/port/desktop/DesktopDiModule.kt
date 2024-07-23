package com.ustadmobile.port.desktop

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.connectivitymonitor.ConnectivityMonitorJvm
import com.ustadmobile.core.connectivitymonitor.ConnectivityTriggerGroupController
import com.ustadmobile.core.contentformats.ContentImportersDiModuleJvm
import com.ustadmobile.core.contentformats.epub.XhtmlFixer
import com.ustadmobile.core.contentformats.epub.XhtmlFixerJsoup
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.MIGRATION_144_145_CLIENT
import com.ustadmobile.core.db.ext.MIGRATION_148_149_CLIENT_WITH_OFFLINE_ITEMS
import com.ustadmobile.core.db.ext.MIGRATION_155_156_CLIENT
import com.ustadmobile.core.db.ext.MIGRATION_161_162_CLIENT
import com.ustadmobile.core.db.ext.MIGRATION_169_170_CLIENT
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.domain.cachelock.AddOfflineItemInactiveTriggersCallback
import com.ustadmobile.core.domain.cachelock.UpdateCacheLockJoinUseCase
import com.ustadmobile.core.domain.compress.audio.CompressAudioUseCase
import com.ustadmobile.core.domain.compress.audio.CompressAudioUseCaseSox
import com.ustadmobile.core.domain.compress.pdf.CompressPdfUseCase
import com.ustadmobile.core.domain.compress.pdf.CompressPdfUseCaseJvm
import com.ustadmobile.core.domain.compress.video.CompressVideoUseCase
import com.ustadmobile.core.domain.compress.video.CompressVideoUseCaseHandbrake
import com.ustadmobile.core.domain.compress.video.FindHandBrakeUseCase
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueImportContentEntryUseCaseJvm
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueImportContentEntryUseCaseRemote
import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExecuteMediaInfoUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExtractMediaMetadataUseCaseMediaInfo
import com.ustadmobile.core.domain.getdeveloperinfo.GetDeveloperInfoUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCaseJvm
import com.ustadmobile.core.domain.validatevideofile.ValidateVideoFileUseCase
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer
import com.ustadmobile.core.getdeveloperinfo.GetDeveloperInfoUseCaseJvm
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.AppConfig
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.config.ManifestAppConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.StringProviderJvm
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerJvm
import com.ustadmobile.core.schedule.initQuartzDb
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.getCommandFile
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.core.util.ext.isWindowsOs
import com.ustadmobile.core.util.ext.toNullIfBlank
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.util.SysPathUtil
import com.ustadmobile.lib.util.ext.bindDataSourceIfNotExisting
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.io.File
import java.util.Locale
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.libcache.CachePaths
import com.ustadmobile.libcache.CachePathsProvider
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.headers.MimeTypeHelper
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.okhttp.UstadCacheInterceptor
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.kodein.di.direct
import org.kodein.di.on
import org.kodein.di.provider
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import org.xmlpull.v1.XmlPullParserFactory
import java.io.FileReader
import java.io.FileWriter
import java.net.InetAddress
import java.util.Properties
import javax.naming.InitialContext


const val TAG_DATA_DIR = "DataDir"

const val TAG_CACHE_DIR = "CacheDir"

const val TAG_CACHE_STORAGE_PATH = "CacheStoragePath"

const val CONNECTIVITY_CHECK_HOST = "google.com"

/**
 * The resources directory is where files required by the app can be stored. This is mainly used
 * for ffmpeg on Windows.
 *
 * See:
 * https://conveyor.hydraulic.dev/11.1/troubleshooting/troubleshooting-windows/#where-to-store-files
 */
fun ustadAppResourcesDir(): File {
    //Jetpack Compose resources directory as per
    //https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md#adding-files-to-packaged-application

    //When running conveyor build, this uses app.dir as per
    // https://conveyor.hydraulic.dev/13.0/configs/jvm/#appjvmsystem-properties
    return System.getProperty("compose.application.resources.dir")?.let {
        File(it)
    } ?: System.getProperty("app.dir")?.let { File(it) }
        ?: throw IllegalStateException("Cannot find resource dir")
}

fun ustadAppHomeDir(): File {
    return System.getProperty("app_home")?.let { File(it) } ?: File(System.getProperty("user.dir"))
}

/**
 * Get the Operating System user data directory. This is used when the conveyor built package is
 * running. If we are running the conveyor distributable, then app.fsname will be set.
 *
 * On Windows: Use Application Data folder
 * On Linux/MacOS: Use user home directory/.app-name
 *
 */
private fun osUserDataDir(): File? {
    val appFsName: String? = System.getProperty("app.fsname")
    return when {
        appFsName == null -> null
        isWindowsOs() -> File(System.getenv("APPDATA"), appFsName)
        else -> File(System.getProperty("user.home"), ".$appFsName")
    }
}

/**
 * The directory for user data storage including SQLite databases, files, etc.
 */
fun ustadAppDataDir(): File {
    return System.getProperty("ustad.datadir")?.let { File(it) }
        ?: osUserDataDir() ?: File(ustadAppHomeDir(), "data")
}

val DesktopHttpModule = DI.Module("Desktop-HTTP") {
    import(ContentImportersDiModuleJvm)
    val cacheLogger = NapierLoggingAdapter()

    bind<File>(tag = TAG_CACHE_DIR) with singleton {
        val dataDir: File = instance(tag = TAG_DATA_DIR)
        File(dataDir, "httpfiles").also {
            if(!it.exists())
                it.mkdirs()
        }
    }

    bind<MimeTypeHelper>() with singleton {
        FileMimeTypeHelperImpl()
    }

    bind<UriHelper>() with singleton {
        UriHelperJvm(
            mimeTypeHelperImpl = FileMimeTypeHelperImpl(),
            httpClient = instance(),
            okHttpClient = instance()
        )
    }

    bind<Path>(tag = TAG_CACHE_STORAGE_PATH) with singleton {
        val dataDir: File = instance(tag = TAG_DATA_DIR)
        dataDir.takeIf { !it.exists() }?.mkdirs()

        Path(File(dataDir, "httpfiles").absolutePath.toString())
    }

    bind<CachePathsProvider>() with singleton {
        val cacheStoragePath: Path = instance(tag = TAG_CACHE_STORAGE_PATH)

        CachePathsProvider {
            CachePaths(
                tmpWorkPath = Path(cacheStoragePath, "tmpWork"),
                persistentPath = Path(cacheStoragePath, "cache"),
                cachePath = Path(cacheStoragePath, "cache")
            )
        }
    }

    bind<UstadCache>() with singleton {
        val cacheStoragePath: Path = instance(tag = TAG_CACHE_STORAGE_PATH)

        val dataDir: File = instance(tag = TAG_DATA_DIR)
        dataDir.takeIf { !it.exists() }?.mkdirs()
        val dbUrl = "jdbc:sqlite:(datadir)/ustadcache.db"
            .replace("(datadir)", dataDir.absolutePath)

        /* Persistent path and cache path are the same. Trying to move files on Windows has caused
         * resulted in errors appearing in logs where files apparently weren't released quickly
         * enough (never seen on Linux).
         *
         * There is no need on the desktop to separate the persistent and cache path on desktop,
         * as is required on Android (where the OS provides a dedicated cache dir for each app).
         */
        UstadCacheBuilder(
            dbUrl = dbUrl,
            storagePath = cacheStoragePath,
            pathsProvider = instance(),
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
                    tmpDirProvider = { interceptorTmpDir },
                    logger = cacheLogger,
                    json = instance(),
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

data class DbAndObservers(
    val db: UmAppDatabase,
    val updateCacheLockJoinUseCase: UpdateCacheLockJoinUseCase,
)


@OptIn(ExperimentalXmlUtilApi::class)
val DesktopDiModule = DI.Module("Desktop-Main") {
    val resourcesDir = ustadAppResourcesDir()
    val mediaInfoResourcesDir = File(resourcesDir, "mediainfo")
    val soxResourcesDir = File(resourcesDir, "sox")
    val mpg123ResourcesDir = File(resourcesDir, "mpg123")

    val mediaInfoFile = SysPathUtil.findCommandInPath(
        commandName = "mediainfo",
        manuallySpecifiedLocation = File(mediaInfoResourcesDir, "mediainfo").getCommandFile(),
    ) ?: throw IllegalStateException("No MediaInfo found")

    val handbrakeResourcesDir = File(resourcesDir, "handbrakecli")
    val findHandBrakeResult = runBlocking {
        FindHandBrakeUseCase(
            specifiedLocation = File(handbrakeResourcesDir, "HandBrakeCLI").getCommandFile()?.absolutePath,
        ).invoke()
    }

    val soxCommand = SysPathUtil.findCommandInPath(
        commandName = "sox",
        manuallySpecifiedLocation = File(soxResourcesDir, "sox").getCommandFile(),
    ) ?: throw IllegalArgumentException("sox command not found")

    val mpg123Command = SysPathUtil.findCommandInPath(
        commandName = "mpg123",
        pathVar = "",
        manuallySpecifiedLocation = File(mpg123ResourcesDir, "mpg123.exe")
    )

    if(isWindowsOs() && mpg123Command == null) {
        throw IllegalStateException("Could not find mpg123.exe : this is required when running on Windows")
    }

    val gsPath = SysPathUtil.findCommandInPath("gs")

    bind<AppConfig>() with singleton {
        ManifestAppConfig()
    }

    bind<SupportedLanguagesConfig>() with singleton {
        val appConfig = instance<AppConfig>()
        SupportedLanguagesConfig(
            systemLocales = listOf(SetLanguageUseCaseJvm.REAL_SYSTEM_DEFAULT.language),
            settings = instance(),
            availableLanguagesConfig = appConfig["com.ustadmobile.uilanguages"]?.toNullIfBlank() ?:
                SupportedLanguagesConfig.DEFAULT_SUPPORTED_LANGUAGES
        )
    }

    bind<StringProvider>() with singleton { StringProviderJvm(Locale.getDefault()) }

    bind<UstadAccountManager>() with singleton {
        UstadAccountManager(instance(), di)
    }

    bind<ApiUrlConfig>() with singleton {
        val appConfig : AppConfig = instance()
        ApiUrlConfig(presetApiUrl = appConfig[AppConfig.KEY_API_URL]?.ifBlank { null })
    }

    bind<File>(tag = TAG_DATA_DIR) with singleton {
        ustadAppDataDir().also { it.takeIf { !it.exists() }?.mkdirs() }
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
        instance<DbAndObservers>().db
    }

    bind<DbAndObservers>() with scoped(EndpointScope.Default).singleton {
        val contextDataDir: File = on(context).instance(tag = DiTag.TAG_CONTEXT_DATA_ROOT)
        val dbUrl = "jdbc:sqlite:${contextDataDir.absolutePath}/UmAppDatabase.db"
        val nodeIdAndAuth: NodeIdAndAuth = instance()

        val db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, dbUrl, nodeIdAndAuth.nodeId)
            .addSyncCallback(nodeIdAndAuth)
            .addMigrations(*migrationList().toTypedArray())
            .addMigrations(MIGRATION_144_145_CLIENT)
            .addMigrations(MIGRATION_148_149_CLIENT_WITH_OFFLINE_ITEMS)
            .addMigrations(MIGRATION_155_156_CLIENT)
            .addMigrations(MIGRATION_161_162_CLIENT)
            .addMigrations(MIGRATION_169_170_CLIENT)
            .addCallback(AddOfflineItemInactiveTriggersCallback())
            .build()

        val cache: UstadCache = instance()
        DbAndObservers(
            db = db,
            updateCacheLockJoinUseCase = UpdateCacheLockJoinUseCase(
                db = db,
                cache = cache,
            )
        )
    }

    bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
        val settings: Settings = instance()
        val contextIdentifier = sanitizeDbNameFromUrl(context.url)
        settings.getOrGenerateNodeIdAndAuth(contextIdentifier)
    }

    bind<Json>() with singleton {
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
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

    bind<EnqueueContentEntryImportUseCase>() with scoped(EndpointScope.Default).singleton {
        EnqueueImportContentEntryUseCaseJvm(
            db = instance(tag = DoorTag.TAG_DB),
            scheduler = instance(),
            endpoint = context,
            enqueueRemoteImport = EnqueueImportContentEntryUseCaseRemote(
                endpoint = context,
                httpClient = instance(),
                json = instance(),
            )
        )
    }

    bind<ClazzLogCreatorManager>() with singleton {
        ClazzLogCreatorManagerJvm(di)
    }

    bind<Scheduler>() with singleton {
        val dataDir: File = instance(tag = TAG_DATA_DIR)
        val dbUrl = "jdbc:hsqldb:file:${dataDir.absolutePath}/quartz"
        val dbProperties = Properties().also {
            it["url"] = dbUrl
            it["driver"] = "org.hsqldb.jdbc.JDBCDriver"
            it["user"] = "SA"
            it["password"] = ""
        }

        InitialContext().apply {
            bindDataSourceIfNotExisting("quartzds", dbProperties)
            initQuartzDb("java:/comp/env/jdbc/quartzds")
        }

        StdSchedulerFactory.getDefaultScheduler().also {
            it.context["di"] = di
        }
    }

    bind<GenderConfig>() with singleton {
        GenderConfig(appConfig = instance())
    }

    bind<File>(tag = DiTag.TAG_TMP_DIR) with singleton {
        File(ustadAppDataDir(), "tmp").also {
            if(!it.exists())
                it.mkdirs()
        }
    }

    bind<ConnectivityMonitorJvm>() with singleton {
        ConnectivityMonitorJvm(
            checkInetAddr = { InetAddress.getByName(CONNECTIVITY_CHECK_HOST) },
            checkPort = 80,
        )
    }

    bind<ConnectivityTriggerGroupController>() with singleton {
        ConnectivityTriggerGroupController(
            scheduler = instance(),
            connectivityMonitorJvm = instance(),
        )
    }

    bind<ExecuteMediaInfoUseCase>() with singleton {
        ExecuteMediaInfoUseCase(
            mediaInfoPath = mediaInfoFile.absolutePath,
            workingDir = ustadAppDataDir(),
            json = instance(),
        )
    }

    bind<ExtractMediaMetadataUseCase>() with singleton {
        ExtractMediaMetadataUseCaseMediaInfo(
            executeMediaInfoUseCase = instance(),
            getStoragePathForUrlUseCase = instance(),
        )
    }


    bind<ValidateVideoFileUseCase>() with singleton {
        ValidateVideoFileUseCase(
            extractMediaMetadataUseCase = instance(),
        )
    }

    bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
        XmlPullParserFactory.newInstance().also {
            it.isNamespaceAware = true
        }
    }

    bind<EmbeddedHttpServer>() with singleton {
        EmbeddedHttpServer(
            port = 0,
            contentEntryVersionServerUseCase = {
                di.on(it).direct.instance()
            },
            xapiServerUseCase = {
                di.on(it).direct.instance()
            },
            staticUmAppFilesDir = File(resourcesDir, "umapp"),
            mimeTypeHelper = instance()
        )
    }

    bind<GetDeveloperInfoUseCase>() with singleton {
        GetDeveloperInfoUseCaseJvm(
            appResourcesDir = ustadAppResourcesDir(),
            dataDir = ustadAppDataDir(),
        )
    }

    if(findHandBrakeResult != null) {
        bind<CompressVideoUseCase>() with provider {
            CompressVideoUseCaseHandbrake(
                handbrakeCommand = findHandBrakeResult.command,
                extractMediaMetadataUseCase = instance(),
                workDir = instance(tag = TAG_DATA_DIR),
                json = instance(),
            )
        }
    }

    bind<CompressAudioUseCase>() with singleton {
        CompressAudioUseCaseSox(
            soxPath = soxCommand.absolutePath,
            mpg123Path = mpg123Command?.absolutePath,
            executeMediaInfoUseCase = instance(),
            workDir = instance(tag = DiTag.TAG_TMP_DIR),
        )
    }


    gsPath?.also {
        bind<CompressPdfUseCase>() with provider {
            CompressPdfUseCaseJvm(
                gsPath = it,
                workDir = instance(tag = TAG_DATA_DIR),
            )
        }
    }

    onReady {
        instance<File>(tag = TAG_DATA_DIR).takeIf { !it.exists() }?.mkdirs()
        instance<ConnectivityMonitorJvm>()
        instance<ConnectivityTriggerGroupController>()
        instance<Scheduler>().start()
        Runtime.getRuntime().addShutdownHook(Thread{
            Napier.i("Shutdown: shutting down scheduler")
            instance<Scheduler>().shutdown()
        })
    }

}
