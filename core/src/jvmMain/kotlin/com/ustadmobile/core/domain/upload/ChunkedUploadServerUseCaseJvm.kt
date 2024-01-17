package com.ustadmobile.core.domain.upload

import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * @param uploadDir The directory that will be used for temporary storage of uploads. If the
 *        directory does not exist yet, it will be created.
 *        Partial uploads will always be stored as uploadDir/(upload-uuid).
 *
 * @param onUploadComplete function that will be invoked when all chunks have been received
 *        (e.g. to complete processing of an upload) .
 */
class ChunkedUploadServerUseCaseJvm(
    private val uploadDir: File,
    private val onUploadComplete: suspend (CompletedChunkedUpload) -> ChunkedUploadResponse
): ChunkedUploadServerUseCase {

    @Volatile
    private var uploadDirChecked = false

    override suspend fun onChunkReceived(
        request: ChunkedUploadRequest,
    ): ChunkedUploadResponse {
        try {
            if(!uploadDirChecked) {
                uploadDir.takeIf { !it.exists() }?.mkdirs()
                uploadDirChecked = true
            }

            val uploadUuid = request.headers.entries.firstOrNull {
                it.key.equals(HEADER_UPLOAD_UUID, true)
            }?.value?.firstOrNull()

            val logPrefix = "ChunkedUploadServerUseCaseJvm($uploadUuid) "
            UUID.fromString(uploadUuid) //validate to block any malicious value
            Napier.i { "$logPrefix receive request "}

            if(uploadUuid == null) {
                Napier.d { "ChunkReceived with no uuid" }
                return ChunkedUploadResponse(
                    statusCode = 400, body = null, headers = emptyMap(), contentType = "text/plain"
                )
            }

            val isFinal = request.headers.entries.firstOrNull {
                it.key.equals(HEADER_IS_FINAL_CHUNK, true)
            }?.value?.firstOrNull()?.toBoolean() ?: false

            val tmpFile = File(uploadDir, uploadUuid)
            withContext(Dispatchers.IO) {
                FileOutputStream(tmpFile, true).use { fileOut ->
                    fileOut.write(request.chunkData)
                    fileOut.flush()
                }
            }
            Napier.v { "$logPrefix appended chunk (isFinal=$isFinal) to ${tmpFile.absolutePath} " }

            return if(isFinal){
                onUploadComplete(
                    CompletedChunkedUpload(
                        request, uploadUuid, Path(tmpFile.absolutePath)
                    )
                )
            }else {
                ChunkedUploadResponse(
                    statusCode = 204,
                    body = null,
                    headers = emptyMap(),
                    contentType = null
                )
            }
        }catch(e: Throwable) {
            Napier.e("ChunkedUploadServerUseCaseJvm: ERROR:", e)
            throw e
        }
    }
}