package com.ustadmobile.lib.contentscrapers.abztract

import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.core.db.dao.ScrapeRunDao
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Scraper.Companion.ERROR_TYPE_TIMEOUT
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import com.ustadmobile.lib.db.entities.ScrapeRun
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.SCRAPER_TAG
import com.ustadmobile.lib.contentscrapers.googledrive.GoogleFile
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.sharedse.contentformats.extractContentEntryMetadataFromFile
import com.ustadmobile.port.sharedse.contentformats.mimeTypeSupported
import com.ustadmobile.sharedse.util.LiveDataWorkQueue
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpStatement
import kotlinx.coroutines.*
import org.apache.commons.cli.*
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import kotlin.system.exitProcess

@ExperimentalStdlibApi
class ScraperManager(indexTotal: Int = 4, scraperTotal: Int = 1, endpoint: Endpoint, override val di: DI) : DIAware {

    private var runDao: ScrapeRunDao
    private var contentEntryDao: ContentEntryDao
    private var queueDao: ScrapeQueueItemDao

    private val db: UmAppDatabase by on(endpoint).instance(tag = UmAppDatabase.TAG_DB)

    private val apiKey: String by di.instance(tag = DiTag.TAG_GOOGLE_API)

    private val logPrefix = "[ScraperManager endpoint url: ${endpoint.url}] "

    init {

        runDao = db.scrapeRunDao
        queueDao = db.scrapeQueueItemDao
        contentEntryDao = db.contentEntryDao

        Napier.base(DebugAntilog())

        LiveDataWorkQueue(queueDao.findNextQueueItems(ScrapeQueueItem.ITEM_TYPE_INDEX),
                { item1, item2 -> item1.sqiUid == item2.sqiUid },
                indexTotal) {

            queueDao.updateSetStatusById(it.sqiUid, ScrapeQueueItemDao.STATUS_RUNNING, 0)

            val startTime = System.currentTimeMillis()
            Napier.i("$logPrefix Started indexer url ${it.scrapeUrl} at start time: $startTime", tag = SCRAPER_TAG)

            queueDao.setTimeStarted(it.sqiUid, startTime)
            try {

                val indexerClazz = ScraperTypes.indexerTypeMap[it.contentType]
                val cons = indexerClazz?.clazz?.getConstructor(Long::class.java, Int::class.java, Int::class.java, Long::class.java, Endpoint::class.java, DI::class.java)
                val obj = cons?.newInstance(it.sqiContentEntryParentUid, it.runId, it.sqiUid, it.sqiContentEntryUid, endpoint, di) as Indexer?
                obj?.indexUrl(it.scrapeUrl!!)
            } catch (e: Exception) {
                Napier.e("$logPrefix Exception running indexer ${it.scrapeUrl}", tag = SCRAPER_TAG)
                Napier.e("$logPrefix ${ExceptionUtils.getStackTrace(e)}", tag = SCRAPER_TAG)
            }

            queueDao.setTimeFinished(it.sqiUid, System.currentTimeMillis())
            val duration = System.currentTimeMillis() - startTime
            Napier.e("$logPrefix Ended indexer for url ${it.scrapeUrl} in duration: $duration", tag = SCRAPER_TAG)
        }.also { indexQueue ->
            GlobalScope.launch {
                indexQueue.start()
            }
        }


        LiveDataWorkQueue(queueDao.findNextQueueItems(ScrapeQueueItem.ITEM_TYPE_SCRAPE),
                { item1, item2 -> item1.sqiUid == item2.sqiUid }, scraperTotal) {

            queueDao.updateSetStatusById(it.sqiUid, ScrapeQueueItemDao.STATUS_RUNNING, 0)

            val startTime = System.currentTimeMillis()
            Napier.i("$logPrefix Started scraper url ${it.scrapeUrl} at start time: $startTime", tag = SCRAPER_TAG)

            queueDao.setTimeStarted(it.sqiUid, startTime)
            var obj: Scraper? = null
            try {
                withTimeout(900000) {

                    val scraperClazz = ScraperTypes.scraperTypeMap[it.contentType]
                    val cons = scraperClazz?.getConstructor(Long::class.java, Int::class.java, Long::class.java, Endpoint::class.java, DI::class.java)
                    obj = cons?.newInstance(it.sqiContentEntryUid, it.sqiUid, it.sqiContentEntryParentUid, endpoint, di)
                    obj?.scrapeUrl(it.scrapeUrl!!)
                }
            } catch (t: TimeoutCancellationException) {
                queueDao.updateSetStatusById(it.sqiUid, ScrapeQueueItemDao.STATUS_FAILED, ERROR_TYPE_TIMEOUT)
                contentEntryDao.updateContentEntryInActive(it.sqiContentEntryParentUid, true)
            } catch (s: ScraperException) {
                Napier.e("$logPrefix Known Exception ${s.message}", tag = SCRAPER_TAG)
            } catch (e: Exception) {
                queueDao.updateSetStatusById(it.sqiUid, ScrapeQueueItemDao.STATUS_FAILED, 0)
                contentEntryDao.updateContentEntryInActive(it.sqiContentEntryParentUid, true)
                Napier.e("$logPrefix Exception running scrapeContent ${it.scrapeUrl}", tag = SCRAPER_TAG)
                Napier.e("$logPrefix ${ExceptionUtils.getStackTrace(e)}", tag = SCRAPER_TAG)
            } finally {
                obj?.close()
            }

            queueDao.setTimeFinished(it.sqiUid, System.currentTimeMillis())
            val duration = System.currentTimeMillis() - startTime
            Napier.i("$logPrefix Ended scrape for url ${it.scrapeUrl} in duration: $duration", tag = SCRAPER_TAG)

        }.also { scrapeQueue ->
            GlobalScope.launch {
                scrapeQueue.start()
            }
        }
    }

