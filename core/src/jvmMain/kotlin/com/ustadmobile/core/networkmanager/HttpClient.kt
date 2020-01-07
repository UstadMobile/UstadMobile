package com.ustadmobile.core.networkmanager

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.JsonFeature
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

private val okHttpClient = OkHttpClient.Builder()
        .dispatcher(Dispatcher().also {
            it.maxRequests = 30
            it.maxRequestsPerHost = 10
        })
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()


private val httpClient = HttpClient(OkHttp){
    install(JsonFeature)

    engine {
        preconfigured = okHttpClient
    }
}

actual fun defaultHttpClient() = httpClient

fun defaultOkHttpClient() = okHttpClient

