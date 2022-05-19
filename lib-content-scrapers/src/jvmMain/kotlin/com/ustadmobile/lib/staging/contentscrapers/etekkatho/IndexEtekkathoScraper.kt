package com.ustadmobile.lib.contentscrapers.etekkatho

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
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
import java.net.MalformedURLException
import java.net.URL
import java.util.*


/**
 * Etekkatho website can be scraped by accessing the page via Jsoup at http://www.etekkatho.org/subjects/
 * This page has all the list of subjects available in the website with their subheading and description
 *
 *
 * The content is placed in a table format and you get all the content via css selector: tr th[scope=row], tr td
 * The heading, subheading and description cant be identified by the html tags so its required to loop through all the elements
 * If the element has an attribute called scope then its the main heading and the next 2 elements are its subheading and the description
 * If the element has a class called span3 then its the subheading of the previous heading and the next element is its description
 * Since we create a content entry, save it into a hashMap of subheading title and content entry to be used later on
 *
 *
 * Once all content entry is made from the table. Loop through again with css selector th.span3 a
 * This will give the href link of the heading.
 * This page contains all the subheadings.
 * Use Css selector again to get the href link of all the subheadings
 * Each subheading has a list of subjects that contain title, desc, author, publisher and link
 * Loop through all the subjects dl.results-item to get the information and scrape the url
 * Need to go to the next page to get more content for the same subheading.
 * This can be found by taking href link of css selector li.next a
 */

class IndexEtekkathoScraper {
    private var url: URL? = null
    private var contentEntryDao: ContentEntryDao? = null
    private var contentParentChildJoinDao: ContentEntryParentChildJoinDao? = null
    private var headingHashMap: HashMap<String, ContentEntry>? = null
    private var englishLang: Language? = null
    private var subjectCount = 0
    private var containerDao: ContainerDao? = null
    private var db: UmAppDatabase? = null
    private var repository: UmAppDatabase? = null
    private var containerDirectory: File? = null

    @Throws(IOException::class)
    fun findContent(urlString: String, destinationDir: File, containerDir: File) {

        try {
            url = URL(urlString)
        } catch (e: MalformedURLException) {
            UMLogUtil.logError("Index Malformed url$urlString")
            throw IllegalArgumentException("Malformed url$urlString", e)
        }

        destinationDir.mkdirs()
        containerDir.mkdirs()

        containerDirectory = containerDir
        //replace this with di
        //db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
        repository = db //db!!.getRepository("https://localhost", "")
        contentEntryDao = repository!!.contentEntryDao
        contentParentChildJoinDao = repository!!.contentEntryParentChildJoinDao
        containerDao = repository!!.containerDao
        val languageDao = repository!!.languageDao
        headingHashMap = HashMap()

        englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English")

        val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null,
                "", false, "", "",
                "", "", 0, contentEntryDao!!)

