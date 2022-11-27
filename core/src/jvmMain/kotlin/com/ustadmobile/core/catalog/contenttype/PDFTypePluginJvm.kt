package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerBuilder
import com.ustadmobile.core.io.ext.*
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

/**
 * PDF Import plugin for JVM. Uses PDFBox.
 */
class PDFTypePluginJvm(
        context: Any,
        endpoint: Endpoint,
        override val di: DI,
        uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
): ContentImportContentPlugin(
    endpoint, context, uploader
), PDFTypePlugin {

    private val PDF_JVM = "PDF_JVM"

    override val pluginId: Int
        get() = PDFTypePlugin.PLUGIN_ID

    override val supportedMimeTypes: List<String>
        get() = PDFTypePlugin.PDF_MIME_MAP.keys.toList()

    override val supportedFileExtensions: List<String>
        get() = PDFTypePlugin.PDF_EXT_LIST.map { it.removePrefix(".") }

    private val httpClient: HttpClient = di.direct.instance()

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    override suspend fun extractMetadata(
        uri: DoorUri,
        process: ContentJobProcessContext
    ): MetadataResult? {
        return getEntry(uri, process)
    }


    override suspend fun makeContainer(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progressListener: ContentJobProgressListener,
        containerStorageUri: DoorUri
    ): Container {
        val localUri = process.getLocalOrCachedUri()
        val fileName = jobItem.contentJobItem?.sourceUri?.let { DoorUri.parse(it) }
            ?.getFileName(context) ?: localUri.getFileName(context)

        return repo.containerBuilder(jobItem.contentJobItem?.cjiContentEntryUid ?: 0,
            supportedMimeTypes.first(), containerStorageUri)
            .addUri(fileName, localUri, context, ContainerBuilder.Compression.NONE)
            .build()
    }

    override suspend fun shouldUpload() = false

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
                    title = pdfPDDocument.documentInformation.title.let {
                        if(!it.isNullOrBlank()) it else fileName
                    }

                    this.author = pdfPDDocument.documentInformation.author
                    this.publisher = pdfPDDocument.documentInformation.producer

                    this.leaf = true
                    sourceUrl = uri.uri.toString()
                    this.contentTypeFlag = ContentEntry.TYPE_PDF
                }
                MetadataResult(entry, pluginId)

            }catch (e: Exception){
                return@withContext null
            }

        }
    }
}