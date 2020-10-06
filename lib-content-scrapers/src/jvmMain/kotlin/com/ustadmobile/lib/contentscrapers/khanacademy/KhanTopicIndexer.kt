package com.ustadmobile.lib.contentscrapers.khanacademy

import ScraperTypes
import ScraperTypes.KHAN_FULL_ARTICLE_SCRAPER
import ScraperTypes.KHAN_FULL_EXERCISE_SCRAPER
import ScraperTypes.KHAN_FULL_VIDEO_SCRAPER
import com.google.gson.GsonBuilder
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.kodein.di.DI
import java.net.URL

@ExperimentalStdlibApi
class KhanTopicIndexer(parentContentEntry: Long, runUid: Int, sqiUid: Int, contentEntryUid: Long, endpoint: Endpoint, di: DI) : Indexer(parentContentEntry, runUid, sqiUid, contentEntryUid, endpoint, di) {


    override fun indexUrl(sourceUrl: String) {

        val url = URL(sourceUrl)

        val jsonContent = getJsonContent(url)

        val gson = GsonBuilder().disableHtmlEscaping().create()

        var response: SubjectListResponse? = gson.fromJson(jsonContent, SubjectListResponse::class.java)
        if (response!!.componentProps == null) {
            response = gson.fromJson(jsonContent, PropsSubjectResponse::class.java).props
        }

        response?.componentProps?.curation?.tabs?.forEachIndexed { i, tab ->

            val tabModules = tab.modules

            if (tabModules == null || tabModules.isEmpty()) {
                return@forEachIndexed
            }

            tabModules.forEachIndexed { moduleCount, module ->

                if (ScraperConstants.TABLE_OF_CONTENTS_ROW == module.kind) {

                    // for url kha.org/math
                    createModule(url, module, moduleCount)


                } else if (ScraperConstants.SUBJECT_PROGRESS == module.kind) {

                    val moduleItems = module.modules

                    if (moduleItems == null || moduleItems.isEmpty()) {
                        return@forEachIndexed
                    }

                    moduleItems.forEachIndexed { itemCount, moduleItem ->

                        if (ScraperConstants.SUBJECT_PAGE_TOPIC_CARD == moduleItem.kind) {

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
                                parentContentEntry!!.primaryLanguageUid,
                                parentContentEntry!!.languageVariantUid,
                                tutorial.description, false,
                                "", "",
                                "", "",
                                0, contentEntryDao)

                        ContentScraperUtil.insertOrUpdateParentChildJoin(
                                contentEntryParentChildJoinDao,
                                parentContentEntry!!, tutorialEntry, tutorialCount)

                        tutorial.contentItems?.forEachIndexed { contentCount, contentItem ->


                            val type = contentKindMap[contentItem.kind]

                            if (type == null) {
                                UMLogUtil.logFatal("Do not have support for kind ${contentItem.kind} for source Url $url")
                                return@forEachIndexed
                            }

                            val contentUrl = URL(url, contentItem.nodeUrl!!)
                            var lang = sourceUrl.substringBefore(".khan").substringAfter("://")

                            if (lang == "www") {
                                lang = ""
                            }

                            val entry = ContentScraperUtil.createOrUpdateContentEntry(
                                    contentItem.slug!!, contentItem.title,
                                    "${ScraperConstants.KHAN_PREFIX}${contentItem.contentId!!}${if (lang.isNotEmpty()) ".$lang" else ""}",
                                    ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC,
                                    parentContentEntry!!.primaryLanguageUid,
                                    parentContentEntry!!.languageVariantUid,
                                    contentItem.description,
                                    true, "",
                                    contentItem.thumbnailUrl,
                                    "", "",
                                    0, contentEntryDao)

                            ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(
                                    contentEntryParentChildJoinDao, tutorialEntry,
                                    entry, contentCount)



                            createQueueItem(contentUrl.toString(), entry,
                                    type, ScrapeQueueItem.ITEM_TYPE_SCRAPE, tutorialEntry.contentEntryUid)


                        }


                    }


                }else if(ScraperConstants.CONTENT_LIST == module.kind){

                    val moduleItems = module.contentItems

                    if (moduleItems == null || moduleItems.isEmpty()) {
                        return@forEachIndexed
                    }

                    val tutorialEntry = ContentScraperUtil.createOrUpdateContentEntry(
                            module.slug!!, module.title, module.topicId!!,
                            ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC,
                            parentContentEntry!!.primaryLanguageUid,
                            parentContentEntry!!.languageVariantUid,
                            module.description, false,
                            "", module.imageUrl,
                            "", "",
                            0, contentEntryDao)

                    ContentScraperUtil.insertOrUpdateParentChildJoin(
                            contentEntryParentChildJoinDao,
                            parentContentEntry!!, tutorialEntry, moduleCount)


                    module.contentItems?.forEachIndexed { itemCount, contentItem ->

                        val type = contentKindMap[contentItem.kind]

                        if (type == null) {
                            UMLogUtil.logFatal("Do not have support for kind ${contentItem.kind} for source Url $url")
                            return@forEachIndexed
                        }

                        val contentUrl = URL(url, contentItem.nodeUrl!!)
                        var lang = sourceUrl.substringBefore(".khan").substringAfter("://")

                        if (lang == "www") {
                            lang = ""
                        }

                        val entry = ContentScraperUtil.createOrUpdateContentEntry(
                                contentItem.slug!!, contentItem.title,
                                "${ScraperConstants.KHAN_PREFIX}${contentItem.contentId!!}${if (lang.isNotEmpty()) ".$lang" else ""}",
                                ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC,
                                parentContentEntry!!.primaryLanguageUid,
                                parentContentEntry!!.languageVariantUid,
                                contentItem.description,
                                true, "",
                                contentItem.thumbnailUrl,
                                "", "",
                                0, contentEntryDao)

                        ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(
                                contentEntryParentChildJoinDao, tutorialEntry,
                                entry, itemCount)

                        createQueueItem(contentUrl.toString(), entry,
                                type, ScrapeQueueItem.ITEM_TYPE_SCRAPE, tutorialEntry.contentEntryUid)


                    }

                }

            }


        }

        setIndexerDone(true, 0)


    }

    override fun close() {

    }

    private fun createModule(url: URL, module: ModuleResponse, count: Int) {

        val subjectUrl = URL(url, module.url!!)

        val subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(module.slug!!,
                module.title, subjectUrl.toString(), ScraperConstants.KHAN,
                ContentEntry.LICENSE_TYPE_CC_BY_NC, parentContentEntry!!.primaryLanguageUid,
                parentContentEntry!!.languageVariantUid, module.description,
                false, "",
                module.icon, "",
                "",
                0, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(
                contentEntryParentChildJoinDao,
                parentContentEntry!!, subjectEntry, count)

        createQueueItem(subjectUrl.toString(), subjectEntry,
                ScraperTypes.KHAN_TOPIC_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX, parentContentEntryUid)


    }

    companion object {

        val contentKindMap = mapOf(
                "Video" to KHAN_FULL_VIDEO_SCRAPER,
                "Talkthrough" to KHAN_FULL_VIDEO_SCRAPER,
                "Article" to KHAN_FULL_ARTICLE_SCRAPER,
                "Exercise" to KHAN_FULL_EXERCISE_SCRAPER)

    }


}