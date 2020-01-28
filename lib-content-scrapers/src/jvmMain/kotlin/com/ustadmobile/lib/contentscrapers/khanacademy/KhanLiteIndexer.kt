package com.ustadmobile.lib.contentscrapers.khanacademy

import ScraperTypes.KHAN_LITE_VIDEO_SCRAPER
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.SeleniumIndexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import java.net.URL

class KhanLiteIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase) : SeleniumIndexer(parentContentEntry, runUid, db) {

    override fun indexUrl(sourceUrl: String) {

        val document = startSeleniumIndexer(sourceUrl) {

            it.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#library-content-main")))

        }

        val fullList = document.select("div#library-content-main div[data-role=page]")

        fullList.forEachIndexed { count, element ->

            val header = element.selectFirst("div.library-content-header h2")?.text() ?: ""

            if (header.isNullOrEmpty()) {
                UMLogUtil.logError("page had a missing header text for count $count for url $sourceUrl")
                return@forEachIndexed
            }

            val description = document.select("div.library-content-list p.topic-desc")?.text() ?: ""

            val headerEntry = ContentScraperUtil.createOrUpdateContentEntry(header, header, header,
                    ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC,
                    contentEntry!!.primaryLanguageUid, contentEntry!!.languageVariantUid,
                    description, false, ScraperConstants.EMPTY_STRING, "",
                    ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                    0, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, contentEntry!!, headerEntry, count)

            val contentList = element.select("div.library-content-list li.subjects-row-first td a.subject-link")

            contentList.forEachIndexed { contentCount, contentElement ->

                val href = contentElement.attr("href")
                val title = contentElement.text()

                val contentUrl = URL(URL(sourceUrl), href)

                val contentId = contentUrl.toString().substringAfter("v=")

                if (contentId.isNullOrEmpty()) {
                    UMLogUtil.logError("no Content Id found for element $title  with href $href in heading $header on url $sourceUrl")
                    return@forEachIndexed
                }

                val entry = ContentScraperUtil.createOrUpdateContentEntry(contentId, title,
                        KhanContentIndexer.KHAN_PREFIX + contentId, ScraperConstants.KHAN,
                        ContentEntry.LICENSE_TYPE_CC_BY_NC, contentEntry!!.primaryLanguageUid,
                        contentEntry!!.languageVariantUid, "", true,
                        ScraperConstants.EMPTY_STRING, "",
                        ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                        ContentEntry.VIDEO_TYPE, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, headerEntry, entry, contentCount)

                createQueueItem(contentUrl.toString(), entry, KHAN_LITE_VIDEO_SCRAPER, ScrapeQueueItem.ITEM_TYPE_SCRAPE)

            }


        }

    }

    override fun close() {

    }
}