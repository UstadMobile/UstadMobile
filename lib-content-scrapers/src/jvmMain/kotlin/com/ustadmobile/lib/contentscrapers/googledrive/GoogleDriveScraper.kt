package com.ustadmobile.lib.contentscrapers.googledrive

import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Scraper
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerETag
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
import org.kodein.di.ktor.di
import java.io.File
import java.io.InputStream
import java.nio.file.Files

@ExperimentalStdlibApi
class GoogleDriveScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long) : Scraper(containerDir, db, contentEntryUid, sqiUid, parentContentEntryUid) {

    private var tempDir: File? = null

    val googleDriveFormat: DateFormat = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    val googleApiKey: String by di().instance()

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
                parameter("key", "AIzaSyCoVemuuYfb3zT3Qe-CuCjATKPDVbmSzO0")
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
                    val primaryLanguage = if (metadataContentEntry.primaryLanguageUid == 0L)
                        parentContentEntry?.primaryLanguageUid ?: contentEntry?.primaryLanguageUid
                        ?: 0 else metadataContentEntry.primaryLanguageUid
                    val variant = if (metadataContentEntry.languageVariantUid == 0L)
                        parentContentEntry?.languageVariantUid ?: contentEntry?.languageVariantUid
                        ?: 0 else metadataContentEntry.languageVariantUid

                    val fileEntry = ContentScraperUtil.createOrUpdateContentEntry(
                            metadataContentEntry.entryId ?: contentEntry?.entryId ?: file.id,
                            metadataContentEntry.title ?: contentEntry?.title ?: file.name,
                            "https://www.googleapis.com/drive/v3/files/${file.id}", metadataContentEntry.publisher ?: parentContentEntry?.publisher
                    ?: "",
                            metadataContentEntry.licenseType, primaryLanguage, variant,
                            metadataContentEntry.description ?: file.description, true, ScraperConstants.EMPTY_STRING,
                            metadataContentEntry.thumbnailUrl ?: file.thumbnailLink, ScraperConstants.EMPTY_STRING,
                            ScraperConstants.EMPTY_STRING,
                            metadataContentEntry.contentTypeFlag, contentEntryDao)

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentContentEntry, fileEntry, 0)

                    importContainerFromFile(fileEntry.contentEntryUid,
                            metadata.mimeType, containerDir.absolutePath,
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