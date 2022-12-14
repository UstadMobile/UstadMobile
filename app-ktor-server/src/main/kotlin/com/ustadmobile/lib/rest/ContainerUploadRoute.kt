package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.io.UploadSessionManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import io.github.aakira.napier.Napier
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import org.kodein.di.on
import java.util.*

fun Route.ContainerUploadRoute2() {

    route("ContainerUpload2") {
        post("{uploadId}/init") {
            val sessionManager: UploadSessionManager = closestDI().on(call).direct.instance()
            try {
                val containerEntryListStr = call.receive<String>()
                val gson: Gson = closestDI().direct.instance()
                val containerEntryList : List<ContainerEntryWithMd5> = gson.fromJson(containerEntryListStr,
                        object: com.google.gson.reflect.TypeToken<List<ContainerEntryWithMd5>>() { }.type)
                val uploadSession = sessionManager.initSession(UUID.fromString(call.parameters["uploadId"]),
                        containerEntryList)
                call.respond(uploadSession.uploadSessionParams)
            }catch(se: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest, "")
            }
        }

        put("{uploadId}/data") {
            val sessionManager: UploadSessionManager = closestDI().on(call).direct.instance()
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

        post("{uploadId}/close") {
            val sessionManager: UploadSessionManager = closestDI().on(call).direct.instance()
            val db: UmAppDatabase = closestDI().on(call).direct.instance(tag = DoorTag.TAG_DB)
            val gson: Gson = closestDI().direct.instance()
            try {
                val sessionUuid = UUID.fromString(call.parameters["uploadId"])
                sessionManager.closeSession(sessionUuid)

                val containerEntryListStr = call.receive<String>()
                val clientContainerEntryWithMd5: List<ContainerEntryWithMd5> = gson.fromJson(containerEntryListStr,
                        object: com.google.gson.reflect.TypeToken<List<ContainerEntryWithMd5>>() { }.type)

                val clientPathAndMd5PairList = clientContainerEntryWithMd5.map{
                    Pair(it.cefMd5, it.cePath)
                }
                val serverPathAndMd5PairList = db.containerEntryDao
                        .findByContainerWithMd5(clientContainerEntryWithMd5[0].ceContainerUid).map {
                            Pair(it.cefMd5, it.cePath)
                        }

                val differenceList = clientPathAndMd5PairList.minus(serverPathAndMd5PairList.toSet())

                if(differenceList.isEmpty()){
                    call.respond(HttpStatusCode.NoContent, "")
                }else {
                    val missingEntries = "Missing ContainerEntriesWithMd5Sum ${differenceList.joinToString(";")}"
                    Napier.d(missingEntries)
                    call.respond(HttpStatusCode.BadRequest, missingEntries)
                }
            }catch(e: Exception){
                call.respond(HttpStatusCode.InternalServerError, "Upload error: $e")
            }
        }
    }
}