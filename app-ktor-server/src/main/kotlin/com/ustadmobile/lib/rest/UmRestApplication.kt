package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.account.*
import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.catalog.contenttype.H5PTypePluginCommonJvm
import com.ustadmobile.core.catalog.contenttype.VideoTypePluginJvm
import com.ustadmobile.core.catalog.contenttype.XapiTypePluginCommonJvm
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentJobManagerJvm
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.di.commonJvmDiModule
import com.ustadmobile.core.networkmanager.ConnectivityLiveData
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.DiTag.TAG_CONTEXT_DATA_ROOT
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.door.*
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.*
import com.ustadmobile.core.catalog.contenttype.ApacheIndexerPlugin
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.io.UploadSessionManager
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.rest.ext.*
import com.ustadmobile.lib.rest.messaging.MailProperties
import com.ustadmobile.lib.util.ext.bindDataSourceIfNotExisting
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.routing.*
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import kotlinx.coroutines.runBlocking
import org.kodein.di.*
import org.kodein.di.ktor.DIFeature
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.nio.file.Files
import javax.naming.InitialContext
import com.ustadmobile.door.util.NodeIdAuthCache
import com.ustadmobile.core.db.PermissionManagementIncomingReplicationListener
import com.ustadmobile.core.contentjob.DummyContentPluginUploader
import io.ktor.response.*
import kotlinx.serialization.json.Json
import com.ustadmobile.core.util.SysPathUtil
import io.ktor.client.*
import io.ktor.websocket.*
import java.util.*

const val TAG_UPLOAD_DIR = 10

const val CONF_DBMODE_VIRTUALHOST = "virtualhost"

const val CONF_DBMODE_SINGLETON = "singleton"

const val CONF_GOOGLE_API = "secret"

/**
 * List of external commands (e.g. media converters) that must be found or have locations specified
 */
val REQUIRED_EXTERNAL_COMMANDS = listOf("ffmpeg", "ffprobe")

/**
 * List of prefixes which are always answered by the KTOR server. When using JsDev proxy mode, any
 * other url will be sent to the JS dev proxy
 */
val KTOR_SERVER_ROUTES = listOf("/UmAppDatabase", "/ConcatenatedContainerFiles2",
    "/ContainerEntryList", "/ContainerEntryFile", "/auth", "/ContainerMount",
    "/ContainerUpload2", "/Site", "/import", "/contentupload", "/websocket")


/**
 * Returns an identifier that is used as a subdirectory for data storage (e.g. attachments,
 * containers, etc).
 */
private fun Endpoint.identifier(
    dbMode: String,
    singletonName: String = CONF_DBMODE_SINGLETON
) = if(dbMode == CONF_DBMODE_SINGLETON) {
    singletonName
}else {
    sanitizeDbNameFromUrl(url)
}

