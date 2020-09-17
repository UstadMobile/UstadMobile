package com.ustadmobile.lib.contentscrapers.googledrive

import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.alternative
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Scraper
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.contentformats.extractContentEntryMetadataFromFile
import com.ustadmobile.port.sharedse.contentformats.importContainerFromFile
import com.ustadmobile.port.sharedse.contentformats.mimeTypeSupported
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpStatement
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File
import java.io.InputStream
import java.nio.file.Files

@ExperimentalStdlibApi
class GoogleDriveScraper(contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long, endpoint: Endpoint, di: DI) : Scraper(contentEntryUid, sqiUid, parentContentEntryUid, endpoint, di) {

    private var tempDir: File? = null

    val googleDriveFormat: DateFormat = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    val googleApiKey: String by di.instance(tag = DiTag.TAG_GOOGLE_API)

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

            defaultHttpClient().get<HttpStatement>(apiCall) {
                parameter("key", googleApiKey)
                parameter("fields", "id,modifiedTime,name,mimeType,description,thumbnailLink")
            }.execute() { fileResponse ->

                val file = fileResponse.receive<GoogleFile>()

                mimeTypeSupported.find { fileMimeType -> fileMimeType == file.mimeType }
                        ?: return@execute

                val recentContainer = containerDao.getMostRecentContainerForContentEntry(contentEntryUid)
                val modifiedTime: Long = googleDriveFormat.parse(file.modifiedTime!!).local.unixMillisLong
                val isUpdated = modifiedTime > recentContainer?.cntLastModified ?: 0
                if (!isUpdated) {
                    showContentEntry()
                    setScrapeDone(true, 0)
                    close()
                    return@execute
                }

                defaultHttpClient().get<HttpStatement>(apiCall) {
                    parameter("alt", "media")
                    parameter("key", googleApiKey)
                }.execute() {

                    val contentFile = File(tempDir, file.id!!)
                    val stream = it.receive<InputStream>()
                    FileUtils.writeByteArrayToFile(contentFile, stream.readBytes())

                    val metadata = extractContentEntryMetadataFromFile(contentFile.path, db)

                    if (metadata == null) {
                        hideContentEntry()
                        setScrapeDone(false, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
                        close()
                        return@execute
                    }

                    val metadataContentEntry = metadata.contentEntry
                    var fileEntry: ContentEntry
                    if (scrapeQueueItem?.overrideEntry == true) {

                        fileEntry = ContentScraperUtil.createOrUpdateContentEntry(
                                contentEntry?.entryId?.alternative(metadataContentEntry.entryId
                                        ?: file.id ?: ""),
                                contentEntry?.title.alternative(metadataContentEntry.title
                                        ?: file.name ?: ""),
                                "https://www.googleapis.com/drive/v3/files/${file.id}",
                                contentEntry?.publisher.alternative(metadataContentEntry.publisher ?: ""),
                                contentEntry?.licenseType?.alternative(metadataContentEntry.licenseType) ?: ContentEntry.LICENSE_TYPE_OTHER,
                                contentEntry?.primaryLanguageUid?.alternative(metadataContentEntry.primaryLanguageUid) ?: 0,
                                contentEntry?.languageVariantUid?.alternative(metadataContentEntry.languageVariantUid) ?: 0,
                                contentEntry?.description.alternative(metadataContentEntry.description ?: file.description ?: "")
                                , true, contentEntry?.author ?: "",
                                contentEntry?.thumbnailUrl.alternative(metadataContentEntry.thumbnailUrl ?: file.thumbnailLink ?: "")
                                , ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                                metadataContentEntry.contentTypeFlag, contentEntryDao)

                    } else {

                        fileEntry = ContentScraperUtil.createOrUpdateContentEntry(
                                metadataContentEntry.entryId ?: contentEntry?.entryId ?: file.id,
                                metadataContentEntry.title ?: contentEntry?.title ?: file.name,
                                "https://www.googleapis.com/drive/v3/files/${file.id}",
                                metadataContentEntry.publisher ?: contentEntry?.publisher ?: "",
                                metadataContentEntry.licenseType.alternative(contentEntry?.licenseType ?: ContentEntry.LICENSE_TYPE_OTHER),
                                metadataContentEntry.primaryLanguageUid.alternative(contentEntry?.primaryLanguageUid ?: 0),
                                metadataContentEntry.languageVariantUid.alternative(contentEntry?.languageVariantUid ?: 0),
                                metadataContentEntry.description.alternative(contentEntry?.description ?: file.description ?: "")
                                , true, metadataContentEntry.author.alternative(contentEntry?.author ?: ""),
                                metadataContentEntry.thumbnailUrl.alternative(contentEntry.thumbnailUrl ?: file.thumbnailLink ?: ""),
                                ScraperConstants.EMPTY_STRING,
                                ScraperConstants.EMPTY_STRING,
                                metadataContentEntry.contentTypeFlag, contentEntryDao)

                    }


                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentContentEntry, fileEntry, 0)

                    importContainerFromFile(fileEntry.contentEntryUid,
                            metadata.mimeType, containerFolder.absolutePath,
                            contentFile.absolutePath, db, db, metadata.importMode, Any())

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