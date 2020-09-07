package com.ustadmobile.lib.contentscrapers.googledrive

import ScraperTypes.GOOGLE_DRIVER_FILE_SCRAPER
import ScraperTypes.GOOGLE_DRIVE_FOLDER_INDEXER
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpStatement
import kotlinx.coroutines.runBlocking

@ExperimentalStdlibApi
class GoogleDriveFolderIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase, sqiUid: Int) : Indexer(parentContentEntry, runUid, db, sqiUid) {


    override fun indexUrl(sourceUrl: String) {

        var apiCall = sourceUrl
        var folderId = sourceUrl.substringAfter("https://www.googleapis.com/drive/v3/files/")
        if (sourceUrl.startsWith("https://drive.google.com/drive")) {
            val fileIdLookUp = sourceUrl.substringAfter("https://drive.google.com/drive/folders/")
            val char = fileIdLookUp.firstOrNull { it == '/' || it == '?' }
            folderId = if (char == null) fileIdLookUp else fileIdLookUp.substringBefore(char)
            apiCall = "https://www.googleapis.com/drive/v3/files/$folderId"
        }

        runBlocking {

            defaultHttpClient().get<HttpStatement>(apiCall) {
                parameter("key", "AIzaSyCoVemuuYfb3zT3Qe-CuCjATKPDVbmSzO0")
                parameter("fields", "id,modifiedTime,name,mimeType,description,thumbnailLink")
            }.execute() {

                val data = it.receive<GoogleFile>()

                val folderEntry = ContentScraperUtil.createOrUpdateContentEntry(
                        folderId, data.name,
                        apiCall, "", ContentEntry.LICENSE_TYPE_OTHER,
                        parentcontentEntry?.primaryLanguageUid
                                ?: 0, parentcontentEntry?.languageVariantUid,
                        data.description, false, ScraperConstants.EMPTY_STRING,
                        data.thumbnailLink, ScraperConstants.EMPTY_STRING,
                        ScraperConstants.EMPTY_STRING,
                        0, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, folderEntry, parentcontentEntry!!, 0)

                defaultHttpClient().get<HttpStatement>("https://www.googleapis.com/drive/v3/files") {
                    parameter("corpora", "user")
                    parameter("includeItemsFromAllDrives", "true")
                    parameter("q", "'$folderId' in parents")
                    parameter("supportsAllDrives", "true")
                    parameter("key", "AIzaSyCoVemuuYfb3zT3Qe-CuCjATKPDVbmSzO0")
                    parameter("fields", "files/modifiedTime, files/id, files/name, files/description, files/thumbnailLink, files/mimeType")
                }.execute { response ->

                    val googleFolder = response.receive<GoogleFolder>()

                    googleFolder.files.forEachIndexed { count, file ->

                        // TODO check the mimetype before starting the scrape

                        val fileEntry = ContentScraperUtil.createOrUpdateContentEntry(
                                file.id!!, file.name,
                                "https://www.googleapis.com/drive/v3/files/${file.id}", "", ContentEntry.LICENSE_TYPE_OTHER,
                                parentcontentEntry?.primaryLanguageUid
                                        ?: 0, parentcontentEntry?.languageVariantUid,
                                file.description, false, ScraperConstants.EMPTY_STRING,
                                file.thumbnailLink, ScraperConstants.EMPTY_STRING,
                                ScraperConstants.EMPTY_STRING,
                                0, contentEntryDao)

                        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, fileEntry, folderEntry, count)

                        if (file.mimeType == "application/vnd.google-apps.folder") {
                            createQueueItem("https://www.googleapis.com/drive/v3/files/${file.id}", fileEntry, GOOGLE_DRIVE_FOLDER_INDEXER,
                                    ScrapeQueueItem.ITEM_TYPE_INDEX,
                                    folderEntry)
                        } else {
                            createQueueItem("https://www.googleapis.com/drive/v3/files/${file.id}", fileEntry, GOOGLE_DRIVER_FILE_SCRAPER,
                                    ScrapeQueueItem.ITEM_TYPE_SCRAPE,
                                    folderEntry)
                        }

                    }

                }
            }
        }

        setIndexerDone(true, 0)

    }

    override fun close() {
    }


}