fun Application.umRestApplication(
    dbModeOverride: String? = null,
    singletonDbName: String = "UmAppDatabase"
) {
    val appConfig = environment.config

    val devMode = environment.config.propertyOrNull("ktor.ustad.devmode")?.getString().toBoolean()

    //Check for required external commands
    REQUIRED_EXTERNAL_COMMANDS.forEach { command ->
        if(!SysPathUtil.commandExists(command,
                manuallySpecifiedLocation = appConfig.commandFileProperty(command))
        ) {
            val message = "FATAL ERROR: Required external command \"$command\" not found in path or " +
                   "manually specified location does not exist. Please set it in application.conf"
            Napier.e(message)
            throw IllegalStateException(message)
        }
    }


    if (devMode) {
        install(CORS) {
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Put)
            method(HttpMethod.Options)
            header(HttpHeaders.ContentType)
            header(HttpHeaders.AccessControlAllowOrigin)
            header("X-nid")
            header("door-dbversion")
            header("door-node")
            anyHost()
        }
    }

    install(CallLogging)

    //TODO: Put in a proper log filter here
    Napier.takeLogarithm()
    Napier.base(DebugAntilog())

    install(ContentNegotiation) {
        gson {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }
    }

    //Avoid sending the body of content if it has not changed since the client last requested it.
    install(ConditionalHeaders)

    val tmpRootDir = Files.createTempDirectory("upload").toFile()

    val dbMode = dbModeOverride ?:
        appConfig.propertyOrNull("ktor.ustad.dbmode")?.getString() ?: CONF_DBMODE_SINGLETON
    val dataDirPath = File(environment.config.propertyOrNull("ktor.ustad.datadir")?.getString() ?: "data")
    dataDirPath.takeIf { !it.exists() }?.mkdirs()

    val apiKey = environment.config.propertyOrNull("ktor.ustad.googleApiKey")?.getString() ?: CONF_GOOGLE_API

    install(DIFeature) {
        import(commonJvmDiModule)
        bind<File>(tag = TAG_UPLOAD_DIR) with scoped(EndpointScope.Default).singleton {
            File(tmpRootDir, context.identifier(dbMode)).also {
                it.takeIf { !it.exists() }?.mkdirs()
            }
        }

        bind<File>(tag = TAG_CONTEXT_DATA_ROOT) with scoped(EndpointScope.Default).singleton {
            File(dataDirPath, context.identifier(dbMode)).also {
                it.takeIf { !it.exists() }?.mkdirs()
            }
        }

        bind<ContainerStorageManager>() with scoped(EndpointScope.Default).singleton {
            ContainerStorageManager(listOf(instance<File>(tag = TAG_CONTEXT_DATA_ROOT)))
        }

        bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
            val systemImpl: UstadMobileSystemImpl = instance()
            val contextIdentifier: String = context.identifier(dbMode)
            systemImpl.getOrGenerateNodeIdAndAuth(contextIdentifier, Any())
        }

        bind<NodeIdAuthCache>() with scoped(EndpointScope.Default).singleton {
            instance<UmAppDatabase>(tag = DoorTag.TAG_DB).nodeIdAuthCache
        }

        bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(EndpointScope.Default).singleton {
            val containerDir = File(instance<File>(tag = TAG_CONTEXT_DATA_ROOT), "container")

            //Move any old container directory to the new path (e.g. pre database v57)
            if(context == Endpoint("localhost")){
                val oldContainerDir = File("build/storage/singleton/container")
                if(oldContainerDir.exists() && !oldContainerDir.renameTo(containerDir)) {
                    throw IllegalStateException("Old singleton container dir present but cannot " +
                            "rename from ${oldContainerDir.absolutePath} to ${containerDir.absolutePath}")
                }
            }

            containerDir.takeIf { !it.exists() }?.mkdirs()
            containerDir
        }

        bind<String>(tag = DiTag.TAG_GOOGLE_API) with singleton {
            apiKey
        }

        bind<Gson>() with singleton { Gson() }

        bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(EndpointScope.Default).singleton {
            Napier.d("creating database for context: ${context.url}")
            val dbHostName = context.identifier(dbMode, singletonDbName)
            val dbProperties = appConfig.databasePropertiesFromSection("ktor.database",
                defaultUrl = "jdbc:sqlite:data/singleton/UmAppDatabase.sqlite?journal_mode=WAL&synchronous=OFF&busy_timeout=30000")
            InitialContext().bindDataSourceIfNotExisting(dbHostName, dbProperties)
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            val attachmentsDir = File(instance<File>(tag = TAG_CONTEXT_DATA_ROOT),
                UstadMobileSystemCommon.SUBDIR_ATTACHMENTS_NAME)
            val db = DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, dbHostName,
                    attachmentsDir)
                .addSyncCallback(nodeIdAndAuth)
                    .addCallback(ContentJobItemTriggersCallback())
                    .addMigrations(*UmAppDatabase.migrationList(nodeIdAndAuth.nodeId).toTypedArray())
                .build()
            db.addIncomingReplicationListener(PermissionManagementIncomingReplicationListener(db))

            //Add listener that will end sessions when authentication has been updated
            db.addIncomingReplicationListener(EndSessionPersonAuth2IncomingReplicationListener(db))
            runBlocking {
                db.connectivityStatusDao.insertAsync(ConnectivityStatus().apply {
                    connectivityState = ConnectivityStatus.STATE_UNMETERED
                    connectedOrConnecting = true
                })
            }
            db
        }


        bind<EpubTypePluginCommonJvm>() with scoped(EndpointScope.Default).singleton{
            EpubTypePluginCommonJvm(Any(), context, di, DummyContentPluginUploader())
        }

        bind<XapiTypePluginCommonJvm>() with scoped(EndpointScope.Default).singleton{
            XapiTypePluginCommonJvm(Any(), context, di, DummyContentPluginUploader())
        }

        bind<H5PTypePluginCommonJvm>() with scoped(EndpointScope.Default).singleton{
            H5PTypePluginCommonJvm(Any(), context, di, DummyContentPluginUploader())
        }
        bind<VideoTypePluginJvm>() with scoped(EndpointScope.Default).singleton{
            VideoTypePluginJvm(Any(), context, di, DummyContentPluginUploader())
        }
        bind<ApacheIndexerPlugin>() with scoped(EndpointScope.Default).singleton{
            ApacheIndexerPlugin(Any(), context, di)
        }

        bind<ContentPluginManager>() with scoped(EndpointScope.Default).singleton {
            ContentPluginManager(listOf(
                    di.on(context).direct.instance<EpubTypePluginCommonJvm>(),
                    di.on(context).direct.instance<XapiTypePluginCommonJvm>(),
                    di.on(context).direct.instance<H5PTypePluginCommonJvm>(),
                    di.on(context).direct.instance<VideoTypePluginJvm>(),
                    di.on(context).direct.instance<ApacheIndexerPlugin>()))
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(EndpointScope.Default).singleton {
            val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
            val doorNode = instance<NodeIdAndAuth>()
            val repo: UmAppDatabase = db.asRepository(repositoryConfig(Any(), "http://localhost/",
                doorNode.nodeId, doorNode.auth, instance(), instance()) {
                useReplicationSubscription = false
            })

            runBlocking { repo.preload() }
            repo.ktorInitRepo(di)
            runBlocking {
                repo.initAdminUser(context, di)
            }
            repo
        }

        bind<Scheduler>() with singleton {
            val dbProperties = environment.config.databasePropertiesFromSection("quartz",
                "jdbc:sqlite:data/quartz.sqlite?journal_mode=WAL&synchronous=OFF&busy_timeout=30000")
            InitialContext().apply {
                bindDataSourceIfNotExisting("quartzds", dbProperties)
                initQuartzDb("java:/comp/env/jdbc/quartzds")
            }
            StdSchedulerFactory.getDefaultScheduler().also {
                it.context.put("di", di)
            }
        }

        bind<ConnectivityLiveData>() with scoped(EndpointScope.Default).singleton {
            val db: UmAppDatabase = on(context).instance(tag = DoorTag.TAG_DB)
            ConnectivityLiveData(db.connectivityStatusDao.statusLive())
        }

        bind<UstadMobileSystemImpl>() with singleton {
            UstadMobileSystemImpl(instance(tag  = DiTag.XPP_FACTORY_NSAWARE), dataDirPath)
        }

        bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
            XmlPullParserFactory.newInstance().also {
                it.isNamespaceAware = true
            }
        }

        bind<Pbkdf2Params>() with singleton {
            val systemImpl: UstadMobileSystemImpl = instance()
            val numIterations = systemImpl.getAppConfigInt(
                AppConfig.KEY_PBKDF2_ITERATIONS,
                UstadMobileConstants.PBKDF2_ITERATIONS, context)
            val keyLength = systemImpl.getAppConfigInt(
                AppConfig.KEY_PBKDF2_KEYLENGTH,
                UstadMobileConstants.PBKDF2_KEYLENGTH, context)

            Pbkdf2Params(numIterations, keyLength)
        }

        bind<AuthManager>() with scoped(EndpointScope.Default).singleton {
            AuthManager(context, di)
        }

        bind<UploadSessionManager>() with scoped(EndpointScope.Default).singleton {
            UploadSessionManager(context, di)
        }

        bind<ContentJobManager>() with singleton {
            ContentJobManagerJvm(di)
        }

        bind<Json>() with singleton {
            Json { encodeDefaults = true }
        }

        bind<File>(tag = DiTag.TAG_FILE_FFMPEG) with singleton {
            //The availability of ffmpeg is checked on startup
            SysPathUtil.findCommandInPath("ffmpeg",
                manuallySpecifiedLocation = appConfig.commandFileProperty("ffmpeg"))!!
        }

        bind<File>(tag = DiTag.TAG_FILE_FFPROBE) with singleton {
            //The availability of ffmpeg is checked on startup
            SysPathUtil.findCommandInPath("ffprobe",
                manuallySpecifiedLocation = appConfig.commandFileProperty("ffprobe"))!!
        }

        bind<File>(tag = DiTag.TAG_FILE_UPLOAD_TMP_DIR) with scoped(EndpointScope.Default).singleton {
            File(instance<File>(tag = TAG_CONTEXT_DATA_ROOT), UPLOAD_TMP_SUBDIR).also {
                if(!it.exists())
                    it.mkdirs()
            }
        }

        try {
            appConfig.config("mail")

            bind<MailProperties>() with singleton {
                MailProperties(appConfig.property("mail.from").getString(),
                    appConfig.toProperties(MailProperties.SMTP_PROPS))
            }

            bind<NotificationSender>() with singleton {
                NotificationSender(di)
            }

            bind<Authenticator>() with singleton {
                object: Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(
                            appConfig.property("mail.user").getString(),
                            appConfig.property("mail.auth").getString())
                    }
                }
            }
        }catch(e: Exception) {
            Napier.w("WARNING: Email sending not configured")
        }

        registerContextTranslator { call: ApplicationCall ->
            appConfig.dbModeToEndpoint(call, dbMode)
        }

        onReady {
            if(dbMode == CONF_DBMODE_SINGLETON) {
                //Get the container dir so that any old directories (build/storage etc) are moved if required
                di.on(Endpoint("localhost")).direct.instance<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)
            }

            instance<Scheduler>().start()
            Runtime.getRuntime().addShutdownHook(Thread{
                instance<Scheduler>().shutdown()
            })
        }
    }

    //Ensure that older clients that make http calls to pages that no longer exist will not make
    // an infinite number of calls and exhaust their data bundle etc.
    install(StatusPages) {
        status(HttpStatusCode.NotFound) {
            Napier.e("NOT FOUND! ${call.request.uri}!")
            call.respondText("Not found", ContentType.Text.Plain, HttpStatusCode.NotFound)
        }
    }

    val jsDevServer = appConfig.propertyOrNull("ktor.ustad.jsDevServer")?.getString()
    if(jsDevServer != null) {
        install(WebSockets)

        intercept(ApplicationCallPipeline.Setup) {
            val requestUri = call.request.uri.let {
                if(it.startsWith("//")) {
                    //This is an edge case with the ContainerFetcher. The ContainerFetcher uses //
                    // at the start of a URI. This workaround will be removed when ContainerFetcher
                    // is removed and replaced with Retriever.
                    it.removePrefix("/")
                }else {
                    it
                }
            }

            if(!KTOR_SERVER_ROUTES.any { requestUri.startsWith(it) }) {
                call.respondReverseProxy(jsDevServer)
                return@intercept finish()
            }
        }
    }

    /**
     * Note: to facilitate Javascript development, make sure that any route prefixes used are listed
     * in UstadAppReactProxy
     */
    install(Routing) {
        ContainerDownload()
        personAuthRegisterRoute()
        ContainerMountRoute()
        ContainerUploadRoute2()
        route("UmAppDatabase") {
            UmAppDatabase_KtorRoute()
        }
        SiteRoute()
        ContentEntryLinkImporter()
        ContentUploadRoute()

        GetAppRoute()

        if (devMode) {
            DevModeRoute()
        }

        static("umapp") {
            resources("umapp")
            static("/") {
                defaultResource("umapp/index.html")
            }
        }

        //Handle default route when running behind proxy
        if(!jsDevServer.isNullOrBlank()) {
            webSocketProxyRoute(jsDevServer)
        }else {
            route("/"){
                get{
                    call.respondRedirect("umapp/")
                }
            }
        }
    }
}

