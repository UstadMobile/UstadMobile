package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.controller.VideoContentPresenterCommon.Companion.VIDEO_MIME_MAP
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_PREFIX
import com.ustadmobile.lib.contentscrapers.abztract.ScraperException
import com.ustadmobile.lib.contentscrapers.abztract.YoutubeScraper
import com.ustadmobile.lib.db.entities.ContainerETag
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.kodein.di.DI
import java.io.File
import java.net.URL
import java.nio.file.Files



class KhanLiteVideoScraper(contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long, endpoint: Endpoint, override val di: DI) : YoutubeScraper(contentEntryUid, sqiUid, parentContentEntryUid, endpoint, di) {


    override fun scrapeUrl(sourceUrl: String) {

        var entry: ContentEntry? = null
        runBlocking {
            entry = db.contentEntryDao.findByUidAsync(contentEntryUid)
        }

        if (entry == null) {
            hideContentEntry()
            throw ScraperException(ERROR_TYPE_ENTRY_NOT_CREATED, "Content Entry was not found for url $sourceUrl")
        }

        val khanId = entry!!.sourceUrl!!.substringAfter(KHAN_PREFIX)

        val url = getValidUrl(khanId)

        if (url == null) {

            val ytUrl = getYoutubeUrl(khanId)
            try {
                super.scrapeYoutubeVideo(ytUrl, "worst[ext=webm]/worst")
            } catch (s: ScraperException){
                close()
                throw s
            } catch (e: Exception) {
                close()
                throw e
            }

        } else {

            val recentContainer = db.containerDao.getMostRecentContainerForContentEntry(contentEntryUid)

            val headRequestValues = isUrlContentUpdated(url, recentContainer)

            val ext = VIDEO_MIME_MAP[headRequestValues.mimeType]
            if (!VIDEO_MIME_MAP.keys.contains(headRequestValues.mimeType)) {
                hideContentEntry()
                setScrapeDone(false, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
                throw ScraperException(ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED, "Video type not supported for ${headRequestValues.mimeType} for url $url")
            }

            if(recentContainer != null){
                if (!headRequestValues.isUpdated) {
                    showContentEntry()
                    setScrapeDone(true, 0)
                    return
                }
            }

            val tempDir = Files.createTempDirectory(khanId).toFile()
            val tempFile = File(tempDir, khanId + ext)
            FileUtils.copyURLToFile(url, tempFile)

            val container = createBaseContainer(headRequestValues.mimeType)
            val containerAddOptions = ContainerAddOptions(storageDirUri = containerFolder.toDoorUri())
            runBlocking {
                repo.addFileToContainer(container.containerUid, tempFile.toDoorUri(),
                        tempFile.name, Any(), di, containerAddOptions)
            }
            if (!headRequestValues.mimeType.isNullOrEmpty()) {
                val etagContainer = ContainerETag(container.containerUid, headRequestValues.etag)
                db.containerETagDao.insert(etagContainer)
            }

            showContentEntry()
            setScrapeDone(true, 0)

            tempDir.deleteRecursively()

        }

    }

    private fun getValidUrl(khanId: String): URL? {
        val lowUrl = URL(getMp4LowUrl(khanId))
        val mp4Url = URL(getMp4Url(khanId))
        return when {
            isUrlValid(lowUrl) -> {
                lowUrl
            }
            isUrlValid(mp4Url) -> {
                mp4Url
            }
            else -> {
                null
            }
        }
    }


    fun getMp4LowUrl(videoId: String): String {
        return "https://cdn.kastatic.org/ka-youtube-converted/$videoId.mp4-low/$videoId-low.mp4"
    }

    fun getMp4Url(videoId: String): String {
        return "https://cdn.kastatic.org/ka-youtube-converted/$videoId.mp4/$videoId.mp4"
    }

}