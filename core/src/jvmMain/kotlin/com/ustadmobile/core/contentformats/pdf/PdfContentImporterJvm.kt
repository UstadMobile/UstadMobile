package com.ustadmobile.core.contentformats.pdf

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.contentjob.SupportedContent
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.manifest.ContentManifestEntry
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.contententry.ContentConstants.MANIFEST_NAME
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.asIStringValues
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.core.util.stringvalues.emptyStringValues
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.StringResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.asInputStream
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File
import java.util.UUID

class PdfContentImporterJvm(
    endpoint: Endpoint,
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val fileSystem: FileSystem = SystemFileSystem,
    private val db: UmAppDatabase,
    private val saveLocalUriAsBlobItemUseCase: SaveLocalUrisAsBlobsUseCase,
    private val json: Json,
) : ContentImporter(endpoint){

    override suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener,
    ): ContentEntryVersion = withContext(Dispatchers.IO) {
        val jobUri = jobItem.requireSourceAsDoorUri()

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)
        val pdfUrl = "${urlPrefix}content.pdf"
        val manifestUrl = "${urlPrefix}$MANIFEST_NAME"

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
            cevContentEntryUid = jobItem.cjiContentEntryUid,
            cevSitemapUrl = manifestUrl,
            cevUrl = pdfUrl,
        )

        val savedBlob = saveLocalUriAsBlobItemUseCase(
            listOf(
                SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                    localUri = tmpFile.toURI().toString(),
                )
            )
        ).first()

        val cacheResponse = cache.retrieve(requestBuilder(savedBlob.blobUrl))
            ?: throw IllegalStateException("Cache did not store blob")

        val manifest = ContentManifest(
            version = 1,
            metadata = emptyMap(),
            entries = listOf(
                ContentManifestEntry(
                    uri = "content.pdf",
                    ignoreQueryParams = true,
                    status = 200,
                    method = "GET",
                    integrity = "",
                    requestHeaders = emptyStringValues(),
                    responseHeaders = cacheResponse.headers.asIStringValues(),
                    bodyDataUrl = savedBlob.blobUrl
                )
            )
        )

        val manifestRequest = requestBuilder {
            url = manifestUrl
        }

        cache.store(
            storeRequest = listOf(
                CacheEntryToStore(
                    request = manifestRequest,
                    response = StringResponse(
                        request = manifestRequest,
                        mimeType = "application/json",
                        body = json.encodeToString(ContentManifest.serializer(), manifest),
                        extraHeaders = headersBuilder {
                            header("cache-control", "immutable")
                        }
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

    override val formatName: String
        get() = "PDF"

    @Suppress("NewApi") //This is JVM only, warning is wrong
    override suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String?,
    ): MetadataResult? = withContext(Dispatchers.IO)  {
        val shouldBePdf = originalFilename?.substringAfterLast(".")?.lowercase()?.endsWith("pdf") == true
                || uriHelper.getMimeType(uri) == "application/pdf"
        if(!shouldBePdf)
            return@withContext null

        try {
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

            MetadataResult(
                entry = entry,
                importerId = importerId,
                originalFilename = originalFilename,
            )
        }catch(e: Throwable) {
            throw InvalidContentException("Invalid PDF: ${e.message}", e)
        }
    }

    companion object {

        const val PLUGINID = 111

    }
}