package com.ustadmobile.lib.contentscrapers.khanacademy

import com.google.gson.GsonBuilder
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.controller.VideoPlayerPresenterCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil
import com.ustadmobile.lib.contentscrapers.abztract.ScraperException
import com.ustadmobile.lib.contentscrapers.abztract.YoutubeScraper
import com.ustadmobile.lib.db.entities.ContainerETag
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files

class KhanVideoScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int) : YoutubeScraper(containerDir, db, contentEntryUid, sqiUid) {

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

        var lang = sourceUrl.substringBefore(".khan").substringAfter("://")
        if (lang == "www") {
            lang = "en"
        }

        val url = URL(sourceUrl)

        val jsonContent = getJsonContent(url)

        val gson = GsonBuilder().disableHtmlEscaping().create()

        var data: SubjectListResponse? = gson.fromJson(jsonContent, SubjectListResponse::class.java)
        if (data!!.componentProps == null) {
            data = gson.fromJson(jsonContent, PropsSubjectResponse::class.java).props
        }

        val compProps = data!!.componentProps
        val navData = compProps!!.tutorialNavData ?: compProps.tutorialPageData

        var contentList: MutableList<SubjectListResponse.ComponentData.NavData.ContentModel>? = navData!!.contentModels
        if (contentList == null || contentList.isEmpty()) {
            contentList = mutableListOf()
            contentList.add(navData.contentModel!!)
        }

        val content = contentList.find { sourceUrl.contains(it.nodeSlug!!) }

        if (content == null) {
            hideContentEntry()
            setScrapeDone(false, ERROR_TYPE_NO_SOURCE_URL_FOUND)
            return
        }

        val khanId = content.id
        val mp4Link = content.downloadUrls?.mp4 ?: content.downloadUrls?.mp4Low
        val mp4Url = URL(url, mp4Link)
        val isValid = isUrlValid(mp4Url)

        if (isValid) {

            val conn = (mp4Url.openConnection() as HttpURLConnection)
            val eTag = conn.getHeaderField("etag")
            val mimetype = conn.contentType
            conn.disconnect()

            if (!VideoPlayerPresenterCommon.VIDEO_MIME_MAP.keys.contains(mimetype)) {
                hideContentEntry()
                throw ScraperException(ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED, "Video type not supported for $mimetype for url $mp4Url")
            }

            val ext = VideoPlayerPresenterCommon.VIDEO_MIME_MAP[mimetype]

            val recentContainer = containerDao.getMostRecentContainerForContentEntry(contentEntryUid)

            if (recentContainer != null) {
                val isUpdated = isUrlContentUpdated(mp4Url, recentContainer)
                if (!isUpdated) {
                    showContentEntry()
                    setScrapeDone(true, 0)
                    return
                }
            }

            val tempDir = Files.createTempDirectory(khanId).toFile()
            val tempFile = File(tempDir, khanId + ext)
            FileUtils.copyURLToFile(mp4Url, tempFile)
            if (lang == "en") {
                val webMFile = File(tempDir, "$khanId.webm")
                ShrinkerUtil.convertKhanVideoToWebMAndCodec2(tempFile, webMFile)
            }

            val container = createBaseContainer(mimetype)
            val containerManager = ContainerManager(container, db, db, containerDir.absolutePath)
            runBlocking {
                containerManager.addEntries(ContainerManager.FileEntrySource(tempFile, tempFile.name))
            }
            if (!eTag.isNullOrEmpty()) {
                val etagContainer = ContainerETag(container.containerUid, eTag)
                db.containerETagDao.insert(etagContainer)
            }

            setScrapeDone(true, 0)

            tempDir.deleteRecursively()


        } else {


            val ytUrl = getYoutubeUrl(content.youtubeId!!)
            try {
                scrapeYoutubeLink(ytUrl)
            } catch (e: Exception) {
                hideContentEntry()
                throw e
            }


        }


    }
}