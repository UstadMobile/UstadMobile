package com.ustadmobile.core.catalog.contenttype

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerBuilder
import com.ustadmobile.core.io.ext.addUri
import com.ustadmobile.core.io.ext.build
import com.ustadmobile.core.io.ext.containerBuilder
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

/**
 * Android implementation for PDF plugin. It currently does not really extract metadata. It will
 * use the filename as the title. It will use Android's PDF image library to check that the file
 * provided is indeed a valid PDF.
 *
 */
class PDFPluginAndroid(
        context: Any,
        endpoint: Endpoint,
        override val di: DI,
        uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
) : ContentImportContentPlugin(
    endpoint, context, uploader
), PDFTypePlugin {

    override val pluginId: Int
        get() = PDFTypePlugin.PLUGIN_ID

    override val supportedMimeTypes: List<String>
        get() = PDFTypePlugin.PDF_MIME_MAP.keys.toList()

    override val supportedFileExtensions: List<String>
        get() = PDFTypePlugin.PDF_EXT_LIST.map { it.removePrefix(".") }

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    override suspend fun extractMetadata(
        uri: DoorUri,
        process: ContentJobProcessContext
    ): MetadataResult? {
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
                this.sourceUrl = uri.uri.toString()
                this.contentTypeFlag = ContentEntry.TYPE_PDF
            }
            MetadataResult(entry, pluginId)
        }
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

}