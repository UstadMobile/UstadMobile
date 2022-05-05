package com.ustadmobile.lib.contentscrapers.phetsimulation

import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.LanguageDao
import com.ustadmobile.core.db.dao.LanguageVariantDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants

import com.ustadmobile.lib.contentscrapers.ScraperConstants.REQUEST_HEAD
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.phetsimulation.IndexPhetContentScraper.Companion.PHET
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.Language
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException


/**
 * The page for each simulation on the website follows the same format
 *
 *
 * The english simulation can be found by using the css selector
 * div.simulation-main-image-panel a.phet-button[href]
 * which provides the button with href link to download the html file.
 *
 *
 * The description of the simulation can be found at the div tag with id "about" to get its content.
 * Within the about html, there is a description used for purpose of tincan which can be found by the selector - p.simulation-panel-indent
 *
 *
 * The translations for the simulation can be found in the table table.phet-table tr
 * In the css selector for that tr row
 * you will get the language in the column with selector - "td.list-highlight-background a[href]"
 * you will get the download link column with selector - "td.img-container a[href]"
 *
 *
 * The download links url have eTag and last modified in the headers to identify new content
 */

class PhetContentScraper(private val url: String, private val destinationDirectory: File, private val containerDir: File) {
    /**
     * @return the title of the simulation in english
     */
    val title: String
    private lateinit var simulationDoc: Document
    private var aboutText: String? = null
    private val langugageList: ArrayList<String> = ArrayList()
    private val languageMapUpdate: MutableMap<String, Boolean>
    private val languageUrlMap: MutableMap<String, String>
    private var langIdMap: MutableMap<Long, String>? = null

    var aboutDescription: String? = null
        private set
    private lateinit var simulationUrl: URL

    val languageUpdatedMap: Map<String, Boolean>
        get() = languageMapUpdate

    val contentEntryLangMap: Map<Long, String>?
        get() = langIdMap

    init {
        languageMapUpdate = HashMap()
        languageUrlMap = HashMap()
        this.title = url.substring(url.lastIndexOf("/") + 1)
    }


    @Throws(IOException::class)
    fun scrapeContent() {

        simulationUrl = URL(url)
        destinationDirectory.mkdirs()

        simulationDoc = Jsoup.connect(url).get()

        if (!simulationDoc!!.select("div.simulation-main-image-panel a span").hasClass("html-badge")) {
            throw IllegalArgumentException("File Type not supported for url " + simulationUrl!!.toString())
        }

        aboutText = simulationDoc!!.getElementById("about")?.html()
        aboutDescription = Jsoup.parse(aboutText!!).select("p.simulation-panel-indent").text()

        val contentUpdated: Boolean
        for (englishLink in simulationDoc!!.select("div.simulation-main-image-panel a.phet-button[href]")) {

            val hrefLink = englishLink.attr("href")

            val englishLocation = File(destinationDirectory, "en")
            englishLocation.mkdirs()

            if (hrefLink.contains("download")) {
                contentUpdated = downloadContent(simulationUrl, hrefLink, englishLocation)
                languageMapUpdate[englishLocation.name] = contentUpdated
                languageUrlMap[englishLocation.name] = hrefLink
                break
            }
        }

        var languageLocation: File? = null
        for (translations in simulationDoc!!.select("table.phet-table tr")) {

            for (langs in translations.select("td.list-highlight-background a[href]")) {

                val hrefLink = langs.attr("href")

                if (hrefLink.contains("translated")) {

                    val langCode = hrefLink.substring(hrefLink.lastIndexOf("/") + 1)
                    langugageList.add(langCode)
                    languageLocation = File(destinationDirectory, langCode)
                    languageLocation.mkdirs()
                    break
                }
            }

            for (links in translations.select("td.img-container a[href]")) {

                val hrefLink = links.attr("href")

                if (hrefLink.contains("download")) {
                    val isLanguageUpdated = downloadContent(simulationUrl, hrefLink, languageLocation)
                    languageMapUpdate[languageLocation!!.name] = isLanguageUpdated
                    languageUrlMap[languageLocation.name] = hrefLink
                    break
                }

            }

        }
    }

    fun getLanguageUrlMap(): Map<String, String> {
        return languageUrlMap
    }

    /**
     * Find the category for the phet simulation
     *
     * @param contentEntryDao
     * @return a list of categories a single phet simulation could be in
     */
    fun getCategoryRelations(contentEntryDao: ContentEntryDao, language: Language): ArrayList<ContentEntry> {

        val selected = simulationDoc!!.select("ul.nav-ul div.link-holder span.selected")

        val categoryRelations = ArrayList<ContentEntry>()
        for (category in selected) {

            if (Arrays.stream(CATEGORY).parallel().noneMatch { category.text().contains(it) }) {

                try {
                    val categoryName = category.text() // category name
                    val path = category.parent()?.attr("href") // url path to category

                    val categoryContentEntry = ContentScraperUtil.createOrUpdateContentEntry(path, categoryName,
                            URL(simulationUrl, path).toString(), PHET, LICENSE_TYPE_CC_BY, language.langUid, null,
                            "", false, "", "",
                            "", "", 0, contentEntryDao)

                    categoryRelations.add(categoryContentEntry)
                } catch (ie: IOException) {
                    UMLogUtil.logError("Error creating category entry" + category.text() + " for url" + simulationUrl!!.toString())
                }

            }
        }

        return categoryRelations

    }

