package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.UstadMobileConstants.UTF8
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.JSON_PATH
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.LanguageVariant
import org.apache.commons.io.IOUtils
import java.net.URL

data class KhanLang(val url: String, val title: String)

private fun getLangUrl(langCode: String): String {
    return "https://$langCode.khanacademy.org/"
}

private fun getJsonURL(langCode: String, sourceUrl: URL): URL {
    return URL(sourceUrl, "$JSON_PATH${sourceUrl.path}?lang=$langCode")
}

fun getLangCodeFromURL(sourceUrl : URL): String{
    var langCode = sourceUrl.toString().substringBefore(".khan").substringAfter("://")

    if (langCode == "www") {
        langCode = "en"
    }

    return langCode
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

fun getJsonContent(sourceUrl: URL): String {

    val langCode = getLangCodeFromURL(sourceUrl)

    return IOUtils.toString(getJsonURL(langCode, sourceUrl), UTF8)
}

fun createKangLangEntry(langCode: String, langName: String, url: String, db: UmAppDatabase): ContentEntry {

    val langSplitArray = langCode.split("-")
    val langEntity: Language
    langEntity = if (langSplitArray[0].length == 2) {
        ContentScraperUtil.insertOrUpdateLanguageByTwoCode(db.languageDao, langSplitArray[0])
    } else {
        ContentScraperUtil.insertOrUpdateLanguageByThreeCode(db.languageDao, langSplitArray[0])
    }

    var langVariantEntity: LanguageVariant? = null
    if (langSplitArray.size > 1) {
        langVariantEntity = ContentScraperUtil.insertOrUpdateLanguageVariant(db.languageVariantDao, langSplitArray[1], langEntity)
    }

    return ContentScraperUtil.createOrUpdateContentEntry(url, langName,
            url, ScraperConstants.KHAN, ContentEntry.LICENSE_TYPE_CC_BY_NC,
            langEntity.langUid, langVariantEntity?.langVariantUid ?: 0,
            ScraperConstants.EMPTY_STRING, false, ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
            ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
            0, db.contentEntryDao)

}

object KhanConstants {

    const val JSON_PATH = "/api/internal/static/content"

    const val regexUrlPrefix = "https://((.*).khanacademy.org|cdn.kastatic.org)/(.*)"
    const val KHAN_CSS = "<link rel='stylesheet' href='https://www.khanacademy.org/khanscraper.css' type='text/css'/>"
    const val KHAN_COOKIE = "<script> document.cookie = \"fkey=abcde;\" </script>"

    const val secondExerciseUrl = "/api/internal/user/exercises/"

    const val exerciseMidleUrl = "/items/"

    const val exercisePostUrl = "/assessment_item?lang="


    val khanLiteMap = mapOf(
            "ar" to KhanLang(getLangUrl("ar"), "العربية"),
            "fa-af" to KhanLang(getLangUrl("fa-af"), "داري"),
            "el" to KhanLang(getLangUrl("el"), "Ελληνικά"),
            "he" to KhanLang(getLangUrl("he"), "עברית"),
            "zlm" to KhanLang(getLangUrl("zlm"), "BahasaMalaysian"),
            "fa" to KhanLang(getLangUrl("fa"), "فارسی"),
            "sw" to KhanLang(getLangUrl("sw"), "Kiswahili"),
            "te" to KhanLang(getLangUrl("te"), "తెలుగు"),
            "th" to KhanLang(getLangUrl("th"), "ไทย"),
            "uk" to KhanLang(getLangUrl("uk"), "українська"),
            "ur" to KhanLang(getLangUrl("ur"), "اردو"),
            "xh" to KhanLang(getLangUrl("xh"), "isiXhosa"),
            "zu" to KhanLang(getLangUrl("zu"), "Zulu"))

    val khanFullMap = mapOf(
            "www" to KhanLang(getLangUrl("en"), "English"),
            "hy" to KhanLang(getLangUrl("hy"), "հայերեն"),
            "bn" to KhanLang(getLangUrl("zlm"), "বাংলা"),
            "bg" to KhanLang(getLangUrl("bg"), "български"),
            "cs" to KhanLang(getLangUrl("cs"), "čeština"),
            "fr" to KhanLang(getLangUrl("fr"), "Français"),
            "ka" to KhanLang(getLangUrl("ka"), "ქართული"),
            "de" to KhanLang(getLangUrl("de"), "Deutsch"),
            "ko" to KhanLang(getLangUrl("ko"), "한국어"),
            "nb" to KhanLang(getLangUrl("nb"), "norsk bokmål"),
            "pl" to KhanLang(getLangUrl("pl"), "Polski"),
            "pt" to KhanLang(getLangUrl("pt"), "Português"),
            "pt-pt" to KhanLang(getLangUrl("pt-pt"), "Português europeu"),
            "sr" to KhanLang(getLangUrl("sr"), "Српски"),
            "es" to KhanLang(getLangUrl("es"), "Español"),
            "tr" to KhanLang(getLangUrl("tr"), "Türkçe"),
            "zh-hans" to KhanLang(getLangUrl("zh-hans"), "简体中文"),
            "da" to KhanLang(getLangUrl("da"), "Dansk"),
            "nl" to KhanLang(getLangUrl("nl"), "Nederlands"),
            "gu" to KhanLang(getLangUrl("gu"), "ગુજરાતી"),
            "hi" to KhanLang(getLangUrl("hi"), "हिंदी"),
            "hu" to KhanLang(getLangUrl("hu"), "Magyar"),
            "it" to KhanLang(getLangUrl("it"), "Italiano"),
            "id" to KhanLang(getLangUrl("id"), "Bahasa Indonesian"),
            "ja" to KhanLang(getLangUrl("ja"), "日本語"),
            "kn" to KhanLang(getLangUrl("kn"), "ಕನ್ನಡ"),
            "mn" to KhanLang(getLangUrl("mn"), "монгол"),
            "ru" to KhanLang(getLangUrl("ru"), "русский"),
            "sv" to KhanLang(getLangUrl("sv"), "Svenska"),
            "ta" to KhanLang(getLangUrl("ta"), "தமிழ்"),
            "uz" to KhanLang(getLangUrl("uz"), "O'zbek"))

}