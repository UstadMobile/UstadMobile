package com.ustadmobile.lib.rest

import io.ktor.application.Application

fun Application.umRestApplicationDevMode() {
    umRestApplication(devMode = true)
}