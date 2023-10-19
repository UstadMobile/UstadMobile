package com.ustadmobile.lib.rest.upload

import com.ustadmobile.core.upload.HEADER_IS_FINAL_CHUNK
import com.ustadmobile.core.upload.HEADER_UPLOAD_UUID
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID



data class CompletedUpload(
    val call: ApplicationCall,
    val file: File,
    val uploadUuid: String,
)

/**
 * Upload Receiver that can accept multiple chunks in order.
 *
 */
fun Route.UploadRoute(
    uploadDir: File,
    path: String,
    onUploadCompleted: suspend (CompletedUpload) -> Unit,
) {
    post(path) {
        val uploadUuid = call.request.headers[HEADER_UPLOAD_UUID]?.let { UUID.fromString(it) }
            ?.toString()
        if(uploadUuid == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val isFinal = call.request.headers[HEADER_IS_FINAL_CHUNK]?.toBoolean() ?: false
        val uploadTmpFile = File(uploadDir, uploadUuid)
        withContext(Dispatchers.IO) {
            FileOutputStream(uploadTmpFile, true).use { fileOut ->
                call.request.receiveChannel().copyTo(fileOut)
                fileOut.flush()
            }
        }

        if(isFinal) {
            onUploadCompleted(
                CompletedUpload(
                    call = call,
                    file = uploadTmpFile,
                    uploadUuid = uploadUuid,
                )
            )
        }else {
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
