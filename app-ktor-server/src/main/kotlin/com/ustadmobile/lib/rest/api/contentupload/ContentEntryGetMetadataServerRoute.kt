package com.ustadmobile.lib.rest.api.contentupload

import com.ustadmobile.lib.rest.domain.contententry.getmetadatafromuri.ContentEntryGetMetadataServerUseCase
import com.ustadmobile.lib.rest.domain.upload.ChunkedUploadRoute
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.Route
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on

const val UPLOAD_TMP_SUBDIR = "upload-tmp"

/**
 * Route that uses ContentEntryGetMetadataServerUseCase to handle chunked content uploads from
 * web clients. When the upload is completed (last chunk received), the metadata (or failure) response
 * will be sent. The file is retained, so that when it is done, ContentEntryImportRoute can run the
 * import itself.
 *
 * Use as follows
 * POST a multipart request with one file field
 * Returns MetadataResult (as JSON)
 */
fun Route.ContentUploadRoute() {
    val di: DI by closestDI()

    ChunkedUploadRoute(
        useCase = { call: ApplicationCall ->
            val getMetadataServerUseCase: ContentEntryGetMetadataServerUseCase =
                di.on(call).direct.instance()
            getMetadataServerUseCase.chunkedUploadServerUseCase
        },
        path = "upload",
    )
}
