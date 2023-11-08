package com.ustadmobile.core.contentformats.pdf

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.AbstractContentImportPlugin
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.ContentImporterUploader
import com.ustadmobile.core.contentjob.DefaultContentPluginUploader
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.contentjob.SupportedContent
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpPathResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.asInputStream
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.util.UUID

class PdfContentImporterJvm(
    endpoint: Endpoint,
    override val di: DI,
    private val cache: UstadCache,
    uriHelper: UriHelper,
    private val fileSystem: FileSystem = SystemFileSystem,
    uploader: ContentImporterUploader = DefaultContentPluginUploader(di),
) : AbstractContentImportPlugin(endpoint, uploader, uriHelper){

    override suspend fun addToCache(
        jobItem: ContentJobItemAndContentJob,
        progressListener: ContentJobProgressListener
    ): ContentEntryVersion = withContext(Dispatchers.IO) {
        val jobUri = jobItem.contentJobItem.requireSourceAsDoorUri()
        val db: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)
        val pdfUrl = "${urlPrefix}content.pdf"

        val tmpFile = File.createTempFile(
            UUID.randomUUID().toString(),
            ".pdf"
        )

        val tmpFilePath = Path(tmpFile.absolutePath)

        fileSystem.sink(tmpFilePath).use { fileSink ->
            uriHelper.openSource(jobUri).transferTo(fileSink)
        }

        val contentEntryVersion = ContentEntryVersion(
            cevUid = contentEntryVersionUid,
            cevContentType = ContentEntryVersion.TYPE_PDF,
            cevContentEntryUid = jobItem.contentJobItem?.cjiContentEntryUid ?: 0L,
            cevUrl = pdfUrl,
        )

        val request = requestBuilder {
            url = pdfUrl
        }

        cache.store(
            storeRequest = listOf(
                CacheEntryToStore(
                    request = request,
                    response = HttpPathResponse(
                        path = tmpFilePath,
                        fileSystem = fileSystem,
                        mimeType = "application/pdf",
                        request = request,
                    )
                )
            )
        )

        contentEntryVersion
    }

    override val importerId: Int = PLUGINID
    override val supportedMimeTypes: List<String>
        get() = listOf("application/pdf")
    override val supportedFileExtensions: List<String>
        get() = SupportedContent.PDF_EXTENSIONS

    @Suppress("NewApi") //This is JVM only, warning is wrong
    override suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String?,
    ): MetadataResult? {
        val isProbablyPdf = originalFilename?.substringAfterLast(".")?.lowercase()?.endsWith("pdf") == true
                || uriHelper.getMimeType(uri) == "application/pdf"
        if(!isProbablyPdf)
            return null

        val pdfPDDocument: PDDocument = uriHelper.openSource(uri).asInputStream().use { inStream ->
            Loader.loadPDF(inStream.readAllBytes())
        }

        val entry = ContentEntryWithLanguage().apply {
            title = pdfPDDocument.documentInformation.title ?: originalFilename
            author = pdfPDDocument.documentInformation.author
            leaf = true
            sourceUrl = uri.toString()
            contentTypeFlag = ContentEntry.TYPE_DOCUMENT
        }

        return MetadataResult(
            entry = entry,
            importerId = importerId,
            originalFilename = originalFilename,
        )
    }

    companion object {

        const val PLUGINID = 111

    }
}