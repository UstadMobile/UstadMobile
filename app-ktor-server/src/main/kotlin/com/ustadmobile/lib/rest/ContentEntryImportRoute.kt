package com.ustadmobile.lib.rest

import io.github.aakira.napier.Napier
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.domain.contententry.import.ImportContentUseCase
import com.ustadmobile.core.domain.contententry.import.ImportRequest
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.createTemporaryDir
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import io.ktor.server.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.coroutines.withTimeout
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on
import java.io.File


private val IMPORT_LINK_TIMEOUT_DEFAULT = (30 * 1000).toLong()

fun Route.ContentEntryImportRoute() {
    /**
     * Validate the link that is provided for a client
     */
    post("validateLink") {
        val url = call.request.queryParameters["url"]?: ""
        val di = closestDI()
        val pluginManager: ContentPluginManager by di.on(call).instance()

        ContentJobProcessContext(DoorUri.parse(url),
                createTemporaryDir("content"), mutableMapOf(), null, di).use { processContext ->
            val metadata: MetadataResult?
            try{
                metadata = withTimeout(IMPORT_LINK_TIMEOUT_DEFAULT) {
                    pluginManager.extractMetadata(DoorUri.parse(url), processContext)
                }.copy(
                    displaySourceUrl = url,
                )
                if (metadata == null) {
                    call.respond(HttpStatusCode.BadRequest, "Unsupported")
                } else {
                    call.respond(metadata)
                }
            }catch (e: Exception){
                Napier.e("Exception extracting metadata to validateLink: $url", e)
            }
       }
    }

    /**
     * Handle a request to run a ContentJob. This could be from the WebApp (where the content
     * was uploaded already) or from Web/Mobile where the item to import is a link.
     *
     * This will use the ImportContentUseCase to start the actual import process.
     */
    post("importRequest") {
        val di = closestDI()
        val jobRequest: ImportRequest = call.receive<ImportRequest>().let {
            val importSourceUri = it.contentJobItem.sourceUri

            if(importSourceUri?.startsWith(MetadataResult.UPLOAD_TMP_LOCATOR_PREFIX) == true) {
                val uploadTmpDir: File = di.direct.on(call).instance(
                    tag = DiTag.TAG_FILE_UPLOAD_TMP_DIR
                )
                val uploadTmpPath = importSourceUri.substringAfter(MetadataResult.UPLOAD_TMP_LOCATOR_PREFIX)
                it.copy(
                    contentJobItem = it.contentJobItem.copy(
                        sourceUri = File(uploadTmpDir, uploadTmpPath).toDoorUri().toString()
                    )
                )
            }else {
                it
            }
        }

        val importUseCase: ImportContentUseCase = di.direct.on(call).instance()
        importUseCase(
            contentJob = jobRequest.contentJob,
            contentJobItem = jobRequest.contentJobItem,
        )

        call.respond(HttpStatusCode.OK)
    }

}