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
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.contentformats.mimeTypeSupported
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpStatement
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.InputStream
import java.nio.file.Files

@ExperimentalStdlibApi
class GoogleDriveScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long) : Scraper(containerDir, db, contentEntryUid, sqiUid, parentContentEntryUid) {

    private var tempDir: File? = null

    val googleDriveFormat: DateFormat = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

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

                val fileEntry = ContentScraperUtil.createOrUpdateContentEntry(
                        file.id!!, file.name,
                        "https://www.googleapis.com/drive/v3/files/${file.id}", "", ContentEntry.LICENSE_TYPE_OTHER,
                        parentContentEntry?.primaryLanguageUid ?: 0,
                        parentContentEntry?.languageVariantUid,
                        file.description, true, ScraperConstants.EMPTY_STRING,
                        file.thumbnailLink, ScraperConstants.EMPTY_STRING,
                        ScraperConstants.EMPTY_STRING,
                        0, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentContentEntry, fileEntry, 0)

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
                    parameter("key", "AIzaSyCoVemuuYfb3zT3Qe-CuCjATKPDVbmSzO0")
                }.execute() {

                    val contentFile = File(tempDir, file.id!!)
                    val stream = it.receive<InputStream>()
                    FileUtils.writeByteArrayToFile(contentFile, stream.readBytes())

                    val container = Container().apply {
                        containerContentEntryUid = fileEntry.contentEntryUid
                        mimeType = file.mimeType
                        mobileOptimized = true
                        cntLastModified = System.currentTimeMillis()
                        containerUid = containerDao.insert(this)
                    }
                    val containerManager = ContainerManager(container, db, db, containerDir.absolutePath)
                    runBlocking {
                        containerManager.addEntries(ContainerManager.FileEntrySource(contentFile, contentFile.name))
                    }

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