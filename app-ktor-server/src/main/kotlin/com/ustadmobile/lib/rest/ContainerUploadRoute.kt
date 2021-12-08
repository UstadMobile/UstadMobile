package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.io.UploadSessionManager
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.http.HttpStatus
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on
import java.util.*

fun Route.ContainerUploadRoute2() {

    route("ContainerUpload2") {
        post("{uploadId}/init") {
            val sessionManager: UploadSessionManager = di().on(call).direct.instance()
            try {
                val containerEntryListStr = call.receive<String>()
                val gson: Gson = di().direct.instance()
                val containerEntryList : List<ContainerEntryWithMd5> = gson.fromJson(containerEntryListStr,
                        object: com.google.gson.reflect.TypeToken<List<ContainerEntryWithMd5>>() { }.type)
                val uploadSession = sessionManager.initSession(UUID.fromString(call.parameters["uploadId"]),
                        containerEntryList)
                call.respond(uploadSession.uploadSessionParams)
            }catch(se: IllegalStateException) {
                call.respond(HttpStatus.SC_BAD_REQUEST)
            }
        }

        put("{uploadId}/data") {
            val sessionManager: UploadSessionManager = di().on(call).direct.instance()
            val uploadUuid = call.parameters["uploadId"]
            if(uploadUuid == null) {
                call.respond(HttpStatusCode.BadRequest, "no uploaduuid")
            }
            try {
                withContext(Dispatchers.IO) {
                    call.receiveStream().use {
                        sessionManager.onReceiveSessionChunk(UUID.fromString(uploadUuid), it)
                    }
                    call.respond(HttpStatusCode.NoContent, "")
                }
            }catch(e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Upload error: $e")
            }
        }

        get("{uploadId}/close") {
            val sessionManager: UploadSessionManager = di().on(call).direct.instance()
            try {
                val sessionUuid = UUID.fromString(call.parameters["uploadId"])
                sessionManager.closeSession(sessionUuid)
                call.respond(HttpStatusCode.NoContent, "")
            }catch(e: Exception){
                call.respond(HttpStatusCode.InternalServerError, "Upload error: $e")
            }
        }
    }
}