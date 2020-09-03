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
import org.apache.commons.lang.exception.ExceptionUtils
import java.io.File
import java.io.IOException


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
    fun findContent(name: String, destinationDir: File, containerDir: File) {

        publisher = name
        containerDir.mkdirs()
        this.containerDir = containerDir
        repository = db //db!!.getRepository("https://localhost", "")
        contentEntryDao = repository!!.contentEntryDao
        contentParentChildJoinDao = repository!!.contentEntryParentChildJoinDao
        containerDao = repository!!.containerDao
        languageDao = repository!!.languageDao
        languageVariantDao = repository!!.languageVariantDao

        LanguageList().addAllLanguages()

        englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao!!, "English")

        val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao!!)


        val parentFolder = ContentScraperUtil.createOrUpdateContentEntry(name, name,
                filePrefix + destinationDir.path, name, LICENSE_TYPE_PUBLIC_DOMAIN, englishLang!!.langUid, null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, 0, contentEntryDao!!)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, masterRootParent, parentFolder, 7)

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
            if (args.size < 3) {
                System.err.println("Usage: <folder parent name> <file destination><folder container><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }
            UMLogUtil.setLevel(if (args.size == 4) args[3] else "")
            UMLogUtil.logInfo(args[0])
            UMLogUtil.logInfo(args[1])
            try {
                IndexFolderScraper().findContent(args[0], File(args[1]), File(args[2]))
            } catch (e: Exception) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logFatal("Exception running findContent Folder")
            }

        }
    }

}
