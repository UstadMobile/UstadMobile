package com.ustadmobile.lib.contentscrapers.folder

import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.contentscrapers.*
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EPUB_EXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_PUBLIC_DOMAIN
import com.ustadmobile.lib.db.entities.Language
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.exception.ExceptionUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Paths


/**
 * Given a directory, create parent and child joins for each subdirectories in them.
 * If an epub is found, open the epub using Zipfile and find the container xml with path: META-INF/container.xml
 * This will contain the path to the rootfile which contains all the content inside the epub.
 * Open the rootfile using the path and extract the id, author etc to create the content entry
 */
class IndexFolderScraper {

    private var contentEntryDao: ContentEntryDao? = null
    private var contentParentChildJoinDao: ContentEntryParentChildJoinDao? = null
    private var languageDao: LanguageDao? = null
    private var englishLang: Language? = null
    private var publisher: String? = null
    private var languageVariantDao: LanguageVariantDao? = null
    private val filePrefix = "file://"
    private var db: UmAppDatabase? = null
    private var repository: UmAppDatabase? = null
    private var containerDao: ContainerDao? = null
    private var containerDir: File? = null

    @Throws(IOException::class)
    fun findContent(name: String, destinationDir: File, containerDir: File) {

        publisher = name
        containerDir.mkdirs()
        this.containerDir = containerDir
        db = UmAppDatabase.getInstance(Any())
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
                        EMPTY_STRING, 0, contentEntryDao!!)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentEntry, childEntry, folderCount++)

                browseSubFolders(folder, childEntry)

            } else if (folder.isFile) {

                if (folder.name.contains(EPUB_EXT)) {

                    var opfFileInputStream: FileInputStream? = null
                    var ocfFileInputStream: FileInputStream? = null
                    try {

                        val tmpFolder = ShrinkerUtil.shrinkEpub(folder)
                        val ocfDoc = OcfDocument()
                        val ocfFile = File(tmpFolder, Paths.get("META-INF", "container.xml").toString())
                        ocfFileInputStream = FileInputStream(ocfFile)
                        val ocfParser = UstadMobileSystemImpl.instance
                                .newPullParser(ocfFileInputStream)
                        ocfDoc.loadFromParser(ocfParser)

                        val opfFile = File(tmpFolder, ocfDoc.getRootFiles()[0].fullPath!!)
                        val document = OpfDocument()
                        opfFileInputStream = FileInputStream(opfFile)
                        val xmlPullParser = UstadMobileSystemImpl.instance
                                .newPullParser(opfFileInputStream)
                        document.loadFromOPF(xmlPullParser)

                        val title = document.title
                        val lang = if (document.getLanguages().isNotEmpty()) document.getLanguages()[0] else null

                        val creators = StringBuilder()
                        for (i in 0 until document.numCreators) {
                            if (i != 0) {
                                creators.append(",")
                            }
                            creators.append(document.getCreator(i))
                        }

                        val id = document.id

                        val date = folder.lastModified()

                        val country = lang?.split("-")
                        val twoCode = country?.get(0)
                        val variant = if (country != null && country.size > 1) country[1] else EMPTY_STRING

                        val language = if (twoCode != null) ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao!!, twoCode) else null
                        val languageVariant = if (language != null)ContentScraperUtil.insertOrUpdateLanguageVariant(languageVariantDao!!, variant, language) else null

                        val childEntry = ContentScraperUtil.createOrUpdateContentEntry(id!!, title,
                                filePrefix + folder.path, publisher!!, LICENSE_TYPE_PUBLIC_DOMAIN, language?.langUid
                                ?: 0, languageVariant?.langVariantUid,
                                EMPTY_STRING, true, creators.toString(), EMPTY_STRING, EMPTY_STRING,
                                EMPTY_STRING, 0, contentEntryDao!!)

                        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentEntry, childEntry, fileCount++)

                        val serverDate = ContentScraperUtil.getLastModifiedOfFileFromContentEntry(childEntry, containerDao!!)

                        if (serverDate == -1L || date > serverDate) {
                            ContentScraperUtil.insertContainer(containerDao!!, childEntry, true,
                                    ScraperConstants.MIMETYPE_EPUB, tmpFolder.lastModified(), tmpFolder, db!!, repository!!,
                                    containerDir!!)
                            UMIOUtils.closeInputStream(opfFileInputStream)
                            UMIOUtils.closeInputStream(ocfFileInputStream)
                            FileUtils.deleteDirectory(tmpFolder)
                        }
                    } catch (e: Exception) {
                        UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                        UMLogUtil.logError("Error while parsing a file " + folder.name)
                    } finally {
                        UMIOUtils.closeInputStream(opfFileInputStream)
                        UMIOUtils.closeInputStream(ocfFileInputStream)
                    }

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
