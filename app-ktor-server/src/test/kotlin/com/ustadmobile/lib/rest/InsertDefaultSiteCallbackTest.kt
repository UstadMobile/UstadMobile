package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import org.junit.Assert
import org.junit.Test


class InsertDefaultSiteCallbackTest {

    @Test
    fun givenDatabaseCreated_thenWhenOpened_itShouldBloodyWellBeThereFfs() {
        val dbUrl = "jdbc:sqlite::memory:"
        val nodeIdAndAuth = NodeIdAndAuth(42, "secret")
        val umAppDb = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, dbUrl,
                nodeId = nodeIdAndAuth.nodeId)
            .addSyncCallback(nodeIdAndAuth)
            .addCallback(InsertDefaultSiteCallback())
            .addMigrations(*migrationList().toTypedArray())
            .build()

        val defaultSite = umAppDb.siteDao().getSite()
        Assert.assertNotNull(defaultSite)
    }

}