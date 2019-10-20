package com.ustadmobile.core.networkmanager

import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.JsonFeature
import io.ktor.network.sockets.aSocket

private val httpClient = if(Build.VERSION.SDK_INT < 21) {
    HttpClient(CIO) {
        install(JsonFeature)
    }
}else {
    HttpClient(OkHttp) {
        install(JsonFeature)
    }
}

actual fun defaultHttpClient() = httpClient