package com.ustadmobile.lib.contentscrapers.khanacademy

import ScraperTypes.KHAN_TOPIC_INDEXER
import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.abztract.HarIndexer
import com.ustadmobile.lib.contentscrapers.abztract.ScraperException
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import java.net.URL

class KhanFullIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase, sqiUid: Int) : HarIndexer(parentContentEntry, runUid, db, sqiUid) {

    override fun indexUrl(sourceUrl: String) {

        val khanEntry = getKhanEntry(englishLang, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, masterRootParent, khanEntry, 12)

        val lang = sourceUrl.substringBefore(".khan").substringAfter("://")

        val khanLang = KhanConstants.khanLangMap[lang]
                ?: throw ScraperException(0, "Do not have support for lite language: $lang")


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
                    parentcontentEntry!!.primaryLanguageUid, parentcontentEntry!!.languageVariantUid,
                    "", false, ScraperConstants.EMPTY_STRING,
                    ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                    ScraperConstants.EMPTY_STRING,
                    0, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao,
                    parentcontentEntry!!, topicEntry, topicCount)

            createQueueItem(topicUrl.toString(), topicEntry, KHAN_TOPIC_INDEXER,
                    ScrapeQueueItem.ITEM_TYPE_INDEX)
        }

    }
}