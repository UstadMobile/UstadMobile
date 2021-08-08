package com.ustadmobile.lib.contentscrapers.ddl

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.db.dao.LanguageDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil

import com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.staging.contentscrapers.replaceMeWithDi
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException


/**
 * The DDL Website comes in 3 languages - English, Farsi and Pashto
 * To scrape all content, we would need to go to each page and traverse the list
 * First we find our the max number of pages for each language by using the css selector on a.page-link
 * Once we found the max number, open each page on ddl website with the parameters /resources/list?page= and the page number until you hit the max
 *
 *
 * Every resource is found by searching the html with a[href] and checking if href url contains "resource/"
 * Traverse all the pages until you hit Max number and then move to next language
 */

class IndexDdlContent {
    private lateinit var db: UmAppDatabase
    private var destinationDirectory: File? = null

    private var maxNumber: Int = 0
    private var parentDdl: ContentEntry? = null
    private var langEntry: ContentEntry? = null
    private var langCount = 0
    private var contentEntryDao: ContentEntryDao? = null
    private var contentParentChildJoinDao: ContentEntryParentChildJoinDao? = null
    private var contentCategoryChildJoinDao: ContentEntryContentCategoryJoinDao? = null
    private lateinit var languageDao: LanguageDao
    private lateinit var containerDir: File


    @Throws(IOException::class)
    fun findContent(destinationDir: File, containerDir: File) {

        destinationDir.mkdirs()
        destinationDirectory = destinationDir
        containerDir.mkdirs()
        this.containerDir = containerDir

        //THIS NEEDS REPLACED WITH DI
        //db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
        val repository = db
        contentEntryDao = repository.contentEntryDao
        contentParentChildJoinDao = repository.contentEntryParentChildJoinDao
        contentCategoryChildJoinDao = repository.contentEntryContentCategoryJoinDao
        languageDao = repository.languageDao

        val englishLang = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, "en")
        val farsiLang = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, "fa")
        val pashtoLang = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, "ps")


        val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.langUid, null,
                "", false, "", "",
                "", "", 0, contentEntryDao!!)


        parentDdl = ContentScraperUtil.createOrUpdateContentEntry("https://www.ddl.af/", "Darakht-e Danesh",
                "https://www.ddl.af/", DDL, LICENSE_TYPE_CC_BY, englishLang.langUid, null,
                "Free and open educational resources for Afghanistan", false, "",
                "https://ddl.af/storage/files/logo-dd.png", "", "", 0, contentEntryDao!!)


        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, masterRootParent, parentDdl!!, 5)

        UMLogUtil.logTrace("browse English")
        browseLanguages("en", englishLang)
        UMLogUtil.logTrace("browse Farsi")
        browseLanguages("fa", farsiLang)
        UMLogUtil.logTrace("browse Pashto")
        browseLanguages("ps", pashtoLang)

    }

    @Throws(IOException::class)
    private fun browseLanguages(lang: String, langEntity: Language) {

        val document = Jsoup.connect("https://www.ddl.af/$lang/resources/list")
                .header("X-Requested-With", "XMLHttpRequest").get()

        val pageList = document.select("a.page-link")

        langEntry = ContentScraperUtil.createOrUpdateContentEntry("$lang/resources/list", langEntity.name,
                "https://www.ddl.af/$lang/resources/list", DDL, LICENSE_TYPE_CC_BY, langEntity.langUid, null,
                "", false, "", "",
                "", "", 0, contentEntryDao!!)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentDdl!!, langEntry!!, langCount)

        maxNumber = 0
        for (page in pageList) {

            val num = page.text()
            try {
                val number = Integer.parseInt(num)
                if (number > maxNumber) {
                    maxNumber = number
                }
            } catch (ignored: NumberFormatException) {
            }

        }
        UMLogUtil.logTrace("$DDL max number of pages: $maxNumber")

        browseList(lang, 1)
        langCount++
    }

    @Throws(IOException::class)
    private fun browseList(lang: String, count: Int) {
        var counter = count

        if (counter > maxNumber) {
            return
        }
        UMLogUtil.logTrace("$DDL starting page: $counter")
        val document = Jsoup.connect("https://www.ddl.af/$lang/resources/list?page=$counter")
                .header("X-Requested-With", "XMLHttpRequest").get()

        val resourceList = document.select("article a[href]")
        UMLogUtil.logTrace("$DDL found " + resourceList.size + " articles to download")
        for (resource in resourceList) {

            val url = resource.attr("href")
            if (url.contains("resource/")) {


                var contentEntry = db.contentEntryDao.findBySourceUrl(url)
                if(contentEntry == null){
                    contentEntry = ContentEntry()
                    contentEntry.sourceUrl = url
                    contentEntry.contentEntryUid = db.contentEntryDao.insert(contentEntry)
                }

              //  val scraper = DdlContentScraper(containerDir, db, contentEntry.contentEntryUid, 0, 0)
                try {
                   // scraper.scrapeUrl(url)
                    UMLogUtil.logTrace("$DDL scraped url: $url")


                } catch (e: Exception) {
                    UMLogUtil.logError("$DDL Exception - Something went wrong here $url")
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                }

            }


        }

        browseList(lang, ++counter)

    }

    companion object {


        internal val DDL = "DDL"


        @JvmStatic
        fun main(args: Array<String>) {

            if (args.size < 3) {
                System.err.println("Usage:<file destination><container destination><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }

            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")

            UMLogUtil.logTrace(args[0])
            UMLogUtil.logTrace(args[1])
            try {
                IndexDdlContent().findContent(File(args[0]), File(args[1]))
            } catch (e: Exception) {
                UMLogUtil.logFatal("$DDL Exception running findContent DDL Scraper")
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))

            }

        }
    }

}
