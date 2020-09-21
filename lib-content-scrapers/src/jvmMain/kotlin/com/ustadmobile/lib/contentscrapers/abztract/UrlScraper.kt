package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.ext.alternative
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContainerETag
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.contentformats.extractContentEntryMetadataFromFile
import com.ustadmobile.port.sharedse.contentformats.importContainerFromFile
import com.ustadmobile.port.sharedse.contentformats.mimeTypeSupported
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.kodein.di.DI
import java.io.File
import java.net.URL
import java.net.URLDecoder
import java.nio.file.Files

@ExperimentalStdlibApi
class UrlScraper(contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long, endpoint: Endpoint, di: DI) : Scraper(contentEntryUid, sqiUid, parentContentEntryUid, endpoint, di) {

    var tempDir: File? = null

    override fun scrapeUrl(sourceUrl: String) {

        val url = URL(sourceUrl)

        val recentContainer = containerDao.getMostRecentContainerForContentEntry(contentEntryUid)

        val headRequestValues = isUrlContentUpdated(url, recentContainer)

        val supported = mimeTypeSupported.find { fileMimeType -> fileMimeType == headRequestValues.mimeType }

        if (supported == null) {
            hideContentEntry()
            setScrapeDone(false, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
            return
        }

        if(recentContainer != null){
            if (!headRequestValues.isUpdated) {
                showContentEntry()
                setScrapeDone(true, 0)
                return
            }
        }

        val urlName = URLDecoder.decode(url.path, ScraperConstants.UTF_ENCODING)
        val name = FilenameUtils.getName(urlName)
        tempDir = Files.createTempDirectory("apache").toFile()
        var file = File(tempDir, name)
        FileUtils.copyURLToFile(url, file)

        runBlocking {

            val metadata = extractContentEntryMetadataFromFile(file, db)

            if (metadata == null) {
                hideContentEntry()
                setScrapeDone(false, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
                return@runBlocking
            }

            val metadataContentEntry = metadata.contentEntry
            val fileEntry: ContentEntry
            val dbEntry = contentEntry
            fileEntry = if (dbEntry != null && scrapeQueueItem?.overrideEntry == true) {
                dbEntry
            } else {
               val entry = ContentScraperUtil.createOrUpdateContentEntry(
                        metadataContentEntry.entryId ?: contentEntry?.entryId ?: name,
                        metadataContentEntry.title ?: contentEntry?.title ?: name, sourceUrl,
                        metadataContentEntry.publisher ?: contentEntry?.publisher ?: "",
                        metadataContentEntry.licenseType.alternative(contentEntry?.licenseType ?: ContentEntry.LICENSE_TYPE_OTHER),
                        metadataContentEntry.primaryLanguageUid.alternative(contentEntry?.primaryLanguageUid ?: 0),
                        metadataContentEntry.languageVariantUid.alternative(contentEntry?.languageVariantUid ?: 0),
                        metadataContentEntry.description.alternative(contentEntry?.description ?: "")
                        , true, metadataContentEntry.author.alternative(contentEntry?.author ?: ""),
                        metadataContentEntry.thumbnailUrl.alternative(contentEntry?.thumbnailUrl ?: ""),
                        "",
                        "",
                        metadataContentEntry.contentTypeFlag, contentEntryDao)
                ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentEntryParentChildJoinDao, parentContentEntry, entry, 0)
                entry
            }


            val container = importContainerFromFile(fileEntry.contentEntryUid, metadata.mimeType, containerFolder.absolutePath, file, db, db, metadata.importMode, Any())
            if (!headRequestValues.etag.isNullOrEmpty()) {
                val etagContainer = ContainerETag(container.containerUid, headRequestValues.etag)
                db.containerETagDao.insert(etagContainer)
            }

            showContentEntry()
            setScrapeDone(true, 0)
            close()

        }

    }

    override fun close() {
        val deleted = tempDir?.deleteRecursively() ?: false
        UMLogUtil.logError("did it delete: $deleted for ${tempDir?.name} ")
    }
}