    private fun downloadContent(simulationUrl: URL, hrefLink: String, languageLocation: File?): Boolean {
        var conn: HttpURLConnection? = null
        var fileName: String? = null
        try {
            val link = URL(simulationUrl, hrefLink)

            val simulationLocation = File(languageLocation, title)
            simulationLocation.mkdirs()

            conn = link.openConnection() as HttpURLConnection
            conn.requestMethod = REQUEST_HEAD

            fileName = hrefLink.substring(hrefLink.lastIndexOf("/") + 1, hrefLink.lastIndexOf("?"))
            val simulationFile = File(simulationLocation, fileName)

            var isUpdated = ContentScraperUtil.isFileModified(conn, languageLocation!!, fileName)

            if (!isUpdated) {
                return false
            }

            if (ContentScraperUtil.fileHasContent(simulationLocation)) {
                FileUtils.deleteDirectory(simulationLocation)
                simulationLocation.mkdirs()
            }

            FileUtils.writeStringToFile(File(simulationLocation, ScraperConstants.ABOUT_HTML), aboutText, ScraperConstants.UTF_ENCODING)

            FileUtils.copyURLToFile(link, simulationFile)

            val simulationTitle = Jsoup.parse(simulationFile, ScraperConstants.UTF_ENCODING).title()
            try {
                ContentScraperUtil.generateTinCanXMLFile(simulationLocation, simulationTitle,
                        languageLocation!!.name, fileName, ScraperConstants.SIMULATION_TIN_CAN_FILE,
                        languageLocation.name + "\\" + this.title,
                        aboutDescription!!, "en")
            } catch (e: ParserConfigurationException) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Tin can file not created for $link")
            } catch (e: TransformerException) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Tin can file not created for $link")
            }

        } catch (e: Exception) {
            UMLogUtil.logError("Error download content for url $simulationUrl with href $hrefLink")
            if (fileName != null) {
                ContentScraperUtil.deleteETagOrModified(languageLocation!!, fileName)
            }
        } finally {
            conn?.disconnect()
        }

        return true
    }


    /**
     * Given a directory of phet simulation content, find the languages it was translated to
     *
     * @param destinationDirectory directory of the all phet simulations
     * @param thumbnailUrl
     * @return a list of languages the phet simulation was translated to
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getTranslations(destinationDirectory: File, contentEntryDao: ContentEntryDao, thumbnailUrl: String, languageDao: LanguageDao, languageVariantDao: LanguageVariantDao): ArrayList<ContentEntry> {

        val translationsEntry = ArrayList<ContentEntry>()
        langIdMap = HashMap()

        for (translationDir in destinationDirectory.listFiles()!!) {

            if (translationDir.isDirectory) {
                val langCode = translationDir.name
                if (!langugageList.contains(langCode)) {
                    continue
                }
                for (contentDirectory in translationDir.listFiles()!!) {

                    if (title.equals(contentDirectory.name, ignoreCase = true)) {

                        for (file in contentDirectory.listFiles()!!) {

                            if (file.name.endsWith(".html")) {

                                try {
                                    val langTitle = simulationDoc!!.selectFirst("td a[href*=_$langCode] span")?.text()

                                    val path = simulationUrl!!.toString().replace("/en/", "/$langCode/")
                                    val translationUrl = URL(path)
                                    val country = langCode.replace("_", "-").split("-")

                                    val lang = country[0]
                                    val variant = if (country.size > 1) country[1] else ""

                                    val language = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, lang)
                                    val languageVariant = ContentScraperUtil.insertOrUpdateLanguageVariant(languageVariantDao, variant, language)

                                    val languageContentEntry = ContentScraperUtil.createOrUpdateContentEntry(translationUrl.path, langTitle,
                                            translationUrl.toString(), PHET, LICENSE_TYPE_CC_BY, language.langUid, languageVariant?.langVariantUid,
                                            aboutDescription, true, "", thumbnailUrl,
                                            "", "", 0, contentEntryDao)

                                    langIdMap!![languageContentEntry.contentEntryUid] = langCode

                                    translationsEntry.add(languageContentEntry)
                                    break
                                } catch (e: Exception) {
                                    UMLogUtil.logError("Error while creating a entry for translated " +
                                            "content lang code " + langCode + " in phet url " + url)
                                }

                            }
                        }
                    }
                }
            }
        }

        return translationsEntry
    }

    companion object {

        val CATEGORY = arrayOf("iPad/Tablet", "New Sims", "Simulations", "HTML5")

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
                PhetContentScraper(args[0], File(args[1]), File(args[2])).scrapeContent()
            } catch (e: IOException) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Exception running scrapeContent phet")
            }

        }
    }
}
