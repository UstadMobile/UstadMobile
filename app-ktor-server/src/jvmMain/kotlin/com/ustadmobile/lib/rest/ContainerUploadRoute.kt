package com.ustadmobile.lib.rest

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContainerWithContainerEntryWithMd5
import com.ustadmobile.sharedse.container.addEntriesFromConcatenatedInputStream
import com.ustadmobile.sharedse.io.ConcatenatedInputStream
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on
import java.io.File
import java.io.FileInputStream

fun Route.ContainerUpload() {

    route("ContainerUpload") {

        post("checkExistingMd5/") {
            val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_DB)
            val md5sumListStr = call.receive<String>()
            if (md5sumListStr.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "md5sum list not provided")
                return@post
            }

            val md5SumList = md5sumListStr.split(";")
            val foundEntries = db.containerEntryFileDao.findEntriesByMd5SumsSafe(md5SumList, db).map { it.cefMd5 }

            val nonExistingMd5SumList = md5SumList.filterNot { it in foundEntries }
            call.respond(nonExistingMd5SumList)
        }

        post("finalizeEntries/") {
            val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_DB)
            val sessionId = call.parameters["sessionId"] ?: ""

            val skipSessionFile = sessionId.isNullOrEmpty()

            val folder: File by di().on(call).instance(tag = TAG_UPLOAD_DIR)
            val sessionFile = File(folder, sessionId)
            val containerWithContainerEntryWithMd5 = call.receive<ContainerWithContainerEntryWithMd5>()
            val containerEntriesList = containerWithContainerEntryWithMd5.containerEntries
            val container = containerWithContainerEntryWithMd5.container
            if (containerEntriesList.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "containerEntriesWithMd5Sums not given")
                return@post
            }

            if (!sessionFile.exists() && !skipSessionFile) {
                call.respond(HttpStatusCode.InternalServerError, "session file not found")
            } else {
                val containerDir: File by di().on(call).instance(tag = DiTag.TAG_CONTAINER_DIR)

                var containerFromDb = db.containerDao.findByUid(container.containerUid)
                if (containerFromDb == null) {
                    db.containerDao.insertWithReplace(container)
                    containerFromDb = container
                }

                val containerManager = ContainerManager(containerFromDb, db, db, containerDir.absolutePath)
                val linkedItems = containerManager.linkExistingItems(containerEntriesList)

                if (!skipSessionFile) {
                    var concatenatedInputStream: ConcatenatedInputStream? = null

                    try {
                        concatenatedInputStream = ConcatenatedInputStream(FileInputStream(sessionFile))
                        containerManager.addEntriesFromConcatenatedInputStream(concatenatedInputStream, linkedItems)
                        call.respond(HttpStatusCode.NoContent)
                    } finally {
                        concatenatedInputStream?.close()
                    }
                }else{
                    call.respond(HttpStatusCode.NoContent)
                }
                sessionFile.delete()
            }
        }
    }

}