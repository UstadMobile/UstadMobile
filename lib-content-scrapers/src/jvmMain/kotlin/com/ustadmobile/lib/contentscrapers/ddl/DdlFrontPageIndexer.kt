package com.ustadmobile.lib.contentscrapers.ddl

import ScraperTypes.DDL_SUBJECT_INDEXER
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.kodein.di.DI


class DdlFrontPageIndexer(parentContentEntryUid: Long, runUid: Int, sqiUid: Int, contentEntryUid: Long, endpoint: Endpoint, di: DI) : Indexer(parentContentEntryUid, runUid, sqiUid, contentEntryUid, endpoint, di) {

    var langCount = 0

    override fun indexUrl(sourceUrl: String) {

        val farsiLang = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(repo.languageDao, "fa")
        val pashtoLang = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(repo.languageDao, "ps")

        val parentDdl = ContentScraperUtil.createOrUpdateContentEntry(sourceUrl, "Darakht-e Danesh",
                sourceUrl, IndexDdlContent.DDL, ContentEntry.LICENSE_TYPE_CC_BY, englishLang.langUid, null,
                "Free and open educational resources for Afghanistan", false, "",
                "https://ddl.af/storage/files/logo-dd.png", "", "", 0, repo.contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(repo.contentEntryParentChildJoinDao, parentContentEntry, parentDdl, 2)

        createLangEntry("English", englishLang, parentDdl)
        createLangEntry("فارسی", farsiLang, parentDdl)
        createLangEntry("پښتو", pashtoLang, parentDdl)

        setIndexerDone(true, 0)
    }

    private fun createLangEntry(langName: String, langEntity: Language, parentDdl: ContentEntry) {

        val url = "https://www.ddl.af/${langEntity.iso_639_1_standard}/resources/list"

        val langEntry = ContentScraperUtil.createOrUpdateContentEntry("${langEntity.iso_639_1_standard}/resources/list", langName,
                url, IndexDdlContent.DDL, ContentEntry.LICENSE_TYPE_CC_BY, langEntity.langUid, null,
                "", false, "", "",
                "", "", 0, repo.contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(repo.contentEntryParentChildJoinDao, parentDdl, langEntry, langCount++)

        createQueueItem(url, langEntry, DDL_SUBJECT_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX, parentDdl.contentEntryUid)
    }


    override fun close() {

    }
}