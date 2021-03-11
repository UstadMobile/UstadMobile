package com.ustadmobile.lib.rest

import io.ktor.routing.*

fun Route.ContainerUpload2() {

    route("ContainerUpload2") {
        get("{uploadId}/initsession") {
            //check if the session exists
        }

        put("{uploadId}/data") {

        }

        get("{uploadId}/close") {

        }
    }
}