package com.ustadmobile.lib.contentscrapers.folder

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.File
import java.lang.Exception
import java.net.URI

@ExperimentalStdlibApi
class FolderIndexer(parentContentEntryUid: Long, runUid: Int, db: UmAppDatabase, sqiUid: Int, contentEntryUid: Long) : Indexer(parentContentEntryUid, runUid, db, sqiUid, contentEntryUid) {

    override fun indexUrl(sourceUrl: String) {

        try {

            val folder = File(sourceUrl)

            UMLogUtil.logInfo("folder generated ${folder.name}")

            val fileList = folder.listFiles()

            val name = folder.nameWithoutExtension

            val folderEntry = ContentScraperUtil.createOrUpdateContentEntry(name, name,
                    folder.path, name, ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN, englishLang.langUid, null,
                    ScraperConstants.EMPTY_STRING, false, ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                    ScraperConstants.EMPTY_STRING, ContentEntry.TYPE_COLLECTION, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentcontentEntry, folderEntry, 0)

            if (fileList == null || fileList.isEmpty()) {
                close()
                return
            }

            fileList.forEach { file ->

                UMLogUtil.logInfo("found file ${file.name}")

                val childEntry = ContentScraperUtil.insertTempContentEntry(contentEntryDao, file.path, folderEntry.primaryLanguageUid, file.name)
                if (file.isDirectory) {
                    createQueueItem(file.path, childEntry, ScraperTypes.FOLDER_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX, folderEntry.contentEntryUid)
                } else if (file.isFile) {
                    createQueueItem(file.path, childEntry, ScraperTypes.FOLDER_SCRAPER, ScrapeQueueItem.ITEM_TYPE_SCRAPE, folderEntry.contentEntryUid)
                }
            }
        }catch (e: Exception){
            UMLogUtil.logInfo(ExceptionUtils.getStackTrace(e))
            setIndexerDone(false, 0)
            return
        }
        close()
    }

    override fun close() {
        setIndexerDone(true, 0)
    }
}