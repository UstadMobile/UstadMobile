package com.ustadmobile.lib.staging.contentscrapers.edraakK12

import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDaoCommon
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ARABIC_FONT_BOLD
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ARABIC_FONT_REGULAR
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ComponentType
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EDRAAK_CSS_FILENAME
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EDRAAK_JS_FILENAME
import com.ustadmobile.lib.contentscrapers.ScraperConstants.INDEX_HTML
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JQUERY_JS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.LAST_MODIFIED_TXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MATERIAL_CSS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MATERIAL_JS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.QUESTIONS_JSON
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TINCAN_FILENAME
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.VIDEO_FILENAME_MP4
import com.ustadmobile.lib.contentscrapers.ScraperConstants.VIDEO_FILENAME_WEBM
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.edraakK12.ContentResponse
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.staging.contentscrapers.replaceMeWithDi
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException


/**
 * Edraak Courses are identified by the component type ImportedComponent on the root json file
 *
 *
 * The course content is found in the object called target_component
 * The target_component have 2 types for component type: Test and Online
 * The Online component type has 2 children:- one with component type Video and the other is Exercise
 *
 *
 * Video Component Type will have a list of encoded videos that contain the url link and its size.
 * Exercise Component Type will have an object called question_set which has a list of questions with all its content
 *
 *
 * The Test component type is the same as Exercise component type
 */

class EdraakK12ContentScraper : Runnable {

    private lateinit var containerDirectory: File
    private var sqiUid: Int = 0
    private lateinit var parentEntry: ContentEntry
    private var destinationDirectory: File? = null
    internal var contentUpdated = false
    private var scrapUrl: URL? = null

    @Throws(MalformedURLException::class)
    constructor(url: String, destinationDir: File) {
        this.scrapUrl = URL(url)
        this.destinationDirectory = destinationDir
    }

    constructor(scrapeUrl: URL, destinationDirectory: File, containerDir: File, parent: ContentEntry, sqiUid: Int) {
        this.destinationDirectory = destinationDirectory
        this.containerDirectory = containerDir
        this.scrapUrl = scrapeUrl
        this.parentEntry = parent
        this.sqiUid = sqiUid
    }

    public override fun run() {
        System.gc()
        //This needs to be replaced with DI
        lateinit var db: UmAppDatabase
        //val db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
        val repository = db // db.getRepository("https://localhost", "");
        val containerDao = repository.containerDao
        val queueDao = db.scrapeQueueItemDao


        val startTime = System.currentTimeMillis()
        UMLogUtil.logInfo("Started scraper url $scrapUrl at start time: $startTime")
        queueDao.setTimeStarted(sqiUid, startTime)

        var successful = false
        try {
            scrapeContent()
            successful = true
            if (hasContentUpdated()) {
                ContentScraperUtil.insertContainer(containerDao, parentEntry, true, ScraperConstants.MIMETYPE_ZIP,
                        destinationDirectory!!.lastModified(), destinationDirectory!!, db, repository, containerDirectory)

            }
        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getMessage(e))
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            val lastModified = File(destinationDirectory!!.getParentFile(), destinationDirectory!!.getName() + LAST_MODIFIED_TXT)
            ContentScraperUtil.deleteFile(lastModified)
        }

