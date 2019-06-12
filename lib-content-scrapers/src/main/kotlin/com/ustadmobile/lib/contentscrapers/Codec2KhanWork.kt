package com.ustadmobile.lib.contentscrapers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContainerEntryDao
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.contentscrapers.util.SrtFormat
import com.ustadmobile.lib.contentscrapers.util.VideoApi
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerWithContentEntry
import com.ustadmobile.port.sharedse.container.ContainerManager

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils

import java.io.File
import java.lang.reflect.Type
import java.net.URL
import java.nio.file.Paths
import java.util.HashMap

import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_KHAN
import com.ustadmobile.lib.contentscrapers.ScraperConstants.SUBTITLE_FILENAME
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.WEBM_EXT

class Codec2KhanWork(containerFolder: File, khanFolder: File) {

    init {
        val gson = GsonBuilder().disableHtmlEscaping().create()
        val type = object : TypeToken<List<SrtFormat>>() {

        }.type

        val db = UmAppDatabase.getInstance(Any())
        val repository = db //db.getRepository("https://localhost", "")
        val containerDao = repository.containerDao
        val khanContainerList = containerDao.findKhanContainers()
        UMLogUtil.logTrace("Number of khan containers left to convert " + khanContainerList.size)
        val containerEntryDao = db.containerEntryDao

        for (khanFile in khanContainerList) {
            try {

                val khanfileList = containerEntryDao.findByContainer(khanFile.containerUid)
                UMLogUtil.logTrace("Number of files in khanfileList " + khanfileList.size)
                if (khanFile.fileSize > 440401920) {

                    UMLogUtil.logTrace("Found a filesize of 420m for container" + khanFile.sourceUrl)
                    containerEntryDao.deleteByContainerUid(khanFile.containerUid)
                    containerDao.deleteByUid(khanFile.containerUid)
                    continue

                }

                var khanId = khanFile.sourceUrl
                khanId = khanId.substring(khanId.lastIndexOf("/") + 1)

                UMLogUtil.logTrace("Got the khanId from sourceUrl $khanId")

                var mp4VideoFile: File? = null
                var contentFolder: File? = null
                var containerEntryUidToDelete = 0L
                for (file in khanfileList) {

                    var nameOfFile = file.cePath
                    if (nameOfFile!!.endsWith(".mp4")) {
                        containerEntryUidToDelete = file.ceUid
                        nameOfFile = if (nameOfFile!!.contains("/")) nameOfFile!!.substring(nameOfFile!!.lastIndexOf("/") + 1) else nameOfFile
                        contentFolder = Paths.get(khanFolder.absolutePath, khanId, khanId).toFile()
                        mp4VideoFile = File(contentFolder, nameOfFile!!)
                    }
                }

                if (contentFolder == null) {
                    UMLogUtil.logError("Did not find the folder" + khanFile.sourceUrl)
                    continue
                }

                UMLogUtil.logTrace("Got the contentFolder  at " + contentFolder.path)
                UMLogUtil.logTrace("Got the mp4  at " + mp4VideoFile!!.path)

                val entryId = khanFile.entryId
                var content = File(mp4VideoFile.path)

                val videoApiUrl = URL("http://www.khanacademy.org/api/v1/videos/$entryId")
                val videoApi = gson.fromJson(IOUtils.toString(videoApiUrl, UTF_ENCODING), VideoApi::class.java)
                var youtubeId: String? = ""
                if (videoApi != null) {
                    youtubeId = videoApi.youtube_id
                    if (videoApi.download_urls != null) {

                        var videoUrl = videoApi.download_urls!!.mp4
                        if (videoUrl == null || videoUrl.isEmpty()) {
                            videoUrl = videoApi.download_urls!!.mp4Low
                            if (videoUrl == null) {
                                UMLogUtil.logError("Video was not available in any format for url: " + khanFile.sourceUrl)
                            }
                        }
                        if (videoUrl != null) {
                            content = File(contentFolder, FilenameUtils.getName(videoUrl))
                            FileUtils.copyURLToFile(URL(videoUrl), content)
                            UMLogUtil.logTrace("Got the video mp4")
                        } else {
                            UMLogUtil.logError("Did not get the video mp4 for " + khanFile.sourceUrl)
                        }
                    }
                }


                try {
                    val url = URL("http://www.khanacademy.org/api/internal/videos/$youtubeId/transcript")
                    val subtitleScript = IOUtils.toString(url, UTF_ENCODING)
                    val subTitleList = gson.fromJson<List<SrtFormat>>(subtitleScript, type)
                    val srtFile = File(contentFolder, SUBTITLE_FILENAME)
                    ContentScraperUtil.createSrtFile(subTitleList, srtFile)
                    UMLogUtil.logTrace("Created the subtitle file")

                } catch (e: Exception) {
                    UMLogUtil.logInfo(ExceptionUtils.getStackTrace(e))
                    UMLogUtil.logInfo("No subtitle for youtube link " + youtubeId + " and fileUid " + khanFile.containerUid)
                }

                val webMFile = File(contentFolder, UMFileUtil.stripExtensionIfPresent(content.name) + WEBM_EXT)
                ShrinkerUtil.convertKhanVideoToWebMAndCodec2(content, webMFile)

                UMLogUtil.logTrace("Converted Coddec2")

                ContentScraperUtil.deleteFile(content)
                if (content.path != mp4VideoFile.path) {
                    ContentScraperUtil.deleteFile(mp4VideoFile)
                }


                val containerManager = ContainerManager(khanFile, db,
                        repository, containerFolder.absolutePath)
                val fileMap = HashMap<File, String>()
                ContentScraperUtil.createContainerFromDirectory(contentFolder, fileMap)
                containerManager.addEntries(fileMap, true)
                containerDao.updateMimeType(MIMETYPE_KHAN, khanFile.containerUid)
                containerEntryDao.deleteByContainerEntryUid(containerEntryUidToDelete)

                UMLogUtil.logDebug("Completed conversion of " + khanFile.sourceUrl)

            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Error converting for video " + khanFile.sourceUrl)
            }


        }


    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 1) {
                System.err.println("Usage: <container file destination><khan folder destination><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }

            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")

            Codec2KhanWork(File(args[0]), File(args[1]))

        }
    }


}
