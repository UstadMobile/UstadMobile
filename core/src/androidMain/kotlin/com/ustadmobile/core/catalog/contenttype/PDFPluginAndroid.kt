package com.ustadmobile.core.catalog.contenttype

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.linkedin.android.litr.MediaTransformer
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addContainerFromUri
import com.ustadmobile.core.io.ext.deleteRecursively
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.core.io.ext.makeTempDir
import com.ustadmobile.core.network.NetworkProgressListenerAdapter
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.updateTotalFromContainerSize
import com.ustadmobile.core.util.ext.updateTotalFromLocalUriIfNeeded
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import io.ktor.client.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File


class PDFPluginAndroid(
        private var context: Any,
        private val endpoint: Endpoint,
        override val di: DI,
        private val uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
) : PDFTypePlugin() {

    private val PDF_ANDROID = "PDFAndroid"

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val defaultContainerDir: File by di.on(endpoint).instance(
        tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val httpClient: HttpClient = di.direct.instance()

    override suspend fun extractMetadata(
        uri: DoorUri,
        process: ContentJobProcessContext
    ): MetadataResult? {
        return getEntry(uri, process)
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        jobProgress: ContentJobProgressListener
    ): ProcessResult {
        val contentJobItem = jobItem.contentJobItem
                ?: throw IllegalArgumentException("missing job item")
        return withContext(Dispatchers.Default) {
            val uri = contentJobItem.sourceUri ?: throw IllegalStateException("missing uri")
            val pdfUri = DoorUri.parse(uri)
            val contentNeedUpload = !pdfUri.isRemote()
            val localUri = process.getLocalOrCachedUri()
            contentJobItem.updateTotalFromLocalUriIfNeeded(localUri, contentNeedUpload,
                jobProgress, context, di)

            val pdfTempDir = makeTempDir(prefix = "tmp")
            val newPDF = File(pdfTempDir,
                localUri.getFileName(context))
            val params: Map<String, String> = safeParse(di, MapSerializer(String.serializer(), String.serializer()),
                jobItem.contentJob?.params ?: "")
            val mediaTransformer = MediaTransformer(context as Context)
            val pdfIsProcessed = contentJobItem.cjiContainerUid != 0L

            try {

                if(!pdfIsProcessed) {


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

                    repo.addContainerFromUri(container.containerUid, localUri, context, di,
                        localUri.getFileName(context),
                        ContainerAddOptions(containerFolderUri))

                    contentJobItem.updateTotalFromContainerSize(contentNeedUpload, db,
                        jobProgress)

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
                        NetworkProgressListenerAdapter(jobProgress, contentJobItem),
                        httpClient, endpoint, process)
                    )
                }

                return@withContext ProcessResult(JobStatus.COMPLETE)
            }catch(c: CancellationException){

                withContext(NonCancellable){
                    newPDF.delete()
                    pdfTempDir.deleteRecursively()
                    if(pdfUri.isRemote()){
                        localUri.toFile().delete()
                    }
                }

                throw c
            }finally {
                pdfTempDir.deleteRecursively()
                mediaTransformer.release()
            }
        }
    }

    private suspend fun getEntry(doorUri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        return withContext(Dispatchers.Default) {

            val localUri: DoorUri = process.getLocalOrCachedUri()

            val fileName: String = localUri.getFileName(context)

            if(!supportedFileExtensions.any { fileName.endsWith(it, true) }) {
                return@withContext null
            }

            val fileDescriptor: ParcelFileDescriptor? =
                (context as Context).contentResolver.openFileDescriptor(localUri.uri,"r")
            if(fileDescriptor != null){
                val renderer = PdfRenderer(fileDescriptor)
                if(renderer.pageCount < 1){
                    return@withContext null
                }
            }else{
                return@withContext null
            }



            val entry = ContentEntryWithLanguage().apply {
                this.title = fileName

                this.leaf = true
                this.sourceUrl = doorUri.uri.toString()
                this.contentTypeFlag = ContentEntry.TYPE_PDF
            }
            MetadataResult(entry, pluginId)
        }
    }

}