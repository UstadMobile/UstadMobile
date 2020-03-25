package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.abztract.YoutubePlaylistIndexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem

class KhanYoutubePlaylistIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase, sqiUid: Int) : YoutubePlaylistIndexer(parentContentEntry, runUid, db, sqiUid) {

    override fun indexUrl(sourceUrl: String) {

        var counter = 0
        try {
            startPlayListIndexer(sourceUrl) {

                val youtubeEntry = ContentScraperUtil.createOrUpdateContentEntry(it.id!!, it.fulltitle,
                        it.webpage_url!!, ScraperConstants.HAB, ContentEntry.LICENSE_TYPE_OTHER,
                        parentcontentEntry?.primaryLanguageUid
                                ?: 0, parentcontentEntry?.languageVariantUid,
                        it.description, true, ScraperConstants.EMPTY_STRING, it.thumbnail,
                        ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING, ContentEntry.VIDEO_TYPE, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentcontentEntry!!, youtubeEntry, counter++)

                createQueueItem(it.webpage_url!!, youtubeEntry, ScraperTypes.KHAN_YOUTUBE_SCRAPER, ScrapeQueueItem.ITEM_TYPE_SCRAPE)

                it
            }
        }catch (e : Exception){
            throw e
        }
        setIndexerDone(true, 0)
    }

    override fun close() {

    }
}