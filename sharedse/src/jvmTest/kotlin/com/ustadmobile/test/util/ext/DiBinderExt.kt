package com.ustadmobile.test.util.ext

import org.mockito.kotlin.spy
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.preload
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import org.kodein.di.*
import javax.naming.InitialContext
import kotlin.random.Random
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import kotlinx.coroutines.runBlocking

fun DI.Builder.bindDbAndRepoWithEndpoint(endpointScope: EndpointScope, clientMode: Boolean = true) {
    bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
        NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
        val dbName = sanitizeDbNameFromUrl(context.url)
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
        spy(DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite:build/tmp/$dbName.sqlite")
            .addSyncCallback(nodeIdAndAuth)
            .build()
            .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
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