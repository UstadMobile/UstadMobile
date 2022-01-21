package com.ustadmobile.lib.contentscrapers.ddl

import ScraperTypes
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.SeleniumIndexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.kodein.di.DI
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import java.net.URL

class DdlSubjectIndexer(parentContentEntryUid: Long, runUid: Int, sqiUid: Int, contentEntryUid: Long, endpoint: Endpoint, di: DI) : SeleniumIndexer(parentContentEntryUid, runUid, sqiUid, contentEntryUid, endpoint, di) {

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
                    subjectUrl.toString(), IndexDdlContent.DDL, ContentEntry.LICENSE_TYPE_CC_BY, parentContentEntry!!.primaryLanguageUid, null,
                    "", false, "", "",
                    "", "", 0, repo.contentEntryDao)
            repo.contentEntryDao.updateContentEntryInActive(subjectEntry.contentEntryUid,
                false, systemTimeInMillis()
            )

            ContentScraperUtil.insertOrUpdateParentChildJoin(repo.contentEntryParentChildJoinDao, parentContentEntry!!, subjectEntry, i)

            subjectSubTopicMap[subjectId] = subjectEntry

            createQueueItem(subjectUrl.toString(), subjectEntry, ScraperTypes.DDL_PAGES_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX, parentContentEntryUid)

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
                        subjectUrl.toString(), IndexDdlContent.DDL, ContentEntry.LICENSE_TYPE_CC_BY, parentContentEntry!!.primaryLanguageUid, null,
                        "", false, "", "",
                        "", "", 0, repo.contentEntryDao)
                repo.contentEntryDao.updateContentEntryInActive(subjectEntry.contentEntryUid,
                    false, systemTimeInMillis())

                ContentScraperUtil.insertOrUpdateParentChildJoin(repo.contentEntryParentChildJoinDao, parentEntry!!, subjectEntry, i)

                createQueueItem(subjectUrl.toString(), subjectEntry, ScraperTypes.DDL_PAGES_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX, parentEntry.contentEntryUid)

            }

        }

        setIndexerDone(true, 0)
    }

    override fun close() {

    }


}