    fun start(startingUrl: String, scraperType: String, parentUid: Long, contentEntryUid: Long, overrideEntry: Boolean = false) {
        val runId = runDao.insert(ScrapeRun(scraperType,
                ScrapeQueueItemDao.STATUS_PENDING)).toInt()

        val isIndexer = ScraperTypes.indexerTypeMap.keys.find { it == scraperType }
        val isScraper = ScraperTypes.scraperTypeMap.keys.find { it == scraperType }

        val itemType = when {
            isIndexer != null -> ScrapeQueueItem.ITEM_TYPE_INDEX
            isScraper != null -> ScrapeQueueItem.ITEM_TYPE_SCRAPE
            else -> throw IllegalArgumentException()
        }

        val scrapeQueue = ScrapeQueueItem().apply {
            this.runId = runId
            contentType = scraperType
            scrapeUrl = startingUrl
            sqiContentEntryParentUid = parentUid
            sqiContentEntryUid = contentEntryUid
            this.overrideEntry = overrideEntry
            status = ScrapeQueueItemDao.STATUS_PENDING
            this.itemType = itemType
            timeAdded = System.currentTimeMillis()
        }
        queueDao.insert(scrapeQueue)
    }

    suspend fun extractMetadata(url: String): ImportedContentEntryMetaData? {

        var tempDir = Files.createTempDirectory("folder").toFile()
        tempDir.mkdir()
        val contentFile = File(tempDir, "url")

        val huc: HttpURLConnection = URL(url).openConnection() as HttpURLConnection

        val mimeType = huc.contentType
        val stream = huc.inputStream
        FileOutputStream(contentFile).use {
            stream.copyTo(it)
            it.flush()
        }
        stream.close()

        val supported = mimeTypeSupported.find { fileMimeType -> fileMimeType == mimeType }
        return if (supported != null) {
            val metaData = extractContentEntryMetadataFromFile(contentFile, db)
            metaData?.scraperType = ScraperTypes.URL_SCRAPER
            metaData?.uri = url
            metaData?.contentEntry?.sourceUrl = url
            Napier.e("$logPrefix metadata uri for urlScraper: ${metaData?.uri ?: "not found"}", tag = SCRAPER_TAG)
            tempDir.deleteRecursively()
            metaData
        } else {

            when {
                url.startsWith("https://drive.google.com/") -> {

                    tempDir.deleteRecursively()

                    if(apiKey == "secret"){
                        Napier.e("$logPrefix api key not set", tag = SCRAPER_TAG)
                        return null
                    }

                    val apiCall: String
                    val fileId: String
                    if (url.startsWith("https://drive.google.com/file/d/")) {
                        val fileIdLookUp = url.substringAfter("https://drive.google.com/file/d/")
                        val char = fileIdLookUp.firstOrNull { it == '/' || it == '?' }
                        fileId = if (char == null) fileIdLookUp else fileIdLookUp.substringBefore(char)
                        apiCall = "https://www.googleapis.com/drive/v3/files/$fileId"
                    } else {
                        Napier.e("$logPrefix unsupported google drive link", tag = SCRAPER_TAG)
                        return null
                    }

                    val statement = defaultHttpClient().get<HttpStatement>(apiCall) {
                        parameter("key", apiKey)
                        parameter("fields", "id,modifiedTime,name,mimeType,description,thumbnailLink")
                    }.execute()

                    val file = statement.receive<GoogleFile>()

                    if (file.mimeType.isNullOrEmpty()) {
                        Napier.e("$logPrefix no mimetype found in googleDriveLink", tag = SCRAPER_TAG)
                        return null
                    }

                    mimeTypeSupported.find { fileMimeType -> fileMimeType == file.mimeType }
                            ?: return null

                    Napier.d("$logPrefix mimetype found for google drive link: ${file.mimeType}", tag = SCRAPER_TAG)

                    val dataStatement = defaultHttpClient().get<HttpStatement>(apiCall) {
                        parameter("alt", "media")
                        parameter("key", apiKey)
                    }.execute()

                    tempDir = Files.createTempDirectory("folder").toFile()
                    tempDir.mkdir()
                    val googleStream = dataStatement.receive<InputStream>()
                    val googleFile = File(tempDir, file.name ?: file.id ?: fileId)

                    FileOutputStream(googleFile).use {
                        googleStream.copyTo(it)
                        it.flush()
                    }
                    stream.close()

                    val metadata = extractContentEntryMetadataFromFile(googleFile, db)
                    metadata?.scraperType = ScraperTypes.GOOGLE_DRIVE_SCRAPE
                    metadata?.uri = apiCall
                    metadata?.contentEntry?.sourceUrl = apiCall
                    Napier.e("$logPrefix metadata uri for drive link: ${metadata?.uri ?: "not found"}", tag = SCRAPER_TAG)
                    tempDir.deleteRecursively()
                    return metadata
                }
                mimeType.contains("text/html") -> {

                    val data = FileUtils.readFileToString(contentFile, ScraperConstants.UTF_ENCODING)
                    tempDir.deleteRecursively()
                    val document = Jsoup.parse(data)

                    val table = document.select("table tr th")
                    if (table.isEmpty()) {
                        return null
                    }

                    val altElement = table.select("[alt]")
                    if (altElement.isNullOrEmpty() || altElement.attr("alt") != "[ICO]") {
                        return null
                    }

                    val urlWithEndingSlash =  url.requirePostfix()
                    val entry = ContentEntryWithLanguage()
                    entry.title = document.title().substringAfterLast("/")
                    entry.sourceUrl = urlWithEndingSlash
                    entry.leaf = false
                    entry.contentTypeFlag = ContentEntry.TYPE_COLLECTION
                    Napier.e("$logPrefix metadata uri for apacheIndexer: $urlWithEndingSlash", tag = SCRAPER_TAG)

                    return ImportedContentEntryMetaData(entry, "text/html", urlWithEndingSlash, 0, ScraperTypes.APACHE_INDEXER)

                }

                else -> return null
            }
        }
    }


