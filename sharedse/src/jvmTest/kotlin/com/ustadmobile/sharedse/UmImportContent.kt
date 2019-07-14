package com.ustadmobile.sharedse

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.REL_TYPE_TRANSLATED_VERSION
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.port.sharedse.util.UmZipUtils
import com.ustadmobile.util.test.checkJndiSetup
import kotlinx.coroutines.runBlocking
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


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

    private val ROOT_ENTRY_ID = 12347120167L

    private val DEFAULT_FILE_PATH = ""

    private val languages = listOf(Language(1L,"English","en"),
            Language(2L,"پښتو","ps"), Language(3L,"دری","fa"))

    private val entryFileMap = HashMap<String, ArrayList<File>?>()

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

        entryFileMap["$ROOT_ENTRY_ID-1"] = arrayListOf()

        for(language in languages){
            appDatabase.languageDao.insert(language)
        }
    }

    private fun zipContainerEntryFiles(files: List<File>){
        ZipOutputStream(BufferedOutputStream(FileOutputStream(
                System.getProperty("user.dir") +"/build/tmp/exported-content.zip"))).use { out ->
            for (file in files) {
                FileInputStream(file).use { input ->
                    BufferedInputStream(input).use { origin ->
                        val entry = ZipEntry(file.name)
                        out.putNextEntry(entry)
                        origin.copyTo(out, 1024)
                    }
                }
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

    private fun getLanguageKey(absolutePath: String, id: Int): String{
        val language = when {
            absolutePath.indexOf("/en/") != -1 -> 1
            absolutePath.indexOf("/ps/") != -1 -> 2
            absolutePath.indexOf("/fa/") != -1 -> 3
            else -> ""
        }
        return "$id-$language"
    }

    data class UmRelation(var entryId: Long, var languageId: Long)

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

        val thumbnailsMap = HashMap<String, File?>()


        var oldEntryTracker:Int = -1

        var currentEntryTracker: Int = -1

        contentsDir.listFiles().forEach {
            if(it.name.contains("_MACOSX")){
                it.deleteRecursively()
            }
        }

        val modulesDir = contentsDir.listFiles()[0]

        contentsDir.walkTopDown().forEach {
            if(it.parent == "${contentsDir.absolutePath}/Modules"){
                if(it.isDirectory){
                    val dirParts = it.name.split("-")
                    if(dirParts.size > 1 && entryFileMap[getLanguageKey(it.absolutePath,dirParts[0].toInt())] == null){
                        currentEntryTracker = dirParts[0].toInt()
                        if(oldEntryTracker != currentEntryTracker){
                            oldEntryTracker = currentEntryTracker
                        }
                    }else{
                        currentEntryTracker = -1
                        if(!it.isDirectory){
                            entryFileMap["$ROOT_ENTRY_ID-1"]!!.add(it)
                        }
                    }
                }else{
                    entryFileMap["$ROOT_ENTRY_ID-1"]!!.add(it)
                }
            }else{

                if(currentEntryTracker == oldEntryTracker && !it.isDirectory){
                    if(entryFileMap[getLanguageKey(it.absolutePath, currentEntryTracker)] == null){
                        entryFileMap[getLanguageKey(it.absolutePath, currentEntryTracker)] = arrayListOf<File>()
                    }
                    entryFileMap[getLanguageKey(it.absolutePath, currentEntryTracker)]!!.add(it)
                }else if(!it.isDirectory){
                    entryFileMap["$ROOT_ENTRY_ID-1"]!!.add(it)
                    if(it.parent == "${contentsDir.absolutePath}/Modules/icons"){
                        if(!it.isDirectory && it.name.contains("-thumb")){
                            thumbnailsMap[getLanguageKey(it.absolutePath,it.name.split("-")[0].toInt())] = it
                        }
                    }
                }
            }
        }


        //insert content to the db
        var currentEntryId  = 1
        val entryRelatedMap = HashMap<String, HashSet<UmRelation>>()
        for(entryId in entryFileMap.toSortedMap().keys){
            if(entryId.split("-")[1].isNotEmpty()){
                val sourceFiles = arrayListOf<ContainerManager.FileEntrySource>()
                val files = entryFileMap[entryId]!!
                var currentEntry: ContentEntry?

                if(entryId == "$ROOT_ENTRY_ID-1"){
                    currentEntry = rootContentEntry
                }else{
                    val entryIdParts = entryId.split("-")
                    val thumbnail = thumbnailsMap[entryId.substring(0, entryId.indexOf("-")+1)]?.name
                    currentEntry = ContentEntry("","",
                            leaf = false, publik = false)
                    val assignedId = currentEntryId++.toLong()
                    currentEntry.thumbnailUrl = thumbnail
                    currentEntry.lastModified = System.currentTimeMillis()
                    currentEntry.licenseType = ContentEntry.PUBLIC_DOMAIN
                    currentEntry.contentEntryUid = assignedId
                    currentEntry.primaryLanguageUid = entryIdParts[1].toLong()
                    currentEntry.languageVariantUid = 0
                    currentEntry.leaf = true
                    currentEntry.publik = true
                    appDatabase.contentEntryDao.insert(currentEntry)

                    /*if((entryIdParts[1] == "1")){

                    }*/

                    appDatabase.contentEntryParentChildJoinDao.insert(
                            ContentEntryParentChildJoin(rootContentEntry, currentEntry, 0))
                    if(entryRelatedMap[entryIdParts[0]] == null){
                        entryRelatedMap[entryIdParts[0]] = hashSetOf()
                    }
                    entryRelatedMap[entryIdParts[0]]!!.add(UmRelation(assignedId,entryIdParts[1].toLong()))
                }

                for(file in files){
                    var source = ContainerManager.FileEntrySource(file, file.name)
                    if(file.name.endsWith(".opf")){
                        val baseDir = file.parent.substring(0,file.parent.indexOf("EPUB"))

                        createTinCanXmlFile(currentEntry,baseDir, file)
                        source = ContainerManager.FileEntrySource(File(baseDir,TINCAN_NAME), TINCAN_NAME)
                    }
                    sourceFiles.add(source)
                }

                val container = Container(currentEntry)
                container.mimeType = "application/tincan+zip"
                container.lastModified = System.currentTimeMillis()
                container.containerUid = appDatabase.containerDao.insert(container)
                val containerManager = ContainerManager(container, appDatabase, appRepo,
                        contentsDir.absolutePath)
                containerManager.addEntries(null,*sourceFiles.toTypedArray())
                containerManager.allEntries

            }
        }

        //update path
        val entryFiles = appDatabase.containerEntryFileDao.getAllEntryFiles()

        val contentEntryRelatedJoinList = ArrayList<ContentEntryRelatedEntryJoin>()

        for(relationEntry in entryRelatedMap.toSortedMap().keys){
            val relatedSet = entryRelatedMap[relationEntry]!!.toList()
            for(relation in relatedSet){
                contentEntryRelatedJoinList.add(ContentEntryRelatedEntryJoin(
                        relatedSet[0].entryId, relation.entryId,relation.languageId, REL_TYPE_TRANSLATED_VERSION))
            }
        }

        appDatabase.contentEntryRelatedEntryJoinDao.insertList(contentEntryRelatedJoinList)


        for(entryFile in entryFiles){
            entryFile.cefPath = entryFile.cefPath!!.replace(
                    contentsDir.absolutePath, DEFAULT_FILE_PATH)
            appDatabase.containerEntryFileDao.update(entryFile)
        }

        val entries =  appDatabase.contentEntryDao.getChildrenByParent(ROOT_ENTRY_ID)

        for(entry in entries){
            val entryWithFile = appDatabase.containerEntryDao.findByPathInContainer(entry.thumbnailUrl!!)!!
            entry.thumbnailUrl = DEFAULT_FILE_PATH + entryWithFile.containerEntryFile!!.cefPath
            appDatabase.contentEntryDao.update(entry)
        }

        modulesDir.deleteRecursively()

        zipContainerEntryFiles(contentsDir.listFiles().toList())

        Assert.assertTrue("Entries added successfully ", appDatabase.contentEntryDao.getChildrenByParent(ROOT_ENTRY_ID).isNotEmpty())

    }
}