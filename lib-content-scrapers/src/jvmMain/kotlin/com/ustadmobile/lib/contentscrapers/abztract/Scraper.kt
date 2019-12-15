package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import java.io.File

abstract class Scraper(val containerDir: File, var db: UmAppDatabase, var contentEntryUid: Long) {

    abstract fun isContentUpdated(): Boolean

    abstract fun scrapeUrl(url: String, tmpLocation: File)

    abstract fun close()

    fun createBaseContainer(): Container{
        val container = Container()
        container.containerContentEntryUid = contentEntryUid
        container.containerUid = db.containerDao.insert(container)
        return container
    }

}