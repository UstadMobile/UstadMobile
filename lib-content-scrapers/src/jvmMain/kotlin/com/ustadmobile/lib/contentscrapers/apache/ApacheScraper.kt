package com.ustadmobile.lib.contentscrapers.apache

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Scraper
import com.ustadmobile.port.sharedse.contentformats.extractContentEntryMetadataFromFile
import com.ustadmobile.port.sharedse.contentformats.importContainerFromFile
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files

@ExperimentalStdlibApi
class ApacheScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long) : Scraper(containerDir, db, contentEntryUid, sqiUid, parentContentEntryUid) {

    var tempDir: File? = null

    override fun scrapeUrl(sourceUrl: String) {

        val url = URL(sourceUrl)

        val conn = url.openConnection() as HttpURLConnection

        val name = sourceUrl.substringAfterLast("/")
        tempDir = Files.createTempDirectory("apache").toFile()
        var file = File(tempDir, name)

        FileUtils.copyInputStreamToFile(conn.inputStream, file)
        conn.disconnect()

        runBlocking {

            val metadata = extractContentEntryMetadataFromFile(file.absolutePath, db)

            if (metadata == null) {
                hideContentEntry()
                setScrapeDone(true, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
                return@runBlocking
            }

            val metadataContentEntry = metadata.contentEntry
            val primaryLanguage = if (metadataContentEntry.primaryLanguageUid == 0L)
                parentContentEntry?.primaryLanguageUid ?: contentEntry?.primaryLanguageUid
                ?: 0 else metadataContentEntry.primaryLanguageUid
            val variant = if (metadataContentEntry.languageVariantUid == 0L)
                parentContentEntry?.languageVariantUid ?: contentEntry?.languageVariantUid
                ?: 0 else metadataContentEntry.languageVariantUid

            val fileEntry = ContentScraperUtil.createOrUpdateContentEntry(
                    metadataContentEntry.entryId ?: contentEntry?.entryId ?: name,
                    metadataContentEntry.title ?: contentEntry?.title ?: name,
                    sourceUrl, metadataContentEntry.publisher ?: parentContentEntry?.publisher
            ?: "",
                    metadataContentEntry.licenseType, primaryLanguage, variant,
                    metadataContentEntry.description, true, ScraperConstants.EMPTY_STRING,
                    metadataContentEntry.thumbnailUrl, ScraperConstants.EMPTY_STRING,
                    ScraperConstants.EMPTY_STRING,
                    metadataContentEntry.contentTypeFlag, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentContentEntry, fileEntry, 0)

            importContainerFromFile(fileEntry.contentEntryUid, metadata.mimeType, containerDir.absolutePath, file.absolutePath, db, db, metadata.importMode, Any())

            close()

        }

    }

    override fun close() {
        val deleted = tempDir?.deleteRecursively() ?: false
        UMLogUtil.logError("did it delete: $deleted for ${tempDir?.name} ")
    }
}