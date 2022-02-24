package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.getAssetFromResource
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.direct
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
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.network.NetworkProgressListenerAdapter
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.updateTotalFromContainerSize
import com.ustadmobile.core.util.ext.updateTotalFromLocalUriIfNeeded
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.lib.db.entities.*
import io.ktor.client.*
import io.ktor.util.*
import kotlinx.serialization.json.*
import java.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException


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

class H5PTypePluginCommonJvm(
        private var context: Any,
        val endpoint: Endpoint,
        override val di: DI,
        private val uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
): ContentPlugin {

        val viewName: String
    get() = XapiPackageContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
    get() = SupportedContent.H5P_MIME_TYPES

    override val supportedFileExtensions: List<String>
    get() = SupportedContent.H5P_EXTENSIONS

    private val MAX_SIZE_LIMIT: Long = 100 * 1024 * 1024

    private val httpClient: HttpClient = di.direct.instance()

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    override val pluginId: Int
        get() = PLUGIN_ID

    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        val size = uri.getSize(context, di)
        if(size > MAX_SIZE_LIMIT){
            return null
        }
        val mimeType = uri.guessMimeType(context, di)
        if(mimeType != null && !supportedMimeTypes.contains(mimeType)){
            return null
        }
        return withContext(Dispatchers.Default) {
            val localUri = process.getLocalOrCachedUri()
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
                    sourceUrl = uri.uri.toString()
                    title = if(json.jsonObject["title"]?.jsonPrimitive?.content.isNullOrEmpty())
                        uri.getFileName(context) else json.jsonObject["title"]?.jsonPrimitive?.content
                    this.author = author
                    leaf = true
                }
                MetadataResult(entry, PLUGIN_ID)
            }
        }
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ContentJobProcessContext, progress: ContentJobProgressListener): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")
        val jobUri = contentJobItem.sourceUri ?: return ProcessResult(JobStatus.FAILED)
        return withContext(Dispatchers.Default) {

            try{

                val doorUri = DoorUri.parse(jobUri)
                val localUri = process.getLocalOrCachedUri()
                val contentNeedUpload = !doorUri.isRemote()
                val progressSize = if(contentNeedUpload) 2 else 1

                contentJobItem.updateTotalFromLocalUriIfNeeded(localUri, contentNeedUpload,
                    progress, context, di)

                if(!contentJobItem.cjiContainerProcessed) {

                    val container = db.containerDao.findByUid(contentJobItem.cjiContainerUid)
                            ?: Container().apply {
                                containerContentEntryUid = contentJobItem.cjiContentEntryUid
                                cntLastModified = System.currentTimeMillis()
                                mimeType = supportedMimeTypes.first()
                                containerUid = repo.containerDao.insertAsync(this)
                            }

                    val containerFolder = jobItem.contentJob?.toUri
                            ?: defaultContainerDir.toURI().toString()
                    val containerFolderUri = DoorUri.parse(containerFolder)
                    val entry = db.contentEntryDao.findByUid(contentJobItem.cjiContentEntryUid)

                    contentJobItem.cjiContainerUid = container.containerUid
                    db.contentJobItemDao.updateContentJobItemContainer(contentJobItem.cjiUid,
                            container.containerUid)

                    val containerAddOptions = ContainerAddOptions(storageDirUri = containerFolderUri)
                    repo.addEntriesToContainerFromZip(container.containerUid, localUri,
                            ContainerAddOptions(
                                storageDirUri = containerFolderUri,
                                fileNamer = PrefixContainerFileNamer("workspace/"),
                                updateContainer = false), context)

                    val h5pDistTmpFile = File.createTempFile("h5p-dist", "zip")
                    val h5pDistIn = getAssetFromResource("/com/ustadmobile/core/h5p/dist.zip", context, this::class)
                            ?: throw IllegalStateException("Could not find h5p dist file")
                    h5pDistIn.writeToFile(h5pDistTmpFile)
                    repo.addEntriesToContainerFromZip(
                            container.containerUid, h5pDistTmpFile.toDoorUri(),
                            ContainerAddOptions(storageDirUri = containerFolderUri,
                                updateContainer = false),
                            context)
                    h5pDistTmpFile.delete()

                    // generate tincan.xml
                    val tinCan = """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <tincan xmlns="http://projecttincan.com/tincan.xsd">
                            <activities>
                                <activity id="${entry?.entryId?.escapeHTML()}" type="http://adlnet.gov/expapi/activities/module">
                                    <name>${entry?.title?.escapeHTML()}</name>
                                    <description lang="en-US">${entry?.description?.escapeHTML()}</description>
                                    <launch lang="en-us">index.html</launch>
                                </activity>
                            </activities>
                        </tincan>
                        """.trimIndent()

                    val tmpTinCanFile = File.createTempFile("h5p-tincan", "xml")
                    tmpTinCanFile.writeText(tinCan)
                    repo.addFileToContainer(container.containerUid, tmpTinCanFile.toDoorUri(),
                            "tincan.xml", context, di,
                            ContainerAddOptions(storageDirUri = containerFolderUri,
                                updateContainer = false)
                    )
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
                            "index.html", context, di,
                        ContainerAddOptions(storageDirUri = containerFolderUri,
                            updateContainer = true)) //Last file, now update container
                    tmpIndexHtmlFile.delete()

                    contentJobItem.updateTotalFromContainerSize(contentNeedUpload, db,
                        progress)

                    db.contentJobItemDao.updateContainerProcessed(contentJobItem.cjiUid, true)

                    contentJobItem.cjiConnectivityNeeded = true
                    db.contentJobItemDao.updateConnectivityNeeded(contentJobItem.cjiUid, true)

                    val haveConnectivityToContinueJob = db.contentJobDao.isConnectivityAcceptableForJob(jobItem.contentJob?.cjUid
                            ?: 0)
                    if (!haveConnectivityToContinueJob) {
                        return@withContext ProcessResult(JobStatus.WAITING_FOR_CONNECTION)
                    }
                }

                contentJobItem.cjiItemProgress = contentJobItem.cjiItemTotal / progressSize
                progress.onProgress(contentJobItem)


                if(contentNeedUpload) {
                    return@withContext ProcessResult(uploader.upload(contentJobItem,
                        NetworkProgressListenerAdapter(progress, contentJobItem),
                        httpClient, endpoint))
                }

                return@withContext ProcessResult(JobStatus.COMPLETE)

            }catch (c: CancellationException){
                throw c
            }
        }
    }

    companion object {

        private const val H5P_PATH = "h5p.json"

        const val PLUGIN_ID = 3
    }
}
