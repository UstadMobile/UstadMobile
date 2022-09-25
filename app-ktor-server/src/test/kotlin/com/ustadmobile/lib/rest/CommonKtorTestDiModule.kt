package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.randomUuid
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.random.Random
import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import kotlinx.coroutines.runBlocking

/**
 * Creates a KodeIn DI Module that will contain most of what the test application engine needs to run
 */
fun commonTestKtorDiModule(
    endpointScope: EndpointScope,
    temporaryFolder: TemporaryFolder
) = DI.Module("Common Ktor Test Module") {
    bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
        NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
        val nodeIdAndAuth : NodeIdAndAuth = instance()
        DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite:build/tmp/UmAppDatabase.sqlite")
            .build().also { db ->
                db.clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
                runBlocking {
                    db.connectivityStatusDao.insertAsync(ConnectivityStatus().apply {
                        connectivityState = ConnectivityStatus.STATE_UNMETERED
                        connectedOrConnecting = true
                    })
                }
            }
    }


    bind<Gson>() with singleton {
        Gson()
    }

    bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
        XmlPullParserFactory.newInstance().also {
            it.isNamespaceAware = true
        }
    }

    bind<UstadMobileSystemImpl>() with singleton {
        UstadMobileSystemImpl(instance(tag = DiTag.XPP_FACTORY_NSAWARE),
            temporaryFolder.newFolder())
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
        val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        db.asRepository(
            RepositoryConfig.repositoryConfig(Any(), "http://localhost/", nodeIdAndAuth.nodeId,
                nodeIdAndAuth.auth, instance(), instance()) {
                    useReplicationSubscription = false
            })
    }

    bind<Pbkdf2Params>() with singleton {
        Pbkdf2Params()
    }

    bind<AuthManager>() with scoped(endpointScope).singleton {
        AuthManager(context, di)
    }

}