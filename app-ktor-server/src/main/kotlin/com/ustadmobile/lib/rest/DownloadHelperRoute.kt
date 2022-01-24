package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.DownloadHelper(db: UmAppDatabase) {
    route("DownloadHelper") {
        get("size") {

        }

        get("entryList") {

        }
    }
}