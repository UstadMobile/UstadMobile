package com.ustadmobile.lib.contentscrapers.abztract

import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.core.util.ext.alternative
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.SCRAPER_TAG
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContainerETag
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.net.URL
import java.net.URLDecoder
import java.nio.file.Files

@ExperimentalStdlibApi
class UrlScraper(contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long, endpoint: Endpoint, di: DI) : Scraper(contentEntryUid, sqiUid, parentContentEntryUid, endpoint, di) {

    var tempDir: File? = null

    private val logPrefix = "[URLScraper SQI ID #$sqiUid] "

    private val contentImportManager: ContentImportManager by di.on(endpoint).instance()

    override fun scrapeUrl(sourceUrl: String) {

        val url = URL(sourceUrl)

        val recentContainer = containerDao.getMostRecentContainerForContentEntry(contentEntryUid)
        val headRequestValues = isUrlContentUpdated(url, recentContainer)

        if(recentContainer != null){
            if (!headRequestValues.isUpdated) {
                Napier.i("$logPrefix with sourceUrl $sourceUrl already has this entry updated, close here", tag = SCRAPER_TAG)
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

            val metadata = contentImportManager.extractMetadata(file.path)

            if (metadata == null) {
                Napier.i("$logPrefix with sourceUrl $sourceUrl had no metadata found, not supported", tag = SCRAPER_TAG)
                hideContentEntry()
                setScrapeDone(false, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
                close()
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
                Napier.d("$logPrefix new entry created/updated with entryUid ${entry.contentEntryUid} with title $name", tag = SCRAPER_TAG)
                ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentEntryParentChildJoinDao, parentContentEntry, entry, 0)
                entry
            }

            val container = contentImportManager.importFileToContainer(file.path, metadata.mimeType, fileEntry.contentEntryUid, containerFolder.path){

            }
            if (!headRequestValues.etag.isNullOrEmpty() && container != null) {
                val etagContainer = ContainerETag(container.containerUid, headRequestValues.etag)
                db.containerETagDao.insert(etagContainer)
            }

            Napier.d("$logPrefix finished Indexing", tag = SCRAPER_TAG)
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