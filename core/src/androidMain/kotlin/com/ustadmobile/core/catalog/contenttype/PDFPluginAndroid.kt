package com.ustadmobile.core.catalog.contenttype

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import io.ktor.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            //TODO

            return@withContext ProcessResult(JobStatus.COMPLETE)
        }
    }

    private suspend fun getEntry(doorUri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        return withContext(Dispatchers.Default) {

            val localUri = process.getLocalOrCachedUri()

            val fileName: String = localUri.getFileName(context)

            if(!supportedFileExtensions.any { fileName.endsWith(it, true) }) {
                return@withContext null
            }


            //TODO: Get page count to validate that it is a PDF.

            val pdfDescriptor = ParcelFileDescriptor.open(
                File(fileName),
                ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pdfDescriptor)
            if(renderer.pageCount < 1){
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