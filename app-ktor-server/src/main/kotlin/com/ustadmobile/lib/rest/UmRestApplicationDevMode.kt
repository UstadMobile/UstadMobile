package com.ustadmobile.lib.rest

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

fun Application.umRestApplicationDevMode() {
    umRestApplication(devMode = true)
    install(StatusPages) {
        exception<Exception> {cause ->
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            cause.printStackTrace()
        }
    }
}