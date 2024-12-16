package com.ustadmobile.core.contentformats.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.net.toUri
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.fileExtensionOrNull
import com.ustadmobile.core.util.ext.removeFileExtension
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryPicture2
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.libcache.UstadCache
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.asInputStream
import kotlinx.serialization.json.Json
import java.io.File

class PdfContentImporterAndroid(
    learningSpace: LearningSpace,
    cache: UstadCache,
    uriHelper: UriHelper,
    db: UmAppDatabase,
    saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
    getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase,
    json: Json,
    private val appContext: Context,
    private val tmpDir: File,
    private val saveLocalUriAsBlobUseCase: SaveLocalUrisAsBlobsUseCase,
): AbstractPdfContentImportCommonJvm(
    learningSpace = learningSpace,
    cache = cache,
    uriHelper = uriHelper,
    db = db,
    saveLocalUriAsBlobAndManifestUseCase = saveLocalUriAsBlobAndManifestUseCase,
    getStoragePathForUrlUseCase =  getStoragePathForUrlUseCase,
    json = json,
    compressPdfUseCase = null,
) {

    override suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String?
    ): MetadataResult? = withContext(Dispatchers.IO) {
        val uriMimeType = uriHelper.getMimeType(uri)
        if(uriMimeType?.lowercase() != "application/pdf" &&
            originalFilename?.fileExtensionOrNull() != "pdf"
        ) {
            return@withContext null
        }


        val tmpFile = File(tmpDir, "${systemTimeInMillis()}-tmp.pdf")
        tmpDir.takeIf { !it.exists() }?.mkdirs()

        val localUri = if(uri.uri.scheme == "file") {
            uri
        }else {
            uriHelper.openSource(uri).asInputStream().use { inStream ->
                tmpFile.outputStream().use { fileOut ->
                    inStream.copyTo(fileOut)
                    fileOut.flush()

                    tmpFile.toDoorUri()
                }
            }
        }


        val fileDescriptor: ParcelFileDescriptor? =
            appContext.contentResolver.openFileDescriptor(localUri.uri,"r")

        try {
            if(fileDescriptor != null) {
                @Suppress("DEPRECATION")
                val firstPageImageUri = PdfRenderer(fileDescriptor).use { pdfRenderer ->
                    if(pdfRenderer.pageCount > 0) {
                        pdfRenderer.openPage(0).use { page ->
                            val bitmap = Bitmap.createBitmap(page.width ,page.height,
                                Bitmap.Config.ARGB_8888)
                            page.render(bitmap, null, null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            val firstPageImgTmpFile = File(tmpDir, randomUuidAsString() + ".webp")
                            firstPageImgTmpFile.outputStream().use { fileOut ->
                                bitmap.compress(
                                    if(Build.VERSION.SDK_INT >= 30) {
                                        Bitmap.CompressFormat.WEBP_LOSSY
                                    }else {
                                        Bitmap.CompressFormat.WEBP
                                    }, 80, fileOut
                                )
                                fileOut.flush()
                            }

                            saveLocalUriAsBlobUseCase(
                                listOf(
                                    SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                                        localUri = firstPageImgTmpFile.toUri().toString(),
                                    )
                                )
                            ).firstOrNull()?.blobUrl
                        }
                    }else {
                        null
                    }
                }

                if(firstPageImageUri != null) {
                    MetadataResult(
                        entry = ContentEntryWithLanguage().apply {
                            title = originalFilename?.removeFileExtension() ?: uri.toString()
                                .substringAfterLast("/").removeFileExtension()
                            leaf = true
                            sourceUrl = uri.toString()
                            contentTypeFlag = ContentEntry.TYPE_DOCUMENT
                        },
                        importerId = importerId,
                        originalFilename = originalFilename,
                        picture = ContentEntryPicture2(
                            cepPictureUri = firstPageImageUri,
                            cepThumbnailUri = firstPageImageUri,
                        )
                    )
                }else {
                    null
                }
            }else {
                null
            }
        }catch(e: Throwable){
            Napier.d(throwable = e) { "Exception: could not check for pdf" }
            throw e
        }finally {
            tmpFile.takeIf { it.exists() }?.delete()
            fileDescriptor?.close()
        }
    }


}