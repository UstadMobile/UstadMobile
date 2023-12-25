package com.ustadmobile.lib.rest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.IContentEntryGetMetaDataFromUriUseCase.Companion.HEADER_ORIGINAL_FILENAME
import com.ustadmobile.core.domain.upload.ChunkedUploadResponse
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.clientEndpoint
import com.ustadmobile.core.util.ext.firstCaseInsensitiveOrNull
import com.ustadmobile.core.util.stringvalues.MapStringValues
import com.ustadmobile.lib.rest.ext.dbModeProperty
import com.ustadmobile.lib.rest.upload.ChunkedUploadRoute
import io.ktor.http.*
import io.ktor.server.application.ApplicationCall
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import java.io.File

const val UPLOAD_TMP_SUBDIR = "upload-tmp"

/**
 * This route provides a simple endpoint that will take content files submitted via the web client
 * as 'normal' multipart file uploads, store them in a temporary directory, and return the
 * MetadataResult.
 *
 * Use as follows
 * POST a multipart request with one file field
 * Returns MetadataResult (as JSON)
 */
fun Route.ContentUploadRoute() {
    val di: DI by closestDI()
    val config = application.environment.config
    val dbModeProp = config.dbModeProperty()
    val json: Json = di.direct.instance()

    ChunkedUploadRoute(
        useCase = { call: ApplicationCall ->
            di.on(call).direct.instance(tag = DiTag.TAG_SERVER_UPLOAD_USE_CASE_CONTENT)
        },
        path = "upload",
        onUploadCompleted = { completedUpload ->
            val originalFilenameParam: String? = completedUpload.request.headers
                .firstCaseInsensitiveOrNull(HEADER_ORIGINAL_FILENAME)
            val endpoint = if(dbModeProp == CONF_DBMODE_SINGLETON) {
                Endpoint(config.property(CONF_KEY_SITE_URL).getString())
            }else {
                MapStringValues(completedUpload.request.headers).clientEndpoint()
            }

            val importersManager: ContentImportersManager by di.on(endpoint).instance()
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
