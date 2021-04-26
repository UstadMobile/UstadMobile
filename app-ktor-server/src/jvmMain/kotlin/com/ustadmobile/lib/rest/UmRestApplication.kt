package com.ustadmobile.lib.rest

import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.google.gson.Gson
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.catalog.contenttype.*
import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.core.contentformats.ContentImportManagerImpl
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.io.UploadSessionManager
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.DiTag.TAG_CONTEXT_DATA_ROOT
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.*
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.contentscrapers.abztract.ScraperManager
import com.ustadmobile.lib.rest.ext.bindHostDatabase
import com.ustadmobile.lib.rest.ext.ktorInitDbWithRepo
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.request.header
import io.ktor.routing.Routing
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.kodein.di.*
import org.kodein.di.ktor.DIFeature
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit
import javax.naming.InitialContext
import io.ktor.client.features.json.JsonFeature

const val TAG_UPLOAD_DIR = 10

const val CONF_DBMODE_VIRTUALHOST = "virtualhost"

const val CONF_DBMODE_SINGLETON = "singleton"

const val CONF_GOOGLE_API = "secret"

/**
 * Returns an identifier that is used as a subdirectory for data storage (e.g. attachments,
 * containers, etc).
 */
private fun Endpoint.identifier(dbMode: String, singletonName: String = CONF_DBMODE_SINGLETON) = if(dbMode == CONF_DBMODE_SINGLETON) {
    singletonName
}else {
    sanitizeDbNameFromUrl(url)
}

fun Application.umRestApplication(dbModeOverride: String? = null,
                                  singletonDbName: String = "UmAppDatabase") {

    val devMode = environment.config.propertyOrNull("ktor.ustad.devmode")?.getString().toBoolean()

    if (devMode) {
        install(CORS) {
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Put)
            method(HttpMethod.Options)
            header(HttpHeaders.ContentType)
            header(HttpHeaders.AccessControlAllowOrigin)
            header("X-nid")
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

    val tmpRootDir = Files.createTempDirectory("upload").toFile()

    val dbMode = dbModeOverride ?:
        environment.config.propertyOrNull("ktor.ustad.dbmode")?.getString() ?: CONF_DBMODE_SINGLETON
    val dataDirPath = File(environment.config.propertyOrNull("ktor.ustad.datadir")?.getString() ?: "data")
    dataDirPath.takeIf { !it.exists() }?.mkdirs()

    val apiKey = environment.config.propertyOrNull("ktor.ustad.googleApiKey")?.getString() ?: CONF_GOOGLE_API

    install(DIFeature) {

        bind<UstadMobileSystemImpl>() with singleton { UstadMobileSystemImpl.instance }

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
            val dbHostName = context.identifier(dbMode, singletonDbName)
            val appConfig = environment.config
            InitialContext().bindHostDatabase(dbHostName, Properties().apply {
                setProperty("driver",
                    appConfig.propertyOrNull("ktor.database.driver")?.getString() ?: "org.sqlite.JDBC")
                setProperty("url",
                    appConfig.propertyOrNull("ktor.database.url")?.getString()
                        ?: "jdbc:sqlite:data/singleton/UmAppDatabase.sqlite?journal_mode=WAL&synchronous=OFF&busy_timeout=30000")
                setProperty("user",
                    appConfig.propertyOrNull("ktor.database.user")?.getString() ?: "")
                setProperty("password",
                    appConfig.propertyOrNull("ktor.database.password")?.getString() ?: "")
            })

            UmAppDatabase.getInstance(Any(), dbHostName)
        }

        bind<ServerUpdateNotificationManager>() with scoped(EndpointScope.Default).singleton {
            ServerUpdateNotificationManagerImpl()
        }

        bind<OkHttpClient>() with singleton {
            OkHttpClient.Builder()
                .dispatcher(Dispatcher().also {
                    it.maxRequests = 30
                    it.maxRequestsPerHost = 10
                })
                .connectTimeout(45, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .build()
        }


        bind<HttpClient>() with singleton {
            HttpClient(OkHttp){
                install(JsonFeature)
                install(HttpTimeout)

                engine {
                    preconfigured = instance()
                }
            }
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(EndpointScope.Default).singleton {
            val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
            val repo = db.asRepository(repositoryConfig(Any(), "http://localhost/",
                instance(), instance()) {
                attachmentsDir = File(instance<File>(tag = TAG_CONTEXT_DATA_ROOT),
                    UstadMobileSystemCommon.SUBDIR_ATTACHMENTS_NAME).absolutePath
                updateNotificationManager = instance()
            })
            ServerChangeLogMonitor(db, repo as DoorDatabaseRepository)
            repo.preload()
            db.ktorInitDbWithRepo(repo, instance<File>(tag = TAG_CONTEXT_DATA_ROOT).absolutePath)
            repo
        }

        bind<ScraperManager>() with scoped(EndpointScope.Default).singleton {
            ScraperManager(endpoint = context, di = di)
        }

        bind<ContentImportManager>() with scoped(EndpointScope.Default).singleton{
            ContentImportManagerImpl(listOf(EpubTypePluginCommonJvm(),
                    XapiTypePluginCommonJvm(), VideoTypePluginJvm(),
                    H5PTypePluginCommonJvm()),
                    Any(), context, di)
        }

        bind<UploadSessionManager>() with scoped(EndpointScope.Default).singleton {
            UploadSessionManager(context, di)
        }

        registerContextTranslator { call: ApplicationCall ->
            if(dbMode == CONF_DBMODE_SINGLETON) {
                Endpoint("localhost")
            }else {
                Endpoint(call.request.header("Host") ?: "localhost")
            }
        }

        onReady {
            if(dbMode == CONF_DBMODE_SINGLETON) {
                //Get the container dir so that any old directories (build/storage etc) are moved if required
                di.on(Endpoint("localhost")).direct.instance<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)
            }
        }
    }

    install(Routing) {
        ContainerDownload()
        PersonAuthRegisterRoute()
        ContainerMountRoute()
        ContainerUploadRoute2()
        UmAppDatabase_KtorRoute(true)
        SiteRoute()
        ContentEntryLinkImporter()
        if (devMode) {
            DevModeRoute()
        }
    }
}

