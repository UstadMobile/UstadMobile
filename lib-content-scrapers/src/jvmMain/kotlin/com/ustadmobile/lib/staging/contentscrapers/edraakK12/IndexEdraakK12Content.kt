package com.ustadmobile.lib.contentscrapers.edraakK12

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDaoCommon.STATUS_RUNNING
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil

import com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.ALL_RIGHTS_RESERVED
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import com.ustadmobile.lib.staging.contentscrapers.edraakK12.EdraakK12ContentScraper
import com.ustadmobile.core.util.LiveDataWorkQueue
import io.ktor.utils.io.charsets.Charset
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


/**
 * The Edraak Website uses json to generate their website to get all the courses and all the content within them.
 * https://programs.edraak.org/api/component/5a6087f46380a6049b33fc19/?states_program_id=41
 *
 *
 * Each section of the website is made out of categories and sections which follows the structure of the json
 *
 *
 * The main json has a component type named MainContentTrack
 * This has 6 children which are the main categories found in the website, they have a component type named Section
 *
 *
 * Each Section has list of Subsections or Course Content
 * SubSections are identified by the component type named SubSection
 * SubSections has list of Course Content
 * Course Content contains a Quiz(list of questions) or a Course that has video and list a questions.
 * Courses and Quizzes are both identified with the component type named ImportedComponent
 *
 *
 * The goal of the index class is to find all the importedComponent by going to the child of each component type
 * until the component type found is ImportedComponent. Once it is found, EdraakK12ContentScraper
 * will decide if its a quiz or course and scrap its content
 */

class IndexEdraakK12Content {

    private var url: URL? = null
    private var destinationDirectory: File? = null
    private var response: ContentResponse? = null
    private lateinit var contentEntryDao: ContentEntryDao
    private lateinit var contentParentChildJoinDao: ContentEntryParentChildJoinDao
    private lateinit var arabicLang: Language
    private lateinit var queueDao: ScrapeQueueItemDao
    private lateinit var scrapeWorkQueue: LiveDataWorkQueue<ScrapeQueueItem>

    private lateinit var containerDirectory: File


    @Throws(IOException::class)
    fun scrapeFromRoot(dest: File, containerDir: File, runId: Int) {
        startScrape(ROOT_URL, dest, containerDir, runId)
    }

    @Throws(IOException::class)
    fun startScrape(scrapeUrl: String, destinationDir: File, containerDir: File, runIdscrape: Int) {
        try {
            url = URL(scrapeUrl)
        } catch (e: MalformedURLException) {
            UMLogUtil.logError("url from main is Malformed = $scrapeUrl")
            throw IllegalArgumentException("Malformed url$scrapeUrl", e)
        }

        destinationDir.mkdirs()
        containerDir.mkdirs()
        containerDirectory = containerDir
        destinationDirectory = destinationDir
        runId = runIdscrape

        //TODO: This needs replaced with DI
        lateinit var db: UmAppDatabase
        //val db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
        val repository = db //db.getRepository("https://localhost", "");
        contentEntryDao = repository.contentEntryDao
        contentParentChildJoinDao = repository.contentEntryParentChildJoinDao
        val languageDao = repository.languageDao
        queueDao = db.scrapeQueueItemDao

        arabicLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "Arabic")
        var connection: HttpURLConnection? = null
        try {
            connection = url!!.openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01")
            response = GsonBuilder().disableHtmlEscaping().create().fromJson<ContentResponse>(IOUtils.toString(connection.inputStream, UTF_ENCODING), ContentResponse::class.java)
        } catch (e: IOException) {
            throw IllegalArgumentException("JSON INVALID", e.cause)
        } catch (e: JsonSyntaxException) {
            throw IllegalArgumentException("JSON INVALID", e.cause)
        } finally {
            connection?.disconnect()
        }

