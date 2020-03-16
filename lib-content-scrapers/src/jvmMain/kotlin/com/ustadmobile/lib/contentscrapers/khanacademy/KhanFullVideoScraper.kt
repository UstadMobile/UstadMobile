package com.ustadmobile.lib.contentscrapers.khanacademy

import com.google.gson.GsonBuilder
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.controller.VideoPlayerPresenterCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Scraper
import com.ustadmobile.lib.contentscrapers.abztract.ScraperException
import com.ustadmobile.lib.db.entities.ContainerETag
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.util.*

class KhanFullVideoScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int) : Scraper(containerDir, db, contentEntryUid, sqiUid) {


    var tempDir: File? = null

    override fun scrapeUrl(sourceUrl: String) {

        var entry: ContentEntry? = null
        runBlocking {
            entry = contentEntryDao.findByUidAsync(contentEntryUid)
        }

        if (entry == null) {
            hideContentEntry()
            setScrapeDone(false, ERROR_TYPE_ENTRY_NOT_CREATED)
            throw ScraperException(ERROR_TYPE_ENTRY_NOT_CREATED, "Content Entry was not found for url $sourceUrl")
        }

        val khanId = entry!!.sourceUrl!!.substringAfter(KhanContentIndexer.KHAN_PREFIX)

        val url = URL(sourceUrl)

        val jsonContent = getJsonContent(url)

        val gson = GsonBuilder().disableHtmlEscaping().create()

        var data: SubjectListResponse? = gson.fromJson(jsonContent, SubjectListResponse::class.java)
        if (data!!.componentProps == null) {
            data = gson.fromJson(jsonContent, PropsSubjectResponse::class.java).props
        }

        val compProps = data!!.componentProps
        var navData: SubjectListResponse.ComponentData.NavData? = compProps!!.tutorialNavData
        if (navData == null) {
            navData = compProps.tutorialPageData
        }
        var contentList: MutableList<SubjectListResponse.ComponentData.NavData.ContentModel>? = navData!!.contentModels!!
        if (contentList == null || contentList.isEmpty()) {
            contentList = ArrayList()
            contentList.add(navData.contentModel!!)
        }

        for (content in contentList) {

            if (sourceUrl.contains(content.relativeUrl!!)) {

                var videoUrl = content.downloadUrls!!.mp4
                if (videoUrl == null || videoUrl.isEmpty()) {
                    videoUrl = content.downloadUrls!!.mp4Low
                    if (videoUrl == null) {
                        hideContentEntry()
                        setScrapeDone(false, ERROR_TYPE_LINK_NOT_FOUND)
                        return
                    }
                    UMLogUtil.logTrace("Video was not available in mp4, found in mp4-low at $url")
                }
                val videoToDownload = URL(url, videoUrl)

                val recentContainer = containerDao.getMostRecentContainerForContentEntry(contentEntryUid)

                val isContentUpdated = if (recentContainer == null) true else {
                    isUrlContentUpdated(videoToDownload, recentContainer)
                }

                if (!isContentUpdated) {
                    showContentEntry()
                    setScrapeDone(true, 0)
                    return
                }

                var conn: HttpURLConnection? = null
                try {
                    conn = videoToDownload.openConnection() as HttpURLConnection
                    conn.requestMethod = "HEAD"
                    val eTag = conn.getHeaderField("etag")
                    val length = conn.contentLengthLong

                    if(length > FILE_SIZE_LIMIT){
                        hideContentEntry()
                        setScrapeDone(false, ERROR_TYPE_FILE_SIZE_LIMIT_EXCEEDED)
                        return
                    }

                    val mimeType = conn.contentType

                    if (!VideoPlayerPresenterCommon.VIDEO_MIME_MAP.keys.contains(mimeType)) {
                        hideContentEntry()
                        setScrapeDone(false, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
                        return
                    }

                    tempDir = Files.createTempDirectory(khanId).toFile()
                    val tempFile = File(tempDir, FilenameUtils.getName(videoToDownload.path))
                    FileUtils.copyURLToFile(url, tempFile)

                    val container = createBaseContainer(mimeType)
                    val containerManager = ContainerManager(container, db, db, containerDir.absolutePath)
                    runBlocking {
                        containerManager.addEntries(ContainerManager.FileEntrySource(tempFile, tempFile.name))
                    }
                    if (!eTag.isNullOrEmpty()) {
                        val etagContainer = ContainerETag(container.containerUid, eTag)
                        db.containerETagDao.insert(etagContainer)
                    }

                    val langCode = getLangCodeFromURL(url)

                    if(langCode == "en"){
                        val webMFile = File(tempDir, FilenameUtils.getName(videoToDownload.path))
                        ShrinkerUtil.convertKhanVideoToWebMAndCodec2(tempFile, webMFile)
                    }

                    setScrapeDone(true, 0)

                } catch (e: Exception) {
                    hideContentEntry()
                    setScrapeDone(false, 0)
                    throw e
                } finally {
                    conn?.disconnect()
                }

            }

        }


    }

    override fun close() {
        tempDir?.deleteRecursively()
    }


}