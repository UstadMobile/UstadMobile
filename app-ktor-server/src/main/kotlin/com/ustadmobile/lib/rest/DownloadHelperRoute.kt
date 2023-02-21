package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.DownloadHelper(db: UmAppDatabase) {
    route("DownloadHelper") {
        get("size") {

        }

        get("entryList") {

        }
    }
}