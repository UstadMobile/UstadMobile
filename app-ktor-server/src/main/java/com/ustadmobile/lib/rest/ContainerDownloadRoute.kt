package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import java.io.File


fun Route.ContainerDownload(db: UmAppDatabase) {
    route("ContainerEntryList") {
        get("findByContainerWithMd5") {
            val containerUid = call.request.queryParameters["containerUid"]?.toLong() ?: 0L
            val entryList = db.containerEntryDao.findByContainerWithMd5(containerUid)
            if(entryList.isNotEmpty()) {
                call.respond(entryList)
            }else {
                call.respond(HttpStatusCode.NotFound, "No such container $containerUid")
            }
        }
    }

    get("ContainerEntryFile/{entryFileUid}") {
        val entryFileUid = call.parameters["entryFileUid"]?.toLong() ?: 0L
        val entryFile = db.containerEntryFileDao.findByUid(entryFileUid)
        val filePath = entryFile?.cefPath
        if(filePath != null) {
            call.response.header("X-Content-Length-Uncompressed", entryFile?.ceTotalSize.toString())
            call.respondFile(File(filePath))
        }else {
            call.respond(HttpStatusCode.NotFound, "No such file: $entryFileUid")
        }
    }


}