package com.ustadmobile.lib.rest.domain.contententry.getmetadatafromuri

import com.ustadmobile.core.contentjob.ContentImportersManager
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.IContentEntryGetMetaDataFromUriUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadResponse
import com.ustadmobile.core.domain.upload.ChunkedUploadServerUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadServerUseCaseJvm
import com.ustadmobile.core.util.ext.firstCaseInsensitiveOrNull
import com.ustadmobile.door.ext.toDoorUri
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Use case which handles receiving content uploads from the web (JS) client. It uses a
 * ChunkedUploadServerUseCase to receive and append ChunkedUploads.
 * ContentEntryGetMetadataServerUseCase will then use the ContentImportersManager to extract metadata
 * from the uploaded content.
 *
 */
class ContentEntryGetMetadataServerUseCase(
    uploadDir: File,
    private val importersManager: ContentImportersManager,
    private val json: Json,
) {

    val chunkedUploadServerUseCase: ChunkedUploadServerUseCase = ChunkedUploadServerUseCaseJvm(
        uploadDir = uploadDir,
        onUploadComplete = { completedUpload ->
            val originalFilenameParam: String? = completedUpload.request.headers
                .firstCaseInsensitiveOrNull(IContentEntryGetMetaDataFromUriUseCase.HEADER_ORIGINAL_FILENAME)

            val completedUploadFile = File(completedUpload.path.toString())

            try {
                val metadataResult = importersManager.extractMetadata(
                    uri = completedUploadFile.toDoorUri(),
                    originalFilename = originalFilenameParam,
                )?.let {
                    //Ensure the original filename is preserved, even if not handled by the plugin
                    if(it.originalFilename == null && originalFilenameParam != null)
                        it.copy(originalFilename = originalFilenameParam)
                    else
                        it
                }

                if(metadataResult != null) {
                    ChunkedUploadResponse(
                        statusCode = 200,
                        body = json.encodeToString(
                            MetadataResult.serializer(),
                            metadataResult,
                        ),
                        contentType = "application/json",
                        headers = emptyMap(),
                    )
                }else {
                    ChunkedUploadResponse(
                        statusCode = HttpStatusCode.NotAcceptable.value,
                        body = importersManager.supportedFormatNames().joinToString(),
                        contentType = "text/plain",
                        headers = emptyMap()
                    )
                }
            }catch(e: InvalidContentException) {
                ChunkedUploadResponse(
                    statusCode = 400,
                    body = e.message ?: "",
                    contentType = "text/plain",
                    headers = emptyMap()
                )
            }
        }
    )

}