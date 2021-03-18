package com.ustadmobile.lib.rest

import com.ustadmobile.core.io.UploadSessionManager
import com.ustadmobile.core.io.UploadSessionParams
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.apache.http.HttpStatus
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on
import java.util.*

fun Route.ContainerUpload2() {

    route("ContainerUpload2") {
        get("{uploadId}/init") {
            val sessionManager: UploadSessionManager = di().on(call).direct.instance()
            try {
                val uploadSessionParams = call.receive<UploadSessionParams>()
                val uploadSession = sessionManager.initSession(UUID.fromString(call.parameters["uploadId"]),
                    uploadSessionParams.containerEntryPaths, uploadSessionParams.md5sExpected)
                call.respondText(contentType = ContentType.Text.Plain) { uploadSession.startFromByte.toString() }
            }catch(se: IllegalStateException) {
                call.respond(HttpStatus.SC_BAD_REQUEST)
            }
        }

        put("{uploadId}/data") {

        }

        get("{uploadId}/close") {

        }
    }
}