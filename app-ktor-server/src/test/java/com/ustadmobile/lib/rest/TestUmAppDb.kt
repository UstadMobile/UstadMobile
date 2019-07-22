package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContentEntry
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.junit.Test

class TestUmAppDb {


    fun testModule() {
        val db = UmAppDatabase.getInstance(Any())
        db.clearAllTables()
        val contentEntry = ContentEntry("Blah", "blah", true, false)
        contentEntry.contentEntryUid = 42L
        db.contentEntryDao.insert(contentEntry)
        embeddedServer(Netty, 8088, module = { UmAppDatabaseModule(db) }).start(wait = true)
    }

}