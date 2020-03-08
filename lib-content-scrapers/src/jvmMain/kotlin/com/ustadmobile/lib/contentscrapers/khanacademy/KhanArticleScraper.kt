package com.ustadmobile.lib.contentscrapers.khanacademy

import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_CSS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import com.ustadmobile.lib.contentscrapers.abztract.ScraperException
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.KHAN_COOKIE
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.KHAN_CSS
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.regexUrlPrefix
import com.ustadmobile.lib.contentscrapers.util.StringEntrySource
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.runBlocking
import net.lightbody.bmp.core.har.HarEntry
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import java.io.File
import java.net.URL
import java.util.*

class KhanArticleScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int) : HarScraper(containerDir, db, contentEntryUid, sqiUid) {


    override fun scrapeUrl(sourceUrl: String) {

        var entry: ContentEntry? = null
        runBlocking {
            entry = contentEntryDao.findByUidAsync(contentEntryUid)
        }

        if (entry == null) {
            hideContentEntry()
            setScrapeDone(false, ERROR_TYPE_NO_SOURCE_URL_FOUND)
            throw ScraperException(ERROR_TYPE_NO_SOURCE_URL_FOUND, "Content Entry was not found for url $sourceUrl")
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

        if (contentList.isEmpty()) {
            throw IllegalArgumentException("Does not have the article data id which we need to scrape the page for url $sourceUrl")
        }

        var foundRelative = false
        var nodeSlug: String? = null
        for (content in contentList) {

            if (sourceUrl.contains(content.relativeUrl!!)) {


                foundRelative = true
                val articleId = content.id
                nodeSlug = content.nodeSlug
                val articleUrl = generateArtcleUrl(articleId)
                val response = gson.fromJson(IOUtils.toString(URL(articleUrl), UTF_ENCODING), ArticleResponse::class.java)
                val dateModified = ContentScraperUtil.parseServerDate(response.date_modified!!)

                val recentContainer = containerDao.getMostRecentContainerForContentEntry(contentEntryUid)

                val isContentUpdated = if (recentContainer == null) true else {
                    dateModified > recentContainer.cntLastModified
                }

                if (!isContentUpdated) {
                    showContentEntry()
                    setScrapeDone(true, 0)
                    return
                }

                break

            }
        }

        if (foundRelative) {
            UMLogUtil.logDebug("found the id at url $sourceUrl")
        } else {
            // TODO error and scraper done
            setScrapeDone(false, 0)
            close()
            throw IllegalArgumentException("did not find id at url $sourceUrl")
        }

        val scraperResult = startHarScrape(sourceUrl, {

            it.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div[id*=discussiontabbedpanel]")))

        }, filters = listOf { entry ->

            if (entry.request.url == sourceUrl) {

                val doc = Jsoup.parse(entry.response.content.text)
                doc.head().append(KHAN_CSS)
                doc.head().append(KHAN_COOKIE)

                entry.response.content.text = doc.html()

            }
            entry
        }) {

            it.har.log.entries.add(addHarEntry(
                    IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.KHAN_CSS_LINK), UTF_ENCODING),
                    mimeType = MIMETYPE_CSS,
                    requestUrl = "https://www.khanacademy.org/khanscraper.css"))

            true
        }


        val linkMap = HashMap<String, String>()
        val navList = navData.navItems
        if (navList != null) {
            for (navItem in navList) {
                if (navItem.nodeSlug == nodeSlug) {
                    continue
                }
                linkMap[regexUrlPrefix + navItem.nodeSlug!!] = KhanContentScraper.CONTENT_DETAIL_SOURCE_URL_KHAN_ID + navItem.id!!
            }
        } else {
            UMLogUtil.logError("Your related items are in another json for url $sourceUrl")
        }

        runBlocking {
            scraperResult.containerManager?.addEntries(StringEntrySource(gson.toJson(linkMap).toString(), listOf("linksMap")))
        }


        setScrapeDone(true, 0)
        close()


    }

    private fun generateArtcleUrl(articleId: String?): String {
        return "http://www.khanacademy.org/api/v1/articles/" + articleId!!
    }
}