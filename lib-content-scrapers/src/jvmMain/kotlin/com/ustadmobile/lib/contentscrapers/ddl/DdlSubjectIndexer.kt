package com.ustadmobile.lib.contentscrapers.ddl

import ScraperTypes
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.jsoup.Jsoup
import java.io.File
import java.net.URL

class DdlSubjectIndexer(contentEntryUid: Long, runUid: Int, db: UmAppDatabase) : Indexer(contentEntryUid, runUid, db) {

    override fun indexUrl(sourceUrl: String) {

        val document = Jsoup.connect(sourceUrl)
                .header("X-Requested-With", "XMLHttpRequest").get()

        val listOfSubjects = document.select("legend#resource-subjects + ul > li")

        val subjectSubTopicMap = mutableMapOf<String, ContentEntry>()

        listOfSubjects.forEachIndexed { i, subject ->

            val subjectId = subject.attr("value")
            val hrefLink = subject.attr("data-link")
            val title = subject.text()

            val subjectUrl = URL(URL(sourceUrl), hrefLink)

            val subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(subjectId, title,
                    subjectUrl.toString(), IndexDdlContent.DDL, ContentEntry.LICENSE_TYPE_CC_BY, contentEntry!!.primaryLanguageUid, null,
                    ScraperConstants.EMPTY_STRING, false, ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                    ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING, 0, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, contentEntry!!, subjectEntry, i)

            subjectSubTopicMap[subjectId] = subjectEntry

            createQueueItem(subjectUrl.toString(), subjectEntry, ScraperTypes.DDL_PAGES_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX)

        }


        val listOfSubTopics = document.select("legend#resource-subjects + ul > div")

        listOfSubTopics.forEach { element ->

            val subjectId = element.id().split("-")[1]
            val subTopics = element.select("li")

            val parentEntry = subjectSubTopicMap[subjectId]

            subTopics.forEachIndexed { i, subTopic ->

                val hrefLink = subTopic.attr("data-link")
                val title = subTopic.text()

                val subjectUrl = URL(URL(sourceUrl), hrefLink)

                val subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(subjectId, title,
                        subjectUrl.toString(), IndexDdlContent.DDL, ContentEntry.LICENSE_TYPE_CC_BY, contentEntry!!.primaryLanguageUid, null,
                        ScraperConstants.EMPTY_STRING, false, ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                        ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING, 0, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentEntry!!, subjectEntry, i)

                createQueueItem(subjectUrl.toString(), subjectEntry, ScraperTypes.DDL_PAGES_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX)

            }

        }

    }

    override fun close() {

    }


}