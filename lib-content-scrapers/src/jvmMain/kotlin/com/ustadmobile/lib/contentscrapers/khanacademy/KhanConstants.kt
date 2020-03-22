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
import java.net.HttpURLConnection
import java.net.URL


data class KhanLang(val url: String, val title: String)

data class KhanFile(val url: String, val fileLocation: String, val mimeType: String)

private fun getLangUrl(langCode: String): String {
    return "https://$langCode.khanacademy.org/"
}

private fun getJsonURL(langCode: String, sourceUrl: URL): URL {
    return URL(sourceUrl, "$JSON_PATH${sourceUrl.path}?lang=$langCode")
}

fun getLangCodeFromURL(sourceUrl: URL): String {
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

fun isUrlValid(url: URL): Boolean {
    val huc: HttpURLConnection = url.openConnection() as HttpURLConnection
    huc.requestMethod = "HEAD"

    val responseCode: Int = huc.responseCode

    return responseCode == 200
}

fun getYoutubeUrl(videoId: String): String {
    return "https://www.youtube.com/watch?v=$videoId"
}

object KhanConstants {

    const val JSON_PATH = "/api/internal/static/content"

    const val regexUrlPrefix = "https://((.*).khanacademy.org|cdn.kastatic.org)/(.*)"
    const val KHAN_CSS = "<link rel='stylesheet' href='https://www.khanacademy.org/khanscraper.css' type='text/css'/>"
    const val KHAN_COOKIE = "<script> document.cookie = \"fkey=abcde;\" </script>"

    const val subTitleUrl = "http://www.khanacademy.org/api/internal/videos/"

    const val subTitlePostUrl = "/transcript?lang="

    const val secondExerciseUrl = "/api/internal/user/exercises/"

    const val exerciseMidleUrl = "/items/"

    const val exercisePostUrl = "/assessment_item?lang="

    val loginLangMap = mapOf(
            "en" to "Log in",
            "pl" to "Zaloguj się",
            "hy" to "Մուտք գործել",
            "bn" to "প্রবেশ",
            "bg" to "Влизане",
            "cs" to "Přihlášení",
            "fr" to "Connexion",
            "ka" to "შესვლა",
            "de" to "Anmelden",
            "ko" to "로그인",
            "nb" to "Logg inn",
            "pt" to "Entrar",
            "pt-pt" to "Iniciar sessão",
            "sr" to "Пријава",
            "es" to "Iniciar sesión",
            "tr" to "Giriş Yap",
            "zh" to "登录",
            "da" to "Login",
            "nl" to "Inloggen",
            "gu" to "લોગ ઇન",
            "hi" to "लॉग इन",
            "hu" to "Jelentkezz be",
            "it" to "Accedi",
            "id" to "Masuk",
            "ja" to "ログイン",
            "kn" to "ಲಾಗಿನ್",
            "mn" to "Нэвтэрье",
            "ru" to "Войти",
            "sv" to "Logga in",
            "ta" to "உள்நுழைக",
            "uz" to "Kirish"
    )

    val fileMap = mapOf(
            "en" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/en/c55338d5bef2f8bf5dcdbf515448fef8.80da5ef39e9989febc9e.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "pl" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/pl/c55338d5bef2f8bf5dcdbf515448fef8.76a6cc3bf717e4197184.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "hy" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/hy/c55338d5bef2f8bf5dcdbf515448fef8.b1d0bffcd86e5b833150.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "bn" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/bn/c55338d5bef2f8bf5dcdbf515448fef8.0181ce417a9a73a8796c.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "bg" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/bg/c55338d5bef2f8bf5dcdbf515448fef8.ce6493abacca45ddf4b1.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "cs" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/cs/c55338d5bef2f8bf5dcdbf515448fef8.e98647d01ed5090a266e.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "fr" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/fr/c55338d5bef2f8bf5dcdbf515448fef8.2bd42fc21391f2969bcc.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "ka" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/ka/c55338d5bef2f8bf5dcdbf515448fef8.57d4b81ec952dd091c6f.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "de" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/de/c55338d5bef2f8bf5dcdbf515448fef8.c578d9928b38c862aecf.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "ko" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/ko/c55338d5bef2f8bf5dcdbf515448fef8.3c1e60c1a68a2c771b59.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "nb" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/nb/c55338d5bef2f8bf5dcdbf515448fef8.275e5a7773c2d39c2b07.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "pt" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/pt/c55338d5bef2f8bf5dcdbf515448fef8.434eaabae48587a5b59e.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "pt-pt" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/pt-pt/c55338d5bef2f8bf5dcdbf515448fef8.668af444372c5fed0303.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "sr" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/sr/c55338d5bef2f8bf5dcdbf515448fef8.ff131cc07e2f93f15dfc.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "es" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/es/c55338d5bef2f8bf5dcdbf515448fef8.47587e69fdd28883bebc.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "tr" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/tr/c55338d5bef2f8bf5dcdbf515448fef8.cb29f21b98c16e503002.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "zh" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/zh-hans/c55338d5bef2f8bf5dcdbf515448fef8.b91d7e254b73f1726820.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "da" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/da/c55338d5bef2f8bf5dcdbf515448fef8.f27f1e7ca590c603d9e3.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "nl" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/nl/c55338d5bef2f8bf5dcdbf515448fef8.ece7a0b24238c9e71de3.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "gu" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/gu/c55338d5bef2f8bf5dcdbf515448fef8.5b4aa592d45ee489e8c1.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "hi" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/hi/c55338d5bef2f8bf5dcdbf515448fef8.885e2da7f874e0e28c5e.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "hu" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/hu/c55338d5bef2f8bf5dcdbf515448fef8.0384112c31c94c0b6f6f.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "it" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/it/c55338d5bef2f8bf5dcdbf515448fef8.47447aa94ade4dc46afd.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "id" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/id/c55338d5bef2f8bf5dcdbf515448fef8.782a0fba76347d4ca034.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "ja" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/ja/c55338d5bef2f8bf5dcdbf515448fef8.31be371c7d9b6d472781.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "kn" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/kn/c55338d5bef2f8bf5dcdbf515448fef8.2997cbf9c554d518d1ea.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "mn" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/mn/c55338d5bef2f8bf5dcdbf515448fef8.1451fb767806bd967755.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "ru" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/ru/c55338d5bef2f8bf5dcdbf515448fef8.d6b21219b68166025e4b.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "sv" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/sv/c55338d5bef2f8bf5dcdbf515448fef8.6fd856f39e747cb3804d.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "ta" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/ta/c55338d5bef2f8bf5dcdbf515448fef8.58a35ab8301166d7449e.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS)),
            "uz" to listOf(
                    KhanFile("https://cdn.kastatic.org/genwebpack/prod/uz/c55338d5bef2f8bf5dcdbf515448fef8.606d6821daf6350da454.js",
                            ScraperConstants.GENWEB_ANSWER_LINK, ScraperConstants.MIMETYPE_JS))
    )

    val khanLiteMap = mapOf(
            "ar" to KhanLang(getLangUrl("ar"), "العربية"),
            "fa-af" to KhanLang(getLangUrl("fa-af"), "داري"),
            "el" to KhanLang(getLangUrl("el"), "Ελληνικά"),
            "he" to KhanLang(getLangUrl("he"), "עברית"),
            "zlm" to KhanLang(getLangUrl("zlm"), "Bahasa Malaysian"),
            "fa" to KhanLang(getLangUrl("fa"), "فارسی"),
            "sw" to KhanLang(getLangUrl("sw"), "Kiswahili"),
            "te" to KhanLang(getLangUrl("te"), "తెలుగు"),
            "th" to KhanLang(getLangUrl("th"), "ไทย"),
            "uk" to KhanLang(getLangUrl("uk"), "українська"),
            "ur" to KhanLang(getLangUrl("ur"), "اردو"),
            "xh" to KhanLang(getLangUrl("xh"), "isiXhosa"),
            "zu" to KhanLang(getLangUrl("zu"), "Zulu"))

    val khanFullMap = mapOf(
            "en" to KhanLang(getLangUrl("en"), "English"),
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
            "pt-pt" to KhanLang(getLangUrl("pt-pt"), "Português Europeu"),
            "sr" to KhanLang(getLangUrl("sr"), "Српски"),
            "es" to KhanLang(getLangUrl("es"), "Español"),
            "tr" to KhanLang(getLangUrl("tr"), "Türkçe"),
            "zh" to KhanLang(getLangUrl("zh"), "简体中文"),
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
            "uz" to KhanLang(getLangUrl("uz"), "Ozbek"))

}