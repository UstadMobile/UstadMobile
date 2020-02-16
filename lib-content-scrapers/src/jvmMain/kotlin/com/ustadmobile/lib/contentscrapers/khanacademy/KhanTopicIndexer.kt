package com.ustadmobile.lib.contentscrapers.khanacademy

import ScraperTypes
import ScraperTypes.KHAN_FULL_ARTICLE_SCRAPER
import ScraperTypes.KHAN_FULL_EXERCISE_SCRAPER
import ScraperTypes.KHAN_FULL_VIDEO_SCRAPER
import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.HarIndexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import java.net.URL

class KhanTopicIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase, sqiUid: Int) : HarIndexer(parentContentEntry, runUid, db, sqiUid) {


    override fun indexUrl(sourceUrl: String) {

        val url = URL(sourceUrl)

        val searchUrl = if (!sourceUrl.endsWith("/")) "$sourceUrl/" else sourceUrl

        val harEntryList = startHarIndexer(searchUrl, listOf(Regex("/content${url.path}"))) {
            true
        }

        val gson = GsonBuilder().disableHtmlEscaping().create()
        val subjectJson = harEntryList[0].response.content.text


        var response: SubjectListResponse? = gson.fromJson(subjectJson, SubjectListResponse::class.java)
        if (response!!.componentProps == null) {
            response = gson.fromJson(subjectJson, PropsSubjectResponse::class.java).props
        }

        response?.componentProps?.curation?.tabs?.forEachIndexed { i, tab ->

            val tabModules = tab.modules

            if (tabModules == null || tabModules.isEmpty()) {
                return@forEachIndexed
            }

            tabModules.forEachIndexed { moduleCount, module ->

                if (KhanContentIndexer.TABLE_OF_CONTENTS_ROW == module.kind) {

                    // for url kha.org/math
                    createModule(url, module, moduleCount)


                } else if (KhanContentIndexer.SUBJECT_PROGRESS == module.kind) {

                    val moduleItems = module.modules

                    if (moduleItems == null || moduleItems.isEmpty()) {
                        return@forEachIndexed
                    }

                    moduleItems.forEachIndexed { itemCount, moduleItem ->

                        if (KhanContentIndexer.SUBJECT_PAGE_TOPIC_CARD == moduleItem.kind) {

                            // for url kha.org/math/early-math
                            createModule(url, moduleItem, itemCount)
                        }

                    }

                } else if (module.tutorials != null && module.tutorials!!.isNotEmpty()) {

                    module.tutorials!!.forEachIndexed { tutorialCount, tutorial ->

                        val subjectUrl = URL(url, tutorial.url!!)
                        val tutorialEntry = ContentScraperUtil.createOrUpdateContentEntry(
                                tutorial.slug!!, tutorial.title, subjectUrl.toString(),
                                ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC,
                                parentcontentEntry!!.primaryLanguageUid,
                                parentcontentEntry!!.languageVariantUid,
                                tutorial.description, false,
                                ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                                ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                                0, contentEntryDao)

                        ContentScraperUtil.insertOrUpdateParentChildJoin(
                                contentEntryParentChildJoinDao,
                                parentcontentEntry!!, tutorialEntry, tutorialCount)

                        tutorial.contentItems?.forEachIndexed { contentCount, contentItem ->

                            val contentUrl = URL(url, contentItem.nodeUrl!!)
                            var lang = sourceUrl.substringBefore(".khan").substringAfter("://")

                            if (lang == "www") {
                                lang = ""
                            }

                            val entry = ContentScraperUtil.createOrUpdateContentEntry(
                                    contentItem.slug!!, contentItem.title,
                                    "${KhanContentIndexer.KHAN_PREFIX}${contentItem.contentId!!}${if (lang.isNotEmpty()) ".$lang" else ""}",
                                    ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC,
                                    parentcontentEntry!!.primaryLanguageUid,
                                    parentcontentEntry!!.languageVariantUid,
                                    contentItem.description,
                                    true, ScraperConstants.EMPTY_STRING,
                                    contentItem.thumbnailUrl,
                                    ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                                    0, contentEntryDao)

                            ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(
                                    contentEntryParentChildJoinDao, tutorialEntry,
                                    entry, contentCount)

                            val type = contentKindMap[contentItem.kind]

                            if (type == null) {
                                UMLogUtil.logFatal("Do not have support for kind ${contentItem.kind} for source Url $url")
                                return@forEachIndexed
                            }

                            createQueueItem(contentUrl.toString(), entry,
                                    type, ScrapeQueueItem.ITEM_TYPE_SCRAPE)


                        }


                    }


                }

            }


        }


    }

    private fun createModule(url: URL, module: ModuleResponse, count: Int) {

        val subjectUrl = URL(url, module.url!!)

        val subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(module.slug!!,
                module.title, subjectUrl.toString(), ScraperConstants.KHAN,
                ContentEntry.LICENSE_TYPE_CC_BY_NC, parentcontentEntry!!.primaryLanguageUid,
                parentcontentEntry!!.languageVariantUid, module.description,
                false, ScraperConstants.EMPTY_STRING,
                module.icon, ScraperConstants.EMPTY_STRING,
                ScraperConstants.EMPTY_STRING,
                0, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(
                contentEntryParentChildJoinDao,
                parentcontentEntry!!, subjectEntry, count)

        createQueueItem(subjectUrl.toString(), subjectEntry,
                ScraperTypes.KHAN_TOPIC_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX)


    }

    companion object {

        val contentKindMap = mapOf(
                "Video" to KHAN_FULL_VIDEO_SCRAPER,
                "Article" to KHAN_FULL_ARTICLE_SCRAPER,
                "Exercise" to KHAN_FULL_EXERCISE_SCRAPER)

    }


}