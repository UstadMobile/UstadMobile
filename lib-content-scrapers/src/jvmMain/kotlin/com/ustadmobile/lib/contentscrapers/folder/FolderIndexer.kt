package com.ustadmobile.lib.contentscrapers.folder

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.alternative
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import com.ustadmobile.port.sharedse.contentformats.mimeTypeSupported
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.kodein.di.DI
import java.io.File
import java.lang.Exception
import java.nio.file.Files

@ExperimentalStdlibApi
class FolderIndexer(parentContentEntryUid: Long, runUid: Int, sqiUid: Int, contentEntryUid: Long, endpoint: Endpoint, di: DI) : Indexer(parentContentEntryUid, runUid, sqiUid, contentEntryUid, endpoint, di) {

    override fun indexUrl(sourceUrl: String) {

        try {

            val folder = File(sourceUrl)

            UMLogUtil.logInfo("folder generated ${folder.name}")

            val fileList = folder.listFiles()

            val name = folder.nameWithoutExtension

            val folderEntry: ContentEntry
            if (scrapeQueueItem?.overrideEntry == true && contentEntry != null) {

                folderEntry = ContentScraperUtil.createOrUpdateContentEntry(contentEntry?.entryId.alternative(name), contentEntry?.title.alternative(name),
                        folder.path, contentEntry?.publisher.alternative(""),
                        contentEntry?.licenseType?.alternative(ContentEntry.LICENSE_TYPE_OTHER)
                                ?: ContentEntry.LICENSE_TYPE_OTHER,
                        contentEntry?.primaryLanguageUid?.alternative(englishLang.langUid)
                                ?: englishLang.langUid,
                        contentEntry?.languageVariantUid,
                        ScraperConstants.EMPTY_STRING, false, contentEntry?.author ?: "", ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                        ScraperConstants.EMPTY_STRING, ContentEntry.TYPE_COLLECTION, contentEntryDao)

            }else{

                folderEntry = ContentScraperUtil.createOrUpdateContentEntry(name, name,
                        folder.path, name, ContentEntry.LICENSE_TYPE_OTHER, englishLang.langUid, null,
                        ScraperConstants.EMPTY_STRING, false, ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                        ScraperConstants.EMPTY_STRING, ContentEntry.TYPE_COLLECTION, contentEntryDao)

            }



            ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentcontentEntry, folderEntry, 0)

            if (fileList == null || fileList.isEmpty()) {
                close()
                return
            }

            fileList.forEach { file ->

                UMLogUtil.logInfo("found file ${file.name}")

                val mimeType = Files.probeContentType(file.toPath())

                mimeTypeSupported.find { fileMimeType -> fileMimeType == mimeType }
                        ?: return@forEach

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