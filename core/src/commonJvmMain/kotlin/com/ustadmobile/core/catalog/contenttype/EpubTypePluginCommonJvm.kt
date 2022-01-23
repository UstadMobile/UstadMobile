package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.network.NetworkProgressListenerAdapter
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.alternative
import com.ustadmobile.core.util.ext.updateTotalFromContainerSize
import com.ustadmobile.core.util.ext.updateTotalFromLocalUriIfNeeded
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.openInputStream
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.direct
import org.kodein.di.on
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.util.*
import io.ktor.client.*
import java.util.zip.ZipInputStream
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException

class EpubTypePluginCommonJvm(
        private var context: Any,
        private val endpoint: Endpoint,
        override val di: DI,
        private val uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
) : ContentPlugin {

    val viewName: String
        get() = EpubContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = SupportedContent.EPUB_MIME_TYPES

    override val supportedFileExtensions: List<String>
        get() = SupportedContent.EPUB_EXTENSIONS

    override val pluginId: Int
        get() = PLUGIN_ID

    private val httpClient: HttpClient = di.direct.instance()

    private val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
            val mimeType = uri.guessMimeType(context, di)
            if(mimeType != null && !supportedMimeTypes.contains(mimeType)){
                return null
            }
            return withContext(Dispatchers.Default) {
                val xppFactory = XmlPullParserFactory.newInstance()
                try {
                    val localUri = process.getLocalOrCachedUri()
                    val opfPath = ZipInputStream(localUri.openInputStream(context)).use {
                        it.skipToEntry { entry -> entry.name == OCF_CONTAINER_PATH } ?: return@use null

                        val ocfContainer = OcfDocument()
                        val xpp = xppFactory.newPullParser()
                        xpp.setInput(it, "UTF-8")
                        ocfContainer.loadFromParser(xpp)

                        return@use ocfContainer.rootFiles.firstOrNull()?.fullPath
                    } ?: return@withContext null

                    return@withContext  ZipInputStream(localUri.openInputStream(context)).use {

                        it.skipToEntry { it.name == opfPath } ?: return@use null

                        val xpp = xppFactory.newPullParser()
                        xpp.setInput(it, "UTF-8")
                        val opfDocument = OpfDocument()
                        opfDocument.loadFromOPF(xpp)

                        val entry = ContentEntryWithLanguage().apply {
                            contentFlags = ContentEntry.FLAG_IMPORTED
                            contentTypeFlag = ContentEntry.TYPE_EBOOK
                            licenseType = ContentEntry.LICENSE_TYPE_OTHER
                            title = if (opfDocument.title.isNullOrEmpty()) localUri.getFileName(context)
                            else opfDocument.title
                            author = opfDocument.getCreator(0)?.creator
                            description = opfDocument.description
                            leaf = true
                            sourceUrl = uri.uri.toString()
                            entryId = opfDocument.id.alternative(UUID.randomUUID().toString())
                            val languageCode = opfDocument.getLanguage(0)
                            if (languageCode != null) {
                                this.language = Language().apply {
                                    iso_639_1_standard = languageCode
                                }
                            }
                        }
                        return@use MetadataResult(entry, PLUGIN_ID)
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }

        override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ContentJobProcessContext, progress: ContentJobProgressListener): ProcessResult {
            val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")
            val jobUri = contentJobItem.sourceUri ?: return ProcessResult(JobStatus.FAILED)
            return withContext(Dispatchers.Default) {

                try {
                    val uri = DoorUri.parse(jobUri)

                    val contentNeedUpload = !uri.isRemote()
                    val localUri = process.getLocalOrCachedUri()

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

                        contentJobItem.cjiContainerUid = container.containerUid
                        db.contentJobItemDao.updateContentJobItemContainer(contentJobItem.cjiUid,
                                container.containerUid)

                        repo.addEntriesToContainerFromZip(container.containerUid,
                                localUri,
                                ContainerAddOptions(storageDirUri = containerFolderUri), context)

                        contentJobItem.updateTotalFromContainerSize(contentNeedUpload, db,
                            progress)

                        db.contentJobItemDao.updateContainerProcessed(contentJobItem.cjiUid, true)
                    }

                    if(contentNeedUpload) {
                        val haveConnectivityToContinueJob = db.contentJobDao
                            .isConnectivityAcceptableForJob(jobItem.contentJob?.cjUid ?: 0)

                        if (!haveConnectivityToContinueJob) {
                            return@withContext ProcessResult(JobStatus.WAITING_FOR_CONNECTION)
                        }

                        db.contentJobItemDao.updateConnectivityNeeded(contentJobItem.cjiUid, true)

                        val progressListenerAdapter = NetworkProgressListenerAdapter(progress,
                            contentJobItem)
                        return@withContext ProcessResult(uploader.upload(
                                contentJobItem, progressListenerAdapter, httpClient, endpoint
                        ))
                    }

                    return@withContext ProcessResult(JobStatus.COMPLETE)
                }catch (c: CancellationException){
                    throw c
                }
            }
        }


        suspend fun findOpfPath(uri: DoorUri, process: ContentJobProcessContext): String? {
            val xppFactory = XmlPullParserFactory.newInstance()
            try {

                val localUri = process.getLocalOrCachedUri()

                ZipInputStream(localUri.openInputStream(context)).use {
                    it.skipToEntry { entry -> entry.name == OCF_CONTAINER_PATH } ?: return null

                    val ocfContainer = OcfDocument()
                    val xpp = xppFactory.newPullParser()
                    xpp.setInput(it, "UTF-8")
                    ocfContainer.loadFromParser(xpp)

                    return ocfContainer.rootFiles.firstOrNull()?.fullPath
                }
            } catch (e: Exception) {
                return null
            }
        }

        companion object {

        private const val OCF_CONTAINER_PATH = "META-INF/container.xml"

        const val PLUGIN_ID = 2
    }
}
