package com.ustadmobile.lib.contentscrapers.ddl

import ScraperTypes.DDL_SUBJECT_INDEXER
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.ScrapeQueueItem

class DdlFrontPageIndexer(contentEntryUid: Long, runUid: Int, db: UmAppDatabase, sqiUid: Int) : Indexer(contentEntryUid, runUid, db, sqiUid) {

    var langCount = 0

    override fun indexUrl(sourceUrl: String) {

        val farsiLang = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, "fa")
        val pashtoLang = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, "ps")

        val parentDdl = ContentScraperUtil.createOrUpdateContentEntry(sourceUrl, "Darakht-e Danesh",
                sourceUrl, IndexDdlContent.DDL, ContentEntry.LICENSE_TYPE_CC_BY, englishLang.langUid, null,
                "Free and open educational resources for Afghanistan", false, ScraperConstants.EMPTY_STRING,
                "https://ddl.af/storage/files/logo-dd.png", ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING, 0, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, masterRootParent, parentDdl, 2)

        createLangEntry("English", englishLang, parentDdl)
        createLangEntry("فارسی", farsiLang, parentDdl)
        createLangEntry("پښتو", pashtoLang, parentDdl)
    }

    private fun createLangEntry(langName: String, langEntity: Language, parentDdl: ContentEntry) {

        val url = "https://www.ddl.af/${langEntity.iso_639_1_standard}/resources/list"

        val langEntry = ContentScraperUtil.createOrUpdateContentEntry("${langEntity.iso_639_1_standard}/resources/list", langName,
                url, IndexDdlContent.DDL, ContentEntry.LICENSE_TYPE_CC_BY, langEntity.langUid, null,
                ScraperConstants.EMPTY_STRING, false, ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING, 0, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentDdl, langEntry, langCount++)

        createQueueItem(url, langEntry, DDL_SUBJECT_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX)
    }


    override fun close() {

    }
}