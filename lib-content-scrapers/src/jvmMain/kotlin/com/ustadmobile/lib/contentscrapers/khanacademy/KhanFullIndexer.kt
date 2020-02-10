package com.ustadmobile.lib.contentscrapers.khanacademy

import ScraperTypes.KHAN_TOPIC_INDEXER
import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.abztract.HarIndexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import java.net.URL

class KhanFullIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase) : HarIndexer(parentContentEntry, runUid, db) {

    override fun indexUrl(sourceUrl: String) {

        val harEntryList = startHarIndexer(sourceUrl, listOf(Regex("learnMenuTopicsQuery"))) {
            true
        }

        val gson = GsonBuilder().disableHtmlEscaping().create()
        val response = gson.fromJson(harEntryList[0].response.content.text, FullMenuResponse::class.java)

        response.data?.learnMenuTopics?.forEachIndexed { topicCount, topic ->

            val topicUrl = URL(URL(sourceUrl), topic.href)

            val topicEntry = ContentScraperUtil.createOrUpdateContentEntry(
                    topic.slug!!, topic.translatedTitle,
                    topicUrl.toString(), ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC,
                    contentEntry!!.primaryLanguageUid, contentEntry!!.languageVariantUid,
                    "", false, ScraperConstants.EMPTY_STRING,
                    ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                    ScraperConstants.EMPTY_STRING,
                    0, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, contentEntry!!, topicEntry, topicCount)

            createQueueItem(topicUrl.toString(), topicEntry, KHAN_TOPIC_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX)
        }


    }
}