package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.account.*
import com.ustadmobile.core.catalog.contenttype.*
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentJobManagerJvm
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.core.networkmanager.ConnectivityLiveData
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.DiTag.TAG_CONTEXT_DATA_ROOT
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.door.*
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.*
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.io.UploadSessionManager
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.rest.ext.*
import com.ustadmobile.lib.rest.messaging.MailProperties
import com.ustadmobile.lib.util.ext.bindDataSourceIfNotExisting
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.github.aakira.napier.Napier
import io.ktor.server.application.*
import io.ktor.serialization.gson.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import kotlinx.coroutines.runBlocking
import org.kodein.di.*
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.nio.file.Files
import javax.naming.InitialContext
import com.ustadmobile.door.util.NodeIdAuthCache
import com.ustadmobile.core.db.PermissionManagementIncomingReplicationListener
import com.ustadmobile.core.contentjob.DummyContentPluginUploader
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.db.ext.preload
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import com.ustadmobile.lib.util.SysPathUtil
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.websocket.*
import org.kodein.di.ktor.di
import java.util.*
import com.ustadmobile.lib.rest.logging.LogbackAntiLog

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
val KTOR_SERVER_ROUTES = listOf(
    "/UmAppDatabase", "/ConcatenatedContainerFiles2",
    "/ContainerEntryList", "/ContainerEntryFile", "/auth", "/ContainerMount",
    "/ContainerUpload2", "/Site", "/import", "/contentupload", "/websocket", "/pdf",
    "/api"
)


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

@Suppress("unused") // This is used as the KTOR server main module via application.conf
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
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Options)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.AccessControlAllowOrigin)
            allowHeader("X-nid")
            allowHeader("door-dbversion")
            allowHeader("door-node")
            anyHost()
        }
    }

    //Uncomment if needed -generates a lot of output
    //install(CallLogging)

    Napier.takeLogarithm()
    Napier.base(LogbackAntiLog())

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

    di {
        import(CommonJvmDiModule)
        bind<SupportedLanguagesConfig>() with singleton { SupportedLanguagesConfig() }

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
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            val attachmentsDir = File(instance<File>(tag = TAG_CONTEXT_DATA_ROOT),
                UstadMobileSystemCommon.SUBDIR_ATTACHMENTS_NAME)
            val dbUrl = appConfig.property("ktor.database.url").getString()
                .replace("(hostname)", dbHostName)
            if(dbUrl.startsWith("jdbc:postgresql"))
                Class.forName("org.postgresql.Driver")

            val db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
                    dbUrl = dbUrl,
                    dbUsername = appConfig.propertyOrNull("ktor.database.user")?.getString(),
                    dbPassword = appConfig.propertyOrNull("ktor.database.password")?.getString(),
                    attachmentDir = attachmentsDir)
                .addSyncCallback(nodeIdAndAuth)
                .addCallback(ContentJobItemTriggersCallback())
                .addCallback(InsertDefaultSiteCallback())
                .addMigrations(*migrationList().toTypedArray())
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
        bind<PDFTypePlugin>() with scoped(EndpointScope.Default).singleton{
            PDFTypePluginJvm(Any(), context, di, DummyContentPluginUploader())
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
                    di.on(context).direct.instance<PDFTypePlugin>(),
                    di.on(context).direct.instance<ApacheIndexerPlugin>()))
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(EndpointScope.Default).singleton {
            val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
            val doorNode = instance<NodeIdAndAuth>()
            db.asRepository(repositoryConfig(Any(), "http://localhost/",
                doorNode.nodeId, doorNode.auth, instance(), instance()) {
                useReplicationSubscription = false
            }).also { repo ->
                runBlocking { repo.preload() }
                repo.ktorInitRepo(di)
            }
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
                AppConfigKeys.KEY_PBKDF2_ITERATIONS,
                UstadMobileConstants.PBKDF2_ITERATIONS, context)
            val keyLength = systemImpl.getAppConfigInt(
                AppConfigKeys.KEY_PBKDF2_KEYLENGTH,
                UstadMobileConstants.PBKDF2_KEYLENGTH, context)

            Pbkdf2Params(numIterations, keyLength)
        }

        bind<AuthManager>() with scoped(EndpointScope.Default).singleton {
            AuthManager(context, di).also { authManager ->
                val repo: UmAppDatabase = on(context).instance(tag = DoorTag.TAG_REPO)
                runBlocking {
                    repo.initAdminUser(context, authManager, di,
                        appConfig.propertyOrNull("ktor.ustad.adminpass")?.getString())
                }
            }

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
            call.callEndpoint
        }

        onReady {
            if(dbMode == CONF_DBMODE_SINGLETON) {
                //Get the container dir so that any old directories (build/storage etc) are moved if required
                di.on(Endpoint("localhost")).direct.instance<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)
                //Generate the admin username/password etc.
                di.on(Endpoint("localhost")).direct.instance<AuthManager>()
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
        status(HttpStatusCode.NotFound) { _: HttpStatusCode ->
            call.respondText("Not found", ContentType.Text.Plain, HttpStatusCode.NotFound)
        }
    }

    val jsDevServer = appConfig.propertyOrNull("ktor.ustad.jsDevServer")?.getString()
    if(jsDevServer != null) {
        install(io.ktor.server.websocket.WebSockets)

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

        route("api") {
            route("pbkdf2"){
                Pbkdf2Route()
            }
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

