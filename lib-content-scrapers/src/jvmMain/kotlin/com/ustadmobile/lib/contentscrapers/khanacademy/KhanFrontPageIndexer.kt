package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.khanFullMap
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.khanLiteMap
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem

class KhanFrontPageIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase, sqiUid: Int) : Indexer(parentContentEntry, runUid, db, 0) {

    private lateinit var parentEntry: ContentEntry

    override fun indexUrl(sourceUrl: String) {

        parentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://www.khanacademy.org/", "Khan Academy",
                sourceUrl, ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC, englishLang.langUid, null,
                "You can learn anything.\n" + "For free. For everyone. Forever.", false, EMPTY_STRING,
                "https://cdn.kastatic.org/images/khan-logo-dark-background.new.png",
                EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, masterRootParent, parentEntry, 12)

        khanLiteMap.values.forEach{
            createQueueItem(it.url, parentEntry, ScraperTypes.KHAN_LITE_INDEXER,  ScrapeQueueItem.ITEM_TYPE_INDEX)
        }

        khanFullMap.values.forEach{
            createQueueItem(it.url, parentEntry, ScraperTypes.KHAN_FULL_INDEXER,  ScrapeQueueItem.ITEM_TYPE_INDEX)
        }


    }

    override fun close() {

    }

}