package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.core.io.ext.guessMimeType
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.core.network.NetworkProgressListenerAdapter
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ShrinkUtils
import com.ustadmobile.core.util.ext.fitWithin
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.updateTotalFromContainerSize
import com.ustadmobile.core.util.ext.updateTotalFromLocalUriIfNeeded
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File

class PDFTypePluginJvm(
        private var context: Any,
        private val endpoint: Endpoint,
        override val di: DI,
        private val uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
): PDFTypePlugin() {

    private val PDF_JVM = "PDF_JVM"

    private val httpClient: HttpClient = di.direct.instance()

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val ffmpegFile: File by di.instance(tag = DiTag.TAG_FILE_FFMPEG)

    private val ffprobeFile: File by di.instance(tag = DiTag.TAG_FILE_FFPROBE)

    override suspend fun extractMetadata(
        uri: DoorUri,
        process: ContentJobProcessContext
    ): MetadataResult? {
        return getEntry(uri, process)
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progress: ContentJobProgressListener
    ): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")
        return withContext(Dispatchers.Default) {
            val uri = contentJobItem.sourceUri ?: throw IllegalStateException("missing uri")

            val pdfUri = DoorUri.parse(uri)
            val localPDFUri = process.getLocalOrCachedUri()
            val pdfFile = localPDFUri.toFile()
            var pathInContainer = if(!pdfUri.isRemote()) {
                pdfFile.name
            }else {
                val extension = pdfUri.guessMimeType(context, di)?.let { mimeType ->
                    PDF_MIME_MAP[mimeType]
                } ?: throw IllegalArgumentException("Unknown mime type for $pdfUri")

                pdfUri.getFileName(context).requirePostfix(extension)
            }

            var pdfFileToAddToContainer = pdfFile
            val contentNeedUpload = !pdfUri.isRemote()
            contentJobItem.updateTotalFromLocalUriIfNeeded(localPDFUri, contentNeedUpload,
                progress, context, di)

            try {

                if(!contentJobItem.cjiContainerProcessed) {


                    val container = db.containerDao.findByUid(contentJobItem.cjiContainerUid)
                        ?: Container().apply {
                            containerContentEntryUid = contentJobItem.cjiContentEntryUid
                            cntLastModified = System.currentTimeMillis()
                            mimeType = supportedMimeTypes.first()
                            containerUid = repo.containerDao.insertAsync(this)

                        }

                    contentJobItem.cjiContainerUid = container.containerUid
                    process.withContentJobItemTransactionMutex { txDb ->
                        txDb.contentJobItemDao.updateContentJobItemContainer(contentJobItem.cjiUid,
                            container.containerUid)
                    }

                    val containerFolder = jobItem.contentJob?.toUri
                        ?: defaultContainerDir.toURI().toString()
                    val containerFolderUri = DoorUri.parse(containerFolder)

                    repo.addFileToContainer(container.containerUid, pdfFileToAddToContainer.toDoorUri(),
                        pathInContainer, context, di,
                        ContainerAddOptions(containerFolderUri, compressionFilter = ContainerAddOptions.NEVER_COMPRESS_FILTER)
                    )

                    contentJobItem.updateTotalFromContainerSize(contentNeedUpload, db,
                        progress)

                    val haveConnectivityToContinueJob = process.withContentJobItemTransactionMutex { txDb ->
                        txDb.contentJobItemDao.updateContainerProcessed(contentJobItem.cjiUid, true)

                        contentJobItem.cjiConnectivityNeeded = true
                        txDb.contentJobItemDao.updateConnectivityNeeded(contentJobItem.cjiUid, true)

                        txDb.contentJobDao.isConnectivityAcceptableForJob(jobItem.contentJob?.cjUid
                            ?: 0)
                    }

                    if (!haveConnectivityToContinueJob) {
                        return@withContext ProcessResult(JobStatus.WAITING_FOR_CONNECTION)
                    }
                }


                if(contentNeedUpload) {
                    return@withContext ProcessResult(uploader.upload(contentJobItem,
                        NetworkProgressListenerAdapter(progress, contentJobItem), httpClient,
                        endpoint, process
                    ))
                }

                return@withContext ProcessResult(JobStatus.COMPLETE)
            }catch (c: CancellationException){

                withContext(NonCancellable){
                    pdfFile.delete()
                    pdfFileToAddToContainer.delete()

                }

                throw c

            }


        }
    }

    suspend fun getEntry(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        return withContext(Dispatchers.Default){

            val localUri = process.getLocalOrCachedUri()

            val fileName = uri.getFileName(context)

            if(!supportedFileExtensions.any { fileName.endsWith(it, true) }) {
                return@withContext null
            }

            val file = localUri.toFile()

            try {
                val pdfPDDocument: PDDocument = Loader.loadPDF(file) ?: return@withContext null
                val entry = ContentEntryWithLanguage().apply {
                    if(pdfPDDocument.documentInformation.title.isEmpty()){
                        this.title = fileName
                    }else {
                        this.title = pdfPDDocument.documentInformation.title
                    }
                    this.author = pdfPDDocument.documentInformation.author
                    this.publisher = pdfPDDocument.documentInformation.producer

                    this.leaf = true
                    sourceUrl = uri.uri.toString()
                    this.contentTypeFlag = ContentEntry.TYPE_PDF
                }
                MetadataResult(entry, PLUGIN_ID)

            }catch (e: Exception){
                return@withContext null
            }

        }
    }

}