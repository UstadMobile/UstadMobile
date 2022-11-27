package com.ustadmobile.lib.contentscrapers.googledrive

import io.github.aakira.napier.Napier
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.alternative
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants.SCRAPER_TAG
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Scraper
import com.ustadmobile.lib.db.entities.ContentEntry
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files

class GoogleDriveScraper(contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long, endpoint: Endpoint, di: DI) : Scraper(contentEntryUid, sqiUid, parentContentEntryUid, endpoint, di) {

    private var tempDir: File? = null

    val googleDriveFormat: DateFormat = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    val googleApiKey: String by di.instance(tag = DiTag.TAG_GOOGLE_API)

    private val logPrefix = "[GoogleDriveScraper SQI ID #$sqiUid] "

    private val contentImportManager: ContentImportManager by di.on(endpoint).instance()

    private val httpClient: HttpClient by di.instance()

    override fun scrapeUrl(sourceUrl: String) {

        var apiCall = sourceUrl
        var fileId = sourceUrl.substringAfter("https://www.googleapis.com/drive/v3/files/")
        if (sourceUrl.startsWith("https://drive.google.com/file/d/")) {
            val fileIdLookUp = sourceUrl.substringAfter("https://drive.google.com/file/d/")
            val char = fileIdLookUp.firstOrNull { it == '/' || it == '?' }
            fileId = if (char == null) fileIdLookUp else fileIdLookUp.substringBefore(char)
            apiCall = "https://www.googleapis.com/drive/v3/files/$fileId"
        }

        tempDir = Files.createTempDirectory(fileId).toFile()
        runBlocking {

            httpClient.prepareGet(apiCall) {
                parameter("key", googleApiKey)
                parameter("fields", "id,modifiedTime,name,mimeType,description,thumbnailLink")
            }.execute() { fileResponse ->

                val googleFileResponse = fileResponse.body<GoogleFile>()

                contentImportManager.getMimeTypeSupported().find { fileMimeType -> fileMimeType == googleFileResponse.mimeType }
                        ?: return@execute

                val recentContainer = db.containerDao.getMostRecentContainerForContentEntry(contentEntryUid)
                val googleModifiedTime = googleFileResponse.modifiedTime
                val parsedModifiedTime: Long = if (googleModifiedTime != null) googleDriveFormat.parse(googleModifiedTime).local.unixMillisLong else 1
                val isUpdated = parsedModifiedTime > recentContainer?.cntLastModified ?: 0
                if (!isUpdated) {
                    Napier.i("$logPrefix with sourceUrl $sourceUrl already has this entry updated, close here", tag = SCRAPER_TAG)
                    showContentEntry()
                    setScrapeDone(true, 0)
                    close()
                    return@execute
                }

                httpClient.prepareGet(apiCall) {
                    parameter("alt", "media")
                    parameter("key", googleApiKey)
                }.execute() {

                    val contentFile = File(tempDir, googleFileResponse.name ?: googleFileResponse.id!!)
                    val stream = it.body<InputStream>()
                    FileOutputStream(contentFile).use { fileOut ->
                        stream.copyTo(fileOut)
                        fileOut.flush()
                    }
                    stream.close()

                    val metadata = contentImportManager.extractMetadata(contentFile.toURI().toString())

                    if (metadata == null) {
                        Napier.i("$logPrefix with sourceUrl $sourceUrl had no metadata found, not supported", tag = SCRAPER_TAG)
                        hideContentEntry()
                        setScrapeDone(false, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
                        close()
                        return@execute
                    }

                    val metadataContentEntry = metadata.contentEntry
                    var dbEntry = contentEntry
                    var fileEntry: ContentEntry

                    if (dbEntry != null && scrapeQueueItem?.overrideEntry == true) {
                        fileEntry = dbEntry
                    } else {
                        fileEntry = ContentScraperUtil.createOrUpdateContentEntry(
                                metadataContentEntry.entryId ?: contentEntry?.entryId ?: googleFileResponse.id,
                                metadataContentEntry.title ?: contentEntry?.title ?: googleFileResponse.name,
                                "https://www.googleapis.com/drive/v3/files/${googleFileResponse.id}",
                                metadataContentEntry.publisher ?: contentEntry?.publisher ?: "",
                                metadataContentEntry.licenseType.alternative(contentEntry?.licenseType
                                        ?: ContentEntry.LICENSE_TYPE_OTHER),
                                metadataContentEntry.primaryLanguageUid.alternative(contentEntry?.primaryLanguageUid
                                        ?: 0),
                                metadataContentEntry.languageVariantUid.alternative(contentEntry?.languageVariantUid
                                        ?: 0),
                                metadataContentEntry.description.alternative(contentEntry?.description
                                        ?: googleFileResponse.description ?: "")
                                , true, metadataContentEntry.author.alternative(contentEntry?.author
                                ?: ""),
                                metadataContentEntry.thumbnailUrl.alternative(contentEntry?.thumbnailUrl
                                        ?: googleFileResponse.thumbnailLink ?: ""),
                                "", "",
                                metadataContentEntry.contentTypeFlag, repo.contentEntryDao)
                        Napier.d("$logPrefix new entry created/updated with entryUid ${fileEntry.contentEntryUid} with title ${googleFileResponse.name}", tag = SCRAPER_TAG)
                        ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(repo.contentEntryParentChildJoinDao, parentContentEntry, fileEntry, 0)
                    }
                    metadata.contentEntry.contentEntryUid = fileEntry.contentEntryUid

                    val params = scrapeQueueItem?.scrapeRun?.conversionParams
                    var conversionParams = mapOf<String, String>()
                    if(params != null){
                        conversionParams = Json.decodeFromString(
                            MapSerializer(String.serializer(), String.serializer()), params)
                    }
                    contentImportManager.importFileToContainer(contentFile.toURI().toString(), metadata.mimeType,
                            fileEntry.contentEntryUid, containerFolder.path, conversionParams){
                    }
                    Napier.d("$logPrefix finished Scraping", tag = SCRAPER_TAG)
                    showContentEntry()
                    setScrapeDone(true, 0)
                    close()

                }

            }

        }
    }

    override fun close() {
        val deleted = tempDir?.deleteRecursively() ?: false
        UMLogUtil.logError("did it delete: $deleted for ${tempDir?.name} ")
    }


}