        queueDao.updateSetStatusById(sqiUid, if (successful) ScrapeQueueItemDaoCommon.STATUS_DONE else ScrapeQueueItemDaoCommon.STATUS_FAILED, 0)
        queueDao.setTimeFinished(sqiUid, System.currentTimeMillis())
        val duration = System.currentTimeMillis() - startTime
        UMLogUtil.logInfo("Ended scrape for url $scrapUrl in duration: $duration")

    }

    /**
     * Given a url and a directory, download all its content and save it in a directory
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun scrapeContent() {

        destinationDirectory!!.mkdirs()

        val response: ContentResponse
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = scrapUrl!!.openConnection() as HttpURLConnection
            urlConnection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01")
            response = GsonBuilder().create().fromJson<ContentResponse>(IOUtils.toString(urlConnection.inputStream, UTF_ENCODING), ContentResponse::class.java)

            val lastModified = File(destinationDirectory!!.parentFile, destinationDirectory!!.name + LAST_MODIFIED_TXT)
            contentUpdated = ContentScraperUtil.isFileContentsUpdated(lastModified, if ((response.updated != null && !response.updated!!.isEmpty()))
                response.updated!!
            else if ((response.created != null && !response.created!!.isEmpty()))
                response.created!!
            else
                (System.currentTimeMillis()).toString())

            if (!contentUpdated) {
                return
            }

        } catch (e: Exception) {
            UMLogUtil.logDebug(e.message + "\n" + e.stackTrace + e.cause)
            throw IllegalArgumentException("JSON INVALID for url " + scrapUrl!!.toString(), e.cause)
        } finally {
            urlConnection?.disconnect()
        }


        if (!ContentScraperUtil.isImportedComponent(response.component_type!!))
            throw IllegalArgumentException("Not an imported content type! for id" + response.id!!)

        if (response.target_component == null || response.target_component!!.children == null)
            throw IllegalArgumentException("Null target component, or target component children are null for id " + response.id!!)


        var hasVideo = false
        var hasQuestions = false
        var exceptionQuestion = ""
        var exceptionVideo = ""

        val questionsList = getQuestionSet(response)
        try {
            downloadQuestions(questionsList, destinationDirectory!!, scrapUrl!!)
            hasQuestions = true
        } catch (e: IllegalArgumentException) {
            exceptionQuestion = ExceptionUtils.getStackTrace(e)
            UMLogUtil.logDebug("The question set was not available for response id " + response.id!!)
        }

        if (ComponentType.ONLINE.type.equals(response.target_component!!.component_type!!, ignoreCase = true)) {

            // Contains children which have video
            for (children in response.target_component!!.children!!) {

                if (ComponentType.VIDEO.type.equals(children.component_type!!, ignoreCase = true)) {

                    try {
                        downloadVideo(children)
                        hasVideo = true
                    } catch (e: IllegalArgumentException) {
                        exceptionVideo = ExceptionUtils.getStackTrace(e)
                        UMLogUtil.logDebug("Video was unable to download or had no video for response id" + response.id!!)
                    }

                }

            }
        }

        if (!hasVideo && !hasQuestions) {
            throw IllegalArgumentException(
                    (exceptionQuestion + "\n" +
                            exceptionVideo +
                            "\nNo Video or Questions found in this id " + response.id))
        }


        val contentJsonFile = File(destinationDirectory, ScraperConstants.CONTENT_JSON)
        if (!ContentScraperUtil.fileHasContent(contentJsonFile)) {
            // store the json in a file after modifying image links
            val gson = GsonBuilder().disableHtmlEscaping().create()
            val jsonString = gson.toJson(response)
            FileUtils.writeStringToFile(contentJsonFile, jsonString, ScraperConstants.UTF_ENCODING)
        }

        try {
            val index = javaClass.getResourceAsStream(ScraperConstants.EDRAAK_INDEX_HTML_TAG).readString()
            val doc = Jsoup.parse(index, UTF_ENCODING)
            doc.head().selectFirst("title")?.text(response.title!!)
            FileUtils.writeStringToFile(File(destinationDirectory, INDEX_HTML), doc.toString(), UTF_ENCODING)

            checkBeforeCopyToFile(ScraperConstants.JS_TAG, File(destinationDirectory, JQUERY_JS))
            checkBeforeCopyToFile(ScraperConstants.MATERIAL_CSS_LINK, File(destinationDirectory, MATERIAL_CSS))
            checkBeforeCopyToFile(ScraperConstants.MATERIAL_JS_LINK, File(destinationDirectory, MATERIAL_JS))
            checkBeforeCopyToFile(ScraperConstants.REGULAR_ARABIC_FONT_LINK, File(destinationDirectory, ARABIC_FONT_REGULAR))
            checkBeforeCopyToFile(ScraperConstants.BOLD_ARABIC_FONT_LINK, File(destinationDirectory, ARABIC_FONT_BOLD))
            checkBeforeCopyToFile(ScraperConstants.EDRAAK_CSS_LINK, File(destinationDirectory, EDRAAK_CSS_FILENAME))
            checkBeforeCopyToFile(ScraperConstants.EDRAAK_JS_LINK, File(destinationDirectory, EDRAAK_JS_FILENAME))

            val tinCanFile = File(destinationDirectory, TINCAN_FILENAME)
            if (!ContentScraperUtil.fileHasContent(tinCanFile)) {

                ContentScraperUtil.generateTinCanXMLFile(destinationDirectory!!, response.title!!, "ar",
                        INDEX_HTML, ScraperConstants.MODULE_TIN_CAN_FILE,
                        scrapUrl!!.toString().substring(0, scrapUrl!!.toString().indexOf("component/")) + response.id!!,
                        "", "en")
            }

        } catch (e: IOException) {
            UMLogUtil.logError("Failed to download the necessary files for response id " + response.id!!)
            throw IOException(ExceptionUtils.getCause(e))
        } catch (e: TransformerException) {
            UMLogUtil.logError("Failed to download the necessary files for response id " + response.id!!)
            throw IOException(ExceptionUtils.getCause(e))
        } catch (e: ParserConfigurationException) {
            UMLogUtil.logError("Failed to download the necessary files for response id " + response.id!!)
            throw IOException(ExceptionUtils.getCause(e))
        }

    }

    private fun downloadVideo(children: ContentResponse): Boolean {
        if (children.video_info == null || children.video_info!!.encoded_videos == null || children.video_info!!.encoded_videos!!.isEmpty())
            throw IllegalArgumentException("Component Type was Video but no video found for response id")

        val videoHref = selectVideo(children.video_info!!.encoded_videos!!)
        val videoUrl: URL
        try {
            videoUrl = URL(scrapUrl, videoHref!!.url!!)
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("video Malformed url for response")
        }


        val videoFile = File(destinationDirectory, VIDEO_FILENAME_MP4)
        val webmFile = File(destinationDirectory, VIDEO_FILENAME_WEBM)
        if (ContentScraperUtil.isContentUpdated(ContentScraperUtil.parseServerDate(videoHref!!.modified!!), webmFile)) {
            try {
                FileUtils.copyURLToFile(videoUrl, videoFile)
                ShrinkerUtil.convertVideoToWebM(videoFile, webmFile)
                ContentScraperUtil.deleteFile(videoFile)
                return true
            } catch (e: IOException) {
                throw IllegalArgumentException("Download Video Malformed url for response")
            }

        }
        return false
    }

    /**
     * Check if any new content has been updated after scraping
     *
     * @return Return true if content has been updated
     */
    fun hasContentUpdated(): Boolean {
        return contentUpdated
    }

    @Throws(IOException::class)
    private fun checkBeforeCopyToFile(fileToDownload: String, locationToSave: File) {
        if (!ContentScraperUtil.fileHasContent(locationToSave)) {
            FileUtils.copyToFile(javaClass.getResourceAsStream(fileToDownload), locationToSave)
        }
    }

    /**
     * Find and return question set for imported content
     *
     * @param response depending on type of course (lesson or test), the question set is in different locations
     * @return the question set if found
     */
    internal fun getQuestionSet(response: ContentResponse): List<ContentResponse>? {

        if (ComponentType.ONLINE.type.equals(response.target_component!!.component_type!!, ignoreCase = true)) {

            for (children in response.target_component!!.children!!) {
                if (ScraperConstants.QUESTION_SET_HOLDER_TYPES.contains(children.component_type)) {

                    return children.question_set!!.children
                }
            }
        } else if (ComponentType.TEST.type.equals(response.target_component!!.component_type!!, ignoreCase = true)) {

            return response.target_component!!.question_set!!.children

        }
        return null
    }


    /**
     * Given an array of questions, find the questions that have image tags in their html and save the image within the directory
     * Finally write the list into a file
     *
     * @param questionsList  list of questions from json response
     * @param destinationDir directory where folder for each question will be saved (for images)
     * @return true if any content updated was updated based on server date and compared to last modified date of folder
     */
    private fun downloadQuestions(questionsList: List<ContentResponse>?, destinationDir: File, url: URL): Boolean {

        if (questionsList == null || questionsList!!.isEmpty())
            throw IllegalArgumentException("No Questions were found in the question set")

        for (exercise in questionsList!!) {

            val exerciseDirectory = File(destinationDir, exercise.id!!)
            exerciseDirectory.mkdirs()

            exercise.full_description = ContentScraperUtil.downloadAllResources(exercise.full_description.toString(), exerciseDirectory, url)
            exercise.explanation = ContentScraperUtil.downloadAllResources(exercise.explanation.toString(), exerciseDirectory, url)
            exercise.description = ContentScraperUtil.downloadAllResources(exercise.description.toString(), exerciseDirectory, url)

            if (ComponentType.MULTICHOICE.type.equals(exercise.component_type!!, ignoreCase = true)) {
                for (choice in exercise.choices!!) {
                    choice.description = ContentScraperUtil.downloadAllResources(choice.description.toString(), exerciseDirectory, url)
                }
            }

            for (hint in exercise.hints!!) {
                hint.description = ContentScraperUtil.downloadAllResources(hint.description.toString(), exerciseDirectory, url)
            }

        }

        try {
            ContentScraperUtil.saveListAsJson(destinationDir, questionsList, QUESTIONS_JSON)
        } catch (e: IOException) {
            throw IllegalArgumentException("Invalid Questions Json")
        }

        return true
    }


    /**
     * Given a list of videos, find the one with the smallest size
     *
     * @param encoded_videos list of videos from json response
     * @return chosen video url based on lowest size
     */
    private fun selectVideo(encoded_videos: List<ContentResponse.Encoded_videos>): ContentResponse.Encoded_videos? {

        var selectedVideo: ContentResponse.Encoded_videos? = null
        val videoSize = Integer.MAX_VALUE

        for (videos in encoded_videos) {
            if (videos.file_size in 1 until videoSize) {
                selectedVideo = videos
            }
        }
        return selectedVideo
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage: <edraak k12 json url> <file destination><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }
            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")
            UMLogUtil.logInfo("main url for edraak = " + args[0])
            UMLogUtil.logInfo("main file destination = " + args[1])
            try {
                EdraakK12ContentScraper(args[0], File(args[1])).scrapeContent()
            } catch (e: IOException) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Exception running scrapeContent")
            }

        }

        fun generateUrl(baseUrl: String, contentId: String, programId: Int): String {
            return baseUrl + "component/" + contentId + "/?states_program_id=" + programId
        }
    }


}
