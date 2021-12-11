package com.ustadmobile.lib.contentscrapers.khanacademy

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.controller.VideoContentPresenterCommon.Companion.VIDEO_MIME_MAP
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addDirToContainer
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.createSrtFile
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_KHAN
import com.ustadmobile.lib.contentscrapers.ScraperConstants.SRT_EXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.SUBTITLE_FILENAME
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil
import com.ustadmobile.lib.contentscrapers.abztract.ScraperException
import com.ustadmobile.lib.contentscrapers.abztract.YoutubeScraper
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.khanFullMap
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.khanLiteMap
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.subTitlePostUrl
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.subTitleUrl
import com.ustadmobile.lib.contentscrapers.util.SrtFormat
import com.ustadmobile.lib.db.entities.ContainerETag
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.kodein.di.DI
import java.io.File
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files
import java.util.*



class KhanVideoScraper(contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long, endpoint: Endpoint,override val di: DI) : YoutubeScraper(contentEntryUid, sqiUid, parentContentEntryUid, endpoint, di) {


    private var tempDir: File? = null

    override fun scrapeUrl(sourceUrl: String) {

        var entry: ContentEntry? = null
        runBlocking {
            entry = db.contentEntryDao.findByUidAsync(contentEntryUid)
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
        val type: Type = object : TypeToken<List<SrtFormat?>?>() {}.getType()

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
        var mp4Url: URL? = null
        var isValid: Boolean
        try {
            mp4Url = URL(url, mp4Link)
            isValid = isUrlValid(mp4Url)
        } catch (e: MalformedURLException) {
            isValid = false
        }

        val sourceId = entry!!.sourceUrl!!
        val commonSourceUrl = "%${sourceId.substringBefore(".")}%"
        val commonEntryList = db.contentEntryDao.findSimilarIdEntryForKhan(commonSourceUrl)
        commonEntryList.forEach {

            if (it.sourceUrl == sourceId) {
                return@forEach
            }

            ContentScraperUtil.insertOrUpdateRelatedContentJoin(db.contentEntryRelatedEntryJoinDao, it, entry!!,
                    ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION)
        }

        val youtubeId = content.youtubeId!!

        tempDir = Files.createTempDirectory(khanId).toFile()
        val langList = khanFullMap + khanLiteMap
        val defaultSrtFile = File(tempDir, "$SUBTITLE_FILENAME-${langList[lang]?.title?.replace("'", "")}$SRT_EXT")
        val defaultSrtText = saveSrtContent(defaultSrtFile, lang, youtubeId, gson, type)

        langList.forEach { khanLang ->

            var code = khanLang.key
            if (code == "www") {
                code = "en"
            }

            if (code == lang) {
                return@forEach
            }

            val srtFile = File(tempDir, "$SUBTITLE_FILENAME-${khanLang.value.title.replace("'", "")}$SRT_EXT")
            val langSrtText = saveSrtContent(srtFile, code, youtubeId, gson, type)

            if (langSrtText.isEmpty() || defaultSrtText == langSrtText) {
                FileUtils.deleteQuietly(srtFile)
                return@forEach
            }
        }

        if (isValid) {

            var conn: HttpURLConnection? = null
            try {
                conn = (mp4Url?.openConnection() as HttpURLConnection)
                val eTag = conn.getHeaderField("etag")
                var mimetype = conn.contentType
                val length = conn.contentLengthLong
                if (length > FILE_SIZE_LIMIT) {
                    hideContentEntry()
                    setScrapeDone(false, ERROR_TYPE_FILE_SIZE_LIMIT_EXCEEDED)
                    throw ScraperException(ERROR_TYPE_FILE_SIZE_LIMIT_EXCEEDED, "$sourceUrl has exceeded file size limit at $length")
                }

                if (!VIDEO_MIME_MAP.keys.contains(mimetype)) {
                    hideContentEntry()
                    setScrapeDone(false, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
                    throw ScraperException(ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED, "Video type not supported for $mimetype for url $mp4Url")
                }

                val ext = VIDEO_MIME_MAP[mimetype]

                val tempFile = File(tempDir, khanId + ext)
                FileUtils.copyURLToFile(mp4Url, tempFile)
                if (lang == "en") {
                    mimetype = MIMETYPE_KHAN
                    val webMFile = File(tempDir, "$khanId.webm")
                    ShrinkerUtil.convertKhanVideoToWebMAndCodec2(tempFile, webMFile)
                    tempFile.delete()
                }
                val container = createBaseContainer(mimetype)
                val containerAddOptions = ContainerAddOptions(storageDirUri = containerFolder.toDoorUri())
                runBlocking {
                    repo.addDirToContainer(container.containerUid, tempDir!!.toDoorUri(),
                            true, Any(), di, containerAddOptions)
                }
                if (!eTag.isNullOrEmpty()) {
                    val etagContainer = ContainerETag(container.containerUid, eTag)
                    db.containerETagDao.insert(etagContainer)
                }

                setScrapeDone(true, 0)

            } catch (s: ScraperException) {
                throw s
            } catch (e: Exception) {
                hideContentEntry()
                setScrapeDone(false, 0)
                throw e
            } finally {
                close()
                conn?.disconnect()
            }


        } else {
/*
            hideContentEntry()
            setScrapeDone(false, ERROR_TYPE_YOUTUBE_ERROR)
            close()*/

            try {
                val ytUrl = getYoutubeUrl(youtubeId)
                super.scrapeYoutubeVideo(ytUrl, "worst[ext=webm]/worst")
            } catch (s: ScraperException) {
                hideContentEntry()
                close()
                throw s
            } catch (e: Exception) {
                close()
                throw e
            }

        }

    }

    override fun close() {
        super.close()
        tempDir?.deleteRecursively()
    }

    private fun saveSrtContent(srtFile: File, code: String, youtubeId: String, gson: Gson, type: Type): String {
        val subTitleUrl = URL(generateSubtitleUrl(youtubeId, code))
        val subtitleScript = IOUtils.toString(subTitleUrl, UTF_ENCODING)
        val subTitleList = gson.fromJson<List<SrtFormat>>(subtitleScript, type)
        return createSrtFile(subTitleList, srtFile)
    }

    private fun generateSubtitleUrl(youtubeId: String, code: String): String? {
        return subTitleUrl + youtubeId + subTitlePostUrl + code
    }

}