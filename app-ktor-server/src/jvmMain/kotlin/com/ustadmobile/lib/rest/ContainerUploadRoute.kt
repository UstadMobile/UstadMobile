package com.ustadmobile.lib.rest

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
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
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.parseList
import java.io.File
import java.io.FileInputStream
import javax.naming.InitialContext

fun Route.ContainerUpload(db: UmAppDatabase, folder: File) {

    route("ContainerUpload") {

        post("checkExistingMd5/") {
            val md5sumListStr = call.receive<String>()
            if (md5sumListStr.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "md5sum list not provided")
                return@post
            }

            val md5SumList = md5sumListStr.split(";")
            val foundEntries = db.containerEntryFileDao.findEntriesByMd5Sums(md5SumList).map { it.cefMd5 }

            val nonExistingMd5SumList = md5SumList.filterNot { it in foundEntries }
            call.respond(nonExistingMd5SumList)
        }

        post("finalizeEntries/sessionId/{sessionId}/") {

            val sessionId = call.parameters["sessionId"] ?: throw Exception()
            val sessionFile = File(folder, sessionId)
            val containerWithContainerEntryWithMd5 = call.receive<ContainerWithContainerEntryWithMd5>()
            val containerEntriesList = containerWithContainerEntryWithMd5.containerEntries
            val container = containerWithContainerEntryWithMd5.container
            if (containerEntriesList.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "containerEntriesWithMd5Sums not given")
                return@post
            }

            if (!sessionFile.exists()) {
                call.respond(HttpStatusCode.InternalServerError, "session file not found")
            } else {

                val iContext = InitialContext()
                val containerDirPath = iContext.lookup("java:/comp/env/ustadmobile/app-ktor-server/containerDirPath") as? String
                        ?: "./build/container"
                val containerDir = File(containerDirPath)
                containerDir.mkdirs()

                var containerFromDb = db.containerDao.findByUid(container.containerUid)
                if (containerFromDb == null) {
                    db.containerDao.insert(container)
                    containerFromDb = container
                }

                val containerManager = ContainerManager(containerFromDb, db, db, containerDir.absolutePath)
                val linkedItems = containerManager.linkExistingItems(containerEntriesList)

                var concatenatedInputStream: ConcatenatedInputStream? = null

                try {
                    concatenatedInputStream = ConcatenatedInputStream(FileInputStream(sessionFile))
                    containerManager.addEntriesFromConcatenatedInputStream(concatenatedInputStream, linkedItems)
                    call.respond(HttpStatusCode.NoContent)
                } finally {
                    concatenatedInputStream?.close()
                }

            }

        }

    }

}