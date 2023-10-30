package com.ustadmobile.lib.rest

import com.ustadmobile.core.contentjob.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on
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
            val originalFilenameParam: String? = completedUpload.call.request
                .queryParameters["originalFilename"]
            val pluginManager: ContentPluginManager by di.on(completedUpload.call).instance()

            try {
                val metadataResult = pluginManager.extractMetadata(
                    uri = completedUpload.file.toDoorUri(),
                    originalFilename = originalFilenameParam,
                ).let {
                    //Ensure the original filename is preserved, even if not handled by the plugin
                    if(it.originalFilename == null && originalFilenameParam != null)
                        it.copy(originalFilename = originalFilenameParam)
                    else
                        it
                }

                completedUpload.call.respond(HttpStatusCode.OK, metadataResult)
            }catch(e: Exception) {
                completedUpload.call.respond(HttpStatusCode.BadRequest)
            }
        },
    )
}
