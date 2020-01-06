package com.ustadmobile.lib.contentscrapers.ddl

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.REQUEST_HEAD
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.ddl.IndexDdlContent.Companion.DDL
import com.ustadmobile.lib.db.entities.ContentCategory
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.Language
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import java.net.*
import java.nio.file.Files
import java.util.*


/**
 * Once the resource page is opened.
 * You can download the list of files by searching with css selector - span.download-item a[href]
 * The url may contain spaces and needs to be encoded. This is done by constructing the url into a uri
 * Check if the file was downloaded before with etag or last modified
 * Create the content entry
 */
class DdlContentScraper @Throws(MalformedURLException::class)
constructor(private val urlString: String, private val destinationDirectory: File, private val containerDir: File, lang: String) {
    private val url: URL
    private val contentEntryDao: ContentEntryDao
    private val categorySchemaDao: ContentCategorySchemaDao
    private val contentCategoryDao: ContentCategoryDao
    private val languageDao: LanguageDao
    private val containerDao: ContainerDao
    private val db: UmAppDatabase
    private val repository: UmAppDatabase
    private val language: Language
    private var doc: Document? = null
    lateinit var contentEntries: ContentEntry
        internal set

    val parentSubjectAreas: ArrayList<ContentEntry>
        get() {

            val subjectAreaList = ArrayList<ContentEntry>()
            val subjectContainer = doc!!.select("article.resource-view-details a[href*=subject_area]")

            val subjectList = subjectContainer.select("a")
            for (subject in subjectList) {

                val title = subject.attr("title")
                val href = subject.attr("href")

                val contentEntry = ContentScraperUtil.createOrUpdateContentEntry(href, title, href,
                        DDL, LICENSE_TYPE_CC_BY, language.langUid, null,
                        EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                        EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao)

                subjectAreaList.add(contentEntry)

            }

            return subjectAreaList
        }


    val contentCategories: ArrayList<ContentCategory>
        get() {


            val categoryRelations = ArrayList<ContentCategory>()
            val subjectContainer = doc!!.select("article.resource-view-details a[href*=level]")

            val ddlSchema = ContentScraperUtil.insertOrUpdateSchema(categorySchemaDao, "DDL Resource Level", "ddl/resource-level/")

            val subjectList = subjectContainer.select("a")
            for (subject in subjectList) {

                val title = subject.attr("title")

                val categoryEntry = ContentScraperUtil.insertOrUpdateCategoryContent(contentCategoryDao, ddlSchema, title)

                categoryRelations.add(categoryEntry)

            }

            return categoryRelations
        }

    init {
        this.url = URL(urlString)
        destinationDirectory.mkdirs()
        db = UmAppDatabase.getInstance(Any())
        repository = db //db.getRepository("https://localhost", "")
        contentEntryDao = repository.contentEntryDao
        categorySchemaDao = repository.contentCategorySchemaDao
        contentCategoryDao = repository.contentCategoryDao
        languageDao = repository.languageDao
        containerDao = repository.containerDao
        language = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, lang)
    }


    @Throws(IOException::class)
    fun scrapeContent() {

        val resourceFolder = File(destinationDirectory, FilenameUtils.getBaseName(urlString))
        resourceFolder.mkdirs()

        doc = Jsoup.connect(urlString).get()

        val downloadList = doc!!.select("span.download-item a[href]")

        val imgTag = doc!!.selectFirst("aside img")
        val thumbnail = if (imgTag != null) imgTag.attr("src") else EMPTY_STRING

        val description = doc!!.selectFirst("meta[name=description]").attr("content")
        val authorTag = doc!!.selectFirst("article.resource-view-details h3:contains(Author) ~ p")
        val farsiAuthorTag = doc!!.selectFirst("article.resource-view-details h3:contains(نویسنده) ~ p")
        val pashtoAuthorTag = doc!!.selectFirst("article.resource-view-details h3:contains(لیکونکی) ~ p")
        val author = if (authorTag != null)
            authorTag.text()
        else if (farsiAuthorTag != null)
            farsiAuthorTag.text()
        else if (pashtoAuthorTag != null) pashtoAuthorTag.text() else EMPTY_STRING
        val publisherTag = doc!!.selectFirst("article.resource-view-details a[href*=publisher]")
        val publisher = if (publisherTag != null) publisherTag.text() else EMPTY_STRING


        contentEntries = ContentScraperUtil.createOrUpdateContentEntry(urlString, doc!!.title(),
                urlString, if (publisher != null && !publisher.isEmpty()) publisher else DDL,
                LICENSE_TYPE_CC_BY, language.langUid, null, description, true, author,
                thumbnail, EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao)

        for (downloadCount in downloadList.indices) {

            val downloadItem = downloadList[downloadCount]
            val href = downloadItem.attr("href")
            val modifiedFile = getEtagOrModifiedFile(resourceFolder, FilenameUtils.getBaseName(FilenameUtils.getName(href)))
            var conn: HttpURLConnection? = null
            try {
                val fileUrl = URL(url, href)

                // this was done to encode url that had empty spaces in the name or other illegal characters
                val decodedPath = URLDecoder.decode(fileUrl.toString(), ScraperConstants.UTF_ENCODING)
                val decodedUrl = URL(decodedPath)

                conn = decodedUrl.openConnection() as HttpURLConnection
                conn.requestMethod = REQUEST_HEAD
                val resourceFile = File(resourceFolder, FilenameUtils.getName(href))
                val mimeType = Files.probeContentType(resourceFile.toPath())

                var isUpdated = ContentScraperUtil.isFileModified(conn, resourceFolder, FilenameUtils.getName(href))

                isUpdated = true

                if (!isUpdated) {
                    continue
                }

                FileUtils.copyURLToFile(decodedUrl, resourceFile)

                ContentScraperUtil.insertContainer(containerDao, contentEntries, true, mimeType,
                        resourceFile.lastModified(), resourceFile, db, repository, containerDir)

            } catch (e: Exception) {
                UMLogUtil.logError("Error downloading resource from url $url/$href")
                if (modifiedFile != null) {
                    ContentScraperUtil.deleteFile(modifiedFile)
                }

            } finally {
                conn?.disconnect()
            }
        }
    }

    private fun getEtagOrModifiedFile(resourceFolder: File, name: String): File? {
        val eTag = File(resourceFolder, name + ScraperConstants.ETAG_TXT)
        if (ContentScraperUtil.fileHasContent(eTag)) {
            return eTag
        }
        val modified = File(resourceFolder, name + ScraperConstants.LAST_MODIFIED_TXT)
        return if (ContentScraperUtil.fileHasContent(modified)) {
            modified
        } else null
    }

    companion object {


        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 3) {
                System.err.println("Usage: <ddl website url> <file destination><container destination><lang en or fa or ps><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }
            UMLogUtil.setLevel(if (args.size == 4) args[3] else "")
            UMLogUtil.logInfo(args[0])
            UMLogUtil.logInfo(args[1])
            try {
                DdlContentScraper(args[0], File(args[1]), File(args[2]), args[3]).scrapeContent()
            } catch (e: IOException) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Exception running scrapeContent ddl")
            }

        }
    }
}
