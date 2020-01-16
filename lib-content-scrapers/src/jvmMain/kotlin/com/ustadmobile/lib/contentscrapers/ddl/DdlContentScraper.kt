package com.ustadmobile.lib.contentscrapers.ddl

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerETagDao
import com.ustadmobile.core.db.dao.ContentCategoryDao
import com.ustadmobile.core.db.dao.ContentCategorySchemaDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import com.ustadmobile.lib.contentscrapers.ddl.IndexDdlContent.Companion.DDL
import com.ustadmobile.lib.db.entities.ContainerETag
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.REL_TYPE_SEE_ALSO
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.REL_TYPE_TRANSLATED_VERSION
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Files


/**
 * Once the resource page is opened.
 * You can download the list of files by searching with css selector - span.download-item a[href]
 * The url may contain spaces and needs to be encoded. This is done by constructing the url into a uri
 * Check if the file was downloaded before with etag or last modified
 * Create the content entry
 */
class DdlContentScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long) : HarScraper(containerDir, db, contentEntryUid) {

    private val categorySchemaDao: ContentCategorySchemaDao
    private val contentCategoryDao: ContentCategoryDao
    private val containerEtagDao: ContainerETagDao
    private val repository: UmAppDatabase = db

    private lateinit var contentEntry: ContentEntry

    init {
        categorySchemaDao = repository.contentCategorySchemaDao
        contentCategoryDao = repository.contentCategoryDao
        containerEtagDao = repository.containerETagDao
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
                val list = chromeDriver.findElements(By.cssSelector("span.download-item a[href]"))
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
                val author = doc.selectFirst("article.resource-view-details h3:contains(Author), h3:contains(نویسنده), h3:contains(لیکونکی) ~ p")?.text()
                        ?: EMPTY_STRING
                val publisher = doc.selectFirst("article.resource-view-details a[href*=publisher]")?.text()
                        ?: DDL

                val twoLangCode = sourceUrl.substring(sourceUrl.indexOf(".af/") + 3).substringBefore("/")

                val langEntity = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(db.languageDao, twoLangCode)

                contentEntry = ContentScraperUtil.createOrUpdateContentEntry(sourceUrl, doc.title(),
                        sourceUrl, publisher, LICENSE_TYPE_CC_BY, langEntity.langUid, null, description, true, author,
                        thumbnail, EMPTY_STRING, EMPTY_STRING, ContentEntry.ARTICLE_TYPE, contentEntryDao)

                val subjectContainer = doc.select("article.resource-view-details a[href*=level]")

                val schemeName = when (twoLangCode) {
                    "en" -> "Resource Level"
                    "fa" -> "سطح منبع"
                    "ps" -> "د سرچینې کچي"
                    else -> "Resource Level"
                }

                val ddlSchema = ContentScraperUtil.insertOrUpdateSchema(categorySchemaDao, schemeName, "ddl/resource-level/$twoLangCode")

                val subjectList = subjectContainer.select("a")

                for (subject in subjectList) {

                    val title = subject.attr("title")

                    val categoryEntry = ContentScraperUtil.insertOrUpdateCategoryContent(contentCategoryDao, ddlSchema, title)
                    ContentScraperUtil.insertOrUpdateChildWithMultipleCategoriesJoin(
                            db.contentEntryContentCategoryJoinDao, categoryEntry, contentEntry)

                }

                val relatedList = doc.select("div.resource-related-items-box div a[href]")

                relatedList.forEach { element ->

                    val relatedHref = element.attr("href")
                    val index = relatedHref.indexOf("af/")
                    val relatedUrl = StringBuilder(relatedHref).insert(index + 3, "$twoLangCode/").toString()

                    val relatedEntry = ContentScraperUtil.insertTempContentEntry(contentEntryDao, relatedUrl, contentEntry.primaryLanguageUid)

                    ContentScraperUtil.insertOrUpdateRelatedContentJoin(db.contentEntryRelatedEntryJoinDao, relatedEntry, contentEntry, REL_TYPE_SEE_ALSO)
                }

                val translatedList = doc.select("article.resource-view-details a[title=language]:not([style])")

                if (translatedList.size > 1) {

                    translatedList.forEach { element ->

                        if (element.attr("hreflang") == twoLangCode) {
                            return@forEach
                        }

                        val relatedTwoCode = element.attr("hreflang")
                        val relatedLink = element.attr("href")

                        val translatedEntry = ContentScraperUtil.insertTempContentEntry(contentEntryDao, relatedLink, ContentScraperUtil.insertOrUpdateLanguageByTwoCode(db.languageDao, relatedTwoCode).langUid)
                        ContentScraperUtil.insertOrUpdateRelatedContentJoin(db.contentEntryRelatedEntryJoinDao, translatedEntry, contentEntry,
                                REL_TYPE_TRANSLATED_VERSION)

                    }
                }


                val downloadList = doc.select("span.download-item a[href]")

                if (downloadList.isEmpty()) {
                    throw IllegalStateException("No link found to download in the source page")
                }

                val downloadItem = downloadList[0]
                val href = downloadItem.attr("href")

                fileUrl = URL(URL(sourceUrl), href)

                val fileEntry = it.har.log.entries.find { harEntry ->
                    harEntry.request.url == fileUrl.toString()
                }

                if (fileEntry == null) {
                    throw IllegalStateException("No File found for content in har entry")
                }

                val entryETag = fileEntry.response.headers.find { valuePair -> valuePair.name == ETAG }
                eTagValue = entryETag?.value

                val container = containerDao.getMostRecentContainerForContentEntry(contentEntry.contentEntryUid)
                        ?: return@startHarScrape true

                return@startHarScrape isContentUpdated(fileEntry, container)


            }
        } catch (e: Exception) {
            contentEntryDao.updateContentEntryInActive(contentEntryUid, true)
            UMLogUtil.logError("$DDL Exception - Error downloading resource from url $sourceUrl")
            close()
            throw Exception(e)
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
            throw IllegalStateException("No File found for content in har entry")
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
                DdlContentScraper(File(args[1]), UmAppDatabase.Companion.getInstance(Any()), 0).scrapeUrl(args[0])
            } catch (e: IOException) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("$DDL Exception running scrapeContent ddl")
            }

        }
    }

}