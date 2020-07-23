package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.WorkSpace
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.WorkSpaceRoute(db: UmAppDatabase) {
    route("Workspace") {
        get("verify") {
            val workSpace = db.workSpaceDao.getWorkSpace()
            call.respond(if(workSpace != null)  HttpStatusCode.OK else HttpStatusCode.NotFound,
                    workSpace?:WorkSpace())
        }
    }
}