    companion object {

        private const val CONTAINER_ARGS = "container"
        private const val CLAZZ_ARGS = "startScrape"
        private const val LOG_ARGS = "log"
        private const val RUN_ID_ARGS = "resume"
        private const val INDEXER_ARGS = "indexer"
        private const val SCRAPER_ARGS = "scraper"
        private const val START_URL_ARGS = "url"
        private const val PARENT_ENTRY_UID_ARGS = "parentUid"
        private const val ENTRY_TITLE_ARGS = "title"
        private const val ENTRY_LANG_ARGS = "lang"
        private const val ENTRY_PUBLISHER_ARGS = "publisher"

        const val ERROR_TYPE_UNKNOWN = 10

        @JvmStatic
        fun main(args: Array<String>) {

            val options = Options()

            val containerOption = Option.builder(CONTAINER_ARGS)
                    .argName("file")
                    .hasArg()
                    .required()
                    .desc("container path")
                    .build()
            options.addOption(containerOption)

            val clazz = Option.builder(CLAZZ_ARGS)
                    .argName("scraperType")
                    .hasArg()
                    .desc("choose the class to run the scraper - see scraper type map")
                    .build()
            options.addOption(clazz)

            val startUrlOption = Option.builder(START_URL_ARGS)
                    .argName("url")
                    .hasArg()
                    .desc("starting url for scrape")
                    .build()
            options.addOption(startUrlOption)

            val parentUidOption = Option.builder(PARENT_ENTRY_UID_ARGS)
                    .argName(PARENT_ENTRY_UID_ARGS)
                    .hasArg()
                    .desc("parentUid for indexer to start from")
                    .build()
            options.addOption(parentUidOption)

            val debugOption = Option.builder(LOG_ARGS)
                    .argName("level")
                    .hasArg()
                    .desc("Set the level of the log [trace, info, debug, error, fatal]")
                    .build()
            options.addOption(debugOption)


            val runOption = Option.builder(RUN_ID_ARGS)
                    .argName("run id")
                    .hasArg()
                    .desc("set the run id to resume previous scrape runner")
                    .build()
            options.addOption(runOption)

            val indexOption = Option.builder(INDEXER_ARGS)
                    .argName("index total")
                    .hasArg()
                    .desc("set the total number of indexer should be running together")
                    .build()
            options.addOption(indexOption)

            val scraperOption = Option.builder(SCRAPER_ARGS)
                    .argName("scraper total")
                    .hasArg()
                    .desc("set the total number of scrapers should be running together")
                    .build()
            options.addOption(scraperOption)

            val entryTitleOption = Option.builder(ENTRY_TITLE_ARGS)
                    .argName("entry title args")
                    .hasArg()
                    .desc("set the title of the new contentEntry")
                    .build()
            options.addOption(entryTitleOption)

            val entryLangOption = Option.builder(ENTRY_LANG_ARGS)
                    .argName("entry lang args")
                    .hasArg()
                    .desc("set the language of the new contentEntry")
                    .build()
            options.addOption(entryLangOption)

            val entryPubOption = Option.builder(ENTRY_PUBLISHER_ARGS)
                    .argName("entry publisher args")
                    .hasArg()
                    .desc("set the publisher of the new contentEntry")
                    .build()
            options.addOption(entryPubOption)


            val cmd: CommandLine
            try {

                val parser: CommandLineParser = DefaultParser()
                cmd = parser.parse(options, args)

            } catch (e: ParseException) {
                System.err.println("Parsing failed.  Reason: " + e.message)
                exitProcess(1)
            }

            val indexTotal: Int = cmd?.getOptionValue(INDEXER_ARGS)?.toIntOrNull() ?: 4
            val scraperTotal: Int = cmd?.getOptionValue(SCRAPER_ARGS)?.toIntOrNull() ?: 1

            val logLevel = cmd?.getOptionValue(LOG_ARGS) ?: ""
            UMLogUtil.setLevel(logLevel)


            val containerPath = cmd.getOptionValue(CONTAINER_ARGS)




            /*  val runner = ScraperManager(indexTotal, scraperTotal)
              if (cmd.hasOption(RUN_ID_ARGS)) {
                  runner.resume(cmd.getOptionValue(RUN_ID_ARGS).toInt())
              } else if (cmd.hasOption(ENTRY_TITLE_ARGS)) {

                  val startingUrl = cmd.getOptionValue(START_URL_ARGS)
                  val entryTitle = cmd?.getOptionValue(ENTRY_TITLE_ARGS)
                  val entryLang = cmd?.getOptionValue(ENTRY_LANG_ARGS) ?: "en"
                  val entryPub = cmd?.getOptionValue(ENTRY_PUBLISHER_ARGS) ?: ""
                  val parentUid = cmd?.getOptionValue(PARENT_ENTRY_UID_ARGS)?.toLongOrNull() ?: 0

                  runner.createEntry(startingUrl, entryTitle, entryLang, parentUid, entryPub)

              } else {

                  val scraperType = cmd.getOptionValue(CLAZZ_ARGS)
                  val startingUrl = cmd.getOptionValue(START_URL_ARGS)
                          ?: ScraperTypes.indexerTypeMap[scraperType]?.defaultUrl
                          ?: throw IllegalArgumentException("No default url for this scraperType, please provide")

                  val parentUid = cmd?.getOptionValue(PARENT_ENTRY_UID_ARGS)?.toLongOrNull()
                          ?: -4103245208651563007L

                  runner.start(startingUrl, scraperType, parentUid)
              }
    */
        }

    }

}