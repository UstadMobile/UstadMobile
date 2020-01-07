package com.ustadmobile.lib.contentscrapers.ddl

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import com.ustadmobile.lib.contentscrapers.ddl.IndexDdlContent.Companion.DDL
import com.ustadmobile.lib.db.entities.ContentCategory
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.Language
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Once the resource page is opened.
 * You can download the list of files by searching with css selector - span.download-item a[href]
 * The url may contain spaces and needs to be encoded. This is done by constructing the url into a uri
 * Check if the file was downloaded before with etag or last modified
 * Create the content entry
 */
class DdlContentScraper(private val destinationDirectory: File, containerDir: File, lang: String, db: UmAppDatabase, contentEntryUid: Long) : HarScraper(containerDir, db, contentEntryUid) {

    private val contentEntryDao: ContentEntryDao
    private val categorySchemaDao: ContentCategorySchemaDao
    private val contentCategoryDao: ContentCategoryDao
    private val languageDao: LanguageDao
    private val containerDao: ContainerDao
    private val repository: UmAppDatabase
    private val language: Language
    private var doc: Document? = null
    lateinit var contentEntry: ContentEntry
        internal set


    init {
        destinationDirectory.mkdirs()
        repository = db
        contentEntryDao = repository.contentEntryDao
        categorySchemaDao = repository.contentCategorySchemaDao
        contentCategoryDao = repository.contentCategoryDao
        languageDao = repository.languageDao
        containerDao = repository.containerDao
        language = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, lang)
    }

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

        const val GMAIL = "scraper"
        const val PASS = "reading123"
        const val SIGN_IN_URL = "https://ddl.af/en/login"


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
                //DdlContentScraper(File(args[1]), File(args[2]), args[3], UmAppDatabase.Companion.getInstance(Any()), 0).scrapeContent()
            } catch (e: IOException) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Exception running scrapeContent ddl")
            }

        }
    }

    override fun isContentUpdated(): Boolean {
        return true
    }

    override fun scrapeUrl(sourceUrl: String) {

        val resourceFolder = File(destinationDirectory, FilenameUtils.getBaseName(sourceUrl))
        resourceFolder.mkdirs()

        doc = Jsoup.connect(sourceUrl).get()

        val thumbnail = doc!!.selectFirst("aside img")?.attr("src")?: EMPTY_STRING

        val description = doc!!.selectFirst("meta[name=description]")?.attr("content")
        val authorTag = doc!!.selectFirst("article.resource-view-details h3:contains(Author) ~ p")
        val farsiAuthorTag = doc!!.selectFirst("article.resource-view-details h3:contains(نویسنده) ~ p")
        val pashtoAuthorTag = doc!!.selectFirst("article.resource-view-details h3:contains(لیکونکی) ~ p")

        val author = when {
            authorTag != null -> authorTag.text()
            farsiAuthorTag != null -> farsiAuthorTag.text()
            pashtoAuthorTag != null -> pashtoAuthorTag.text()
            else -> EMPTY_STRING
        }
        val publisherTag = doc!!.selectFirst("article.resource-view-details a[href*=publisher]")
        val publisher = publisherTag?.text() ?: DDL

        contentEntry = ContentScraperUtil.createOrUpdateContentEntry(sourceUrl, doc!!.title(),
                sourceUrl, publisher, LICENSE_TYPE_CC_BY, language.langUid, null, description, true, author,
                thumbnail, EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao)

        chromeDriver.get(SIGN_IN_URL)
        val waitDriver = WebDriverWait(chromeDriver, ScraperConstants.TIME_OUT_SELENIUM.toLong())
        waitForJSandJQueryToLoad(waitDriver)

        chromeDriver.findElement(By.id("user-field")).sendKeys(GMAIL)
        chromeDriver.findElement(By.id("password")).sendKeys(PASS)
        chromeDriver.findElement(By.id("user-field")).sendKeys(Keys.RETURN)

        waitForJSandJQueryToLoad(waitDriver)

        val downloadList = doc!!.select("span.download-item a[href]")

        var containerManager: ContainerManager? = null

        val downloadItem = downloadList[0]
        val href = downloadItem.attr("href")
        val modifiedFile = getEtagOrModifiedFile(resourceFolder, FilenameUtils.getBaseName(FilenameUtils.getName(href)))
        try {
            val fileUrl = URL(URL(sourceUrl), href)

            containerManager = startHarScrape(sourceUrl, {

                chromeDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS)

            }, addHarContent = false, filters = listOf { entry ->

                if (entry.request.url != fileUrl.toString()) {
                    entry.response = null
                }

                entry
            }) {
                true
            }
        } catch (e: Exception) {
            UMLogUtil.logError("Error downloading resource from url $sourceUrl/$href")
            if (modifiedFile != null) {
                ContentScraperUtil.deleteFile(modifiedFile)
            }

        }

        if (containerManager?.allEntries?.isEmpty() != false) {

            contentEntryDao.updateContentEntryInActive(contentEntryUid, true)
            return
        }

        runBlocking {

            val entry = containerManager.allEntries[0]
            val mimeType = Files.probeContentType(File(entry.cePath ?: "").toPath())
            val container = containerDao.findByUid(containerManager.containerUid)
            containerDao.updateMimeType(mimeType, container?.containerUid ?: 0)

        }


    }
}