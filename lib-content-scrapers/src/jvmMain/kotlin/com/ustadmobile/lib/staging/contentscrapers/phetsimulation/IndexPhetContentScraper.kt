package com.ustadmobile.lib.contentscrapers.phetsimulation

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants

import com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.staging.contentscrapers.replaceMeWithDi
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

/**
 * The Phet Simulation Website provides a list of all the available Html5 Content in one of their categories found at
 * https://phet.colorado.edu/en/simulations/category/html
 *
 *
 * By using a css selector: td.simulation-list-item span.sim-badge-html
 * We can get the url to each simulation in that category to give to PhetContentScraper to scrap its content
 */

class IndexPhetContentScraper {
    private lateinit var destinationDirectory: File
    private lateinit var url: URL
    private lateinit var contentEntryDao: ContentEntryDao
    private lateinit var contentParentChildJoinDao: ContentEntryParentChildJoinDao
    private lateinit var contentEntryCategoryJoinDao: ContentEntryContentCategoryJoinDao
    private lateinit var contentEntryRelatedJoinDao: ContentEntryRelatedEntryJoinDao
    private lateinit var languageDao: LanguageDao
    private lateinit var englishLang: Language
    private lateinit var languageVariantDao: LanguageVariantDao
    private lateinit var containerDao: ContainerDao
    private lateinit var db: UmAppDatabase
    private lateinit var repository: UmAppDatabase
    private lateinit var containerDir: File

    /**
     * Given a phet url, find the content and download
     *
     * @param urlString      url link to phet category
     * @param destinationDir destination folder for phet content
     * @throws IOException
     */
    @Throws(IOException::class)
    fun findContent(urlString: String, destinationDir: File, containerDir: File) {

        try {
            url = URL(urlString)
        } catch (e: MalformedURLException) {
            UMLogUtil.logError("Index Malformed url$urlString")
            throw IllegalArgumentException("Malformed url$urlString", e)
        }

        destinationDir.mkdirs()
        destinationDirectory = destinationDir
        containerDir.mkdirs()
        this.containerDir = destinationDir

        //replace with DI
        //db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
        repository = db //db!!.getRepository("https://localhost", "")
        contentEntryDao = repository!!.contentEntryDao
        contentParentChildJoinDao = repository!!.contentEntryParentChildJoinDao
        contentEntryCategoryJoinDao = repository!!.contentEntryContentCategoryJoinDao
        contentEntryRelatedJoinDao = repository!!.contentEntryRelatedEntryJoinDao
        containerDao = repository!!.containerDao
        languageDao = repository!!.languageDao
        languageVariantDao = repository!!.languageVariantDao


        val document = Jsoup.connect(urlString).get()

        browseCategory(document)

    }


    @Throws(IOException::class)
    private fun browseCategory(document: Document) {

        val simulationList = document.select("td.simulation-list-item span.sim-badge-html")

        englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English")

        val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null,
                "", false, "", "",
                "", "", 0, contentEntryDao!!)


        val phetParentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://phet.colorado.edu/", "Phet Interactive Simulations",
                "https://phet.colorado.edu/", PHET, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null,
                "INTERACTIVE SIMULATIONS\nFOR SCIENCE AND MATH", false, "",
                "https://phet.colorado.edu/images/phet-social-media-logo.png", "", "", 0, contentEntryDao!!)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, masterRootParent, phetParentEntry, 14)

        for (simulation in simulationList) {

            val path = simulation.parent()?.attr("href")
            val simulationUrl = URL(url, path).toString()
            val title = simulationUrl.substring(simulationUrl.lastIndexOf("/") + 1)
            val thumbnail = simulation.parent()?.selectFirst("img")?.attr("src")

            val scraper = PhetContentScraper(simulationUrl,
                    destinationDirectory, containerDir)
            try {
                scraper.scrapeContent()
                val englishSimContentEntry = ContentScraperUtil.createOrUpdateContentEntry(path, title,
                        simulationUrl, PHET, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null,
                        scraper.aboutDescription, true, "",
                        thumbnail, "", "", 0, contentEntryDao!!)

                val isEnglishUpdated = scraper.languageUpdatedMap["en"]
                val enLangLocation = File(destinationDirectory, "en")
                val englishContentFile = File(enLangLocation, title)
                if (isEnglishUpdated!!) {
                    ContentScraperUtil.insertContainer(containerDao!!, englishSimContentEntry,
                            true, ScraperConstants.MIMETYPE_TINCAN,
                            englishContentFile.lastModified(),
                            englishContentFile, db, repository, containerDir)
                    FileUtils.deleteDirectory(englishContentFile)

                }

                val categoryList = scraper.getCategoryRelations(contentEntryDao, englishLang)
                val translationList = scraper.getTranslations(destinationDirectory!!, contentEntryDao, thumbnail ?: "", languageDao, languageVariantDao)

                // TODO remove all categories that no longer exist
                // TODO remove all categories that dont belong in a phet simulation anymore

                var categoryCount = 0
                for (category in categoryList) {

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, phetParentEntry, category, categoryCount++)
                    ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao!!, category, englishSimContentEntry, 0)

                    var translationsCount = 1
                    for (translation in translationList) {

                        ContentScraperUtil.insertOrUpdateRelatedContentJoin(contentEntryRelatedJoinDao!!, translation, englishSimContentEntry, ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION)

                        val langCode = scraper.contentEntryLangMap!![translation.contentEntryUid]

                        val langLocation = File(destinationDirectory, langCode)
                        val content = File(langLocation, title + ScraperConstants.ZIP_EXT)
                        if (scraper.languageUpdatedMap[langCode]!!) {

                            ContentScraperUtil.insertContainer(containerDao, translation,
                                    true, ScraperConstants.MIMETYPE_TINCAN,
                                    content.lastModified(),
                                    content, db, repository, containerDir)

                        }

                        ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao!!, category, translation, translationsCount++)
                    }
                }

            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Failed to scrape Phet Content for url$simulationUrl")
            }

        }
    }

    companion object {

        val PHET = "Phet"


        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 3) {
                System.err.println("Usage: <phet html url> <file destination><file container><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }
            UMLogUtil.setLevel(if (args.size == 4) args[3] else "")
            UMLogUtil.logInfo(args[0])
            UMLogUtil.logInfo(args[1])
            try {
                IndexPhetContentScraper().findContent(args[0], File(args[1]), File(args[2]))
            } catch (e: IOException) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logFatal("Exception running findContent phet")
            }

        }
    }

}
