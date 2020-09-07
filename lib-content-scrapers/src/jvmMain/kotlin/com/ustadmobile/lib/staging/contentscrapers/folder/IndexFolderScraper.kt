package com.ustadmobile.lib.staging.contentscrapers.folder

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.LanguageList
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_PUBLIC_DOMAIN
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.TYPE_COLLECTION
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.sharedse.contentformats.extractContentEntryMetadataFromFile
import com.ustadmobile.port.sharedse.contentformats.importContainerFromFile
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.*
import org.apache.commons.lang.exception.ExceptionUtils
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess


/**
 * Given a directory, create parent and child joins for each subdirectories in them.
 * If an epub is found, open the epub using Zipfile and find the container xml with path: META-INF/container.xml
 * This will contain the path to the rootfile which contains all the content inside the epub.
 * Open the rootfile using the path and extract the id, author etc to create the content entry
 */
@ExperimentalStdlibApi
class IndexFolderScraper {

    private var contentEntryDao: ContentEntryDao? = null
    private var contentParentChildJoinDao: ContentEntryParentChildJoinDao? = null
    private var languageDao: LanguageDao? = null
    private var englishLang: Language? = null
    private var publisher: String? = null
    private var languageVariantDao: LanguageVariantDao? = null
    private val filePrefix = "file://"
    private var db: UmAppDatabase = UmAppDatabase.getInstance(Any())
    private var repository: UmAppDatabase? = null
    private var containerDao: ContainerDao? = null
    private var containerDir: File? = null

    @Throws(IOException::class)
    fun findContent(name: String, destinationDir: File, containerDir: File, parentUid: Long) {

        containerDir.mkdirs()
        this.containerDir = containerDir
        repository = db
        contentEntryDao = repository!!.contentEntryDao
        contentParentChildJoinDao = repository!!.contentEntryParentChildJoinDao
        containerDao = repository!!.containerDao
        languageDao = repository!!.languageDao
        languageVariantDao = repository!!.languageVariantDao

        LanguageList().addAllLanguages()

        englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao!!, "English")

        val parentFolder = ContentScraperUtil.createOrUpdateContentEntry(name, name,
                filePrefix + destinationDir.path, name, LICENSE_TYPE_PUBLIC_DOMAIN, englishLang!!.langUid, null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, 0, contentEntryDao!!)

        var parentEntry = contentEntryDao!!.findByUid(parentUid)

        if (parentEntry == null) {
            parentEntry = ContentEntry().apply {
                this.contentEntryUid = parentUid
            }
        }

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentEntry, parentFolder, 7)

        browseSubFolders(destinationDir, parentFolder)

    }

    private fun browseSubFolders(destinationDir: File, parentEntry: ContentEntry) {

        val fileList = destinationDir.listFiles()

        if (fileList == null || fileList.isEmpty()) {
            return
        }

        var folderCount = 0
        var fileCount = 0
        for (folder in fileList) {

            if (folder.isDirectory) {

                val name = folder.name

                val childEntry = ContentScraperUtil.createOrUpdateContentEntry(name, name,
                        filePrefix + folder.path, name, LICENSE_TYPE_PUBLIC_DOMAIN, englishLang!!.langUid, null,
                        EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING,
                        EMPTY_STRING, TYPE_COLLECTION, contentEntryDao!!)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentEntry, childEntry, folderCount++)

                browseSubFolders(folder, childEntry)

            } else if (folder.isFile) {

                println("file name is ${folder.name}")
                runBlocking {

                    val metadata = extractContentEntryMetadataFromFile(folder.absolutePath, db)
                            ?: return@runBlocking

                    val metadataContentEntry = metadata.contentEntry

                    val fileEntry = ContentScraperUtil.createOrUpdateContentEntry(metadataContentEntry.entryId, metadataContentEntry.title,
                            filePrefix + folder.path, metadataContentEntry.publisher ?: "",
                            metadataContentEntry.licenseType, metadataContentEntry.primaryLanguageUid, metadataContentEntry.languageVariantUid,
                            metadataContentEntry.description, true, EMPTY_STRING,
                            metadataContentEntry.thumbnailUrl, EMPTY_STRING,
                            EMPTY_STRING,
                            metadataContentEntry.contentTypeFlag, contentEntryDao!!)

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentEntry, fileEntry, fileCount++)

                    importContainerFromFile(fileEntry.contentEntryUid, metadata.mimeType, containerDir!!.absolutePath, folder.absolutePath, db, db, metadata.importMode, Any())

                }
            }

        }


    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            val options = Options()

            val scrapeOption = Option.builder("scrape")
                    .argName("folder")
                    .hasArg()
                    .required()
                    .desc("path to folder for scrape ")
                    .build()
            options.addOption(scrapeOption)

            val containerOption = Option.builder("container")
                    .argName("container")
                    .hasArg()
                    .required()
                    .desc("path to container to store")
                    .build()
            options.addOption(containerOption)


            val parentOption = Option.builder("parentUid")
                    .argName("uid")
                    .hasArg()
                    .desc("parentUid to folder to join")
                    .build()
            options.addOption(parentOption)

            val folderNameOption = Option.builder("folderName")
                    .argName("name")
                    .hasArg()
                    .desc("optional name to folder")
                    .build()
            options.addOption(folderNameOption)

            val logOption = Option.builder("log")
                    .argName("option")
                    .hasArg()
                    .desc("og{trace, debug, info, warn, error, fatal}")
                    .build()
            options.addOption(logOption)

            val cmd: CommandLine
            try {

                val parser: CommandLineParser = DefaultParser()
                cmd = parser.parse(options, args)

                val container = cmd.getOptionValue("container")
                val folderPath = cmd.getOptionValue("scrape")
                val file = File(folderPath)
                val parentUid = cmd.getOptionValue("parentUid")?.toLongOrNull()
                        ?: -4103245208651563007L
                val folderName = cmd.getOptionValue("folderName") ?: file.name
                val log = cmd.getOptionValue("log") ?: "INFO"

                UMLogUtil.setLevel(log)

                IndexFolderScraper().findContent(folderName, file, File(container), parentUid)

            } catch (e: ParseException) {
                System.err.println("Parsing failed.  Reason: " + e.message)
                exitProcess(1)
            } catch (e: Exception) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logFatal("Exception running findContent Folder")
            }
        }
    }

}
