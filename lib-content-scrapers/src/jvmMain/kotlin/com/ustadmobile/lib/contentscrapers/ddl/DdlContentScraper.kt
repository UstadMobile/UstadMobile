package com.ustadmobile.lib.contentscrapers.ddl

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMUUID
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import com.ustadmobile.lib.contentscrapers.abztract.Scraper
import com.ustadmobile.lib.contentscrapers.ddl.IndexDdlContent.Companion.DDL
import com.ustadmobile.lib.db.entities.ContainerETag
import com.ustadmobile.lib.db.entities.ContentCategory
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.Language
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.http.client.utils.DateUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.net.URL
import java.nio.file.Files
import java.util.*


/**
 * Once the resource page is opened.
 * You can download the list of files by searching with css selector - span.download-item a[href]
 * The url may contain spaces and needs to be encoded. This is done by constructing the url into a uri
 * Check if the file was downloaded before with etag or last modified
 * Create the content entry
 */
class DdlContentScraper(containerDir: File, lang: String, db: UmAppDatabase, contentEntryUid: Long) : HarScraper(containerDir, db, contentEntryUid) {

    private val contentEntryDao: ContentEntryDao
    private val categorySchemaDao: ContentCategorySchemaDao
    private val contentCategoryDao: ContentCategoryDao
    private val containerEtagDao: ContainerETagDao
    private val languageDao: LanguageDao
    private val containerDao: ContainerDao
    private val repository: UmAppDatabase = db
    private val language: Language
    private var doc: Document? = null
    lateinit var contentEntry: ContentEntry
        internal set


    init {
        contentEntryDao = repository.contentEntryDao
        categorySchemaDao = repository.contentCategorySchemaDao
        contentCategoryDao = repository.contentCategoryDao
        languageDao = repository.languageDao
        containerDao = repository.containerDao
        containerEtagDao = repository.containerETagDao
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

    override fun scrapeUrl(sourceUrl: String) {

        var fileUrl: URL? = null
        var eTagValue: String? = null
        val scraperResult: HarScraperResult?

        try {

            scraperResult = startHarScrape(sourceUrl, {

                it.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.se-pre-con")))
                it.until<WebElement>(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("span.download-item a[href]")))
                var list = chromeDriver.findElements(By.cssSelector("span.download-item a[href]"))
                if (list.isNotEmpty()) {
                    list[0].click()
                }
                waitForJSandJQueryToLoad(it)
                Thread.sleep(30000)

            }, addHarContent = false, filters = listOf { entry ->

                if (entry.request.url != fileUrl.toString()) {
                    entry.response = null
                }

                entry
            }) {

                val entry = it.har.log.entries.find { harEntry ->
                    harEntry.request.url == sourceUrl
                }

                if (entry == null) {
                    throw IllegalStateException("no source url found in har entry")
                }

                val doc = Jsoup.parse(entry.response.content.text)

                val thumbnail = doc!!.selectFirst("aside img")?.attr("src") ?: EMPTY_STRING

                val description = doc.selectFirst("meta[name=description]")?.attr("content")
                val authorTag = doc.selectFirst("article.resource-view-details h3:contains(Author) ~ p")
                val farsiAuthorTag = doc.selectFirst("article.resource-view-details h3:contains(نویسنده) ~ p")
                val pashtoAuthorTag = doc.selectFirst("article.resource-view-details h3:contains(لیکونکی) ~ p")

                val author = when {
                    authorTag != null -> authorTag.text()
                    farsiAuthorTag != null -> farsiAuthorTag.text()
                    pashtoAuthorTag != null -> pashtoAuthorTag.text()
                    else -> EMPTY_STRING
                }
                val publisherTag = doc.selectFirst("article.resource-view-details a[href*=publisher]")
                val publisher = publisherTag?.text() ?: DDL

                contentEntry = ContentScraperUtil.createOrUpdateContentEntry(sourceUrl, doc.title(),
                        sourceUrl, publisher, LICENSE_TYPE_CC_BY, language.langUid, null, description, true, author,
                        thumbnail, EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao)

                val downloadList = doc.select("span.download-item a[href]")

                if (downloadList.isEmpty()) {
                    throw IllegalStateException("No link found to download in the source page")
                }

                val downloadItem = downloadList[0]
                val href = downloadItem.attr("href")

                fileUrl = URL(URL(sourceUrl), href)

                val container = containerDao.getMostRecentContainerForContentEntry(contentEntry.contentEntryUid)
                        ?: return@startHarScrape true

                val fileEntry = it.har.log.entries.find { harEntry ->
                    harEntry.request.url == fileUrl.toString()
                }

                if (fileEntry == null) {
                    throw IllegalStateException("No File found for content in har entry")
                }

                val result = isContentUpdated(fileEntry, container)
                eTagValue = result.etag

                return@startHarScrape result.updated


            }
        } catch (e: Exception) {
            UMLogUtil.logError("$DDL Exception - Error downloading resource from url $sourceUrl")
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            contentEntryDao.updateContentEntryInActive(contentEntryUid, true)
            close()
            return
        }

        if (!scraperResult.updated) {
            close()
            return
        }

        val containerManager = scraperResult.containerManager

        if (containerManager?.allEntries?.isEmpty() != false) {

            contentEntryDao.updateContentEntryInActive(contentEntryUid, true)
            UMLogUtil.logError("$DDL Debug - Did not find any content to download at url $sourceUrl")
            close()
            return
        }

        runBlocking {

            val entry = containerManager.allEntries[0]
            val mimeType = Files.probeContentType(File(entry.cePath ?: "").toPath())
            val container = containerDao.findByUid(containerManager.containerUid)
            containerDao.updateMimeType(mimeType, container?.containerUid ?: 0)
            if (!eTagValue.isNullOrEmpty()) {
                val etagContainer = ContainerETag(container?.containerUid ?: 0, eTagValue!!)
                containerEtagDao.insert(etagContainer)
            }

        }

        close()
    }


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage: <ddl website url><container destination><lang en or fa or ps><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }
            UMLogUtil.setLevel(if (args.size == 4) args[3] else "")
            UMLogUtil.logInfo(args[0])
            UMLogUtil.logInfo(args[1])
            try {
                DdlContentScraper(File(args[1]), args[3], UmAppDatabase.Companion.getInstance(Any()), 0).scrapeUrl(args[0])
            } catch (e: IOException) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("$DDL Exception running scrapeContent ddl")
            }

        }
    }

}