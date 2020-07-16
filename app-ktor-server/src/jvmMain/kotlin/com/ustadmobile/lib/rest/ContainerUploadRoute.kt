package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.ContainerUpload(db: UmAppDatabase) {

    route("ContainerUpload") {

        post("checkExistingMd5/{md5sumList}") {
            val md5sumListStr = call.parameters["md5sumList"]
            if (md5sumListStr == null) {
                call.respond(HttpStatusCode.BadRequest, "md5sum list not provided")
                return@post
            }

            val md5SumList = md5sumListStr.split(";")
            val foundEntries = db.containerEntryFileDao.findEntriesByMd5Sums(md5SumList).map { it.cefMd5 }

            val nonExistingMd5SumList = md5SumList.filterNot { it in foundEntries }.joinToString(";")
            call.respond(nonExistingMd5SumList)
        }

    }

}