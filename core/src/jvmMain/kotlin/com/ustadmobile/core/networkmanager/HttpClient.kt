package com.ustadmobile.core.networkmanager

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.JsonFeature

private val httpClient = HttpClient(OkHttp){
    install(JsonFeature)
}

actual fun defaultHttpClient() = httpClient

