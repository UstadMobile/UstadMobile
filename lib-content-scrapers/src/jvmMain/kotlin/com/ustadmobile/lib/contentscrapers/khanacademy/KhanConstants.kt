package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Language

data class KhanLang(val url: String, val title: String)

private fun getLangUrl(langCode: String): String {
    return "https://$langCode.khanacademy.org/"
}

fun getKhanEntry(englishLang: Language, contentEntryDao: ContentEntryDao): ContentEntry {
    return ContentScraperUtil.createOrUpdateContentEntry(
            "https://www.khanacademy.org/", "Khan Academy",
            "https://www.khanacademy.org/", ScraperConstants.KHAN,
            ContentEntry.LICENSE_TYPE_CC_BY_NC, englishLang.langUid, null,
            "You can learn anything.\n" + "For free. For everyone. Forever.",
            false, ScraperConstants.EMPTY_STRING,
            "https://cdn.kastatic.org/images/khan-logo-dark-background.new.png",
            ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
            0, contentEntryDao)
}

object KhanConstants {

    val khanLangMap = mapOf(
            "www" to KhanLang(getLangUrl("en"), "English"),
            "ar" to KhanLang(getLangUrl("ar"), "العربية"),
            "fa-af" to KhanLang(getLangUrl("fa-af"), "داري"),
            "el" to KhanLang(getLangUrl("el"), "Ελληνικά"),
            "he" to KhanLang(getLangUrl("he"), "עברית"),
            "zlm" to  KhanLang(getLangUrl("zlm"), "BahasaMalaysian"),
            "fa" to KhanLang(getLangUrl("fa"), "فارسی"),
            "sw" to KhanLang(getLangUrl("sw"), "Kiswahili"),
            "te" to KhanLang(getLangUrl("te"), "తెలుగు"),
            "th" to KhanLang(getLangUrl("th"), "ไทย"),
            "uk" to KhanLang(getLangUrl("uk"), "українська"),
            "ur" to KhanLang(getLangUrl("ur"), "اردو"),
            "xh" to  KhanLang(getLangUrl("xh"), "isiXhosa"),
            "zu" to KhanLang(getLangUrl("zu"), "Zulu"))

}