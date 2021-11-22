package com.ustadmobile.test.util.ext

import org.mockito.kotlin.spy
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.door.ext.clearAllTablesAndResetSync
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import kotlinx.coroutines.runBlocking
import org.kodein.di.*
import javax.naming.InitialContext
import kotlin.random.Random

fun DI.Builder.bindDbAndRepoWithEndpoint(endpointScope: EndpointScope, clientMode: Boolean = true) {
    bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
        NodeIdAndAuth(Random.nextInt(0, Int.MAX_VALUE), randomUuid().toString())
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
        val dbName = sanitizeDbNameFromUrl(context.url)
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
        spy(DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, dbName)
            .addSyncCallback(nodeIdAndAuth, false)
            .build()
            .clearAllTablesAndResetSync(nodeIdAndAuth.nodeId)
            .also { runBlocking { it.preload() } })
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        val endpointUrl = if(clientMode) { context.url } else { "http://localhost/dummy" }
        val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
        spy(db.asRepository(repositoryConfig(Any(), endpointUrl, nodeIdAndAuth.nodeId,
            nodeIdAndAuth.auth, instance(), instance()))
        ).also {
            it.siteDao.insert(Site().apply {
                siteName = "Test"
                authSalt = randomString(16)
            })
        }
    }

}