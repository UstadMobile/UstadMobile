package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.*
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.*
import com.ustadmobile.door.util.NodeIdAuthCache
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.random.Random
import org.junit.rules.TemporaryFolder
import javax.naming.InitialContext

/**
 * Creates a DI Module with per-context dependencies (e.g. where a test is simulating client/server
 * or multiple client interactions, each should have it's own DI context-specific module)
 *
 * @param tempFolder TemporaryFolder rule that will be used to create temp folders for the database
 *        attachments dir, systemimpl, etc.
 * @param dbNameSuffix This suffix will be added to the name of the database when looking it up
 *        through JNDI. This allows multiple client DIs to have their own instance of a database that
 *        would otherwise have the same JNDI/JDBC name.
 * @param replicationSubscriptionEnabled if true, then use the replication subscription on the
 *        repository.
 */
fun ustadTestContextModule(
    tempFolder: TemporaryFolder,
    dbNameSuffix: String,
    //"Client" mode means use the replication subscription
    replicationSubscriptionEnabled: Boolean,
    endpointScope: EndpointScope,
) : DI.Module = DI.Module("UstadTest-Db-$dbNameSuffix") {
    bind<UstadMobileSystemImpl>() with singleton {
        UstadMobileSystemImpl(instance(tag  = DiTag.XPP_FACTORY_NSAWARE),
            tempFolder.newFolder())
    }

    bind<UstadAccountManager>() with singleton {
        UstadAccountManager(instance(), Any(), di)
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        val dbName = sanitizeDbNameFromUrl(context.url) + dbNameSuffix
        InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
        DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, dbName)
            .addSyncCallback(nodeIdAndAuth)
            .addCallback(ContentJobItemTriggersCallback())
            .build().also { db ->
                db.clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
                db.addIncomingReplicationListener(PermissionManagementIncomingReplicationListener(db))
            }
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
        val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
        val doorNode = instance<NodeIdAndAuth>()
        val repo: UmAppDatabase = db.asRepository(
            RepositoryConfig.repositoryConfig(
                Any(), context.url("/UmAppDatabase/"),
                doorNode.nodeId, doorNode.auth, instance(), instance()
            ) {
                useReplicationSubscription = replicationSubscriptionEnabled
                if(replicationSubscriptionEnabled)
                    replicationSubscriptionInitListener = RepSubscriptionInitListener()

                attachmentsDir = tempFolder.newFolder().absolutePath
            }).also {
            if(replicationSubscriptionEnabled) {
                it.siteDao.insert(Site().apply {
                    siteName = "Test"
                    authSalt = randomString(16)
                })
            }
        }

        repo
    }

    bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
        NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), "secret")
    }

    bind<NodeIdAuthCache>() with scoped(endpointScope).singleton {
        instance<UmAppDatabase>(tag = DoorTag.TAG_DB).nodeIdAuthCache
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

    bind<CoroutineScope>(tag = DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with singleton {
        GlobalScope
    }

    bind<ContainerStorageManager>() with scoped(endpointScope).singleton {
        ContainerStorageManager(listOf(tempFolder.newFolder("container_storage_$dbNameSuffix")))
    }
}

/**
 * These are modules that can be shared in tests (even when the tests are representing a different
 * context e.g. client - server, multiple clients, etc). This contains things like XML parsers,
 * http clients, json, etc.
 */
fun ustadTestCommonModule() : DI.Module = DI.Module("Ustad-Test-Common") {
    bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
        XmlPullParserFactory.newInstance().also {
            it.isNamespaceAware = true
        }
    }

    bind<OkHttpClient>() with singleton {
        OkHttpClient.Builder().build()
    }

    bind<HttpClient>() with singleton {
        HttpClient(OkHttp) {
            install(JsonFeature)
            install(HttpTimeout)

            engine {
                preconfigured = instance()
            }
        }
    }

    bind<Json>() with singleton {
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }

    bind<XmlPullParserFactory>(tag = DiTag.XPP_FACTORY_NSUNAWARE) with singleton {
        XmlPullParserFactory.newInstance()
    }

}