        val parentEtek = ContentScraperUtil.createOrUpdateContentEntry("http://www.etekkatho.org/subjects/", "eTekkatho",
                "http://www.etekkatho.org/", ETEKKATHO, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null,
                "Educational resources for the Myanmar academic community", false, "",
                "http://www.etekkatho.org/img/logos/etekkatho-myanmar-lang.png",
                "", "", 0, contentEntryDao!!)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, masterRootParent, parentEtek, 6)

        val document = Jsoup.connect(urlString).get()

        val elements = document.select("tr th[scope=row], tr td")

        var subjectCount = 0
        var headingCount = 0
        var subjectEntry: ContentEntry? = null
        var i = 0
        while (i < elements.size) {

            val element = elements[i]

            if (!element.attr("scope").isEmpty()) {

                val headingUrl = URL(url, element.selectFirst("a")?.attr("href"))
                // found Main Content
                subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(element.text(),
                        element.text(), headingUrl.toString(),
                        ETEKKATHO, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null,
                        "", false, "", "",
                        "", "", 0, contentEntryDao!!)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentEtek, subjectEntry!!, subjectCount++)

                val subHeadingElement = elements[++i]
                val descriptionElement = elements[++i]

                var title = subHeadingElement.text()
                if (title.contains("*")) {
                    title = title.replace("*", "").trim { it <= ' ' }
                }

                val subHeadingEntry = ContentScraperUtil.createOrUpdateContentEntry(title,
                        title, element.text() + "/" + title, ETEKKATHO, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null,
                        descriptionElement.text(), false, "", "",
                        "", "", 0, contentEntryDao!!)

                headingHashMap!![title] = subHeadingEntry

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, subjectEntry, subHeadingEntry, headingCount++)


            } else if (element.hasClass("span3")) {

                val descriptionElement = elements[++i]
                var title = element.text()
                if (title.contains("*")) {
                    title = title.replace("*", "").trim { it <= ' ' }
                }

                val subHeadingEntry = ContentScraperUtil.createOrUpdateContentEntry(element.text(),
                        title, subjectEntry!!.title + "/" + title,
                        ETEKKATHO, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null,
                        descriptionElement.text(), false, "", "",
                        "", "", 0, contentEntryDao!!)

                headingHashMap!![title] = subHeadingEntry

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, subjectEntry, subHeadingEntry, headingCount++)

            } else if (element.hasClass("span6")) {

                UMLogUtil.logError("Should not come here" + element.text())

            }
            i++

        }

        val subjectList = document.select("th.span3 a")
        for (subject in subjectList) {

            val hrefLink = subject.attr("href")
            val folder = File(destinationDir, subject.text())
            folder.mkdirs()
            browseSubHeading(hrefLink, folder)


        }


    }

    @Throws(IOException::class)
    private fun browseSubHeading(hrefLink: String, folder: File) {

        val subHeadingUrl = URL(url, hrefLink)
        val document = Jsoup.connect(subHeadingUrl.toString()).get()

        val subHeadingList = document.select("div.row li a")
        for (subHeading in subHeadingList) {

            val subHrefLink = subHeading.attr("href")
            val title = subHeading.text()
            val subHeadingFolder = File(folder, title)
            subHeadingFolder.mkdirs()

            var subject: ContentEntry? = headingHashMap!![title]
            if (subject == null) {
                UMLogUtil.logError("Subheading title was not found $title")
                if (title == "Agriculture, aquaculture and the environment") {
                    subject = headingHashMap!!["Agriculture and the environment"]
                }
            }

            browseSubjects(subject, subHrefLink, subHeadingFolder)

        }


    }

    @Throws(IOException::class)
    private fun browseSubjects(contentEntry: ContentEntry?, subHrefLink: String, subHeadingFolder: File) {

        val subjectListUrl = URL(url, subHrefLink)
        val document = Jsoup.connect(subjectListUrl.toString()).get()

        val subjectList = document.select("dl.results-item")
        for (subject in subjectList) {

            val titleElement = subject.selectFirst("dd.title")
            val title = if (titleElement != null) titleElement.text() else ""

            val descriptionElement = subject.selectFirst("dd.description")
            val description = if (descriptionElement != null) descriptionElement.text() else ""

            val authorElement = subject.selectFirst("dd.author")
            val author = if (authorElement != null) authorElement.text() else ""

            val publisherElement = subject.selectFirst("dd.publisher")
            val publisher = if (publisherElement != null) publisherElement.text() else ETEKKATHO

            val hrefLink = subject.selectFirst("a")?.attr("href")

            val subjectUrl = URL(url, hrefLink)
            val subjectUrlString = subjectUrl.toString()

            val lessonEntry = ContentScraperUtil.createOrUpdateContentEntry(subjectUrl.query,
                    title, subjectUrlString, publisher, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null, description, true, author, "",
                    "", "", 0, contentEntryDao!!)

            ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao!!, contentEntry!!, lessonEntry, subjectCount++)

            val scraper = EtekkathoScraper(subjectUrlString, subHeadingFolder)
            try {
                scraper.scrapeContent()

                val fileName = subjectUrlString.substring(subjectUrlString.indexOf("=") + 1)
                val contentFolder = File(subHeadingFolder, fileName)
                val content = File(contentFolder, fileName)

                if (scraper.isUpdated) {
                    ContentScraperUtil.insertContainer(containerDao!!, lessonEntry, true,
                            scraper.mimeType!!, content.lastModified(), content, db!!, repository!!,
                            containerDirectory!!)
                    ContentScraperUtil.deleteFile(content)
                }

            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Unable to scrape content from $title at url $subjectUrlString")
            }


        }

        val nextLink = document.selectFirst("li.next a")
        if (nextLink != null) {
            browseSubjects(contentEntry, nextLink.attr("href"), subHeadingFolder)
        }

    }

    companion object {

        private val ETEKKATHO = "Etekkatho"

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 3) {
                System.err.println("Usage: <etekkatho html url> <file destination><file container><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }

            UMLogUtil.setLevel(if (args.size == 4) args[3] else "")
            UMLogUtil.logInfo(args[0])
            UMLogUtil.logInfo(args[1])
            try {
                IndexEtekkathoScraper().findContent(args[0], File(args[1]), File(args[2]))
            } catch (e: IOException) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logFatal("Exception running findContent Etek")
            }

        }
    }


}
