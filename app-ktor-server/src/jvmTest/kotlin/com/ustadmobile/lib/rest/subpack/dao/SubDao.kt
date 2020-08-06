package com.ustadmobile.lib.rest.subpack.dao

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.SubDao(){
    route("SubDao") {
        get("hello") {
            call.respond("Hello")
        }
    }
}
