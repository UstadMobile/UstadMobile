package com.ustadmobile.core.catalog.contenttype

import com.turn.ttorrent.tracker.Tracker
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.getAssetFromResource
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.util.zip.ZipInputStream
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.door.ext.openInputStream
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.core.container.PrefixContainerFileNamer
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.contentjob.ext.processMetadata
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.lib.db.entities.ContentJobItem
import kotlinx.serialization.json.*
import java.util.*


val licenseMap = mapOf(
        "CC-BY" to ContentEntry.LICENSE_TYPE_CC_BY,
        "CC BY-SA" to ContentEntry.LICENSE_TYPE_CC_BY_SA,
        "CC BY-ND" to ContentEntry.LICENSE_TYPE_CC_BY,
        "CC BY-NC" to ContentEntry.LICENSE_TYPE_OTHER,
        "CC BY-NC-SA" to ContentEntry.LICENSE_TYPE_CC_BY_NC_SA,
        "CC CC-BY-NC-CD" to ContentEntry.LICENSE_TYPE_OTHER,
        "CC0 1.0" to ContentEntry.LICENSE_TYPE_CC_0,
        "GNU GPL" to ContentEntry.LICENSE_TYPE_OTHER,
        "PD" to ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN,
        "ODC PDDL" to ContentEntry.LICENSE_TYPE_OTHER,
        "CC PDM" to ContentEntry.LICENSE_TYPE_OTHER,
        "C" to ContentEntry.ALL_RIGHTS_RESERVED,
        "U" to ContentEntry.LICENSE_TYPE_OTHER
)

class H5PTypePluginCommonJvm(private var context: Any, val endpoint: Endpoint,override val di: DI): ContentPlugin {

        val viewName: String
    get() = XapiPackageContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
    get() = SupportedContent.H5P_MIME_TYPES

    override val supportedFileExtensions: List<String>
    get() = SupportedContent.H5P_EXTENSIONS

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val torrentDir: File by di.on(endpoint).instance(tag = DiTag.TAG_TORRENT_DIR)

    private val tracker: Tracker = di.direct.instance<Tracker>()

    private val ustadTorrentManager: UstadTorrentManager by di.on(endpoint).instance()

    override val pluginId: Int
        get() = PLUGIN_ID

    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult? {
        val mimeType = uri.guessMimeType(context, di)
        if(mimeType != null && !supportedMimeTypes.contains(mimeType)){
            return null
        }
        return withContext(Dispatchers.Default) {
            val localUri = process.getLocalUri(uri, context, di)
            val inputStream = localUri.openInputStream(context)
            return@withContext ZipInputStream(inputStream).use {
                it.skipToEntry { it.name == H5P_PATH } ?: return@withContext null

                val data = String(it.readBytes())

                val json = Json.parseToJsonElement(data).jsonObject

                // take the name from the role Author otherwise take last one
                var author: String? = ""
                var name: String? = ""
                json["authors"]?.jsonArray?.forEach {
                    name = it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
                    val role = it.jsonObject["role"]?.jsonPrimitive?.content ?: ""
                    if (role == "Author") {
                        author = name
                    }
                }
                if (author.isNullOrEmpty()) {
                    author = name
                }

                val entry = ContentEntryWithLanguage().apply {
                    contentFlags = ContentEntry.FLAG_IMPORTED
                    contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                    licenseType = licenseMap[json.jsonObject["license"] ?: ""]
                            ?: ContentEntry.LICENSE_TYPE_OTHER
                    title = if(json.jsonObject["title"]?.jsonPrimitive?.content.isNullOrEmpty())
                        uri.getFileName(context) else json.jsonObject["title"]?.jsonPrimitive?.content
                    this.author = author
                    leaf = true
                }
                MetadataResult(entry, EpubTypePluginCommonJvm.PLUGIN_ID)
            }
        }
    }

    override suspend fun processJob(jobItem: ContentJobItem, process: ProcessContext, progress: ContentJobProgressListener): ProcessResult {
        val jobUri = jobItem.fromUri ?: return ProcessResult(404)
        val container = withContext(Dispatchers.Default) {

            val doorUri = DoorUri.parse(jobUri)
            val contentEntryUid = processMetadata(jobItem, process, context, endpoint)
            val localUri = process.getLocalUri(doorUri, context, di)

            val container = Container().apply {
                containerContentEntryUid = contentEntryUid
                cntLastModified = System.currentTimeMillis()
                mimeType = supportedMimeTypes.first()
                containerUid = repo.containerDao.insertAsync(this)
            }

            jobItem.cjiContainerUid = container.containerUid

            val containerFolder = jobItem.toUri ?: defaultContainerDir.toURI().toString()
            val containerFolderUri = DoorUri.parse(containerFolder)
            val entry = db.contentEntryDao.findByUid(jobItem.cjiContentEntryUid)

            val containerAddOptions = ContainerAddOptions(storageDirUri = containerFolderUri)
            repo.addEntriesToContainerFromZip(container.containerUid, localUri,
                    ContainerAddOptions(storageDirUri = containerFolderUri,
                            fileNamer = PrefixContainerFileNamer("workspace/")), context)

            val h5pDistTmpFile = File.createTempFile("h5p-dist", "zip")
            val h5pDistIn = getAssetFromResource("/com/ustadmobile/core/h5p/dist.zip", context, this::class)
                    ?: throw IllegalStateException("Could not find h5p dist file")
            h5pDistIn.writeToFile(h5pDistTmpFile)
            repo.addEntriesToContainerFromZip(container.containerUid, h5pDistTmpFile.toDoorUri(),
                    containerAddOptions, context)
            h5pDistTmpFile.delete()


            // generate tincan.xml
            val tinCan = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tincan xmlns="http://projecttincan.com/tincan.xsd">
                <activities>
                    <activity id="${entry?.entryId ?: ""}" type="http://adlnet.gov/expapi/activities/module">
                        <name>${entry?.title ?: ""}</name>
                        <description lang="en-US">${entry?.description ?: ""}</description>
                        <launch lang="en-us">index.html</launch>
                    </activity>
                </activities>
            </tincan>
        """.trimIndent()

            val tmpTinCanFile = File.createTempFile("h5p-tincan", "xml")
            tmpTinCanFile.writeText(tinCan)
            repo.addFileToContainer(container.containerUid, tmpTinCanFile.toDoorUri(),
                    "tincan.xml", context, di, containerAddOptions)
            tmpTinCanFile.delete()


            // generate index.html
            val index = """
            <html>
            <head>
                <meta charset="utf-8" />
                <script type="text/javascript" src="dist/main.bundle.js"></script>
            </head>
            <body>
            <div id="h5p-container" data-workspace="workspace"></div>
            </body>
            </html>
        """.trimIndent()
            val tmpIndexHtmlFile = File.createTempFile("h5p-index", "html")
            tmpIndexHtmlFile.writeText(index)
            repo.addFileToContainer(container.containerUid, tmpIndexHtmlFile.toDoorUri(),
                    "index.html", context, di, containerAddOptions)
            tmpIndexHtmlFile.delete()

            repo.addTorrentFileFromContainer(container.containerUid,
                    DoorUri.parse(torrentDir.toURI().toString()),
                    tracker.announceUrl)

            ustadTorrentManager.addTorrent(container.containerUid)

            repo.containerDao.findByUid(container.containerUid)

        }
        return ProcessResult(200)
    }

    companion object {

        private const val H5P_PATH = "h5p.json"

        const val PLUGIN_ID = 3
    }
}
