package com.ustadmobile.lib.rest.dimodules

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.contentformats.epub.XhtmlFixer
import com.ustadmobile.core.contentformats.epub.XhtmlFixerJsoup
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.domain.cachelock.AddRetainAllActiveUriTriggersCallback
import com.ustadmobile.core.domain.cachelock.CreateCacheLocksForActiveContentEntryVersionUseCase
import com.ustadmobile.core.domain.cachelock.Migrate131to132AddRetainActiveUriTriggers
import com.ustadmobile.core.domain.cachelock.UpdateCacheLockJoinUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CreateRetentionLocksForManifestUseCaseCommonJvm
import com.ustadmobile.core.domain.message.AddOutgoingReplicationForMessageTriggerCallback
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemImpl
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
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import org.xmlpull.v1.XmlPullParserFactory
import java.io.FileReader
import java.io.FileWriter
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
    contextScope: EndpointScope = EndpointScope.Default,
) = DI.Module("JvmBackendDiModule") {
    val dataDirPath = config.absoluteDataDir()
    dataDirPath.takeIf { !it.exists() }?.mkdirs()

    val dbMode = config.dbModeProperty()

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
}
