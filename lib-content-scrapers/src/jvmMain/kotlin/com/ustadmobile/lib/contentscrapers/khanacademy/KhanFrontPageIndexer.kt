package com.ustadmobile.lib.contentscrapers.khanacademy

import ScraperTypes.KHAN_LITE_INDEXER
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.LanguageVariant
import com.ustadmobile.lib.db.entities.ScrapeQueueItem

class KhanFrontPageIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase) : Indexer(parentContentEntry, runUid, db) {

    private var langCount = 0

    private lateinit var parentEntry: ContentEntry

    override fun indexUrl(sourceUrl: String) {

        parentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://www.khanacademy.org/", "Khan Academy",
                sourceUrl, ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC, englishLang.langUid, null,
                "You can learn anything.\n" + "For free. For everyone. Forever.", false, EMPTY_STRING,
                "https://cdn.kastatic.org/images/khan-logo-dark-background.new.png",
                EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, masterRootParent, parentEntry, 12)

        createFullLangEntry("en", "English")

        createLiteLangEntry("ar","العربية")
        createLiteLangEntry("fa-af", "داري")
        createLiteLangEntry("el", "Ελληνικά")
        createLiteLangEntry("he", "עברית")
        createLiteLangEntry("zlm", "BahasaMalaysian")
        createLiteLangEntry("fa", "فارسی")
        createLiteLangEntry("sw", "Kiswahili")
        createLiteLangEntry("te", "తెలుగు")
        createLiteLangEntry("th", "ไทย")
        createLiteLangEntry("uk", "українська")
        createLiteLangEntry("ur", "اردو")
        createLiteLangEntry("xh", "isiXhosa")
        createLiteLangEntry("zu", "Zulu")

    }

    private fun createFullLangEntry(langCode: String, langName: String) {

        val url = "https://$langCode.khanacademy.org/"

        val langEntry = createEntry(langCode, langName, url)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentEntry, langEntry, langCount++)

    }

    private fun createLiteLangEntry(langCode: String, langName: String) {

        val url = "https://$langCode.khanacademy.org/"

        val langEntry = createEntry(langCode, langName, url)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentEntryParentChildJoinDao, parentEntry, langEntry, 200 + langCount++)

        createQueueItem(url, langEntry, KHAN_LITE_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX)

    }

    private fun createEntry(langCode: String, langName: String, url: String): ContentEntry {

        val langSplitArray = langCode.split("-")
        val langEntity = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, langSplitArray[0])
        var langVariantEntity: LanguageVariant? = null
        if (langSplitArray.size > 1) {
            langVariantEntity = ContentScraperUtil.insertOrUpdateLanguageVariant(db.languageVariantDao, langSplitArray[1], langEntity)
        }

        return ContentScraperUtil.createOrUpdateContentEntry(url, langName,
                url, ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC,
                langEntity.langUid, langVariantEntity?.langVariantUid ?: 0,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING,
                0, contentEntryDao)

    }


    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}