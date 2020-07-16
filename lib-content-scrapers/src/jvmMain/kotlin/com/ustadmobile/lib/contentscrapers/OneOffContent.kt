import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.util.MimeType
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.*
import java.io.File
import kotlin.system.exitProcess

@ExperimentalStdlibApi
class OneOffContent(val containerFolder: File) {

    private var contentEntryDao: ContentEntryDao
    private var parentChildJoinDao: ContentEntryParentChildJoinDao
    private var containerDao: ContainerDao
    var db: UmAppDatabase = UmAppDatabase.getInstance(Any())

    lateinit var masterRootParent: ContentEntry
    lateinit var ahlanParent: ContentEntry
    lateinit var psa_1Entry: ContentEntry
    lateinit var psa_2Entry: ContentEntry
    lateinit var psa_3Entry: ContentEntry
    lateinit var psa_4Entry: ContentEntry
    var psaMap: Map<String, ContentEntry>

    init {
        contentEntryDao = db.contentEntryDao
        parentChildJoinDao = db.contentEntryParentChildJoinDao
        containerDao = db.containerDao
        runBlocking {
            masterRootParent = contentEntryDao.findByUidAsync(-4103245208651563007)!!
            ahlanParent = contentEntryDao.findByUidAsync(58261)!!
            psa_1Entry = contentEntryDao.findByUidAsync(69376)!!
            psa_2Entry = contentEntryDao.findByUidAsync(69375)!!
            psa_3Entry = contentEntryDao.findByUidAsync(69377)!!
            psa_4Entry = contentEntryDao.findByUidAsync(69374)!!
        }
        psaMap = mapOf("PSA_1" to psa_1Entry,
                "PSA_2" to psa_2Entry, "PSA_3" to psa_3Entry, "PSA_4" to psa_4Entry)
    }

    fun createEntriesFromFolder(folder: File) {

        var folderEntry = ContentScraperUtil.createOrUpdateContentEntry(folder.name, folder.name,
                "file://${folder.name}", ahlanParent.publisher ?: "", ahlanParent.licenseType,
                ahlanParent.primaryLanguageUid, ahlanParent.languageVariantUid,
                ScraperConstants.EMPTY_STRING, false, ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING,
                ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING, 0, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(parentChildJoinDao, ahlanParent, folderEntry, 0)

        folder.walkTopDown().forEach {

            println(it.nameWithoutExtension)
            val entryToCopyFrom = psaMap[it.nameWithoutExtension] ?: return@forEach

            val newVideoEntry = ContentScraperUtil.createOrUpdateContentEntry(entryToCopyFrom.entryId!!,
                    entryToCopyFrom.title,
                    "file://${folder.name}/${it.name}", entryToCopyFrom.publisher
                    ?: "", entryToCopyFrom.licenseType,
                    entryToCopyFrom.primaryLanguageUid, entryToCopyFrom.languageVariantUid,
                    entryToCopyFrom.description, true, entryToCopyFrom.author, entryToCopyFrom.thumbnailUrl,
                    ScraperConstants.EMPTY_STRING, ScraperConstants.EMPTY_STRING, ContentEntry.VIDEO_TYPE, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(parentChildJoinDao, folderEntry, newVideoEntry, 0)

            val container = Container().apply {
                containerContentEntryUid = newVideoEntry.contentEntryUid
                mimeType = MimeType.MP4
                mobileOptimized = true
                cntLastModified = System.currentTimeMillis()
            }
            container.containerUid = containerDao.insert(container)

            val containerManager = ContainerManager(container, db, db, this.containerFolder.absolutePath)
            runBlocking {
                containerManager.addEntries(ContainerManager.FileEntrySource(it, it.name))
            }
        }
    }


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            val options = Options()

            val containerOption = Option.builder("folder")
                    .argName("folder path ")
                    .hasArg()
                    .required()
                    .desc("path to folder")
                    .build()
            options.addOption(containerOption)

            val dirOption = Option.builder("container")
                    .argName("container folder")
                    .hasArg()
                    .required()
                    .desc("path to container")
                    .build()
            options.addOption(dirOption)

            val cmd: CommandLine
            try {

                val parser: CommandLineParser = DefaultParser()
                cmd = parser.parse(options, args)

                val folderPath = cmd.getOptionValue("folder")
                val containerPath = cmd.getOptionValue("container")

                val content = OneOffContent(File(containerPath))
                content.createEntriesFromFolder(File(folderPath))

            } catch (e: ParseException) {
                System.err.println("Parsing failed.  Reason: " + e.message)
                exitProcess(1)
            }


        }


    }
}