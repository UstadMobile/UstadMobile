package com.ustadmobile.core.contentformats.pdf

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.libcache.UstadCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.asInputStream
import kotlinx.serialization.json.Json
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument

/**
 * For PDF on JVM view: maybe: https://github.com/pcorless/icepdf
 */
class PdfContentImporterJvm(
    endpoint: Endpoint,
    cache: UstadCache,
    uriHelper: UriHelper,
    db: UmAppDatabase,
    saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
    json: Json,
) : AbstractPdfContentImportCommonJvm(
    endpoint = endpoint,
    cache = cache,
    uriHelper = uriHelper,
    db = db,
    saveLocalUriAsBlobAndManifestUseCase = saveLocalUriAsBlobAndManifestUseCase,
    json = json,
) {

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

}