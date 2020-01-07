package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.MimeType
import com.ustadmobile.core.util.mimeTypeToPlayStoreIdMap
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import java.io.File

abstract class Scraper(val containerDir: File, var db: UmAppDatabase, var contentEntryUid: Long) {

    val mimeTypeToContentFlag: Map<String, Int> = mapOf(
            MimeType.PDF to ContentEntry.DOCUMENT_TYPE,
            MimeType.MPEG to ContentEntry.AUDIO_TYPE,
            MimeType.WEB_CHUNK to ContentEntry.INTERACTIVE_EXERICSE_TYPE,
            MimeType.KHAN_VIDEO to ContentEntry.VIDEO_TYPE,
            MimeType.MP4 to ContentEntry.VIDEO_TYPE,
            MimeType.MKV to ContentEntry.VIDEO_TYPE,
            MimeType.WEBM to ContentEntry.VIDEO_TYPE,
            MimeType.EPUB to ContentEntry.EBOOK_TYPE
    )

    abstract fun isContentUpdated(): Boolean

    abstract fun scrapeUrl(sourceUrl: String)

    abstract fun close()

    fun createBaseContainer(mimeType: String): Container {
        val container = Container()
        container.containerContentEntryUid = contentEntryUid
        container.mimeType = mimeType
        container.mobileOptimized = true
        container.cntLastModified = System.currentTimeMillis()
        container.containerUid = db.containerDao.insert(container)
        return container
    }

}