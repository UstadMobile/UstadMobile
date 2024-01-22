package com.ustadmobile.core.contentformats.pdf

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.getLocalUriIfRemote
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.displayFilename
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.libcache.UstadCache
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase,
    json: Json,
) : AbstractPdfContentImportCommonJvm(
    endpoint = endpoint,
    cache = cache,
    uriHelper = uriHelper,
    db = db,
    saveLocalUriAsBlobAndManifestUseCase = saveLocalUriAsBlobAndManifestUseCase,
    getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
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
            val localUri = getStoragePathForUrlUseCase.getLocalUriIfRemote(uri)
            val pdfFile = localUri.toFile()

            val pdfPDDocument: PDDocument = Loader.loadPDF(pdfFile)

            val metadataTitle = pdfPDDocument.documentInformation.title
            val entry = ContentEntryWithLanguage().apply {
                title = if(!metadataTitle.isNullOrBlank()) {
                    metadataTitle
                }else {
                    originalFilename?.displayFilename() ?: uri.toString().displayFilename()
                }

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
            Napier.w(throwable = e) { "PdfContentImporterJvm: error importing $uri" }
            throw InvalidContentException("Invalid PDF: ${e.message}", e)
        }
    }

}