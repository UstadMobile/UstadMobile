package com.ustadmobile.lib.contentscrapers.ddl

import ScraperTypes
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

class DdlSubjectIndexer(contentEntryUid: Long, runUid: Int, db: UmAppDatabase) : SeleniumIndexer(contentEntryUid, runUid, db) {

    override fun indexUrl(sourceUrl: String) {

        val document = startSeleniumIndexer(sourceUrl) {

            it.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.se-pre-con")))
        }

        val listOfSubjects = document.select("legend#resource-subjects + ul > li")

        UMLogUtil.logInfo("list of subjects found ${listOfSubjects.size}")

        val subjectSubTopicMap = mutableMapOf<String, ContentEntry>()

        listOfSubjects.forEachIndexed { i, subject ->

            val subjectId = subject.attr("value")
            val hrefLink = subject.attr("data-link")
            val title = subject.text()

            UMLogUtil.logInfo("found subject $title")

            val subjectUrl = URL(URL(sourceUrl), hrefLink)

            UMLogUtil.logInfo("with subject url $subjectUrl")

            val subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(subjectId, title,
                    subjectUrl.toString(), IndexDdlContent.DDL, ContentEntry.LICENSE_TYPE_CC_BY, parentcontentEntry!!.primaryLanguageUid, null,
                    ScraperConstants.EMPTY_STRING, false, ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                    ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING, 0, contentEntryDao)
            contentEntryDao.updateContentEntryInActive(subjectEntry.contentEntryUid, false)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentcontentEntry!!, subjectEntry, i)

            subjectSubTopicMap[subjectId] = subjectEntry

            createQueueItem(subjectUrl.toString(), subjectEntry, ScraperTypes.DDL_PAGES_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX)

        }


        val listOfSubTopics = document.select("legend#resource-subjects + ul > div")

        UMLogUtil.logInfo("list of sub topics found found ${listOfSubTopics.size}")

        listOfSubTopics.forEach { element ->

            val subjectId = element.id().split("-")[1]
            val subTopics = element.select("li")

            val parentEntry = subjectSubTopicMap[subjectId]

            UMLogUtil.logInfo("found ${subTopics.size} for parent $subjectId")

            subTopics.forEachIndexed { i, subTopic ->

                val hrefLink = subTopic.attr("data-link")
                val title = subTopic.text()
                val subTopicId = subTopic.attr("value")

                UMLogUtil.logInfo("found subtopic $title")

                val subjectUrl = URL(URL(sourceUrl), hrefLink)

                UMLogUtil.logInfo("with subtopic url $subjectUrl")

                val subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(subTopicId, title,
                        subjectUrl.toString(), IndexDdlContent.DDL, ContentEntry.LICENSE_TYPE_CC_BY, parentcontentEntry!!.primaryLanguageUid, null,
                        ScraperConstants.EMPTY_STRING, false, ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                        ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING, 0, contentEntryDao)
                contentEntryDao.updateContentEntryInActive(subjectEntry.contentEntryUid, false)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentEntry!!, subjectEntry, i)

                createQueueItem(subjectUrl.toString(), subjectEntry, ScraperTypes.DDL_PAGES_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX)

            }

        }

    }

    override fun close() {

    }


}