package com.ustadmobile.lib.rest.dimodules

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.EndSessionPersonAuth2IncomingReplicationListener
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.rest.InsertDefaultSiteCallback
import com.ustadmobile.lib.rest.ext.dbModeProperty
import com.ustadmobile.lib.rest.ext.initAdminUser
import com.ustadmobile.lib.rest.identifier
import io.github.aakira.napier.Napier
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking
import org.kodein.di.*
import java.io.File
import com.ustadmobile.door.ext.addIncomingReplicationListener
import com.ustadmobile.lib.rest.ext.absoluteDataDir
import org.xmlpull.v1.XmlPullParserFactory

/**
 * DI Module that provides dependencies which are used both by the server and command line tools
 * e.g. password reset, any import/export, etc.
 */
fun makeJvmBackendDiModule(
    config: ApplicationConfig,
    syncEnabled: Boolean = true,
    contextScope: EndpointScope = EndpointScope.Default,
) = DI.Module("JvmBackendDiModule") {
    val dataDirPath = config.absoluteDataDir()

    val dbMode = config.dbModeProperty()

    bind<File>(tag = DiTag.TAG_CONTEXT_DATA_ROOT) with scoped(contextScope).singleton {
        File(dataDirPath, context.identifier(dbMode)).also {
            it.takeIf { !it.exists() }?.mkdirs()
        }
    }

    bind<UstadMobileSystemImpl>() with singleton {
        UstadMobileSystemImpl(instance(tag  = DiTag.XPP_FACTORY_NSAWARE), dataDirPath)
    }

    bind<AuthManager>() with scoped(contextScope).singleton {
        AuthManager(context, di).also { authManager ->
            val repo: UmAppDatabase = on(context).instance(tag = DoorTag.TAG_REPO)
            runBlocking {
                repo.initAdminUser(context, authManager, di,
                    config.propertyOrNull("ktor.ustad.adminpass")?.getString())
            }
        }
    }

    bind<Pbkdf2Params>() with singleton {
        val numIterations = UstadMobileConstants.PBKDF2_ITERATIONS
        val keyLength = UstadMobileConstants.PBKDF2_KEYLENGTH

        Pbkdf2Params(numIterations, keyLength)
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(contextScope).singleton {
        Napier.d("creating database for context: ${context.url}")
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
            )
            .addSyncCallback(nodeIdAndAuth)
            .addCallback(ContentJobItemTriggersCallback())
            .addCallback(InsertDefaultSiteCallback())
            .addMigrations(*migrationList().toTypedArray())
            .build()

        if(syncEnabled) {

            //Add listener that will end sessions when authentication has been updated
            db.addIncomingReplicationListener(EndSessionPersonAuth2IncomingReplicationListener(db))
            runBlocking {
                db.connectivityStatusDao.insertAsync(ConnectivityStatus().apply {
                    connectivityState = ConnectivityStatus.STATE_UNMETERED
                    connectedOrConnecting = true
                })
            }
        }

        db
    }

    bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
        val systemImpl: UstadMobileSystemImpl = instance()
        val contextIdentifier: String = context.identifier(dbMode)
        systemImpl.getOrGenerateNodeIdAndAuth(contextIdentifier, Any())
    }

    bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
        XmlPullParserFactory.newInstance().also {
            it.isNamespaceAware = true
        }
    }
}
