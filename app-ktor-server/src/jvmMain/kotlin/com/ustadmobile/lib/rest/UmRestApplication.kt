package com.ustadmobile.lib.rest

import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.google.gson.Gson
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.*
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.lib.contentscrapers.abztract.ScraperManager
import com.ustadmobile.lib.rest.ext.ktorInit
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
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
import org.kodein.di.*
import org.kodein.di.ktor.DIFeature
import java.io.File
import java.nio.file.Files
import javax.naming.InitialContext

const val TAG_UPLOAD_DIR = 10

const val CONF_DBMODE_VIRTUALHOST = "virtualhost"

const val CONF_DBMODE_SINGLETON = "singleton"

const val CONF_GOOGLE_API = "secret"

/**
 *
 */
private fun Endpoint.identifier(dbMode: String, singletonName: String = CONF_DBMODE_SINGLETON) = if(dbMode == CONF_DBMODE_SINGLETON) {
    singletonName
}else {
    sanitizeDbNameFromUrl(url)
}

@ExperimentalStdlibApi
fun Application.umRestApplication(devMode: Boolean = false, dbModeOverride: String? = null,
                                  singletonDbName: String = "UmAppDatabase") {

    if (devMode) {
        install(CORS) {
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Put)
            method(HttpMethod.Options)
            header(HttpHeaders.ContentType)
            anyHost()
        }
    }

    install(CallLogging)

    //TODO: Put in a proper log filter here
    Napier.base(DebugAntilog())

    install(ContentNegotiation) {
        gson {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }
    }

    val tmpRootDir = Files.createTempDirectory("upload").toFile()
    val iContext = InitialContext()
    val containerDirPath = iContext.lookup("java:/comp/env/ustadmobile/app-ktor-server/containerDirPath") as? String
            ?: "./build/container"

    val autoCreateDb = environment.config.propertyOrNull("ktor.ustad.autocreatedb")?.getString()?.toBoolean() ?: false
    println("auto create = $autoCreateDb")
    val dbMode = dbModeOverride ?:
        environment.config.propertyOrNull("ktor.ustad.dbmode")?.getString() ?: CONF_DBMODE_SINGLETON
    val storageRoot = File(environment.config.propertyOrNull("ktor.ustad.storagedir")?.getString() ?: "build/storage")
    storageRoot.takeIf { !it.exists() }?.mkdirs()

    val apiKey = environment.config.propertyOrNull("ktor.ustad.googleApiKey")?.getString() ?: CONF_GOOGLE_API

    install(DIFeature) {
        bind<File>(tag = TAG_UPLOAD_DIR) with scoped(EndpointScope.Default).singleton {
            File(tmpRootDir, context.identifier(dbMode)).also {
                it.takeIf { !it.exists() }?.mkdirs()
            }
        }

        bind<File>(tag = DiTag.TAG_CONTAINER_DIR) with scoped(EndpointScope.Default).singleton {
            File(File(storageRoot, context.identifier(dbMode)), "container").also {
                it.takeIf { !it.exists() }?.mkdirs()
            }
        }

        bind<String>(tag = DiTag.TAG_GOOGLE_API) with singleton {
            apiKey
        }

        bind<Gson>() with singleton { Gson() }

        bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(EndpointScope.Default).singleton {
            val dbName = context.identifier(dbMode, singletonDbName)

            if(autoCreateDb) {
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName,
                        isPrimary = true, sqliteDir = File(storageRoot, context.identifier(dbMode)))
            }

            UmAppDatabase.getInstance(Any(), dbName).also {
                it.preload()
                it.ktorInit(File(storageRoot, context.identifier(dbMode)).absolutePath)
            }
        }

        bind<ServerUpdateNotificationManager>() with scoped(EndpointScope.Default).singleton {
            ServerUpdateNotificationManagerImpl()
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(EndpointScope.Default).singleton {
            val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
            val repo = db.asRepository(Any(), "http://localhost/",
                "", defaultHttpClient(), File(".").absolutePath,
                instance(), false)
            ServerChangeLogMonitor(db, repo as DoorDatabaseRepository)
            repo
        }

        bind<ScraperManager>() with scoped(EndpointScope.Default).singleton {
            ScraperManager(endpoint = context, di = di)
        }

        registerContextTranslator { call: ApplicationCall -> Endpoint(call.request.header("Host") ?: "nohost") }
    }

    install(Routing) {
        ContainerDownload()
        PersonAuthRegisterRoute()
        ContainerMountRoute()
        ResumableUploadRoute()
        ContainerUpload()
        UmAppDatabase_KtorRoute(true)
        WorkSpaceRoute()
        ContentEntryLinkImporter()
        if (devMode) {
            DevModeRoute()
        }
    }
}

