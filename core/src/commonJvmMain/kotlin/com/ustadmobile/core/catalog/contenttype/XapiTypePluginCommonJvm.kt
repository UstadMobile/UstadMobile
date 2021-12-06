package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.tincan.TinCanXML
import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.openInputStream
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.direct
import org.kodein.di.on
import io.ktor.client.*
import org.xmlpull.v1.XmlPullParserFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException

class XapiTypePluginCommonJvm(
        private var context: Any,
        private val endpoint: Endpoint,
        override val di: DI,
        private val uploader: ContentPluginUploader = DefaultContentPluginUploader()
) : ContentPlugin {

    val viewName: String
        get() = XapiPackageContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = SupportedContent.XAPI_MIME_TYPES

    override val supportedFileExtensions: List<String>
        get() = SupportedContent.ZIP_EXTENSIONS


    override val pluginId: Int
        get() = PLUGIN_ID


    private val httpClient: HttpClient = di.direct.instance()

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        val mimeType = uri.guessMimeType(context, di)
        if (mimeType != null && !supportedMimeTypes.contains(mimeType)) {
            return null
        }
        return withContext(Dispatchers.Default) {
            val localUri = process.getLocalOrCachedUri()
            val inputStream = localUri.openInputStream(context)
            return@withContext ZipInputStream(inputStream).use {
                it.skipToEntry { it.name == TINCAN_FILENAME } ?: return@withContext null

                val xppFactory = XmlPullParserFactory.newInstance()
                val xpp = xppFactory.newPullParser()
                xpp.setInput(it, "UTF-8")
                val activity = TinCanXML.loadFromXML(xpp).launchActivity
                        ?: throw IOException("TinCanXml from name has no launchActivity!")

                val entry = ContentEntryWithLanguage().apply {
                    contentFlags = ContentEntry.FLAG_IMPORTED
                    licenseType = ContentEntry.LICENSE_TYPE_OTHER
                    title = if (activity.name.isNullOrEmpty())
                        uri.getFileName(context) else activity.name
                    contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                    description = activity.desc
                    leaf = true
                    entryId = activity.id
                    sourceUrl = uri.uri.toString()
                }
                MetadataResult(entry, PLUGIN_ID)
            }
        }
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ContentJobProcessContext, progress: ContentJobProgressListener): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("mising job item")
        val uri = contentJobItem.sourceUri ?: return ProcessResult(JobStatus.FAILED)
        return withContext(Dispatchers.Default) {

            try {

                val doorUri = DoorUri.parse(uri)
                val localUri = process.getLocalOrCachedUri()
                val contentNeedUpload = !doorUri.isRemote()
                val progressSize = if (contentNeedUpload) 2 else 1
                val xapiIsProcessed = contentJobItem.cjiContainerUid != 0L

                if(!xapiIsProcessed) {

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

                    repo.addEntriesToContainerFromZip(container.containerUid,
                            localUri,
                            ContainerAddOptions(storageDirUri = containerFolderUri), context)


                    val containerUidFolder = File(containerFolderUri.toFile(), container.containerUid.toString())
                    containerUidFolder.mkdirs()

                    contentJobItem.cjiContainerUid = container.containerUid
                    db.contentJobItemDao.updateContentJobItemContainer(contentJobItem.cjiUid, container.containerUid)

                    contentJobItem.cjiConnectivityNeeded = true
                    db.contentJobItemDao.updateConnectivityNeeded(contentJobItem.cjiUid, true)

                    val haveConnectivityToContinueJob = db.contentJobDao.isConnectivityAcceptableForJob(jobItem.contentJob?.cjUid
                            ?: 0)
                    if (!haveConnectivityToContinueJob) {
                        return@withContext ProcessResult(JobStatus.QUEUED)
                    }
                }

                contentJobItem.cjiItemProgress = contentJobItem.cjiItemTotal / progressSize
                progress.onProgress(contentJobItem)

                if(contentNeedUpload) {
                    uploader.upload(
                            contentJobItem, progress, httpClient, endpoint)
                }

                return@withContext ProcessResult(JobStatus.COMPLETE)

            }catch (c: CancellationException){
                throw c
            }
        }
    }

    companion object {

        const val TINCAN_FILENAME = "tincan.xml"

        const val PLUGIN_ID = 8

    }
}