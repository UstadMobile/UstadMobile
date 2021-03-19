package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.io.UploadSessionManager
import com.ustadmobile.core.io.UploadSessionParams
import com.ustadmobile.core.util.ext.distinctMds5sSorted
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
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

        }

        get("{uploadId}/close") {

        }
    }
}