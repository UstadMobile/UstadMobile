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
import net.lightbody.bmp.core.har.HarEntry
import java.lang.Exception
import java.net.URL

@ExperimentalStdlibApi
class KhanFullIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase, sqiUid: Int) : HarIndexer(parentContentEntry, runUid, db, sqiUid) {

    override fun indexUrl(sourceUrl: String) {

        val khanEntry = getKhanEntry(englishLang, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, masterRootParent, khanEntry, 12)

        val lang = sourceUrl.substringBefore(".khan").substringAfter("://")

        val khanLang = KhanConstants.khanFullMap[lang]
                ?: throw ScraperException(0, "Do not have support for language: $lang")

        val parentLangEntry = createKangLangEntry(if (lang == "www") "en" else lang, khanLang.title, khanLang.url, db)
        hideContentEntry(parentLangEntry.contentEntryUid)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, khanEntry, parentLangEntry, 0)

        val harEntryList: List<HarEntry>
        try {
            harEntryList = startHarIndexer(sourceUrl, listOf(Regex("learnMenuTopicsQuery"))) {
                true
            }
        }catch (e: Exception){
            throw e
        }

        val gson = GsonBuilder().disableHtmlEscaping().create()
        val response = gson.fromJson(harEntryList[0].response.content.text, FullMenuResponse::class.java)

        response.data?.learnMenuTopics?.forEachIndexed { topicCount, topic ->

            val topicUrl = URL(URL(sourceUrl), topic.href)

            val topicEntry = ContentScraperUtil.createOrUpdateContentEntry(
                    topic.slug!!, topic.translatedTitle,
                    topicUrl.toString(), ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC,
                    parentLangEntry.primaryLanguageUid, parentLangEntry.languageVariantUid,
                    "", false, ScraperConstants.EMPTY_STRING,
                    ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                    ScraperConstants.EMPTY_STRING,
                    0, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao,
                    parentLangEntry, topicEntry, topicCount)

            createQueueItem(topicUrl.toString(), topicEntry, KHAN_TOPIC_INDEXER,
                    ScrapeQueueItem.ITEM_TYPE_INDEX)

            if(lang == "hi"){

                topic.children?.forEachIndexed { childTopicCount,  childTopic ->

                    val childTopicUrl = URL(URL(sourceUrl), childTopic.href)

                    val childTopicEntry = ContentScraperUtil.createOrUpdateContentEntry(
                            childTopic.slug!!, childTopic.translatedTitle,
                            childTopicUrl.toString(), ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC,
                            topicEntry.primaryLanguageUid, topicEntry.languageVariantUid,
                            "", false, ScraperConstants.EMPTY_STRING,
                            ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                            ScraperConstants.EMPTY_STRING,
                            0, contentEntryDao)


                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao,
                            topicEntry, childTopicEntry, childTopicCount)

                    createQueueItem(childTopicUrl.toString(), childTopicEntry, KHAN_TOPIC_INDEXER,
                            ScrapeQueueItem.ITEM_TYPE_INDEX)

                }
            }

        }

        setIndexerDone(true, 0)

    }
}