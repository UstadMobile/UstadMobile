package com.ustadmobile.core.networkmanager

import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
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

private val defaultGsonSerializer = GsonSerializer()

private val httpClient = if(Build.VERSION.SDK_INT < 21) {
    HttpClient(CIO) {
        install(JsonFeature)
    }
}else {
    HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = defaultGsonSerializer
        }

        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 30
        dispatcher.maxRequestsPerHost = 10

        engine {
            preconfigured = okHttpClient
        }

    }
}

actual fun defaultHttpClient() = httpClient

fun defaultOkHttpClient() = okHttpClient

fun defaultGsonSerializer() = defaultGsonSerializer
