package com.ustadmobile.lib.contentscrapers.folder

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Scraper
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.exception.ExceptionUtils
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File

@ExperimentalStdlibApi
class FileScraper(contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long, endpoint: Endpoint, di: DI) : Scraper(contentEntryUid, sqiUid, parentContentEntryUid, endpoint, di) {

    private val contentImportManager: ContentImportManager by di.on(endpoint).instance()

    override fun scrapeUrl(sourceUrl: String) {

        val file = File(sourceUrl)

        UMLogUtil.logInfo("folder generated ${file.name}")

        val name = file.nameWithoutExtension

        runBlocking {

            try {

                val metadata = contentImportManager.extractMetadata(file.path)

                if (metadata == null) {
                    hideContentEntry()
                    setScrapeDone(false, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
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
                        sourceUrl, metadataContentEntry.publisher ?: parentContentEntry?.publisher ?: "",
                        metadataContentEntry.licenseType, primaryLanguage, variant,
                        metadataContentEntry.description, true, "",
                        metadataContentEntry.thumbnailUrl, "",
                        "",
                        metadataContentEntry.contentTypeFlag, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentContentEntry, fileEntry, 0)

                contentImportManager.importFileToContainer(file.path, metadata.mimeType, fileEntry.contentEntryUid, containerFolder.path){

                }
                close()
                UMLogUtil.logInfo("finished scrape for $sourceUrl")

            }catch (e: Exception){
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            }

        }



    }

    override fun close() {
        showContentEntry()
        setScrapeDone(true, 0)
    }
}