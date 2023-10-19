package com.ustadmobile.lib.rest

import com.ustadmobile.core.contentjob.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on
import java.io.File
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.lib.rest.upload.UploadRoute
import io.ktor.http.*
import org.kodein.di.DI
import org.kodein.di.direct

const val UPLOAD_TMP_SUBDIR = "upload-tmp"

/**
 * This route provides a simple endpoint that will take content files submitted via the web client
 * as 'normal' multipart file uploads, store them in a temporary directory, and return the
 * MetadataResult.
 *
 * Use as follows
 * POST a multipart request with one file field
 * Returns MetadataResult (as JSON)
 *
 */
fun Route.ContentUploadRoute() {

    UploadRoute(
        path = "upload",
        uploadDir = {
            val di: DI by closestDI()
            di.on(it).direct.instance(tag = DiTag.TAG_FILE_UPLOAD_TMP_DIR)
        },
        onUploadCompleted = { completedUpload ->
            val di: DI by completedUpload.call.closestDI()
            val pluginManager: ContentPluginManager by di.on(completedUpload.call).instance()
            val uploadTmpDir: File = di.direct.on(completedUpload.call).instance(
                tag = DiTag.TAG_FILE_UPLOAD_TMP_DIR
            )

            try {
                val extractTmpDir = File(uploadTmpDir, completedUpload.uploadUuid).also {
                    it.mkdirs()
                }

                val metadataResult = pluginManager.extractMetadata(
                    uri = completedUpload.file.toDoorUri(),
                    processContext = ContentJobProcessContext(
                        srcUri = completedUpload.file.toDoorUri(),
                        tempDirUri = extractTmpDir.toDoorUri(),
                        params = mutableMapOf(),
                        transactionRunner = null,
                        di = di
                    )
                )

                completedUpload.call.respond(HttpStatusCode.OK, metadataResult)
            }catch(e: Exception) {
                completedUpload.call.respond(HttpStatusCode.BadRequest)
            }
        },
    )
}
