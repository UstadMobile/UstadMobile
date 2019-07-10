package com.ustadmobile.sharedse

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.port.sharedse.util.UmZipUtils
import com.ustadmobile.util.test.checkJndiSetup
import kotlinx.coroutines.runBlocking
import kotlinx.io.InputStream
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.IOException


/**
 * Class used to generate database for the database preload
 *
 * Make sure /com/ustadmobile/port/sharedse/contents.zip do exists
 */

class UmImportContent {


    private val context = Any()

    private lateinit var appDatabase: UmAppDatabase

    private lateinit var appRepo: UmAppDatabase

    private lateinit var rootContentEntry: ContentEntry

    private val TINCAN_NAME = "tincan.xml"

    private val ROOT_ENTRY_ID = -12347120167L

    private val entryFileMap = HashMap<Long, ArrayList<File>?>()

    private val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    @Before
    @Throws(IOException::class)
    fun setUp() {
        checkJndiSetup()
        appDatabase = UmAppDatabase.getInstance(context)
        appRepo = UmAccountManager.getRepositoryForActiveAccount(context)
        appDatabase.clearAllTables()

        rootContentEntry = ContentEntry("MSST","Soft skills",
                leaf = false, publik = true)
        rootContentEntry.contentEntryUid = ROOT_ENTRY_ID
        appDatabase.contentEntryDao.insert(rootContentEntry)

        entryFileMap[ROOT_ENTRY_ID] = arrayListOf()
    }

    private fun File.copyInputStreamToFile(inputStream: InputStream) {
        inputStream.use { input ->
            this.outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
        }
    }

    private fun createTinCanXmlFile(entry: ContentEntry = ContentEntry(),baseDir:String, file: File){
        val inputStream = FileInputStream(file)
        val xpp = impl.newPullParser(inputStream)
        val opfDocument = OpfDocument()
        opfDocument.loadFromOPF(xpp)

        entry.title = opfDocument.title
        entry.description = opfDocument.description
        opfDocument.title
        val tincanXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +
                "\n" +
                "<tincan xmlns=\"http://projecttincan.com/tincan.xsd\">\n" +
                "    <activities>\n" +
                "        <activity id=\""+opfDocument.id+"\" type=\"http://adlnet.gov/expapi/activities/module\">\n" +
                "            <name>"+opfDocument.title+"</name>\n" +
                "            <description lang=\"en-US\">"+opfDocument.description+"</description>\n" +
                "            <launch lang=\"en-us\">EPUB/main.html</launch>\n" +
                "        </activity>\n" +
                "    </activities>\n" +
                "</tincan>"

        File(baseDir, TINCAN_NAME).printWriter().use { out ->
            out.write(tincanXml)
        }
        appDatabase.contentEntryDao.update(entry)
    }

    @Test
    fun givenExitingContentToBeImported_whenLoaded_thenShouldImport() = runBlocking {

        val contentsDir = UmFileUtilSe.makeTempDir("importContent",
                "contents")

        val inputStream = UstadMobileSystemImpl.instance.getAssetSync(context,
                "/com/ustadmobile/port/sharedse/contents.zip")

        val tempFile = File.createTempFile("importContent", "tmpfile")

        tempFile.copyInputStreamToFile(inputStream)

        UmZipUtils.unzip(tempFile,contentsDir)

        tempFile.deleteOnExit()

        val thumbnailsMap = HashMap<Long, File?>()

        var oldEntryTracker:Long = -1L

        var currentEntryTracker: Long = -1

        contentsDir.walkTopDown().forEach {
            //if zip was generated on MaCOS it will generate _MACOSX dir, skipp it
            if(!it.absolutePath.contains("_")){

                if(it.parent == "${contentsDir.absolutePath}/Modules"){
                    if(it.isDirectory){
                        val dirParts = it.name.split("-")
                        if(dirParts.size > 1 && entryFileMap[dirParts[0].toLong()] == null){
                            currentEntryTracker = dirParts[0].toLong()
                            if(oldEntryTracker != currentEntryTracker){
                                oldEntryTracker = currentEntryTracker
                                entryFileMap[currentEntryTracker] = arrayListOf<File>()
                            }
                        }else{
                            currentEntryTracker = -1
                            if(!it.isDirectory){
                                entryFileMap[ROOT_ENTRY_ID]!!.add(it)
                            }
                        }
                    }else{
                        if(it.name.endsWith(".jpg")){
                            thumbnailsMap[it.name.split("-")[0].toLong()] = it
                        }
                        entryFileMap[ROOT_ENTRY_ID]!!.add(it)
                    }
                }else{
                    if(currentEntryTracker == oldEntryTracker && !it.isDirectory){
                        entryFileMap[currentEntryTracker]!!.add(it)
                    }else if(!it.isDirectory){
                        entryFileMap[ROOT_ENTRY_ID]!!.add(it)
                    }
                }
            }
        }


        for(entryId in entryFileMap.keys){
            val sourceFiles = arrayListOf<ContainerManager.FileEntrySource>()
            val files = entryFileMap[entryId]!!
            var currentEntry: ContentEntry?

            if(entryId == ROOT_ENTRY_ID){
                currentEntry = rootContentEntry
            }else{
                val thumbnail = thumbnailsMap[entryId]?.name
                currentEntry = ContentEntry("","",
                        leaf = false, publik = false)
                currentEntry.thumbnailUrl = thumbnail
                currentEntry.contentEntryUid = appDatabase.contentEntryDao.insert(currentEntry)

                appDatabase.contentEntryParentChildJoinDao.insert(
                        ContentEntryParentChildJoin(rootContentEntry, currentEntry, 0))
            }

            for(file in files){
                var source = ContainerManager.FileEntrySource(file, file.name)
                if(file.name.endsWith(".opf")){
                    val baseDir = file.parent.substring(0,file.parent.indexOf("EPUB") - 3)
                    createTinCanXmlFile(currentEntry,baseDir, file)
                    source = ContainerManager.FileEntrySource(File(baseDir,TINCAN_NAME), TINCAN_NAME)
                }
                sourceFiles.add(source)
            }

            val container = Container(currentEntry)
            container.containerUid = appDatabase.containerDao.insert(container)
            val containerManager = ContainerManager(container, appDatabase, appRepo,
                    contentsDir.absolutePath)
            containerManager.addEntries(null,*sourceFiles.toTypedArray())

        }

        contentsDir.deleteRecursively()

        Assert.assertTrue("Entries added successfully ", appDatabase.contentEntryDao.getChildrenByParent(ROOT_ENTRY_ID).isNotEmpty())

    }
}