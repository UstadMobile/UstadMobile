package com.ustadmobile.util.test

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry

abstract class AbstractContentEntryExportTest {

    var container: Container? = null

    var contentEntryUid: Long?  = null

    fun insertContainer(db: UmAppDatabase, repo: UmAppDatabase){
        val entry = ContentEntry()
        entry.title = "Sample Entry Title"
        entry.leaf = true
        entry.contentEntryUid = db.contentEntryDao.insert(entry)
        container = Container()
        container!!.containerContentEntryUid = entry.contentEntryUid
        container!!.containerUid = repo.containerDao.insert(container!!)
        contentEntryUid = entry.contentEntryUid
    }
}