        val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, arabicLang.langUid, null,
                "", false, "", "",
                "", "", 0, contentEntryDao)

        var description = ("تعليم مجانيّ\n" +
                "إلكترونيّ باللغة العربيّة!" +
                "\n Free Online \n" +
                "Education, In Arabic!")

        description = String(description.toByteArray(), Charset.defaultCharset())

        val edraakParentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://www.edraak.org/k12/", "Edraak K12",
                "https://www.edraak.org/k12/", EDRAAK, ALL_RIGHTS_RESERVED, arabicLang.langUid, null,
                description, false, "", "https://www.edraak.org/static/images/logo-dark-ar.fa1399e8d134.png",
                "", "", 0, contentEntryDao)


        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, edraakParentEntry, 4)


        val scrapePrecessor = 1
        scrapeWorkQueue = LiveDataWorkQueue(queueDao.findNextQueueItems(ScrapeQueueItem.ITEM_TYPE_SCRAPE),
                { item1, item2 -> item1.sqiUid == item2.sqiUid }, scrapePrecessor) {


            queueDao.updateSetStatusById(it.sqiUid, STATUS_RUNNING, 0)
            val parent = contentEntryDao.findByUidAsync(it.sqiContentEntryParentUid)

            val scrapeContentUrl: URL
            try {
                scrapeContentUrl = URL(it.scrapeUrl!!)
                EdraakK12ContentScraper(scrapeContentUrl,
                        File(it.destDir!!),
                        containerDir,
                        parent!!, it.sqiUid).run()
            } catch (ignored: IOException) {
                throw RuntimeException(("SEVERE: invalid URL to scrape: should not be in queue:" + it.scrapeUrl!!))
            }


        }

        findImportedComponent(response!!, edraakParentEntry)

        GlobalScope.launch {
            scrapeWorkQueue.start()
        }


    }

    @Throws(MalformedURLException::class)
    private fun findImportedComponent(parentContent: ContentResponse, parentEntry: ContentEntry) {

        if (ContentScraperUtil.isImportedComponent(parentContent.component_type!!)) {

            // found the last child
            val scrapeUrl = EdraakK12ContentScraper.generateUrl(
                    (url!!.protocol + "://" + url!!.host + (if (url!!.port > 0)
                        (":" + url!!.port)
                    else
                        "") + "/api/"), parentContent.id!!,
                    if (parentContent.program == 0) response!!.program else parentContent.program)

            ContentScraperUtil.createQueueItem(queueDao, URL(scrapeUrl), parentEntry,
                    File(destinationDirectory, parentContent.id!!), "",
                    runId, ScrapeQueueItem.ITEM_TYPE_SCRAPE)

        } else {

            for (children in parentContent.children!!) {

                val sourceUrl = children.id
                val isLeaf = ContentScraperUtil.isImportedComponent(children.component_type!!)

                val childEntry = ContentScraperUtil.createOrUpdateContentEntry(children.id!!, children.title,
                        sourceUrl!!, EDRAAK, getLicenseType(children.license!!), arabicLang!!.langUid, null,
                        "", isLeaf, "", "",
                        "", "", 0, contentEntryDao!!)


                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentEntry, childEntry, children.child_index)

                findImportedComponent(children, childEntry)

            }

        }
    }

    private fun getLicenseType(license: String): Int {
        if (license.toLowerCase().contains("cc-by-nc-sa")) {
            return ContentEntry.LICENSE_TYPE_CC_BY_NC_SA
        } else if (license.toLowerCase().contains("all_rights_reserved")) {
            return ALL_RIGHTS_RESERVED
        } else {
            UMLogUtil.logError("License type not matched for license: $license")
            return ALL_RIGHTS_RESERVED
        }
    }

    companion object{

        var runId: Int = 0

        private const val ROOT_URL = "https://programs.edraak.org/api/component/5a6087f46380a6049b33fc19/?states_program_id=41"

        const val EDRAAK = "Edraak"

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage: <file destination><file container><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }
            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")
            UMLogUtil.logInfo(args[0])
            ContentScraperUtil.checkIfPathsToDriversExist()

            try {
                //This needs replaced with DI
                //lateinit var runDao: ScrapeRunDao
                //val runDao = UmAppDatabase.getInstance(Any(), replaceMeWithDi()).scrapeRunDao


                val index = IndexEdraakK12Content()

                index.scrapeFromRoot(File(args[0]), File(args[1]), runId)
            } catch (e: Exception) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Main method exception catch khan")
            }

        }

    }

}
