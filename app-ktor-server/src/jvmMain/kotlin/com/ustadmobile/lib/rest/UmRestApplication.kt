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
import com.ustadmobile.core.io.UploadSessionManager
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.DiTag.TAG_CONTEXT_DATA_ROOT
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.*
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.lib.contentscrapers.abztract.ScraperManager
import com.ustadmobile.lib.rest.ext.ktorInitDbWithRepo
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
 * Returns an identifier that is used as a subdirectory for data storage (e.g. attachments,
 * containers, etc).
 */
private fun Endpoint.identifier(dbMode: String, singletonName: String = CONF_DBMODE_SINGLETON) = if(dbMode == CONF_DBMODE_SINGLETON) {
    singletonName
}else {
    sanitizeDbNameFromUrl(url)
}

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
    Napier.takeLogarithm()
    Napier.base(DebugAntilog())

    install(ContentNegotiation) {
        gson {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }
    }

    val tmpRootDir = Files.createTempDirectory("upload").toFile()

    val autoCreateDb = environment.config.propertyOrNull("ktor.ustad.autocreatedb")?.getString()?.toBoolean() ?: false
    println("auto create = $autoCreateDb")
    val dbMode = dbModeOverride ?:
        environment.config.propertyOrNull("ktor.ustad.dbmode")?.getString() ?: CONF_DBMODE_SINGLETON
    val dataDirPath = File(environment.config.propertyOrNull("ktor.ustad.datadir")?.getString() ?: "data")
    dataDirPath.takeIf { !it.exists() }?.mkdirs()

    val apiKey = environment.config.propertyOrNull("ktor.ustad.googleApiKey")?.getString() ?: CONF_GOOGLE_API

    install(DIFeature) {
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
            val dbName = context.identifier(dbMode, singletonDbName)

            if(autoCreateDb) {
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName,
                        isPrimary = true, sqliteDir = instance(tag = TAG_CONTEXT_DATA_ROOT))
            }

            UmAppDatabase.getInstance(Any(), dbName)
        }

        bind<ServerUpdateNotificationManager>() with scoped(EndpointScope.Default).singleton {
            ServerUpdateNotificationManagerImpl()
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(EndpointScope.Default).singleton {
            val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
            val attachmentsDir = File(instance<File>(tag = TAG_CONTEXT_DATA_ROOT),
                UstadMobileSystemCommon.SUBDIR_ATTACHMENTS_NAME)
            val repo = db.asRepository(Any(), "http://localhost/",
                    "", defaultHttpClient(), attachmentsDir.absolutePath,
                    instance(), false)
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

