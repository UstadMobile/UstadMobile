package com.ustadmobile.core.contentformats.pdf

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.contentjob.SupportedContent
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.storeText
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.contententry.ContentConstants.MANIFEST_NAME
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.libcache.UstadCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.asInputStream
import kotlinx.serialization.json.Json
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument

class PdfContentImporterJvm(
    endpoint: Endpoint,
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val db: UmAppDatabase,
    private val saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
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

        val manifestEntriesAndBlobs = saveLocalUriAsBlobAndManifestUseCase(
            listOf(
                SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                    blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                        localUri = jobUri.toString(),
                        entityUid = contentEntryVersionUid,
                        tableId = ContentEntryVersion.TABLE_ID,
                        mimeType = "application/pdf",
                    ),
                    manifestUri = "content.pdf",
                )
            )
        )

        val contentEntryVersion = ContentEntryVersion(
            cevUid = contentEntryVersionUid,
            cevContentType = ContentEntryVersion.TYPE_PDF,
            cevContentEntryUid = jobItem.cjiContentEntryUid,
            cevSitemapUrl = manifestUrl,
            cevUrl = pdfUrl,
        )

        val manifest = ContentManifest(
            version = 1,
            metadata = emptyMap(),
            entries = manifestEntriesAndBlobs.map { it.manifestEntry }
        )

        cache.storeText(
            url = manifestUrl,
            text = json.encodeToString(ContentManifest.serializer(), manifest),
            mimeType = "application/json"
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