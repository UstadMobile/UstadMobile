package com.ustadmobile.lib.rest.subpack

import com.ustadmobile.lib.rest.subpack.dao.SubDao
import io.ktor.routing.Route
import io.ktor.routing.route

fun Route.Db() {
    route("Db") {
        SubDao()
    }
}